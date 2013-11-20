/**
 *
 */
package d24;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * @author F413852
 *
 */
public class TTank extends AdvancedRobot {

	private Map<String, EnemyTank> enemies = new ConcurrentHashMap<>();

	private static final int STRATEGY_MELEE = 1;
	private static final int STRATEGY_ONE_ONE = 2;

	private static final double wallMargin = 40;

	private int moveDirection = 1;

	private int applyStrategy = STRATEGY_MELEE;

	private double _bfWidth;

	private double _bfHeight;
	
	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		_bfHeight = getBattleFieldHeight();
		_bfWidth = getBattleFieldWidth();
		while (true) {
			applyStrategy = getOthers() == 1 ? STRATEGY_ONE_ONE : STRATEGY_MELEE;
			if (nearWall()) {
				double normalizeBearing = BotUtil.normalizeBearing(getHeading());
				out.println("[" + getX() + ", " + getY() + "] change due to near wall " + getHeading() + ", " + normalizeBearing + ", " + moveDirection);
				if (getX() <= wallMargin) {
					if (normalizeBearing >= -180 && normalizeBearing <= 0) {
						out.println("back");
						setBack(1000);						
					} else {
						out.println("ahead");
						setAhead(1000);
					}
				} else 
				if (getX() >= getBattleFieldWidth() - wallMargin) {
					if (normalizeBearing >= 0 && normalizeBearing <= 180) {
						out.println("back");
						setBack(1000);						
					} else {
						out.println("ahead");
						setAhead(1000);
					}
				} else 
				if (getY() <= wallMargin) {
					if (normalizeBearing <= -90 || normalizeBearing >= 90) {
						out.println("back");
						setBack(1000);						
					} else {
						out.println("ahead");
						setAhead(1000);
					}
				} else
				if (getY() >= getBattleFieldHeight() - wallMargin) {
					if (normalizeBearing >= -90 && normalizeBearing <= 90) {
						out.println("back");
						setBack(1000);						
					} else {
						out.println("ahead");
						setAhead(1000);
					}
				} else {
					out.println("not handled!!!");
				}
				moveDirection *= -1;
			}
			setTurnRadarLeft(360);
			execute();
		}
	}

	private boolean nearWall() {
		return (getX() <= wallMargin ||
				 // or we're too close to the right wall
				 getX() >= getBattleFieldWidth() - wallMargin ||
				 // or we're too close to the bottom wall
				 getY() <= wallMargin ||
				 // or we're too close to the top wall
				 getY() >= getBattleFieldHeight() - wallMargin)
				;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		if (enemies.containsKey(event.getName())) {
			enemies.get(event.getName()).update(event, this);
		} else {
			enemies.put(event.getName(), new EnemyTank(event));
		}
		switch (applyStrategy) {
			case STRATEGY_ONE_ONE:
//				break;
			case STRATEGY_MELEE:
				doMelee();
				break;
			default: // should not happen
		}
	}
	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		if (enemies.containsKey(event.getName())) {
			enemies.remove(event.getName());
		}
	}

	private void doMelee() {
		EnemyTank target = findTargetEnemy();
		long currentTick = getTime();

		if (currentTick % 20 == 0) {
			out.println("Change due to tick");
			moveDirection *= -1;
			setMaxVelocity(8);
		}
		double absAngle = target.getBearing() + 90 - (15 * moveDirection);
//		out.println("absAngle BEFORE smoothing = " + absAngle + ", currentX = " + getX() + ", currentY = " + getY() + ", heading = " + getHeading());
		// need to apply some wall smoothing here
//		absAngle = wallSmoothing(new Point2D.Double(getX(), getY())
//						, Math.toRadians(absAngle)
//						, moveDirection
//						, target.getDistance());
//		double goAngle = Math.toDegrees(absAngle);
//		out.println("absAngle AFTER  smoothing = " + goAngle);
//		out.println("smoothedPoint = " + smoothedPoint + ", moveDirection = " + moveDirection + ", currentTick = " + currentTick);
		setTurnRight(BotUtil.normalizeBearing(absAngle));
		setAhead(1000 * moveDirection);
		predictiveFire(target);
	}
	
	private EnemyTank findTargetEnemy() {
		EnemyTank target = new EnemyTank();
		// find the nearest bot
		for (EnemyTank et : enemies.values()) {
			if (et.getDistance() < target.getDistance()) {
				target = et;
			}
		}
		return target;
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
        double absDeg = BotUtil.absoluteBearing(getX(), getY(), futureX, futureY);
        // non-predictive firing can be done like this:
        //double absDeg = absoluteBearing(getX(), getY(), enemy.getX(), enemy.getY());
 
        // turn the gun to the predicted x,y location
        setTurnGunRight(BotUtil.normalizeBearing(absDeg - getGunHeading()));
 
        // if the gun is cool and we're pointed in the right direction, shoot!
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(firePower);
        }
	}
	
	@Override
	public void onHitWall(HitWallEvent event) {
		out.println(" on hit wall----------------------");
//		moveDirection *= -1;
	}	
	
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		if (applyStrategy == STRATEGY_ONE_ONE) {
			moveDirection *= -1;
		}
	}
	
	private static final double SMOOTH_AWAY_DISTANCE = 75;
	private static final double WALL_STICK = 160;
	
	private double wallSmoothing(Point2D.Double startLocation,
		      double goAngleRadians, int direction, double currentDistance) {
	 
		int smoothingDirection = currentDistance < SMOOTH_AWAY_DISTANCE ? -1 : 1;
	    double smoothedAngle = doWallSmoothing(startLocation,
	        goAngleRadians,  direction * smoothingDirection,
	        WALL_STICK);
	    return smoothedAngle;
	}
	
	/**
	 * Do some Voodoo and wall smooth in a very efficient way. (In terms of CPU
	 * cycles, not amount of code.)
	 */
	private double doWallSmoothing(
			Point2D.Double startLocation, double startAngle, int orientation,
			double wallStick) {
		double angle = startAngle;
		double wallDistanceX = Math.min(startLocation.x - 18, _bfWidth
				- startLocation.x - 18);
		double wallDistanceY = Math.min(startLocation.y - 18,
				_bfHeight - startLocation.y - 18);

		if (wallDistanceX > wallStick && wallDistanceY > wallStick) {
			return startAngle;
		}

		double testX = startLocation.x + (Math.sin(angle) * wallStick);
		double testY = startLocation.y + (Math.cos(angle) * wallStick);
		double testDistanceX = Math.min(testX - 18, _bfWidth - testX
				- 18);
		double testDistanceY = Math.min(testY - 18, _bfHeight - testY
				- 18);

		double adjacent = 0;
		int g = 0; // shouldn't be needed, but infinite loop sanity check

		while ((testDistanceX < 0 || testDistanceY < 0) && g++ < 25) {
			while (angle < 0) {
				angle += (2 * Math.PI);
			}
			if (testDistanceY < 0 && testDistanceY < testDistanceX) {
				// wall smooth North or South wall
				angle = ((int) ((angle + (Math.PI / 2)) / Math.PI)) * Math.PI;
				adjacent = Math.abs(wallDistanceY);
			} else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
				// wall smooth East or West wall
				angle = (((int) (angle / Math.PI)) * Math.PI) + (Math.PI / 2);
				adjacent = Math.abs(wallDistanceX);
			}

			angle += orientation
					* (Math.abs(Math.acos(adjacent / wallStick)) + 0.0005);
			testX = startLocation.x + (Math.sin(angle) * wallStick);
			testY = startLocation.y + (Math.cos(angle) * wallStick);
			testDistanceX = Math
					.min(testX - 18, _bfWidth - testX - 18);
			testDistanceY = Math.min(testY - 18, _bfHeight - testY
					- 18);
		}

		return angle;
	}
	
	private void setBackAsFront(double goAngleInRadians) {
		double angle = Utils.normalRelativeAngle(goAngleInRadians);

		if (Math.abs(angle) > (Math.PI / 2)) {
			if (angle < 0) {
				setTurnRightRadians(Math.PI + angle);
			} else {
				setTurnLeftRadians(Math.PI - angle);
			}
			setBack(100);
		} else {
			if (angle < 0) {
				setTurnLeftRadians(-1 * angle);
			} else {
				setTurnRightRadians(angle);
			}
			setAhead(100);
		}
	}
	
	public void onWin(robocode.WinEvent event) {
		setMaxVelocity(0);
	};
}
