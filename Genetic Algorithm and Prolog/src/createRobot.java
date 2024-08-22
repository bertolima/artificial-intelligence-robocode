import javax.tools.*;
import java.io.*;

public class createRobot {

    public static void create(double[] chromo) {
        createRobotFile(chromo);
        String files[] = new String[2];
        files[0] = "C:/robocode/robots/edo/FatorIntegrante.java";
        files[1] = "C:/robocode/robots/edo/Enemy.java";
        compile(files);
    }

    public static void compile (String[] files) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, files[0], files[1]);
    }

    public static void createRobotFile(double[] chromo){
        try {
            FileWriter fstream = new FileWriter("C:/robocode/robots/edo/FatorIntegrante.java");
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("package edo;\n" +
                    "import robocode.*;\n" +
                    "import robocode.control.events.RoundStartedEvent;\n" +
                    "import robocode.util.Utils;\n" +
                    "import org.jpl7.Query;\n" +
                    "import org.jpl7.Term;\n" +
                    "import org.jgap.*;\n" +
                    "import org.jgap.impl.*;\n" +
                    "\n" +
                    "import java.awt.*;\n" +
                    "import java.awt.geom.Point2D;\n" +
                    "import java.util.*;\n" +
                    "\n" +
                    "\n" +
                    "public class FatorIntegrante extends AdvancedRobot {\n" +
                    "    private static final int LIMIT_FROM_ENEMY = 20;\n" +
                    "    private static final int MIN_ENERGY = 20;\n" +
                    "    private static final double DISTANCE_TO_WALK = 50;\n" +
                    "    public double ZIGZAG_TIME = 40d;\n" +
                    "\n" +
                    "    HashMap<String, Double> global_values = new HashMap<>();\n" +
                    "    HashMap<String, Query> querys = new HashMap<>();\n" +
                    "    private boolean isNearWall = false;\n" +
                    "    private boolean isLowRanged = false;\n" +
                    "    private boolean isMidRanged = false;\n" +
                    "    private boolean isNearEnemy = false;\n" +
                    "    private boolean isEnergyLow = false;\n" +
                    "    private boolean isEnemyEnergyLow = false;\n" +
                    "    private boolean isEnemyMoving = false;\n" +
                    "    private boolean canTurnGunLeft = false;\n" +
                    "    private boolean canTurnGunRight = false;\n" +
                    "    private boolean currentInWall = false;\n" +
                    "    public int direction = 1;\n" +
                    "    private static int[] boundaries = {0, 45, 90, 135, 180, 225, 270, 315, 360};\n" +
                    "    private static int[] zones = {1, -1, 1, -1, 1, -1, 1, -1};\n" +
                    "    private double angleNearWall;\n" +
                    "\n" +
                    "\n" +
                    "    private Enemy enemy = new Enemy();\n" +
                    "    private Point2D[] enemyPosition = {new Point2D.Double(0d,0d), new Point2D.Double(0d,0d)};\n" +
                    "\n" +
                    "\n" +
                    "    public void run() {\n" +
                    "        setColors(Color.BLACK, Color.RED, Color.RED, Color.black, Color.GREEN);\n" +
                    "        setAdjustRadarForGunTurn(true);\n" +
                    "        setAdjustRadarForRobotTurn(true);\n" +
                    "        setAdjustGunForRobotTurn(true);\n" +
                    "        if (!Query.hasSolution(\"consult('C:/robocode/arg.pl').\")) {\n" +
                    "            System.out.println(\"Consult failed\");\n" +
                    "        }\n" +
                    "\n" +
                    "        initFlags();\n" +
                    "        initQuerys();\n" +
                    "        setTurnRadarRight(Double.POSITIVE_INFINITY);\n" +
                    "        while (true) {\n" +
                    "            execute();\n" +
                    "            scan();\n" +
                    "\n" +
                    "            updateQuerys();\n" +
                    "            executeQuerys();\n" +
                    "\n" +
                    "            moveset();\n" +
                    "            explosion();\n" +
                    "        }\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "    private void initFlags(){\n" +
                    "        global_values.put(\"AngleToEnemy\", 0d);\n" +
                    "    }\n" +
                    "\n" +
                    "    private void initQuerys(){\n" +
                    "        querys.put(\"isNearWall\", null);\n" +
                    "        querys.put(\"isLowRanged\", null);\n" +
                    "        querys.put(\"isMidRanged\", null);\n" +
                    "        querys.put(\"isNearEnemy\", null);\n" +
                    "        querys.put(\"isEnergyLow\", null);\n" +
                    "        querys.put(\"isEnemyEnergyLow\", null);\n" +
                    "        querys.put(\"isEnemyMoving\", null);\n" +
                    "        querys.put(\"canTurnGunLeft\", null);\n" +
                    "        querys.put(\"canTurnGunRight\", null);\n" +
                    "    }\n" +
                    "\n" +
                    "    public void updateQuerys(){\n" +
                    "        querys.replace(\"isNearWall\",\n" +
                    "                new Query(\"isNearWall\", new Term[]{new org.jpl7.Float(getX()),\n" +
                    "                        new org.jpl7.Float(getY()),\n" +
                    "                        new org.jpl7.Float(getBattleFieldWidth()),\n" +
                    "                        new org.jpl7.Float(getBattleFieldHeight()),\n" +
                    "                        new org.jpl7.Float(" + chromo[5] + ")}));\n" +
                    "\n" +
                    "        querys.replace(\"isEnergyLow\", new Query(\"isEnergyLow\", new Term[]{new org.jpl7.Float(getEnergy()),\n" +
                    "                new org.jpl7.Float(MIN_ENERGY)}));\n" +
                    "    }\n" +
                    "\n" +
                    "    public void executeQuerys(){\n" +
                    "        if (querys.get(\"isNearWall\").hasSolution()) isNearWall = true;\n" +
                    "        else isNearWall = false;\n" +
                    "\n" +
                    "        if (querys.get(\"isEnergyLow\").hasSolution()) isEnergyLow = true;\n" +
                    "        else isEnergyLow = false;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void updateEnemyQuerys(ScannedRobotEvent ev){\n" +
                    "        querys.replace(\"isLowRanged\", new Query(\"isEnemyLowRanged\", new Term[]{new org.jpl7.Float(ev.getDistance())}));\n" +
                    "        querys.replace(\"isMidRanged\", new Query(\"isEnemyMidRanged\", new Term[]{new org.jpl7.Float(ev.getDistance())}));\n" +
                    "        querys.replace(\"isNearEnemy\", new Query(\"isNearEnemy\", new Term[]{new org.jpl7.Float(ev.getDistance()),\n" +
                    "                                                                            new org.jpl7.Float(LIMIT_FROM_ENEMY)}));\n" +
                    "        querys.replace(\"isEnemyEnergyLow\", new Query(\"isEnemyEnergyLow\", new Term[]{new org.jpl7.Float(ev.getEnergy()),\n" +
                    "                                                                            new org.jpl7.Float(MIN_ENERGY)}));\n" +
                    "        querys.replace(\"isEnemyMoving\", new Query(\"isEnemyMoving\", new Term[]{new org.jpl7.Float(enemyPosition[0].distance(enemyPosition[1]))}));\n" +
                    "        querys.replace(\"canTurnGunLeft\", new Query(\"canTurnGunLeft\", new Term[]{new org.jpl7.Float(getX()),\n" +
                    "        new org.jpl7.Float(getY()), new org.jpl7.Float(enemyPosition[0].getX()), new org.jpl7.Float(enemyPosition[0].getY()),\n" +
                    "        new org.jpl7.Float(enemyPosition[1].getX()), new org.jpl7.Float(enemyPosition[1].getY())}));\n" +
                    "        querys.replace(\"canTurnGunRight\", new Query(\"canTurnGunRight\", new Term[]{new org.jpl7.Float(getX()),\n" +
                    "                new org.jpl7.Float(getY()), new org.jpl7.Float(enemyPosition[0].getX()), new org.jpl7.Float(enemyPosition[0].getY()),\n" +
                    "                new org.jpl7.Float(enemyPosition[1].getX()), new org.jpl7.Float(enemyPosition[1].getY())}));\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "    public void executeEnemyQuerys(){\n" +
                    "        isLowRanged = querys.get(\"isLowRanged\").hasSolution();\n" +
                    "        isMidRanged = querys.get(\"isMidRanged\").hasSolution();\n" +
                    "        isNearEnemy = querys.get(\"isNearEnemy\").hasSolution();\n" +
                    "        isEnemyEnergyLow = querys.get(\"isEnemyEnergyLow\").hasSolution();\n" +
                    "        isEnemyMoving = querys.get(\"isEnemyMoving\").hasSolution();\n" +
                    "        canTurnGunLeft = querys.get(\"canTurnGunLeft\").hasSolution();\n" +
                    "        canTurnGunRight = querys.get(\"canTurnGunRight\").hasSolution();\n" +
                    "    }\n" +
                    "\n" +
                    "    private void resetQuerys(){\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "    private void lockIn(ScannedRobotEvent ev){\n" +
                    "        global_values.replace(\"AngleToEnemy\", getHeadingRadians() + ev.getBearingRadians());\n" +
                    "        Double newRadarAngle = global_values.get(\"AngleToEnemy\") - getRadarHeadingRadians();\n" +
                    "        setTurnRadarRightRadians(Utils.normalRelativeAngle(newRadarAngle));\n" +
                    "    }\n" +
                    "\n" +
                    "    public double updateDirection(double currentHeading){\n" +
                    "        double newAngle;\n" +
                    "        if(Utils.isNear(getTime() % ZIGZAG_TIME, 0d) && !isNearWall) direction *= -1;\n" +
                    "        if (direction == 1 && !isNearWall) newAngle = Math.toRadians(" + chromo[0] + ");\n" +
                    "        else if(direction == -1 && !isNearWall) newAngle = -1 * Math.toRadians(" + chromo[1] + ");\n" +
                    "        else newAngle = avoidColission();\n" +
                    "        out.println(newAngle);\n" +
                    "        return newAngle;\n" +
                    "    }\n" +
                    "\n" +
                    "    public double avoidColission(){\n" +
                    "        if (isNearWall && !currentInWall){\n" +
                    "            currentInWall = true;\n" +
                    "            Vector<Boolean> nearTable = new Vector<Boolean>();\n" +
                    "            for (int i=0;i<8;i++) nearTable.add(getHeading() > boundaries[i] && getHeading() <= boundaries[i+1]);\n" +
                    "            int closestIndex = nearTable.indexOf(true);\n" +
                    "            double targetAngle = (boundaries[closestIndex+1] + 180) % 180;\n" +
                    "            angleNearWall = Math.toRadians(getHeading() - targetAngle + " + chromo[4] + ");\n" +
                    "        }\n" +
                    "        return angleNearWall;\n" +
                    "    }\n" +
                    "\n" +
                    "    private void moveset(){\n" +
                    "        double turnBodyToEnemy = global_values.get(\"AngleToEnemy\") - getHeadingRadians();\n" +
                    "        double newBodyAngle = updateDirection(turnBodyToEnemy);\n" +
                    "        setTurnRightRadians(newBodyAngle);\n" +
                    "        setAhead(DISTANCE_TO_WALK);\n" +
                    "    }\n" +
                    "\n" +
                    "    public double calculateProjection(){\n" +
                    "        if (!isEnemyMoving) return 0d;\n" +
                    "        else if(canTurnGunRight && !canTurnGunLeft) return Math.toRadians(" + chromo[2] + ");\n" +
                    "        return Math.toRadians(" + chromo[3] + ") * -1;\n" +
                    "    }\n" +
                    "\n" +
                    "    public double calculateFirePower(){\n" +
                    "        if (isLowRanged) return 3d;\n" +
                    "        else if(isMidRanged) return 2d;\n" +
                    "        else if (isEnergyLow) return 0.8d;\n" +
                    "        else if(isEnemyEnergyLow) return 1d;\n" +
                    "        else return 1d;\n" +
                    "    }\n" +
                    "\n" +
                    "    private void explosion(){\n" +
                    "        double newAngleGun = global_values.get(\"AngleToEnemy\") - this.getGunHeadingRadians();\n" +
                    "        double projection = calculateProjection();\n" +
                    "        setTurnGunRightRadians(Utils.normalRelativeAngle(newAngleGun + projection));\n" +
                    "        double firePower = calculateFirePower();\n" +
                    "        setFire(firePower);\n" +
                    "    }\n" +
                    "\n" +
                    "    public void onScannedRobot(ScannedRobotEvent ev) {\n" +
                    "        if (this.enemy.none() || ev.getName().equals(enemy.getName())) enemy.update(ev);\n" +
                    "        updateEnemyPosition(ev);\n" +
                    "        lockIn(ev);\n" +
                    "        updateEnemyQuerys(ev);\n" +
                    "        executeEnemyQuerys();\n" +
                    "        resetQuerys();\n" +
                    "    }\n" +
                    "\n" +
                    "    public void updateEnemyPosition(ScannedRobotEvent ev){\n" +
                    "        this.enemyPosition[1] = this.enemyPosition[0];\n" +
                    "        Point2D.Double currentEnemyPosition = new Point2D.Double(getX() + ev.getDistance()*Math.sin(global_values.get(\"AngleToEnemy\")), getY() + ev.getDistance()*Math.cos(global_values.get(\"AngleToEnemy\")));\n" +
                    "        this.enemyPosition[0] = currentEnemyPosition;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void onBulletHit (BulletHitEvent ev){\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "    public void onHitByBullet(HitByBulletEvent ev){\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "    public void onHitWall(HitWallEvent ev){\n" +
                    "\n" +
                    "    }\n" +
                    "\n" +
                    "\n" +
                    "    public void onRobotDeath(RobotDeathEvent event){\n" +
                    "        this.enemy.reset();\n" +
                    "        setTurnRadarRight (Double.POSITIVE_INFINITY);\n" +
                    "    }\n" +
                    "}\n");

            out.close();

        } catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }
}