package edo;

import net.sourceforge.jFuzzyLogic.FunctionBlock;

import java.awt.geom.Point2D;
import java.util.Random;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

public class Functions {

    public static boolean needNormalize(Point2D.Double p, double offset, double battleFieldX, double battleFieldY){
        return (p.x < offset
                || p.y < offset
                || p.x > battleFieldX - offset
                || p.y > battleFieldY - offset);
    }

    public static Point2D.Double normalizeCoords(Point2D.Double p, double offset, double battleFieldX, double battleFieldY){
        p.x = Math.min(Math.max(18.0 + offset, p.x), battleFieldX - 18.0 - offset);
        p.y = Math.min(Math.max(18.0 + offset, p.y), battleFieldY - 18.0 - offset);
        return p;
    }

    public static double calculateFirePower(double energy, double distance, double enemyEnergy){
        double fp;
        if (energy < 10d) fp = 0.1d;
        else if(distance > 400) fp = 1.5d;
        else if (distance < 200) fp = 3d;
        else fp = 2d;
        return Math.min(enemyEnergy/4, fp);
    }

    public static void calculateNextPosition(Point2D.Double myPos, Point2D.Double otherPos, double angle, double distance){
        myPos.setLocation(
                otherPos.getX() + Math.cos(angle) * distance,
                otherPos.getY() + Math.sin(angle) * distance);
    }

    public static double calculateFirePower(FunctionBlock firePower, double distance, double energy, double enemyEnergy){
        firePower.setVariable("distance", distance);
        firePower.setVariable("enemyEnergy", enemyEnergy);
        firePower.setVariable("energy", energy);
        firePower.evaluate();

        return firePower.getVariable("firePower").getValue();
    }

    public static Point2D.Double getTargetPosition(Point2D.Double myPos, Point2D.Double enemyPos, Point2D.Double battleSize, double enemySpeed, double enemyCurrentHeading, double headingDif, double bulletTravelTime, double firePower){
        if (Math.abs(headingDif) > 0.01d) return predictCircularMove(enemyPos, enemySpeed, enemyCurrentHeading, headingDif, bulletTravelTime, firePower);
        return linearTargeting(bulletTravelTime);
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

    private Point2D.Double linearTargeting(double bulletTime){
        Point2D.Double newPos = new Point2D.Double(this.enemy.getX(), this.enemy.getY());
        FatorIntegrante.Axis axis = determineLinearMovement();
        double projection = 2 * (this.enemy.getVelocity() * bulletTime);

        if (this.signal.equals(FatorIntegrante.Signal.MINUS)) projection *= -1;

        if (axis.equals(FatorIntegrante.Axis.X)) {
            newPos.setLocation(newPos.getX() + projection, newPos.getY());
        } else if (axis.equals(FatorIntegrante.Axis.Y)) {
            newPos.setLocation(newPos.getX(), newPos.getY() + projection);
        } else if(axis.equals(FatorIntegrante.Axis.NEUTRAL)){
            newPos.setLocation(newPos.getX() + Math.cos(this.enemy.getHeading())*projection,
                    newPos.getY()+ Math.sin(this.enemy.getHeading())*projection);
        }
        this.signal = FatorIntegrante.Signal.NONE;
        return newPos;
    }

    private FatorIntegrante.Axis determineLinearMovement(){
        Point2D.Double coordDiff = new Point2D.Double(this.enemy.getX() - this.enemy.getPrevCoord().getX(),
                this.enemy.getY() - this.enemy.getPrevCoord().getY());
        this.enemy.setPrevDistance(this.enemy.getCoord().distance(this.enemy.getPrevCoord()));
        this.distanceBeetweenMyCoords = this.myPrevCoord.distance(getX(), getY());
        if (Math.abs(Math.abs(coordDiff.x) - Math.abs(coordDiff.y)) > this.enemy.getPrevDistance()/2){
            if (Math.max(Math.abs(coordDiff.x), Math.abs(coordDiff.y)) == Math.abs(coordDiff.x)){
                if (coordDiff.x < 0) this.signal = FatorIntegrante.Signal.MINUS;
                else this.signal = FatorIntegrante.Signal.PLUS;
                return FatorIntegrante.Axis.X;
            }
            else{
                if (coordDiff.y < 0) this.signal = FatorIntegrante.Signal.MINUS;
                else this.signal = FatorIntegrante.Signal.PLUS;
                return FatorIntegrante.Axis.Y;
            }
        } else{
            this.signal = FatorIntegrante.Signal.NEUTRAL;
            return FatorIntegrante.Axis.NEUTRAL;
        }
    }
}
