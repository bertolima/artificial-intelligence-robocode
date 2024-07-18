package edo;

import java.awt.geom.Point2D;

public class Functions {

    public static boolean needNormalize(Point2D.Double p, double offset, double battleFieldX, double battleFieldY){
        return (p.x < 18.0 + offset
                || p.y < 18.0 + offset
                || p.x > battleFieldX - 18.0 - offset
                || p.y > battleFieldY - 18.0 - offset);
    }

    public static Point2D.Double normalizeCoords(Point2D.Double p, double offset, double battleFieldX, double battleFieldY){
        p.x = Math.min(Math.max(18.0 + offset, p.x), battleFieldX - 18.0 - offset);
        p.y = Math.min(Math.max(18.0 + offset, p.y), battleFieldY - 18.0 - offset);
        return p;
    }
}
