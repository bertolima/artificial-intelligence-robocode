package edo;

import Enemy.EnemyBot;
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

    public static Point2D.Double getTargetPosition(Point2D.Double myPos, EnemyBot enemy, Point2D.Double battleSize, double enemyCurrentHeading, double headingDif, double bulletTravelTime){
        if (Math.abs(headingDif) > 0.01d) return predictCircularMove(myPos, enemy.getCoord(), battleSize, enemy.getVelocity(), enemyCurrentHeading, headingDif, bulletTravelTime);
        return linearTargeting(enemy, enemy.getVelocity() * bulletTravelTime);
    }

    private static Point2D.Double predictCircularMove(Point2D myPos, Point2D enemyPos, Point2D.Double battleSize, double speed, double currentHeading, double diffHeading, double time){
        Point2D.Double newPos = new Point2D.Double(enemyPos.getX(), enemyPos.getY());
        double delta = 0d;
        while ((++delta) * time < Point2D.Double.distance(myPos.getX(), myPos.getX(), newPos.x, newPos.y)){

            newPos.x += Math.sin(currentHeading) * speed;
            newPos.y += Math.cos(currentHeading) * speed;
            currentHeading += diffHeading;

            if (needNormalize(newPos, 0, battleSize.getX(), battleSize.getY())){
                newPos.setLocation(Functions.normalizeCoords(newPos, 0, battleSize.getX(), battleSize.getY()));
                break;
            }
        }
        return newPos;
    }

    private static Point2D.Double linearTargeting(EnemyBot enemy, double enemyDistance){
        Point2D.Double newPos = new Point2D.Double(enemy.getX(), enemy.getY());
        byte[] ans = determineLinearMovement(enemy);
        double projection = 2 * enemyDistance;

        if (ans[1] == -1) projection *= -1;

        if (ans[0] == 1) newPos.setLocation(newPos.getX() + projection, newPos.getY());
        else if (ans[0] == -1) newPos.setLocation(newPos.getX(), newPos.getY() + projection);
        else newPos.setLocation(newPos.getX() + Math.cos(enemy.getHeading())*projection,
                    newPos.getY()+ Math.sin(enemy.getHeading())*projection);

        return newPos;
    }

    private static byte[] determineLinearMovement(EnemyBot enemy){
        Point2D.Double diffPos = new Point2D.Double(enemy.getX() - enemy.getPrevCoord().getX(),
                enemy.getY() - enemy.getPrevCoord().getY());
        enemy.setPrevDistance(enemy.getCoord().distance(enemy.getPrevCoord()));
        byte[] ans = {0,0};
        if (Math.abs(Math.abs(diffPos.x) - Math.abs(diffPos.y)) > enemy.getPrevDistance()/2){
            if (Math.max(Math.abs(diffPos.x), Math.abs(diffPos.y)) == Math.abs(diffPos.x)){
                if (diffPos.x < 0) ans[1] = -1;
                else ans[1] = 1;;
                ans[0] = 1;
            }
            else{
                if (diffPos.y < 0) ans[1] = -1;
                else ans[1] = 1;
                ans[0] = -1;
            }
        }
        
        return ans;
    }
}
