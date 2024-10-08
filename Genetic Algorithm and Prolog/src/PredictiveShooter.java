import robocode.*;
import java.awt.geom.Point2D;

public class PredictiveShooter extends AdvancedRobot {

    public class EnemyBot extends Robot {

        private volatile double bearing;
        private volatile double distance;
        private volatile double energy;
        private volatile double heading;
        private volatile String name = "";
        private volatile double velocity;

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getBearing() {
            return bearing;
        }

        public void setBearing(double bearing) {
            this.bearing = bearing;
        }

        public double getEnergy() {
            return energy;
        }

        public void setEnergy(double energy) {
            this.energy = energy;
        }

        public double getHeading() {
            return heading;
        }

        public void setHeading(double heading) {
            this.heading = heading;
        }

        public double getVelocity() {
            return velocity;
        }

        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }

        public void reset() {
            bearing = 0.0;
            distance = 0.0;
            energy = 0.0;
            heading = 0.0;
            name = "";
            velocity = 0.0;

            System.out.printf("Reset tracked bot!\n");
        }


        public boolean none() {
            System.out.printf("None tracked!\n");
            return "".equals(name);
        }

        public void update(ScannedRobotEvent e) {
            bearing = e.getBearing();
            distance = e.getDistance();
            energy = e.getEnergy();
            heading = e.getHeading();
            name = e.getName();
            velocity = e.getVelocity();
        }

        @Override
        public String toString() {
            return "EnemyBot{" +
                    "bearing=" + bearing +
                    ", distance=" + distance +
                    ", energy=" + energy +
                    ", heading=" + heading +
                    ", name='" + name + '\'' +
                    ", velocity=" + velocity +
                    '}';
        }
    }
    public class AdvancedEnemyBot extends EnemyBot{

        private double x;
        private double y;

        @Override
        public void reset() {
            super.reset();

            x = 0.0;
            y = 0.0;
        }

        public void update(ScannedRobotEvent e, Robot robot) {
            super.update(e);

            double absBearingDeg = (robot.getHeading() + e.getBearing());
            if (absBearingDeg < 0) absBearingDeg += 360;

            // yes, you use the _sine_ to get the X value because 0 deg is North
            x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();

            // yes, you use the _cosine_ to get the Y value because 0 deg is North
            y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
        }

        public double getFutureX(long when){
            return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
        }

        public double getFutureY(long when){
            return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
        }

        public double getFutureT(Robot robot, double bulletVelocity){

            // enemy velocity
            double v_E = getVelocity();

            // temp variables
            double x_diff = x - robot.getX();
            double y_diff = y - robot.getY();

            // angles of enemy's heading
            double sin = Math.sin(Math.toRadians(getHeading()));
            double cos = Math.cos(Math.toRadians(getHeading()));

            // calculated time
            double T;
            double v_B = bulletVelocity;

            double xy = (x_diff*sin + y_diff*cos);

            T = ( (v_E*xy) + Math.sqrt(sqr(v_E)*sqr(xy) + (sqr(x_diff) + sqr(y_diff))*(sqr(v_B) + sqr(v_E))) ) / (sqr(v_B) - sqr(v_E));

            return T;

        }

        private double sqr(double in){
            return in * in;
        }
    }
    private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

    public void run() {
        // divorce radar movement from gun movement
        setAdjustRadarForGunTurn(true);
        // divorce gun movement from tank movement
        setAdjustGunForRobotTurn(true);
        // we have no enemy yet
        enemy.reset();
        // initial scan
        setTurnRadarRight(360);

        while (true) {
            // rotate the radar
            setTurnRadarRight(360);
            // sit & spin
            setTurnRight(360);
            setAhead(20);
            // doGun does predictive targeting
            doGun();
            // carry out all the queued up actions
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        // track if we have no enemy, the one we found is significantly
        // closer, or we scanned the one we've been tracking.
        if ( enemy.none() || e.getDistance() < enemy.getDistance() - 70 ||
                e.getName().equals(enemy.getName())) {

            // track him using the NEW update method
            enemy.update(e, this);
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        // see if the robot we were tracking died
        if (e.getName().equals(enemy.getName())) {
            enemy.reset();
        }
    }

    void doGun() {

        // don't shoot if I've got no enemy
        if (enemy.none())
            return;

        // calculate firepower based on distance
        double firePower = Math.min(36000 / enemy.getDistance(), 3);
        // calculate speed of bullet
        double bulletSpeed = 20 - firePower * 3;
        // distance = rate * time, solved for time
        long time = (long)(enemy.getDistance() / bulletSpeed);

        // calculate gun turn to predicted x,y location
        double futureX = enemy.getFutureX(time);
        double futureY = enemy.getFutureY(time);
        double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
        // non-predictive firing can be done like this:
        //double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());

        // turn the gun to the predicted x,y location
        setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));

        // if the gun is cool and we're pointed in the right direction, shoot!
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }
    }

    // computes the absolute bearing between two points
    double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) { // both pos: lower-Left
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }

        return bearing;
    }

    // normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}