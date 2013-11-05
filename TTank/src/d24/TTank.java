/**
 * 
 */
package d24;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

/**
 * 1. Use advancedrobot for unblocking calls
 * 2. Avoid wall contact during the move
 * 
 * @author trung
 *
 */
public class TTank extends AdvancedRobot {

	@Override
	public void run() {
		while (true) {
			// turn perpendicular
			if (getHeading() <= 90) {
				turnLeft(getHeading());
			} else if (getHeading() <= 180) {
				turnRight(180 - getHeading());
			} else if (getHeading() <= 270) {
				turnLeft(getHeading() - 180);
			} else {
				turnRight(360 - getHeading());
			}
			
			toNearestWall();
			
//			turnGunRight(90);
			
			execute();
		}
	}
	
	private void toNearestWall() {
		double fieldWidth = getBattleFieldWidth();
		double fieldHeight = getBattleFieldHeight();
		double currentX = getX();
		double currentY = getY();
		double topGap = fieldHeight - currentY;
		double bottomGap = currentY;
		double leftGap = currentX;
		double rightGap = fieldWidth - currentX;
		
		if (topGap <= bottomGap && topGap <= Math.min(leftGap, rightGap)) {
			double a = (int) getHeading() > 0 ? -1 : 1;
			ahead(adjustAhead(topGap, 100) * a);
		}
		
		if (bottomGap <= topGap && bottomGap <= Math.min(leftGap, rightGap)) {
			double a = (int) getHeading() > 0 ? 1 : -1;
			ahead(adjustAhead(bottomGap, 100) * a);
		}
		
		if (leftGap <= rightGap && leftGap <= Math.min(topGap, bottomGap)) {
			if ((int) getHeading() > 0) {
				turnRight(90);
			} else {
				turnLeft(90);
			}
			ahead(adjustAhead(leftGap, 100));
		}
		
		if (rightGap <= leftGap && rightGap <= Math.min(topGap, bottomGap)) {
			if ((int) getHeading() > 0) {
				turnLeft(90);
			} else {
				turnRight(90);
			}
			ahead(adjustAhead(rightGap, 100));
		}
	}
	
	/**
	 * This adjusts the travel distance before it hits the wall
	 * 
	 * @param maxDistance
	 * @param i 
	 */
	private double adjustAhead(double gap, double maxDistance) {
		return Math.min(Math.max(gap - 18, 0), maxDistance);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
//		fire(1);
	}
	
	@Override
	public void onHitWall(HitWallEvent event) {
		
	}
}
