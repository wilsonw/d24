package d24;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;


public class AdvancedEnemyBot extends EnemyBot {
	
	public AdvancedEnemyBot() {
		reset();
	}

	private double x;
	private double y;
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public void reset() {
		super.reset();
		x = 0.0;
		y = 0.0;
	}
	
	public void update(ScannedRobotEvent e, AdvancedRobot robot) {
		update(e);
		double absBearingDeg = (robot.getHeading() + e.getBearing());
		if (absBearingDeg < 0) {
			absBearingDeg += 360;
		}
		
		// yes, you use the _sine_ to get the X value because 0 deg is North
		x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * e.getDistance();
		// yes, you use the _cosine_ to get the Y value because 0 deg is North
		y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * e.getDistance();
	}
	
	public double getFutureX(long when) {
		return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
	}
	
	public double getFutureY(long when) {
		return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
	}
}
