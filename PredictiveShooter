package d24;

import robocode.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class PredictiveShooter extends AdvancedRobot {
	private AdvancedEnemyBot enemyOne = null;
	
	private Map<String, AdvancedEnemyBot> enemies = new HashMap <String, AdvancedEnemyBot>();

	public void run() {
		// divorce radar movement from gun movement
		setAdjustRadarForGunTurn(true);
		// divorce gun movement from tank movement
		setAdjustGunForRobotTurn(true);
		// we have no enemy yet
		enemies = new HashMap <String, AdvancedEnemyBot>();
		// initial scan
		setTurnRadarRight(360);
		
		BotUtil.setWidth(getWidth(), getBattleFieldWidth());
		
		while (true) {
			// rotate the radar
			if (enemyOne == null) { 
				setTurnRadarRight(360);
			} else {
				// lock to one enemy
				setTurnRadarRight(getHeading() - getRadarHeading() + enemyOne.getBearing());				
			}
			// sit & spin
			setTurnRight(5);
			setAhead(20);
			// doGun does predictive targeting
			doGun();
			// carry out all the queued up actions
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		AdvancedEnemyBot enemy = enemies.get(e.getName());
		
		if (enemyOne == null && getOthers() == 1) { 
			enemyOne = enemy;			
		}
						
		if (enemy == null) {
			enemy = new AdvancedEnemyBot();
			enemies.put(enemy.getName(), enemy);
		}		
		enemy.update(e, this);		
	}

	public void onRobotDeath(RobotDeathEvent e) {
		enemyOne = null;
		enemies.remove(e.getName());		
	}   

	void doGun() {
		AdvancedEnemyBot enemy = null;
		if(enemyOne == null) {
			enemy = getHeightWeightedEnemy();
		} else {
			enemy = enemyOne;
		}
		
		// don't shoot if I've got no enemy
		if (enemy == null) {
			return;
		}
		
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

	private AdvancedEnemyBot getHeightWeightedEnemy() {
		return Collections.max(enemies.values());
	}

	// computes the absolute bearing between two points
	double absoluteBearing(double x1, double y1, double x2, double y2) {
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
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
}
