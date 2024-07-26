package edo;

import Enemy.EnemyBot;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import robocode.util.Utils;

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

    public static double calculateDistanceFromEnemy(FunctionBlock distance ,double energy, double enemyEnergy){
        distance.setVariable("enemyEnergy", enemyEnergy);
        distance.setVariable("energy", energy);
        distance.evaluate();
        return distance.getVariable("distance").getValue();
    }



        public static Point2D.Double getTargetPosition(Point2D.Double myPos, EnemyBot enemy, Point2D.Double battleSize, double enemyCurrentHeading, double headingDif, double bulletTravelTime, BayesNode[] nodes, IBayesInferer inferer, HashMap<Integer, String> getDirection){
        if (Math.abs(headingDif) > 0.01d) return predictCircularMove(myPos, enemy.getCoord(), battleSize, enemy.getVelocity(), enemyCurrentHeading, headingDif, bulletTravelTime);
        return linearTargeting(enemy, enemy.getVelocity() * bulletTravelTime, nodes, inferer, getDirection);
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

    private static Point2D.Double linearTargeting(EnemyBot enemy, double enemyDistance, BayesNode[] nodes, IBayesInferer inferer, HashMap<Integer, String> getDirection){
        Point2D.Double newPos = new Point2D.Double(enemy.getX(), enemy.getY());
        int i = determineLinearMovement(enemy, nodes, inferer);
        String ans;
        if (i != -1) ans = getDirection.get(i);
        else return newPos;
        System.out.println(enemy.getHeading());
        System.out.println(ans);
        switch (ans) {
            case "xplus":
                newPos.setLocation(newPos.getX() + enemyDistance, newPos.getY());
                break;
            case "xminus":
                newPos.setLocation(newPos.getX() - enemyDistance, newPos.getY());
                break;
            case "yplus":
                newPos.setLocation(newPos.getX(), newPos.getY() + enemyDistance);
                break;
            case "yminus":
                newPos.setLocation(newPos.getX(), newPos.getY() - enemyDistance);
                break;
            case "xyplus":
                newPos.setLocation(newPos.getX() + Math.cos(enemy.getHeading())*enemyDistance, newPos.getY()+ Math.sin(enemy.getHeading())*enemyDistance);
                break;
            case "xyminus":
                newPos.setLocation(newPos.getX() + Math.cos(enemy.getHeading())*enemyDistance*-1, newPos.getY()+ Math.sin(enemy.getHeading())*enemyDistance*-1);
                break;
            case "xplusyminus":
                newPos.setLocation(newPos.getX() + Math.cos(enemy.getHeading())*enemyDistance, newPos.getY()+ Math.sin(enemy.getHeading())*enemyDistance*-1);
                break;
            case "xminusyplus":
                newPos.setLocation(newPos.getX() + Math.cos(enemy.getHeading())*enemyDistance*-1, newPos.getY()+ Math.sin(enemy.getHeading())*enemyDistance);
                break;
            case "still":
                break;
        }

        return newPos;
    }

    private static int determineLinearMovement(EnemyBot enemy, BayesNode[] nodes, IBayesInferer inferer){
        Point2D.Double diffPos = new Point2D.Double(enemy.getX() - enemy.getPrevCoord().getX(),
                enemy.getY() - enemy.getPrevCoord().getY());
        enemy.setPrevDistance(enemy.getCoord().distance(enemy.getPrevCoord()));

        Map<BayesNode, String> evidence = new HashMap<>();

        if (diffPos.x < 0) evidence.put(nodes[1], "minus");
        else evidence.put(nodes[1], "plus");
        if (diffPos.y < 0) evidence.put(nodes[2], "minus");
        else evidence.put(nodes[2], "plus");

        if (Utils.isNear(diffPos.x, 0) && Utils.isNear(diffPos.y, 0)) evidence.put(nodes[0], "still");
        else if(Math.abs(Math.abs(diffPos.x) - Math.abs(diffPos.y)) > enemy.getPrevDistance()/2){
            if (Math.abs(diffPos.x) > Math.abs(diffPos.y)) evidence.put(nodes[0], "x");
            else  evidence.put(nodes[0], "y");
        }
        else evidence.put(nodes[0], "xy");

        inferer.setEvidence(evidence);
        double[] beliefsD = inferer.getBeliefs(nodes[3]);
        double best = Arrays.stream(beliefsD).max().getAsDouble();
        int index = -1;
        for (int i = 0; i< beliefsD.length;i++) {
           if (beliefsD[i] == best) return i;
        }

       return -1;

    }

    public static BayesNode[] initBayesNetwork(IBayesInferer inferer){
        BayesNet net = new BayesNet();
        BayesNode[] nodes = new BayesNode[4];
        nodes[0] = net.createNode("axis");
        nodes[0].addOutcomes("x", "y", "xy", "still");
        nodes[0].setProbabilities (0.25, 0.25, 0.25, 0.25);

        nodes[1] = net.createNode("xsignal");
        nodes[1].addOutcomes("plus", "minus");
        nodes[1].setParents(Arrays.asList(nodes[0]));
        nodes[1].setProbabilities (
                0.5, 0.5, // a == x
                0.5, 0.5, // a == y
                0.5, 0.5, // a == xy
                0.5, 0.5 // a == still
        );

        nodes[2] = net.createNode("ysignal");
        nodes[2].addOutcomes("plus", "minus");
        nodes[2].setParents(Arrays.asList(nodes[0], nodes[1]));
        nodes[2].setProbabilities (
                //a = x
                0.5, 0.5, // b == xplus
                0.5, 0.5, // b == xminus
                //a=y
                0.5, 0.5, // b == xplus
                0.5, 0.5, // b == xminus
                //a=xy
                0.5, 0.5, // a == xplus
                0.5, 0.5, // a == xminus
                //a=still
                0.5, 0.5, // b == plus
                0.5, 0.5  // b == minus
        );

        nodes[3] = net.createNode("d");
        nodes[3].addOutcomes("xplus", "xminus", "yplus", "yminus", "xyplus", "xyminus", "xplusyminus", "xminusyplus", "still");
        nodes[3].setParents(Arrays.asList(nodes[0], nodes[1], nodes[2]));

        nodes[3].setProbabilities(
                // a == x
                0.5, 0.0, 0.0, 0.0, 0.3, 0.0, 0.0, 0.0, 0.2, // b == xplusyplus
                0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.3, 0.0, 0.2, // b == xplusyminus
                0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.3, 0.2, // b == xminyplus
                0.0, 0.5, 0.0, 0.0, 0.0, 0.3, 0.0, 0.0, 0.2, // b == xminyminus
                // a == y
                0.0, 0.0, 0.5, 0.0, 0.3, 0.0, 0.0, 0.0, 0.2, // b == xplusyplus
                0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.3, 0.0, 0.2, // b == xplusyminus
                0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.3, 0.2, // b == xminyplus
                0.0, 0.0, 0.0, 0.5, 0.0, 0.3, 0.0, 0.0, 0.2, // b == xminyminus
                // a == xy
                0.15, 0.0, 0.15, 0.0, 0.5, 0.0, 0.0, 0.0, 0.2, // b == xplusyplus
                0.15, 0.0, 0.0, 0.15, 0.0, 0.0, 0.5, 0.0, 0.2, // b == xplusyminus
                0.0, 0.15, 0.15, 0.0, 0.0, 0.0, 0.0, 0.5, 0.2, // b == xminyplus
                0.0, 0.15, 0.0, 0.15, 0.0, 0.5, 0.0, 0.0, 0.2, // b == xminyminus
                // a == still
                0.1, 0.0, 0.0, 0.0, 0.2, 0.0, 0.2, 0.0, 0.5, // b == xplusyplus
                0.0, 0.1, 0.0, 0.0, 0.0, 0.2, 0.0, 0.2, 0.5, // b == xplusyminus
                0.0, 0.0, 0.1, 0.0, 0.2, 0.0, 0.0, 0.2, 0.5, // b == xminyplus
                0.0, 0.0, 0.0, 0.1, 0.0, 0.2, 0.2, 0.0, 0.5  // b == xminyminus

        );
        inferer.setNetwork(net);
        return nodes;
    }

    public static HashMap<Integer, String> initDirectionHash(){
        HashMap<Integer, String> getDirection = new HashMap<>();
        getDirection.put(0, "xplus");
        getDirection.put(1, "xminus");
        getDirection.put(2, "yplus");
        getDirection.put(3, "yminus");
        getDirection.put(4, "xyplus");
        getDirection.put(5, "xyminus");
        getDirection.put(6, "xplusyminus");
        getDirection.put(7, "xminusyplus");
        getDirection.put(8, "still");
        return getDirection;
    }

}
