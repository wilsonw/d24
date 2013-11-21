package d24;

import robocode.Robot;
import robocode.ScannedRobotEvent;

public class EnemyTank {

	private double bearing;

	private double distance;
	private double energy;
	private double heading;
	private String name;
	private double velocity;

	private double x;

	private double y;

	private boolean hasFired;

	private long lastUpdate;

	public EnemyTank() {
		update(new ScannedRobotEvent(null, -1, 0, Double.MAX_VALUE, 0, 0));
	}

	public EnemyTank(ScannedRobotEvent event) {
		update(event);
	}

	public void update(ScannedRobotEvent event) {
		this.hasFired = energy != -1 && event.getEnergy() != energy;
		this.bearing = event.getBearing();
		this.distance = event.getDistance();
		this.energy = event.getEnergy();
		this.heading = event.getHeading();
		this.name = event.getName();
		this.lastUpdate = System.currentTimeMillis();
		this.velocity = event.getVelocity();
	}

	public double getBearing() {
		return bearing;
	}

	public double getDistance() {
		return distance;
	}

	public double getEnergy() {
		return energy;
	}

	public double getHeading() {
		return heading;
	}

	public String getName() {
		return name;
	}

	public boolean hasFired() {
		return hasFired;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public long diffLastUpdateInMillis() {
		return System.currentTimeMillis() - lastUpdate;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getVelocity() {
		return velocity;
	}

	/**
	 * updating information.
	 *
	 * @param e
	 *            event.
	 * @param robot
	 *            my robot.
	 */
	public void update(ScannedRobotEvent e, Robot robot) {
		update(e);
		double absBearingDeg = (robot.getHeading() + e.getBearing());
		if (absBearingDeg < 0) {
			absBearingDeg += 360;
		}
		x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg))
				* e.getDistance();
		y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg))
				* e.getDistance();
	}

	/**
	 * predict the enemy's future x position.
	 *
	 * @return x the enemy future position.
	 *
	 * @param time
	 *            which calculate in Beater class.
	 */
	public double getFutureX(long time) {
		return x + Math.sin(Math.toRadians(getHeading())) * getVelocity()
				* time;
	}

	/**
	 * predict the enemy's future y position.
	 *
	 * @return y the enemy future position.
	 *
	 * @param time
	 *            which calculate in Beater class.
	 */

	public double getFutureY(long time) {
		return y + Math.cos(Math.toRadians(getHeading())) * getVelocity()
				* time;
	}
}
