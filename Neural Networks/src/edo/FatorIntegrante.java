package edo;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.sgd.StochasticGradientDescent;
import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

import static edo.Functions.*;
import static org.encog.persist.EncogDirectoryPersistence.loadObject;


public class FatorIntegrante extends AdvancedRobot {

    final static String fclFileName = "/home/luis/workspace/java/robocode/robots/fatorintegrante.fcl";

    /* Variaveis de controle de ações do robo */
    private final double limitFromWall = 50d;
    private final double maxDistanceBetweenRobots = 100d;
    private final double wallDistanceLimit = 30d;
    private final double angleBeetweenEnemy = 90d;
    private final double circularSpeed = 100d;
    private Point2D.Double myPos;
    private int direction = 1;
    private byte angulationFactor = 1;

    /* Variaveis de armazenamento de informações externas ao robo*/
    private EnemyBot enemy = new EnemyBot();
    private Point2D.Double predicted_enemyPos;

    //variaveis para gerar dataset
    private double targetDelta = 0d;
    private double delta = 0d;
    private double time;
    private String specs;

    //model e variaveis pra treinar in-game
    private static BasicNetwork network = (BasicNetwork) loadObject(new File("/home/luis/workspace/java/artificial-intelligence-robocode/Neural Networks/network1.eg"));
    private double[] input;
    private double[] out;
    private static ArrayList<double[]> inputs = new ArrayList<>();
    private static ArrayList<double[]> outs = new ArrayList<>();

    //variaveis pra normalizar e desnormalizar valores utilizados pelo modelo
    private static final double min_x = 18d;
    private static final double max_x = 782d;
    private static final double min_y = 18d;
    private static final double max_y = 582d;
    private static final double min_dist = 35.905843;
    private static final double max_dist = 885.884239;
    private static final double min_speed = -8.0;
    private static final double max_speed = 8.0;
    private static final double min_heading = 0d;
    private static final double max_heading = 6.282866;
    private static final double min_time = 2.509667;
    private static final double max_time = 51.326127;

    //variaveis de logica fuzzy
    FIS fis = null;
    private FunctionBlock firePowerFunction;
    private FunctionBlock distanceFromEnemy;
    private FunctionBlock angulationFromEnemy;

    //wave surfing
    private static int surf_status_size = 49;
    private static double surf_status[] = new double[surf_status_size];

    private ArrayList<Wave> enemy_waves;
    private ArrayList<Integer> surf_directions;
    private ArrayList<Double> surf_abs_bearings;

    public static Rectangle2D.Double battlefield_rect = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    public static double distance_from_wall = 160;


    public void run() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        setColors(Color.BLACK, Color.RED, Color.RED, Color.black, Color.GREEN);
        fis = FIS.load(fclFileName);

        if( fis == null ) {
            System.err.println("Erro ao carregar arquivo: '" + fclFileName + "'");
            return;
        }

//        fast train into neural network before each round start
        if (outs.size() > 0){
            while(inputs.size() > outs.size()){
                inputs.remove(inputs.size()-1);
            }
            double[][]in_arr = new double[inputs.size()][6];
            double[][]out_arr = new double[inputs.size()][2];

            for(int i=0; i<outs.size(); i++) {
                out_arr[i] = outs.get(i);
                in_arr[i] = inputs.get(i);
            }

            MLDataSet trainingSet = new BasicMLDataSet(in_arr,out_arr);

            final StochasticGradientDescent train = new StochasticGradientDescent (network, trainingSet);
            train.setLearningRate(0.00001d);
            int epoch = 1;
            do {
                train.iteration();
                epoch++;
            } while (epoch < 1500);
            train.finishTraining();

        }

        //init surfing variables
        enemy_waves = new ArrayList();
        surf_directions = new ArrayList();
        surf_abs_bearings = new ArrayList();

        //init fuzzy variables
        firePowerFunction = fis.getFunctionBlock("getFirePower");
        distanceFromEnemy = fis.getFunctionBlock("getDistance");
        angulationFromEnemy = fis.getFunctionBlock("getAngulation");

        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);

        setTurnRadarRight(Double.POSITIVE_INFINITY);

        while (true) {
            execute();
            scan();
        }
    }

    //aim fixed into a enemy
    public void lockIn (){
        double angleToEnemy = getHeadingRadians() + enemy.getBearing();
        double newRadarAngle = angleToEnemy - getRadarHeadingRadians();
        setTurnRadarRightRadians(Utils.normalRelativeAngle(newRadarAngle));
    }


    private void shoot(){
        double firePower = calculateFirePower(firePowerFunction, enemy.getDistance(), getEnergy(), enemy.getEnergy());
        double bulletTravelTime = enemy.getDistance()/Rules.getBulletSpeed(firePower);

        input = new double[]{normalize(enemy.getX(), min_x, max_x),
                normalize(enemy.getY(), min_y, max_y),
                normalize(enemy.getDistance(), min_dist, max_dist),
                normalize(enemy.getVelocity(), min_speed, max_speed),
                normalize(enemy.getHeading(), min_heading, max_heading),
                normalize(bulletTravelTime, min_time, max_time)};;

//        genDataSet(bulletTravelTime);
        if(delta == 0d){
            targetDelta = bulletTravelTime;
            inputs.add(input);
        }

        delta = delta + (getTime()-time);

        if (delta >= targetDelta){
            out = new double[]{normalize(enemy.getX(), min_x, max_x),normalize(enemy.getY(), min_y, max_y)};
            outs.add(out);
            delta = 0d;
        }
        time = getTime();

        MLData data = new BasicMLData(input);
        MLData output = network.compute(data);

        Point2D.Double target = new Point2D.Double(denormalize(output.getData(0), min_x, max_x), denormalize(output.getData(1), min_y, max_y));


        double angle =  Utils.normalAbsoluteAngle(Math.atan2(target.x - getX(), target.y - getY()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(angle - getGunHeadingRadians()));

        setFire(firePower);

    }

    public void onScannedRobot (ScannedRobotEvent ev) {
        if (this.enemy.none() || ev.getName().equals(enemy.getName())) enemy.update(ev, this);

        myPos = new Point2D.Double(getX(), getY());

        createWaves();
        updateWaves();
        doSurfing();

        this.lockIn();
        this.shoot();
    }

    public double wallSmoothing(Point2D.Double pos, double angle, int orientation) {
        while (!battlefield_rect.contains(calculateNextPosition(pos, angle, distance_from_wall))) angle += orientation*0.05;
        return angle;
    }

    public void createWaves(){
        double lateral_speed = getVelocity()*Math.sin(enemy.getBearing());
        double abs_bearing = enemy.getBearing() + getHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(abs_bearing- getRadarHeadingRadians()) * 2);
        
        int direction = ((lateral_speed >= 0) ? 1 : -1);
        surf_directions.add(0, direction);
        surf_abs_bearings.add(0, abs_bearing + Math.PI);

        double bullet_power = enemy.getPrev_energy() - enemy.getEnergy();
        if (bullet_power < 3.01 && bullet_power > 0.09 && surf_directions.size() > 2) {
            Wave wave = new Wave();
            wave.fireTime = getTime() - 1;
            wave.bulletVelocity = Rules.getBulletSpeed(bullet_power);
            wave.distanceTraveled = Rules.getBulletSpeed(bullet_power);
            wave.direction = surf_directions.get(2);
            wave.directAngle = surf_abs_bearings.get(2);
            wave.fireLocation = (Point2D.Double) predicted_enemyPos.clone();
            enemy_waves.add(wave);
        }
        
        predicted_enemyPos = calculateNextPosition(myPos, abs_bearing, enemy.getDistance());
    }

    public void updateWaves() {
        for (int x = 0; x < enemy_waves.size(); x++) {
            Wave wave = enemy_waves.get(x);
            wave.distanceTraveled = (getTime() - wave.fireTime) * wave.bulletVelocity;
            if (wave.distanceTraveled > myPos.distance(wave.fireLocation) + 50) {
                enemy_waves.remove(x);
                x--;
            }
        }
    }

    public Wave getClosestSurfableWave() {
        double closest_distance = Double.POSITIVE_INFINITY;
        Wave surfWave = null;
        
        for (int x = 0; x < enemy_waves.size(); x++) {
            Wave wave = (Wave)enemy_waves.get(x);
            double distance = myPos.distance(wave.fireLocation) - wave.distanceTraveled;

            if (distance > wave.bulletVelocity && distance < closest_distance) {
                surfWave = wave;
                closest_distance = distance;
            }
        }
        return surfWave;
    }

    public void onCustomEvent(CustomEvent e) {

    }

    public void onRobotDeath(RobotDeathEvent event){
        this.enemy.reset();
        setTurnRadarRight (Double.POSITIVE_INFINITY);
    }
    

    public void onHitByBullet(HitByBulletEvent e) {
        if (!enemy_waves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            Wave hitWave = null;

            for (int i = 0; i < enemy_waves.size(); i++) {
                Wave wave = (Wave)enemy_waves.get(i);

                if (Math.abs(wave.distanceTraveled - myPos.distance(wave.fireLocation)) < 50 && (Math.abs(e.getVelocity()) - wave.bulletVelocity) < 0.001) {
                    hitWave = wave;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);
                enemy_waves.remove(enemy_waves.lastIndexOf(hitWave));
            }
        }
    }

    public Point2D.Double predictPosition(Wave surfWave, int direction) {
        Point2D.Double predicted_position = (Point2D.Double)myPos.clone();
        double predicted_speed = getVelocity();
        double predicted_heading = getHeadingRadians();
        double max_turn, moveAngle, moveDir;

        int count = 0;
        boolean intercepted = false;

        do {
            moveAngle = wallSmoothing(predicted_position, getAbsoluteBearing(surfWave.fireLocation,
                            predicted_position) + (direction * (Math.PI/2)), direction)
                            - predicted_heading;
            moveDir = 1;

            if(Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            max_turn = Math.PI/720d*(40d - 3d*Math.abs(predicted_speed));

            predicted_heading = Utils.normalRelativeAngle(predicted_heading + getLimit(moveAngle, -max_turn, max_turn));

            predicted_speed += (predicted_speed * moveDir < 0 ? 2*moveDir : moveDir);
            predicted_speed = getLimit(predicted_speed, -8, 8);
            predicted_position = calculateNextPosition(predicted_position, predicted_heading, predicted_speed);

            count++;

            if (predicted_position.distance(surfWave.fireLocation) <
                    surfWave.distanceTraveled + (count * surfWave.bulletVelocity)
                            + surfWave.bulletVelocity) {
                intercepted = true;
            }
        } while(!intercepted && count < 500);

        return predicted_position;
    }

    public double checkDanger(Wave surfWave, int direction) {
        int index = getFactorIndex(surfWave, predictPosition(surfWave, direction), surf_status_size);
        return surf_status[index];
    }

    public void doSurfing() {
        Wave surfWave = getClosestSurfableWave();

        if (surfWave == null) { return; }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = getAbsoluteBearing(surfWave.fireLocation, myPos);

        if (dangerLeft < dangerRight) goAngle = wallSmoothing(myPos, goAngle - (Math.PI/2), -1);
        else goAngle = wallSmoothing(myPos, goAngle + (Math.PI/2), 1);

        double angle = Utils.normalRelativeAngle(goAngle - getHeadingRadians());

        if (Math.abs(angle) > (Math.PI/2)) {
            if (angle < 0) setTurnRightRadians(Math.PI + angle);
            else setTurnLeftRadians(Math.PI - angle);
            setBack(100);
        }else {
            if (angle < 0) setTurnLeftRadians(-1*angle);
            else setTurnRightRadians(angle);
            setAhead(100);
        }
    }

    public void logHit(Wave wave, Point2D.Double target) {
        int index = getFactorIndex(wave, target, surf_status_size);
        for (int x = 0; x < surf_status_size; x++) surf_status[x] += 1.0 / (Math.pow(index - x, 2) + 1);
    }

    private void genDataSet(double bulletTravelTime){
        if(delta == 0d){
            targetDelta = bulletTravelTime;
            specs = String.format("%f", enemy.getX()) + "," + String.format("%f", enemy.getY())  + "," +  String.format("%f", enemy.getDistance()) + "," + String.format("%f", enemy.getVelocity()) + ","+ String.format("%f", enemy.getHeading()) + ","+ String.format("%f", bulletTravelTime) + ",";
        }

        delta = delta + (getTime()-time);

        if (delta >= targetDelta){
            specs = specs + String.format("%f", enemy.getX()) + "," + String.format("%f", enemy.getY()) + "\n";
            Writer.main("/home/luis/workspace/java/robocode/train.csv", specs);
            delta = 0d;
        }

    }

}