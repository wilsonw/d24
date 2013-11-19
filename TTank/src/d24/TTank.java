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

	private static final long MAX_DIFF_LAST_UPDATE_IN_MILLIS = 300; // timeout assume death for enemy

	private int applyStrategy = STRATEGY_MELEE;

	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		EllipseEquation.initialize(getBattleFieldWidth() / 2 - 40, getBattleFieldHeight() / 2 - 40, getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);
		while (true) {
			applyStrategy = getOthers() == 1 ? STRATEGY_ONE_ONE : STRATEGY_MELEE;
			setTurnRadarLeft(360);
//			setTurnL
			moveInEllipse();
			execute();
		}
	}

	/**
	 * -a^2*b^2 + y^2*a^2 + x^2*b^2 = 0;
	 */
	private void moveInEllipse() {
		// if we're in the orbit path, then just move based on the moveDirection, otherwise need to position our self
		out.println("x=" + getX() + " - y=" + getY());
		if (EllipseEquation.getInstance().inPath(getX(), getY())) {
			out.println("I'm in the orbit path now!!");
			// TODO how to move within the orbit
		} else {
			double x1 = EllipseEquation.getInstance().getX(getY());
			double y1 = EllipseEquation.getInstance().getY(getX());
			double a = Math.abs(getX() - x1);
			double b = Math.abs(getY() - y1);
			double c = Point2D.distance(x1, getY(), getX(), y1);
			double distance = a*b/c;
			double deltaX = distance * Math.sqrt(sqr(a) - sqr(distance)) / a;
			double deltaY = distance * Math.sqrt(sqr(b) - sqr(distance)) / b;
			double futureX = getX() + (x1 < getX() ? -1 : 1) * deltaX;
			double futureY = getY() + (y1 < getY() ? -1 : 1) * deltaY;
			out.println(new StringBuffer()
						.append("x1=").append(x1).append("\n")
						.append("y1=").append(y1).append("\n")
						.append("c=").append(c).append("\n")
						.append("a=").append(a).append("\n")
						.append("b=").append(b).append("\n")
						.append("c=").append(c).append("\n")
					);
			out.println("Future(x,y): " + futureX + " - " + futureY + ", distance=" + distance);
			
			double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
			
			setTurnLeft(normalizeBearing(absDeg) - getHeading());
			setAhead(distance);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		if (enemies.containsKey(event.getName())) {
			enemies.get(event.getName()).update(event, this);
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
			if (et.diffLastUpdateInMillis() >= MAX_DIFF_LAST_UPDATE_IN_MILLIS) {
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
		// setTurnRight(nearest.getBearing());
		// ahead(nearest.getDistance() + 5);
		predictiveFire(nearest);
	}

	private void predictiveFire(EnemyTank enemy) {
		// calculate firepower based on distance
        double firePower = Math.min(500 / enemy.getDistance(), 3);
        // calculate speed of bullet
        double bulletSpeed = 20 - firePower * 3;
        // distance = rate * time, solved for time
        long time = (long)(enemy.getDistance() / bulletSpeed);
 
        // calculate gun turn to predicted x,y location
        double futureX = enemy.getFutureX(time);
        double futureY = enemy.getFutureY(time);
        double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
        // non-predictive firing can be done like this:
        //double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());
 
        // turn the gun to the predicted x,y location
        setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
 
        // if the gun is cool and we're pointed in the right direction, shoot!
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }
	}
	
	// computes the absolute bearing between two points
    private double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;
 
        if (xo > 0 && yo > 0) { // both pos: lower-Left
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actually 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }
 
        return bearing;
    }
 
    // normalizes a bearing to between +180 and -180
    private double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
    
    private double sqr(double v) {
		return v*v;
	}
}
