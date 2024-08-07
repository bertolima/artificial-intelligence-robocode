package edo;

import robocode.*;

import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;

import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;

import java.util.HashMap;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;


public class FatorIntegrante extends AdvancedRobot {

    final static String fclFileName = "robots/fatorintegrante.fcl";

    /* Variaveis de controle de ações do robo */
    private final double limitFromWall = 50d;
    private final double maxDistanceBetweenRobots = 100d;
    private final double wallDistanceLimit = 30d;
    private final double angleBeetweenEnemy = 90d;
    private final double circularSpeed = 100d;
    private Point2D.Double myCoord;
    private int direction = 1;
    private byte angulationFactor = 1;

    /* Variaveis de parâmetros para calculos */
    private double angleToEnemy;
    private double newRadarAngle;
    private double newAngleGun;
    private double projection;

    /* Variaveis de armazenamento de informações externas ao robo*/
    private EnemyBot enemy = new EnemyBot();
    private Point2D.Double myPrevCoord = new Point2D.Double(0d,0d);
    private double accourate = 0d;
    private double totalShots = 0d;
    private double hits = 0d;

    private FunctionBlock firePowerFunction;
    private FunctionBlock distanceFromEnemy;
    private FunctionBlock angulationFromEnemy;
    FIS fis = null;

    private BayesNode[] nodes;
    IBayesInferer inferer = new JunctionTreeAlgorithm();
    HashMap<Integer, String> getDirection = Functions.initDirectionHash();



    public void run() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        setColors(Color.BLACK, Color.RED, Color.RED, Color.black, Color.GREEN);
        fis = FIS.load(fclFileName);

        if( fis == null ) {
            System.err.println("Erro ao carregar arquivo: '" + fclFileName + "'");
            return;
        }

        firePowerFunction = fis.getFunctionBlock("getFirePower");
        distanceFromEnemy = fis.getFunctionBlock("getDistance");
        angulationFromEnemy = fis.getFunctionBlock("getAngulation");

        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);

        nodes = Functions.initBayesNetwork(inferer);

        setTurnRadarRight(Double.POSITIVE_INFINITY);
        this.myCoord = new Point2D.Double(getX(), getY());
        while (true) {
            execute();
            scan();
            move();
        }
    }

    //fixa a mira em um robo especifico
    public void lockIn (){
        this.angleToEnemy = this.getHeadingRadians() + this.enemy.getBearingRadians();
        this.newRadarAngle = this.angleToEnemy - this.getRadarHeadingRadians();
        setTurnRadarRightRadians(Utils.normalRelativeAngle(newRadarAngle));
    }


    private void move(){
        double distance = Functions.calculateDistanceFromEnemy(distanceFromEnemy, getEnergy(), this.enemy.getEnergy());
        Point2D.Double newPos = this.getMyNextPos(distance);
        if (!this.enemy.none()){
            if (this.enemy.getDistance() <= 250){
                double angulation = Functions.calculateAngulationFromEnemy(angulationFromEnemy, enemy.getDistance(), enemy.getVelocity());
                if (getTime() % 56 == 0) angulationFactor *=-1;
                angulation *= angulationFactor;
                double newX = enemy.getX() + distance * Math.sin(enemy.getHeading()+angulation);
                double newY = enemy.getY() + distance * Math.cos(enemy.getHeading()+angulation);
                newPos.setLocation(newX, newY);
            }
        }
        if (Functions.needNormalize(newPos, this.wallDistanceLimit, getBattleFieldWidth(), getBattleFieldHeight()))
            newPos = Functions.normalizeCoords(newPos, this.wallDistanceLimit, getBattleFieldWidth(), getBattleFieldHeight());
        goTo(newPos);
    }

    private void shoot(){
        double firePower = Functions.calculateFirePower(firePowerFunction, this.enemy.getDistance(), getEnergy(), this.enemy.getEnergy());

        double currentHeading = this.enemy.getHeadingRadians();
        double headingDiff = currentHeading - this.enemy.getPrevHeadingRadians();
        this.enemy.setPrevHeadingRadians(currentHeading);
        double bulletTravelTime = enemy.getDistance()/Rules.getBulletSpeed(firePower);;

        Point2D.Double target = Functions.getTargetPosition(new Point2D.Double(getX(), getY()), enemy, new Point2D.Double(getBattleFieldWidth(),
                getBattleFieldHeight()), currentHeading, headingDiff, bulletTravelTime, nodes, inferer, getDirection);

        double currentAngle = Math.atan2(this.enemy.getY()-this.getY(), this.enemy.getX()-this.getX());
        double futureAngle = Math.atan2(target.getY()-this.getY(), target.getX()-this.getX());
        //double angle =  Utils.normalAbsoluteAngle(Math.atan2(target.x - getX(), target.y - getY()));

        this.projection = futureAngle-currentAngle;
        this.newAngleGun = this.angleToEnemy - this.getGunHeadingRadians();

        //setTurnGunRightRadians(Utils.normalRelativeAngle(angle - getGunHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(newAngleGun - projection));
        if(enemy.getDistance() < 500) setFire(firePower);
        this.accourate = this.totalShots/this.hits;
    }


    //calcula posição atual do robo inimigo
    private void getEnemyPos(){
        this.enemy.setPrevCoord(this.enemy.getX(), this.enemy.getY());
        this.myPrevCoord.setLocation(getX(), getY());
        double enemyX = getX() + this.enemy.getDistance() * Math.sin(this.angleToEnemy);
        double enemyY = getY() + this.enemy.getDistance() * Math.cos(this.angleToEnemy);
        this.enemy.setCoord(enemyX, enemyY);

    }

    //calcula posição em que eu quero posicionar o meu robo
    private Point2D.Double getMyNextPos(double distance){
        Point2D.Double newPos = new Point2D.Double(getX(), getY());
        if (!this.enemy.none()) Functions.calculateNextPosition(newPos, this.enemy.getCoord(), angleToEnemy, distance);
        return newPos;
    }

    private void goTo(Point2D.Double coord) {
        coord.x = coord.x - this.getX();
        coord.y = coord.y - this.getY();
        double goAngle = Utils.normalRelativeAngle(Math.atan2(coord.x, coord.y) - this.getHeadingRadians());
        setTurnRightRadians(Math.atan(Math.tan(goAngle)));
        setAhead(Math.cos(goAngle) * Math.hypot(coord.x, coord.y));
    }

    public void onScannedRobot (ScannedRobotEvent ev) {
        if (this.enemy.none() || ev.getName().equals(enemy.getName())) enemy.update(ev);
        this.getEnemyPos();
        this.lockIn();
        this.shoot();
    }

    public void onCustomEvent(CustomEvent e) {

    }

    public void onRobotDeath(RobotDeathEvent event){
        this.enemy.reset();
        setTurnRadarRight (Double.POSITIVE_INFINITY);
    }

    public void onBulletHit(BulletHitEvent b){
        this.hits++;
        this.totalShots++;
    }

    public void onBulletMissed(BulletMissedEvent b){
        this.totalShots++;
    }

    public void onHitRobotEvent(HitRobotEvent e){
        Point2D.Double newPos = new Point2D.Double(getX() + 100 * Math.cos(getHeading() + 180), getY() + 100 * Math.sin(getHeading() + 180));
        if (Functions.needNormalize(newPos, this.wallDistanceLimit, getBattleFieldWidth(), getBattleFieldHeight()))
        newPos = Functions.normalizeCoords(newPos, this.wallDistanceLimit, getBattleFieldWidth(), getBattleFieldHeight());
        clearAllEvents();
        goTo(newPos);
    }

}