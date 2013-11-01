/**
 * 
 */
package d24;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

/**
 * @author trung
 *
 */
public class TTank extends Robot {
	public void run() {
		while (true) {
			sirYesSir();
		}
	}
	
	private void sirYesSir() {
		ahead(100);
		turnRight(30);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		fire(1);
	}
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
		back(100);
	}
	
	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		super.onBulletHitBullet(event);
	}
}
