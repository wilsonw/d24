/**
 *
 */
package d24;

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
}
