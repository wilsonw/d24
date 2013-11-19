/**
 *
 */
package d24;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

/**
 * @author F413852
 *
 */
public class TTank extends AdvancedRobot {

	private Map<String, EnemyTank> enemies = new ConcurrentHashMap<>();

	private static final int STRATEGY_MELEE = 1;
	private static final int STRATEGY_ONE_ONE = 2;

	private static final int CLOCK_WISE = 1;
	private static final int ANTI_CLOCK_WISE = -1;


	private int moveDirection = CLOCK_WISE;

	private static final long MAX_DIFF_LAST_UPDATE_IN_SECS = 1; // if bot can't be detected within 1 second, assume death

	private int applyStrategy = STRATEGY_MELEE;

	private double a = 0;
	private double b = 0;

	@Override
	public void run() {
		a = getBattleFieldWidth() - 40; // some space for wall and tank size
		b = getBattleFieldHeight() - 40;
		while (true) {
			applyStrategy = getOthers() == 1 ? STRATEGY_ONE_ONE : STRATEGY_MELEE;
			turnRadarLeft(360);
//			setTurnL
			moveInEllipse();
			execute();
		}
	}

	/**
	 * -a^2*b^2 + y^2*a^2 + x^2*b^2 = 0;
	 */
	private void moveInEllipse() {

	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		if (enemies.containsKey(event.getName())) {
			enemies.get(event.getName()).update(event);
		} else {
			enemies.put(event.getName(), new EnemyTank(event));
		}
		// clean up if enemy is death
		cleanUpEnemy();
		switch (applyStrategy) {
			case STRATEGY_ONE_ONE:
//				break;
			case STRATEGY_MELEE:
				doMelee();
				break;
			default: // should not happen
		}
	}

	private void cleanUpEnemy() {
		List<String> deletedList = new ArrayList<>();
		for (EnemyTank et : enemies.values()) {
			if (et.diffLastUpdateInSecs() >= MAX_DIFF_LAST_UPDATE_IN_SECS) {
				deletedList.add(et.getName());
			}
		}
		for (String n : deletedList) {
			enemies.remove(n);
		}
	}

	private void doMelee() {
		EnemyTank nearest = new EnemyTank();
		// find the nearest bot
		for (EnemyTank et : enemies.values()) {
			if (et.getDistance() < nearest.getDistance()) {
				nearest = et;
			}
		}
		// fire at it
		setTurnRight(nearest.getBearing());
		ahead(nearest.getDistance() + 5);
		predictiveFire(nearest);
	}

	private void predictiveFire(EnemyTank et) {
		if (et.getDistance() < 100)
	    {
	        fire(3);
	    }
	    else
	    {
	        fire(1);
	    }
	}
	
	/** 
	 * Determines the angle of a straight line drawn between point one and two. 
	 * The number returned, which is a double in degrees, tells us how much we have to rotate a horizontal line clockwise for it to match the line between the two points. 
	 * If you prefer to deal with angles using radians instead of degrees, just change the last line to: "return Math.atan2(yDiff, xDiff);"
	 * 
	 * p1 is the centre of the battle field and p2 is the own bot location
	 **/ 

	public static double GetAngleOfLineBetweenTwoPoints(Point2D.Double p1, Point2D.Double p2) { 
		double xDiff = p2.x - p1.x; 
		double yDiff = p2.y - p1.y; 
		return Math.toDegrees(Math.atan2(yDiff, xDiff)); 
	}


	/*
	 * Given ellipse hight, width, angle and p1 is the centre of the battle field
	 * 
	 * returns the nearest point on the circumference of the ellipse
	 * 
	 */
	public static Point2D PointOnEllipse(float width, float height, float angleInDegrees, Point2D p1) {
	        double ePX = p1.getX() + (int) (width  * Math.cos(Math.toRadians(angleInDegrees)));
	        double ePY = p1.getY() + (int) (height * Math.sin(Math.toRadians(angleInDegrees)));
	        return new Point2D.Double(ePX, ePY);
	}
}
