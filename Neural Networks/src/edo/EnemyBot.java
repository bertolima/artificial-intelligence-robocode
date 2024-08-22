package edo;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.geom.Point2D;

public class EnemyBot extends AdvancedRobot {

    private volatile double bearing;
    private volatile double distance;
    private volatile double energy = 100d;
    private volatile double prev_energy = 0;
    private volatile double heading;
    private volatile String name = "";
    private volatile double velocity;
    private volatile double x;
    private volatile double y;
    private volatile double prev_x;
    private volatile double prev_y;


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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPrevX() {
        return prev_x;
    }
    public double getPrevY() {
        return prev_y;
    }
    public Point2D.Double getPos(){
        return new Point2D.Double(getX(), getY());
    }

    public double getPrev_energy() {
        return prev_energy;
    }

    public void reset() {
        bearing = 0.0;
        distance = 0.0;
        energy = 0.0;
        heading = 0.0;
        name = "";
        velocity = 0.0;
        prev_energy= 100d;
        x = 0.0;
        y = 0.0;
        prev_x = 0.0;
        prev_y = 0.0;
    }


    public boolean none() {
        return "".equals(name);
    }

    public void update(ScannedRobotEvent e, AdvancedRobot robot) {
        prev_x = x;
        prev_y = y;
        double angle = robot.getHeadingRadians() + e.getBearingRadians();
        x = robot.getX() + Math.sin(angle) * e.getDistance();
        y = robot.getY() + Math.cos(angle) * e.getDistance();
        bearing = e.getBearingRadians();
        distance = e.getDistance();
        prev_energy = energy;
        energy = e.getEnergy();
        heading = e.getHeadingRadians();
        name = e.getName();
        velocity = e.getVelocity();
    }
}
