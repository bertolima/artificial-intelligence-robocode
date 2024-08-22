package edo;

import robocode.BattleResults;
import robocode.control.events.*;

class battleObserver extends BattleAdaptor {

    public void onBattleMessage(BattleMessageEvent e) {
        System.out.println("Msg> " + e.getMessage());
    }

    public void onBattleError(BattleErrorEvent e) {
        System.err.println("Err> " + e.getError());
    }
}