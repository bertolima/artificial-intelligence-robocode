
import robocode.*;
import robocode.control.events.RoundStartedEvent;
import robocode.util.Utils;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jgap.*;
import org.jgap.impl.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;


public class FatorIntegrante extends AdvancedRobot {
    private static final int LIMIT_FROM_ENEMY = 20;
    private static final int MIN_ENERGY = 20;
    private static final double DISTANCE_TO_WALK = 50;
    public double ZIGZAG_TIME = 40d;

    HashMap<String, Double> global_values = new HashMap<>();
    HashMap<String, Query> querys = new HashMap<>();
    private boolean isNearWall = false;
    private boolean isLowRanged = false;
    private boolean isMidRanged = false;
    private boolean isNearEnemy = false;
    private boolean isEnergyLow = false;
    private boolean isEnemyEnergyLow = false;
    private boolean isEnemyMoving = false;
    private boolean canTurnGunLeft = false;
    private boolean canTurnGunRight = false;
    private boolean currentInWall = false;
    public int direction = 1;
    private static int[] boundaries = {0, 45, 90, 135, 180, 225, 270, 315, 360};
    private static int[] zones = {1, -1, 1, -1, 1, -1, 1, -1};
    private double angleNearWall;


    private Enemy enemy = new Enemy();
    private Point2D[] enemyPosition = {new Point2D.Double(0d,0d), new Point2D.Double(0d,0d)};


    public void run() {
        setColors(Color.BLACK, Color.RED, Color.RED, Color.black, Color.GREEN);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        if (!Query.hasSolution("consult('C:/robocode/arg.pl').")) {
            System.out.println("Consult failed");
        }

        initFlags();
        initQuerys();
        setTurnRadarRight(Double.POSITIVE_INFINITY);
        while (true) {
            execute();
            scan();

            updateQuerys();
            executeQuerys();

            moveset();
            explosion();
        }

    }

    private void initFlags(){
        global_values.put("AngleToEnemy", 0d);
    }

    private void initQuerys(){
        querys.put("isNearWall", null);
        querys.put("isLowRanged", null);
        querys.put("isMidRanged", null);
        querys.put("isNearEnemy", null);
        querys.put("isEnergyLow", null);
        querys.put("isEnemyEnergyLow", null);
        querys.put("isEnemyMoving", null);
        querys.put("canTurnGunLeft", null);
        querys.put("canTurnGunRight", null);
    }

    public void updateQuerys(){
        querys.replace("isNearWall",
                new Query("isNearWall", new Term[]{new org.jpl7.Float(getX()),
                        new org.jpl7.Float(getY()),
                        new org.jpl7.Float(getBattleFieldWidth()),
                        new org.jpl7.Float(getBattleFieldHeight()),
                        new org.jpl7.Float(22.2222222)}));

        querys.replace("isEnergyLow", new Query("isEnergyLow", new Term[]{new org.jpl7.Float(getEnergy()),
                new org.jpl7.Float(MIN_ENERGY)}));
    }

    public void executeQuerys(){
        if (querys.get("isNearWall").hasSolution()) isNearWall = true;
        else isNearWall = false;

        if (querys.get("isEnergyLow").hasSolution()) isEnergyLow = true;
        else isEnergyLow = false;
    }

    public void updateEnemyQuerys(ScannedRobotEvent ev){
        querys.replace("isLowRanged", new Query("isEnemyLowRanged", new Term[]{new org.jpl7.Float(ev.getDistance())}));
        querys.replace("isMidRanged", new Query("isEnemyMidRanged", new Term[]{new org.jpl7.Float(ev.getDistance())}));
        querys.replace("isNearEnemy", new Query("isNearEnemy", new Term[]{new org.jpl7.Float(ev.getDistance()),
                                                                            new org.jpl7.Float(LIMIT_FROM_ENEMY)}));
        querys.replace("isEnemyEnergyLow", new Query("isEnemyEnergyLow", new Term[]{new org.jpl7.Float(ev.getEnergy()),
                                                                            new org.jpl7.Float(MIN_ENERGY)}));
        querys.replace("isEnemyMoving", new Query("isEnemyMoving", new Term[]{new org.jpl7.Float(enemyPosition[0].distance(enemyPosition[1]))}));
        querys.replace("canTurnGunLeft", new Query("canTurnGunLeft", new Term[]{new org.jpl7.Float(getX()),
        new org.jpl7.Float(getY()), new org.jpl7.Float(enemyPosition[0].getX()), new org.jpl7.Float(enemyPosition[0].getY()),
        new org.jpl7.Float(enemyPosition[1].getX()), new org.jpl7.Float(enemyPosition[1].getY())}));
        querys.replace("canTurnGunRight", new Query("canTurnGunRight", new Term[]{new org.jpl7.Float(getX()),
                new org.jpl7.Float(getY()), new org.jpl7.Float(enemyPosition[0].getX()), new org.jpl7.Float(enemyPosition[0].getY()),
                new org.jpl7.Float(enemyPosition[1].getX()), new org.jpl7.Float(enemyPosition[1].getY())}));

    }

    public void executeEnemyQuerys(){
        isLowRanged = querys.get("isLowRanged").hasSolution();
        isMidRanged = querys.get("isMidRanged").hasSolution();
        isNearEnemy = querys.get("isNearEnemy").hasSolution();
        isEnemyEnergyLow = querys.get("isEnemyEnergyLow").hasSolution();
        isEnemyMoving = querys.get("isEnemyMoving").hasSolution();
        canTurnGunLeft = querys.get("canTurnGunLeft").hasSolution();
        canTurnGunRight = querys.get("canTurnGunRight").hasSolution();
    }

    private void resetQuerys(){

    }

    private void lockIn(ScannedRobotEvent ev){
        global_values.replace("AngleToEnemy", getHeadingRadians() + ev.getBearingRadians());
        Double newRadarAngle = global_values.get("AngleToEnemy") - getRadarHeadingRadians();
        setTurnRadarRightRadians(Utils.normalRelativeAngle(newRadarAngle));
    }

    public double updateDirection(double currentHeading){
        double newAngle;
        if(Utils.isNear(getTime() % ZIGZAG_TIME, 0d) && !isNearWall) direction *= -1;
        if (direction == 1 && !isNearWall) newAngle = Math.toRadians(66.66845579429642);
        else if(direction == -1 && !isNearWall) newAngle = -1 * Math.toRadians(69.04885496677623);
        else newAngle = avoidColission();
        out.println(newAngle);
        return newAngle;
    }

    public double avoidColission(){
        if (isNearWall && !currentInWall){
            currentInWall = true;
            Vector<Boolean> nearTable = new Vector<Boolean>();
            for (int i=0;i<8;i++) nearTable.add(getHeading() > boundaries[i] && getHeading() <= boundaries[i+1]);
            int closestIndex = nearTable.indexOf(true);
            double targetAngle = (boundaries[closestIndex+1] + 180) % 180;
            angleNearWall = Math.toRadians(getHeading() - targetAngle);
        }
        return angleNearWall;
    }

    private void moveset(){
        double turnBodyToEnemy = global_values.get("AngleToEnemy") - getHeadingRadians();
        double newBodyAngle = updateDirection(turnBodyToEnemy);
        setTurnRightRadians(newBodyAngle);
        setAhead(DISTANCE_TO_WALK);
    }

    public double calculateProjection(){
        if (!isEnemyMoving) return 0d;
        else if(canTurnGunRight && !canTurnGunLeft) return Math.toRadians(41.84547098322906);
        return Math.toRadians(66.30967372714659) * -1;
    }

    public double calculateFirePower(){
        if (isLowRanged) return 3d;
        else if(isMidRanged) return 2d;
        else if (isEnergyLow) return 0.8d;
        else if(isEnemyEnergyLow) return 1d;
        else return 1d;
    }

    private void explosion(){
        double newAngleGun = global_values.get("AngleToEnemy") - this.getGunHeadingRadians();
        double projection = calculateProjection();
        setTurnGunRightRadians(Utils.normalRelativeAngle(newAngleGun + projection));
        double firePower = calculateFirePower();
        setFire(firePower);
    }

    public void onScannedRobot(ScannedRobotEvent ev) {
        if (this.enemy.none() || ev.getName().equals(enemy.getName())) enemy.update(ev);
        updateEnemyPosition(ev);
        lockIn(ev);
        updateEnemyQuerys(ev);
        executeEnemyQuerys();
        resetQuerys();
    }

    public void updateEnemyPosition(ScannedRobotEvent ev){
        this.enemyPosition[1] = this.enemyPosition[0];
        Point2D.Double currentEnemyPosition = new Point2D.Double(getX() + ev.getDistance()*Math.sin(global_values.get("AngleToEnemy")), getY() + ev.getDistance()*Math.cos(global_values.get("AngleToEnemy")));
        this.enemyPosition[0] = currentEnemyPosition;
    }

    public void onBulletHit (BulletHitEvent ev){

    }

    public void onHitByBullet(HitByBulletEvent ev){

    }

    public void onHitWall(HitWallEvent ev){

    }


    public void onRobotDeath(RobotDeathEvent event){
        this.enemy.reset();
        setTurnRadarRight (Double.POSITIVE_INFINITY);
    }
}
