/**
 *
 */
package d24;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * @author Trung Nguyen F413852
 * @author Kavita Masur D412874
 * @author Wilson Wu
 * @author Derrick
 *
 */
public class TTank extends AdvancedRobot {

	private Map<String, EnemyTank> enemies = new ConcurrentHashMap<>();

	private static final int STRATEGY_MELEE = 1;
	private static final int STRATEGY_ONE_ONE = 2;

	private static final double wallMargin = 40;

	private int moveDirection = 1;

	private int applyStrategy = STRATEGY_MELEE;

	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true) {
			applyStrategy = getOthers() == 1 ? STRATEGY_ONE_ONE : STRATEGY_MELEE;
			if (nearWall()) {
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
			if (target == null || et.getDistance() < target.getDistance()) {
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
