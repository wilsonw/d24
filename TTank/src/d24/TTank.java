/**
 *
 */
package d24;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * Movement: arc movement
 * Radar: 360
 * Gun: melee: predictive, one v one: Guess Factor
 * 
 * @author Trung Nguyen F413852
 * @author Kavita Masur D412874
 * @author Wilson Wu
 * @author Derrick
 *
 */
public class TTank extends AdvancedRobot {

	private Map<String, EnemyTank> enemies = new ConcurrentHashMap<>();

	private static final double BULLET_POWER = 1.9;
	
	private static double lateralDirection;
	private static double lastEnemyVelocity;
	private static GFTMovement movement;
	
	
	private static final int STRATEGY_MELEE = 1;
	private static final int STRATEGY_ONE_ONE = 2;

	private static final double wallMargin = 40;

	private int moveDirection = 1;

	private int applyStrategy = STRATEGY_MELEE;
	
	public TTank() {
	}

	@Override
	public void run() {
		movement = new GFTMovement(this);
		
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true) {
			applyStrategy = getOthers() == 1 ? STRATEGY_ONE_ONE : STRATEGY_MELEE;
			if (nearWall() && STRATEGY_MELEE == applyStrategy) {
				double normalizeBearing = BotUtil.normalizeBearing(getHeading());
//				out.println("[" + getX() + ", " + getY() + "] change due to near wall " + getHeading() + ", " + normalizeBearing + ", " + moveDirection);
				if (getX() <= wallMargin) {
					if (normalizeBearing >= -180 && normalizeBearing <= 0) {
//						out.println("back");
						setBack(1000);
					} else {
//						out.println("ahead");
						setAhead(1000);
					}
				} else
				if (getX() >= getBattleFieldWidth() - wallMargin) {
					if (normalizeBearing >= 0 && normalizeBearing <= 180) {
//						out.println("back");
						setBack(1000);
					} else {
//						out.println("ahead");
						setAhead(1000);
					}
				} else
				if (getY() <= wallMargin) {
					if (normalizeBearing <= -90 || normalizeBearing >= 90) {
//						out.println("back");
						setBack(1000);
					} else {
//						out.println("ahead");
						setAhead(1000);
					}
				} else
				if (getY() >= getBattleFieldHeight() - wallMargin) {
					if (normalizeBearing >= -90 && normalizeBearing <= 90) {
//						out.println("back");
						setBack(1000);
					} else {
//						out.println("ahead");
						setAhead(1000);
					}
				} else {
					out.println("not handled!!!");
				}
				moveDirection *= -1;
			}
			if (applyStrategy == STRATEGY_MELEE) {
				setTurnRadarLeft(360);
			} else {
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
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
//		out.println(event.getName());
		if (enemies.containsKey(event.getName())) {
			enemies.get(event.getName()).update(event, this);
		} else {
			enemies.put(event.getName(), new EnemyTank(event));
		}
		switch (applyStrategy) {
			case STRATEGY_ONE_ONE:
				doOneOne(event);
				break;
			case STRATEGY_MELEE:
				doMelee();
				break;
			default: // should not happen
		}
	}

	private void doOneOne(ScannedRobotEvent e) {
		double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyDistance = e.getDistance();
		double enemyVelocity = e.getVelocity();
		if (enemyVelocity != 0) {
			lateralDirection = GFTUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
		}
		GFTWave wave = new GFTWave(this);
		wave.gunLocation = new Point2D.Double(getX(), getY());
		GFTWave.targetLocation = GFTUtils.project(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
		wave.lateralDirection = lateralDirection;
		wave.bulletPower = BULLET_POWER;
		wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
		lastEnemyVelocity = enemyVelocity;
		wave.bearing = enemyAbsoluteBearing;
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
		setFire(wave.bulletPower);
		if (getEnergy() >= BULLET_POWER) {
			addCustomEvent(wave);
		}
		movement.onScannedRobot(e);
		setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) * 2);		
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

		if ((applyStrategy == STRATEGY_MELEE && currentTick % 20 == 0) || target.hasFired()) {
//			out.println("Change. " + target.hasFired());
			moveDirection *= -1;
			setMaxVelocity(8);
		}
		double absAngle = target.getBearing() + 90 - (15 * moveDirection);
		setTurnRight(BotUtil.normalizeBearing(absAngle));
		setAhead(1000 * moveDirection);
		predictiveFire(target);
	}
	
	private EnemyTank findTargetEnemy() {
		EnemyTank target = null;
		for (EnemyTank et : enemies.values()) {
			if ((target == null || et.getDistance() < target.getDistance()) && !et.getName().startsWith("d24.")) {
				target = et;
			}
		}
		if (enemies.values().size() == 1) {
			target = enemies.values().iterator().next();
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
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		if (applyStrategy == STRATEGY_ONE_ONE) {
			moveDirection *= -1;
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
//		moveDirection *= -1;
	}

	public void onWin(robocode.WinEvent event) {
		setMaxVelocity(0);
	}
}

class GFTWave extends Condition {
	static Point2D targetLocation;

	double bulletPower;
	Point2D gunLocation;
	double bearing;
	double lateralDirection;

	private static final double MAX_DISTANCE = 900;
	private static final int DISTANCE_INDEXES = 5;
	private static final int VELOCITY_INDEXES = 5;
	private static final int BINS = 25;
	private static final int MIDDLE_BIN = (BINS - 1) / 2;
	private static final double MAX_ESCAPE_ANGLE = 0.7;
	private static final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;
	
	private static int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];

	private int[] buffer;
	private AdvancedRobot robot;
	private double distanceTraveled;
	
	GFTWave(AdvancedRobot _robot) {
		this.robot = _robot;
	}
	
	public boolean test() {
		advance();
		if (hasArrived()) {
			buffer[currentBin()]++;
			robot.removeCustomEvent(this);
		}
		return false;
	}

	double mostVisitedBearingOffset() {
		return (lateralDirection * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
	}
	
	void setSegmentations(double distance, double velocity, double lastVelocity) {
		int distanceIndex = (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES));
		int velocityIndex = (int)Math.abs(velocity / 2);
		int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
		buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
	}

	private void advance() {
		distanceTraveled += GFTUtils.bulletVelocity(bulletPower);
	}

	private boolean hasArrived() {
		return distanceTraveled > gunLocation.distance(targetLocation) - 18;
	}
	
	private int currentBin() {
		int bin = (int)Math.round(((Utils.normalRelativeAngle(GFTUtils.absoluteBearing(gunLocation, targetLocation) - bearing)) /
				(lateralDirection * BIN_WIDTH)) + MIDDLE_BIN);
		return GFTUtils.minMax(bin, 0, BINS - 1);
	}
	
	private int mostVisitedBin() {
		int mostVisited = MIDDLE_BIN;
		for (int i = 0; i < BINS; i++) {
			if (buffer[i] > buffer[mostVisited]) {
				mostVisited = i;
			}
		}
		return mostVisited;
	}	
}

class GFTUtils {
	static double bulletVelocity(double power) {
		return 20 - 3 * power;
	}
	
	static Point2D project(Point2D sourceLocation, double angle, double length) {
		return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
				sourceLocation.getY() + Math.cos(angle) * length);
	}
	
	static double absoluteBearing(Point2D source, Point2D target) {
		return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
	}

	static int sign(double v) {
		return v < 0 ? -1 : 1;
	}
	
	static int minMax(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}

class GFTMovement {
	private double BATTLE_FIELD_WIDTH = 800;
	private double BATTLE_FIELD_HEIGHT = 600;
	private static final double WALL_MARGIN = 18;
	private static final double MAX_TRIES = 125;
	private static final double REVERSE_TUNER = 0.421075;
	private static final double DEFAULT_EVASION = 1.2;
	private static final double WALL_BOUNCE_TUNER = 0.699484;

	private AdvancedRobot robot;
	private Rectangle2D fieldRectangle;
	private double enemyFirePower = 3;
	private double direction = 0.4;

	GFTMovement(AdvancedRobot _robot) {
		this.robot = _robot;
		this.BATTLE_FIELD_HEIGHT = _robot.getBattleFieldHeight();
		this.BATTLE_FIELD_WIDTH = _robot.getBattleFieldWidth();
		fieldRectangle = new Rectangle2D.Double(WALL_MARGIN, WALL_MARGIN,
				BATTLE_FIELD_WIDTH - WALL_MARGIN * 2, BATTLE_FIELD_HEIGHT - WALL_MARGIN * 2);
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		enemyFirePower = Math.min(500 / e.getDistance(), 3);
		double enemyAbsoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
		double enemyDistance = e.getDistance();
		Point2D robotLocation = new Point2D.Double(robot.getX(), robot.getY());
		Point2D enemyLocation = GFTUtils.project(robotLocation, enemyAbsoluteBearing, enemyDistance);
		Point2D robotDestination;
		double tries = 0;
		while (!fieldRectangle.contains(robotDestination = GFTUtils.project(enemyLocation, enemyAbsoluteBearing + Math.PI + direction,
				enemyDistance * (DEFAULT_EVASION - tries / 100.0))) && tries < MAX_TRIES) {
			tries++;
		}
		if ((Math.random() < (GFTUtils.bulletVelocity(enemyFirePower) / REVERSE_TUNER) / enemyDistance ||
				tries > (enemyDistance / GFTUtils.bulletVelocity(enemyFirePower) / WALL_BOUNCE_TUNER))) {
			direction = -direction;
		}
		// Jamougha's cool way
		double angle = GFTUtils.absoluteBearing(robotLocation, robotDestination) - robot.getHeadingRadians();
		robot.setAhead(Math.cos(angle) * 100);
		robot.setTurnRightRadians(Math.tan(angle));
	}
}