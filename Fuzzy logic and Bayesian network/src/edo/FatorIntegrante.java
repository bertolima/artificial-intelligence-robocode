package edo;
import robocode.*;
import net.sourceforge.jFuzzyLogic.FIS;

public class FatorIntegrante extends AdvancedRobot{
	public void run() {
		while(true) {
			ahead(100);
			turnGunRight(90);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		fire(1);
	}
}
