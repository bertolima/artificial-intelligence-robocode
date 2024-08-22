import robocode.ScannedRobotEvent;

import java.awt.geom.Point2D;

public class Enemy {
    private double distance;
    private double bearing;
    private double bearingRadians;
    private double energy;
    private double heading;
    private double headingRadians;
    private String name;
    private double velocity;
    private double distanceByCoords;
    private double prevHeading;
    private double prevHeadingRadians;
    private double prevEnergy;
    private Point2D.Double prevCoord;
    private Point2D.Double coord;

    public Enemy(){
        reset();
    }

    public void reset(){
        this.name = "";
        this.bearing = 0d;
        this.distance = 0d;
        this.energy = 0d;
        this.prevHeading = 0d;
        this.velocity = 0d;
        this.prevHeading = 0d;
        this.heading = 0d;
        this.headingRadians = 0d;
        this.bearingRadians = 0d;
        this.prevHeadingRadians = 0d;
        this.distanceByCoords = 0d;
        this.prevHeading = 0d;
        this.prevHeadingRadians = 0d;
        this.energy = 0d;
        this.coord = new Point2D.Double(0d,0d);
        this.prevCoord = new Point2D.Double(0d,0d);
    }

    public void update(ScannedRobotEvent ev){
        this.name = ev.getName();
        this.prevHeading = new Double(this.heading);
        this.prevHeadingRadians = new Double(this.headingRadians);
        this.headingRadians = ev.getHeadingRadians();
        this.bearingRadians = ev.getBearingRadians();
        this.bearing = ev.getBearing();
        this.distance = ev.getDistance();
        this.prevEnergy = new Double(this.energy);
        this.energy = ev.getEnergy();
        this.velocity = ev.getVelocity();
        this.heading = ev.getHeading();
    }

    public double getPrevEnergy() {
        return prevEnergy;
    }

    public void setPrevDistance(double d) {
        this.distanceByCoords = d;
    }

    public void setPrevHeading(double prevHeading) {
        this.prevHeading = prevHeading;
    }

    public void setPrevHeadingRadians(double prevHeadingRadians) {
        this.prevHeadingRadians = prevHeadingRadians;
    }

    public void setPrevCoord(double x, double y){
        this.prevCoord.setLocation(x,y);
    }

    public Point2D.Double getPrevCoord(){
        return this.prevCoord;
    }

    public void setCoord(double x, double y){
        this.coord.setLocation(x,y);
    }

    public Point2D.Double getCoord(){
        return this.coord;
    }

    public double getX(){
        return this.coord.getX();
    }

    public double getY(){
        return this.coord.getY();
    }

    public double getDistance() {
        return distance;
    }

    public double getBearing() {
        return bearing;
    }

    public double getEnergy() {
        return energy;
    }

    public double getHeading() {
        return heading;
    }

    public String getName() {
        return name;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getPrevDistance() {
        return distanceByCoords;
    }

    public double getPrevHeading() {
        return prevHeading;
    }

    public double getBearingRadians() {
        return bearingRadians;
    }

    public double getHeadingRadians() {
        return headingRadians;
    }

    public double getPrevHeadingRadians() {
        return prevHeadingRadians;
    }

    public boolean none(){
        return this.name.equals("");
    }

}