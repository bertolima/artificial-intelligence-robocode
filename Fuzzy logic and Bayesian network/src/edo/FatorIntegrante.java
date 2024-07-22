package edo;

import Enemy.EnemyBot;
import robocode.*;

import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;

public class FatorIntegrante extends AdvancedRobot {

    enum Signal {
        PLUS, MINUS, NEUTRAL, NONE
    }

    enum Axis {
        X, Y, NEUTRAL, NONE
    }

    /* Variaveis de controle de ações do robo */
    private final double limitFromWall = 50d;
    private final double maxDistanceBetweenRobots = 100d;
    private final double wallDistanceLimit = 30d;
    private final double angleBeetweenEnemy = 90d;
    private final double circularSpeed = 100d;
    private Point2D.Double myCoord;
    private int direction = 1;

    /* Variaveis de parâmetros para calculos */
    private double angleToEnemy;
    private double newRadarAngle;
    private double newAngleGun;
    private double projection;
    private Signal signal;

    /* Variaveis de armazenamento de informações externas ao robo*/
    private EnemyBot enemy = new EnemyBot();
    private Point2D.Double myPrevCoord = new Point2D.Double(0d,0d);
    private double distanceBeetweenMyCoords;
    private double accourate = 0d;
    private double totalShots = 0d;
    private double hits = 0d;


    public void run() {
        setColors(Color.BLACK, Color.RED, Color.RED, Color.black, Color.GREEN);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        addCustomEvent(new Condition("nearWall") {
            public boolean test() {
                return (getX() <= limitFromWall || getX() >= getBattleFieldWidth() - limitFromWall  || getY() <= limitFromWall || getY() >= getBattleFieldHeight() - limitFromWall);}
        });
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
        Point2D.Double newPos = this.getMyNextPos();
        if (Functions.needNormalize(newPos, this.wallDistanceLimit, getBattleFieldWidth(), getBattleFieldHeight()))
            newPos = Functions.normalizeCoords(newPos, this.wallDistanceLimit, getBattleFieldWidth(), getBattleFieldHeight());
        if (!this.enemy.none()){

            if (this.enemy.getDistance() < 200){

            }
            else this.goTo(newPos);

        }
    }

    private void chooseTargeting(){
        double firePower = Functions.calculateFirePower(getEnergy(), this.enemy.getDistance(), this.enemy.getEnergy());
        double currentHeading = this.enemy.getHeadingRadians();
        double headingDiff = currentHeading - this.enemy.getPrevHeadingRadians();
        this.enemy.setPrevHeadingRadians(currentHeading);

        double bulletSpeed = Rules.getBulletSpeed(firePower);
        double travellingBulletTime;

        Point2D.Double target;

        if (Math.abs(headingDiff) > 0.01d){
            travellingBulletTime  = this.enemy.getDistance()/bulletSpeed;
            target = (Point2D.Double) this.predictCircularMove(this.enemy.getCoord(), this.enemy.getVelocity(), currentHeading, headingDiff, travellingBulletTime, firePower);
        }
        else{
            travellingBulletTime = (this.enemy.getDistance() + this.enemy.getPrevDistance())/bulletSpeed;
            target = linearTargeting(travellingBulletTime);
        }

        double currentAngle = Math.atan2(this.enemy.getY()-this.getY(), this.enemy.getX()-this.getX());
        double futureAngle = Math.atan2(target.getY()-this.getY(), target.getX()-this.getX());
        //double angle =  Utils.normalAbsoluteAngle(Math.atan2(target.x - getX(), target.y - getY()));

        this.projection = futureAngle-currentAngle;
        this.newAngleGun = this.angleToEnemy - this.getGunHeadingRadians();

        //setTurnGunRightRadians(Utils.normalRelativeAngle(angle - getGunHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(newAngleGun - projection));
        setFire(firePower);
        this.accourate = this.totalShots/this.hits;
    }


    private Point2D predictCircularMove(Point2D p, double speed, double currentHeading, double diffHeading, double time, double firePower){
        Point2D.Double newPos = new Point2D.Double(p.getX(), p.getY());
        double delta = 0d;
        while ((++delta) * time < Point2D.Double.distance(getX(), getY(), newPos.x, newPos.y)){

            newPos.x += Math.sin(currentHeading) * speed;
            newPos.y += Math.cos(currentHeading) * speed;
            currentHeading += diffHeading;

            if (Functions.needNormalize(newPos, 0, getBattleFieldWidth(), getBattleFieldHeight())){
                newPos.setLocation(Functions.normalizeCoords(newPos, 0, getBattleFieldWidth(), getBattleFieldHeight()));
                break;
            }
        }
        return newPos;
    }

    public Point2D.Double linearTargeting(double bulletTime){
        Point2D.Double newPos = new Point2D.Double(this.enemy.getX(), this.enemy.getY());
        Axis axis = determineLinearMovement();
        double projection = 2 * (this.enemy.getVelocity() * bulletTime);

        if (this.signal.equals(Signal.MINUS)) projection *= -1;

        if (axis.equals(Axis.X)) {
            newPos.setLocation(newPos.getX() + projection, newPos.getY());
        } else if (axis.equals(Axis.Y)) {
            newPos.setLocation(newPos.getX(), newPos.getY() + projection);
        } else if(axis.equals(Axis.NEUTRAL)){
            newPos.setLocation(newPos.getX() + Math.cos(this.enemy.getHeading())*projection,
                    newPos.getY()+ Math.sin(this.enemy.getHeading())*projection);
        }
        this.signal = Signal.NONE;
        return newPos;
    }

    public Axis determineLinearMovement(){
        Point2D.Double coordDiff = new Point2D.Double(this.enemy.getX() - this.enemy.getPrevCoord().getX(),
                this.enemy.getY() - this.enemy.getPrevCoord().getY());
        this.enemy.setPrevDistance(this.enemy.getCoord().distance(this.enemy.getPrevCoord()));
        this.distanceBeetweenMyCoords = this.myPrevCoord.distance(getX(), getY());
        if (Math.abs(Math.abs(coordDiff.x) - Math.abs(coordDiff.y)) > this.enemy.getPrevDistance()/2){
            if (Math.max(Math.abs(coordDiff.x), Math.abs(coordDiff.y)) == Math.abs(coordDiff.x)){
                if (coordDiff.x < 0) this.signal = Signal.MINUS;
                else this.signal = Signal.PLUS;
                return Axis.X;
            }
            else{
                if (coordDiff.y < 0) this.signal = Signal.MINUS;
                else this.signal = Signal.PLUS;
                return Axis.Y;
            }
        } else{
            this.signal = Signal.NEUTRAL;
            return Axis.NEUTRAL;
        }
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
    private Point2D.Double getMyNextPos(){
        Point2D.Double newPos = new Point2D.Double(getX(), getY());
        if (!this.enemy.none()) {
            Functions.calculateNextPosition(newPos, this.enemy.getCoord(), angleToEnemy, maxDistanceBetweenRobots);
        }
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
        this.chooseTargeting();
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

}