package edo;

import java.awt.geom.Point2D;
import java.util.Random;

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
}
