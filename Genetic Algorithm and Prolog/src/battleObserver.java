import net.sf.robocode.battle.Battle;
import robocode.BattleResults;
import robocode.control.events.*;

class battleObserver extends BattleAdaptor {

    public void onBattleCompleted(BattleCompletedEvent e) {
        int robotScore = 0,enemyScore = 0;
        runGA robot = new runGA();
        BattleResults[] results = e.getSortedResults();

        for (robocode.BattleResults result : e.getSortedResults()) {
            if(robot.isRobot(result.getTeamLeaderName())){
                robotScore = result.getScore();
            }
            else enemyScore = result.getScore();
        }
        robot.setScore(robotScore,enemyScore);
        String[] s = new String[1];
        s[0] = robotScore + " " + enemyScore + "\n\n";
        GenesWriter.main(s);
    }

    public void onBattleMessage(BattleMessageEvent e) {
        System.out.println("Msg> " + e.getMessage());
    }

    public void onBattleError(BattleErrorEvent e) {
        System.err.println("Err> " + e.getError());
    }
}