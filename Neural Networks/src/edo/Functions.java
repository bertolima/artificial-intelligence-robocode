package edo;

import net.sourceforge.jFuzzyLogic.FunctionBlock;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;

public class Functions {

    public static boolean needNormalize(Point2D.Double p, double offset, double battleFieldX, double battleFieldY){
        return (p.x < offset
                || p.y < offset
                || p.x > battleFieldX - offset
                || p.y > battleFieldY - offset);
    }

    public static double normalize(double x, double min_x, double max_x){
        return 2*((x-min_x)/(max_x-min_x))-1;
    }

    public static double denormalize(double x, double min_x, double max_x){
        return (x + 1)*((max_x-min_x)/2) + min_x;
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

    public static Point2D.Double calculateNextPosition(Point2D.Double Pos, double angle, double distance){
        return new Point2D.Double(Pos.x + Math.sin(angle) * distance,Pos.y + Math.cos(angle) * distance);
    }

    public static double calculateFirePower(FunctionBlock firePower, double distance, double energy, double enemyEnergy){
        firePower.setVariable("distance", distance);
        firePower.setVariable("enemyEnergy", enemyEnergy);
        firePower.setVariable("energy", energy);
        firePower.evaluate();

        return firePower.getVariable("firePower").getValue();
    }

    public static double calculateDistanceFromEnemy(FunctionBlock distance ,double energy, double enemyEnergy){
        distance.setVariable("enemyEnergy", enemyEnergy);
        distance.setVariable("energy", energy);
        distance.evaluate();
        return distance.getVariable("dist").getValue();
    }

    public static double calculateAngulationFromEnemy(FunctionBlock angulation ,double distance, double speed){
        angulation.setVariable("distance", distance);
        angulation.setVariable("speed", speed);
        angulation.evaluate();
        return angulation.getVariable("angulation").getValue();
    }

    public static double getAbsoluteBearing(Point2D.Double Pos, Point2D.Double target) {
        return Math.atan2(target.x - Pos.x, target.y - Pos.y);
    }

    public static double getLimit(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double getMaxScapeAngle(double velocity) {
        return Math.asin(8.0/velocity);
    }


    public static int getFactorIndex(Wave w, Point2D.Double targetLocation, int bins) {
        double offsetAngle = (getAbsoluteBearing(w.fireLocation, targetLocation)- w.directAngle);
        double factor = Utils.normalRelativeAngle(offsetAngle)/getMaxScapeAngle(w.bulletVelocity) * w.direction;

        return (int)getLimit(0, (factor * ((bins - 1) / 2)) + ((bins - 1) / 2),bins - 1);
    }



}
