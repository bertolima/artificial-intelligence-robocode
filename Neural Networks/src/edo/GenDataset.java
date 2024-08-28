package edo;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotSpecification;

import java.io.File;

public class GenDataset {
    public static final int NUMBER_OF_ROUNDS = 10000;
    public static int robotScore,enemyScore;
    public static final String trainingRobots = "edo.FatorIntegrante*,";
    public static final String[] enemys = {"sample.Interactive", "sample.Walls", "darkcanuck.Pris 0.92", "sample.SpinBot", "sample.VelociRobot"};
    public static final String robocodePath = "/home/luis/workspace/java/robocode";

    public static void main(String[] args) throws Exception {
        new GenDataset().batalhar(); // run main
    }

    public void batalhar(){
        File f = new File("train.csv");
        if(!f.exists()) {
            CreateFile.main("train.csv");
            Writer.main("train.csv", "myX,myY,waveX,waveY,vel,head,dire,dist,bearing,fX,fY\n");
        }

        RobocodeEngine engine = new RobocodeEngine(new java.io.File(robocodePath));
        engine.addBattleListener(new battleObserver());
        engine.setVisible(false);

        BattlefieldSpecification battlefield = new BattlefieldSpecification(800, 600);
        RobotSpecification[] selectedRobots = engine.getLocalRepository(((trainingRobots+enemys[2])));
        BattleSpecification battleSpec = new BattleSpecification(NUMBER_OF_ROUNDS, battlefield, selectedRobots);

        engine.runBattle(battleSpec, true);
        engine.close();
    }
}
