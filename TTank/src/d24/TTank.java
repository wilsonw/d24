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
import robocode.Condition;
import robocode.CustomEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
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

	private static final int CLOCK_WISE = 1;
	private static final int ANTI_CLOCK_WISE = -1;

	private int tooCloseToWall = 0;
	
	private int moveDirection = CLOCK_WISE;

	private static final long MAX_DIFF_LAST_UPDATE_IN_MILLIS = 300; // timeout assume death for enemy

	private int applyStrategy = STRATEGY_MELEE;
	
	private double wallMargin = 60;

	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		double vertexX = getBattleFieldWidth() / 2;
		double vertexY = getBattleFieldHeight() / 2;
		//EllipseEquation.initialize(getBattleFieldWidth() / 2 - 40, getBattleFieldHeight() / 2 - 40, getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);
		double deltaX = Math.abs(vertexX - getX());
		double deltaY = Math.abs(vertexY - getY());
		double distanceToCenter = Math.sqrt(sqr(deltaX) + sqr(deltaY));
		double a = distanceToCenter;
		double b = a; // circle
		EllipseEquation.initialize(a, b, vertexX, vertexY);
		
//		addCustomEvent(new Condition("too_close_to_walls") {
//            public boolean test() {
//                return (
//                    // we're too close to the left wall
//                    (getX() <= wallMargin ||
//                     // or we're too close to the right wall
//                     getX() >= getBattleFieldWidth() - wallMargin ||
//                     // or we're too close to the bottom wall
//                     getY() <= wallMargin ||
//                     // or we're too close to the top wall
//                     getY() >= getBattleFieldHeight() - wallMargin)
//                    );
//                }
//            });
		
		while (true) {
			applyStrategy = getOthers() == 1 ? STRATEGY_ONE_ONE : STRATEGY_MELEE;
			setTurnRadarLeft(360);
//			setTurnL
//			moveInEllipse();
			execute();
		}
	}

	/**
	 * -a^2*b^2 + y^2*a^2 + x^2*b^2 = 0;
	 */
	private void moveInEllipse() {
		// if we're in the orbit path, then just move based on the moveDirection, otherwise need to position our self
		boolean isInside = EllipseEquation.getInstance().inside(getX(), getY());
		out.println("x=" + getX() + " - y=" + getY() + " - inside=" + isInside);
		double futureX = getX() + moveDirection * (EllipseEquation.getInstance().isBelowY(getY()) ? -1 : 1) * 37; // follow the moveDirection
		if (futureX < EllipseEquation.getInstance().leftMostX()) {
			futureX = EllipseEquation.getInstance().leftMostX();
		}
		if (futureX > EllipseEquation.getInstance().rightMostX()) {
			futureX = EllipseEquation.getInstance().rightMostX();
		}
		double futureY = EllipseEquation.getInstance().getY(futureX);
		double distance = Point2D.distance(getX(), getY(), futureX, futureY);
		if (EllipseEquation.getInstance().inPath(getX(), getY())) {
			out.println("I'm in the orbit path now!!");
			// TODO how to move within the orbit
			
		} else {
			double x1 = EllipseEquation.getInstance().getX(getY());
			double y1 = EllipseEquation.getInstance().getY(getX());
			if (!Double.isNaN(y1) && !Double.isNaN(x1)) {
				double a = Math.abs(getX() - x1);
				double b = Math.abs(getY() - y1);
				double c = Point2D.distance(x1, getY(), getX(), y1);
				distance = a*b/c;
				double deltaX = distance * Math.sqrt(sqr(a) - sqr(distance)) / a;
				double deltaY = distance * Math.sqrt(sqr(b) - sqr(distance)) / b;
				futureX = getX() + (isInside ? 1 : -1) * (x1 < getX() ? -1 : 1) * deltaX;
				futureY = getY() + (isInside ? 1 : -1) * (y1 < getY() ? -1 : 1) * deltaY;
				out.println(new StringBuffer()
							.append("x1=").append(x1).append("\n")
							.append("y1=").append(y1).append("\n")
							.append("a=").append(a).append("\n")
							.append("b=").append(b).append("\n")
							.append("c=").append(c)
						);
			}
		}
		out.println("Future(x,y): " + futureX + " - " + futureY + ", distance=" + distance + "\n");
		
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		
		setTurnRight(normalizeBearing(absDeg - getHeading()));
		setAhead(distance);
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
		// if we're close to the wall, eventually, we'll move away
        if (tooCloseToWall > 0) { tooCloseToWall--; }
        
		if (getVelocity() == 0 || getTime() % 20 == 0) {
			moveDirection *= -1;
			setMaxVelocity(8);
		}
		if ((getX() <= wallMargin ||
              getX() >= getBattleFieldWidth() - wallMargin ||
              getY() <= wallMargin ||
              getY() >= getBattleFieldHeight() - wallMargin)
             ) {
			moveDirection *= -1;
		}
		setTurnRight(normalizeBearing(nearest.getBearing() + 90 - (15 * moveDirection)));
		setAhead(1000 * moveDirection);		
		predictiveFire(nearest);
	}
	
	private double adjustHeadingForWall(double x) {
		
		return x;
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
	
	/** 
	 * Determines the angle of a straight line drawn between point one and two. 
	 * The number returned, which is a double in degrees, tells us how much we have to rotate a horizontal line clockwise for it to match the line between the two points. 
	 * If you prefer to deal with angles using radians instead of degrees, just change the last line to: "return Math.atan2(yDiff, xDiff);"
	 * 
	 * p1 is the centre of the battle field and p2 is the own bot location
	 **/ 

	public static double getAngleOfLineBetweenTwoPoints(Point2D p1, Point2D p2) { 
		double xDiff = p2.getX() - p1.getX(); 
		double yDiff = p2.getY() - p1.getY(); 
		return Math.toDegrees(Math.atan2(yDiff, xDiff)); 
	}


	/*
	 * Given ellipse hight, width, angle and p1 is the centre of the battle field
	 * 
	 * returns the nearest point on the circumference of the ellipse
	 * 
	 */
	public static Point2D pointOnEllipse(double width, double height, double angleInDegrees, Point2D p1) {
	        double ePX = p1.getX() + (int) (width  * Math.cos(Math.toRadians(angleInDegrees)));
	        double ePY = p1.getY() + (int) (height * Math.sin(Math.toRadians(angleInDegrees)));
	        return new Point2D.Double(ePX, ePY);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent event) {
		moveDirection *= -1;
	}
	
	@Override
	public void onHitWall(HitWallEvent event) {
		moveDirection *= -1;
	}
	
	public void onCustomEvent(CustomEvent e) {
        if (e.getCondition().getName().equals("too_close_to_walls"))
        {
            if (tooCloseToWall <= 0) {
                // if we weren't already dealing with the walls, we are now
                tooCloseToWall += wallMargin;
                setMaxVelocity(0); // stop!!!
            }
        }
    }
	
	public Point2D.Double fastWallSmooth(Point2D.Double orbitCenter, Point2D.Double position, double direction, double distanceToOrbitCenter){
		final double MARGIN = 18;
		final double STICK_LENGTH = 150;
	 
		double fieldWidth = getBattleFieldWidth(), fieldHeight = getBattleFieldHeight();
	 
		double stick = Math.min(STICK_LENGTH, distanceToOrbitCenter);
		double stickSquared = sqr(stick);
	 
		int LEFT = -1, RIGHT = 1, TOP = 1, BOTTOM = -1;
	 
		int topOrBottomWall = 0;
		int leftOrRightWall = 0;
	 
		double desiredAngle = Utils.normalAbsoluteAngle(absoluteAngle(position, orbitCenter) - direction * Math.PI / 2.0);
		Point2D.Double projected = projectPoint(position, desiredAngle, stick);
		if(projected.x >= 18 && projected.x <= fieldWidth - 18 && projected.y >= 18 && projected.y <= fieldHeight - 18)
			return projected;
	 
		if(projected.x  > fieldWidth - MARGIN || position.x  > fieldWidth - stick - MARGIN) leftOrRightWall = RIGHT;
		else if (projected.x < MARGIN || position.x < stick + MARGIN) leftOrRightWall = LEFT;
	 
		if(projected.y > fieldHeight - MARGIN || position.y > fieldHeight - stick - MARGIN) topOrBottomWall = TOP;
		else if (projected.y < MARGIN || position.y < stick + MARGIN) topOrBottomWall = BOTTOM;
	 
		if(topOrBottomWall == TOP){
			if(leftOrRightWall == LEFT){
				if(direction > 0)
					//smooth against top wall
					return new Point2D.Double(position.x + direction * Math.sqrt(stickSquared - sqr(fieldHeight - MARGIN - position.y)), fieldHeight - MARGIN);
				else
					//smooth against left wall
					return new Point2D.Double(MARGIN, position.y + direction * Math.sqrt(stickSquared - sqr(position.x - MARGIN)));
	 
			} else if(leftOrRightWall == RIGHT){
				if(direction > 0)
					//smooth against right wall
					return new Point2D.Double(fieldWidth - MARGIN, position.y - direction * Math.sqrt(stickSquared - sqr(fieldWidth - MARGIN - position.x)));
				else 
					//smooth against top wall
					return new Point2D.Double(position.x + direction * Math.sqrt(stickSquared - sqr(fieldHeight - MARGIN - position.y)), fieldHeight - MARGIN);
	 
			}
			//Smooth against top wall
			return new Point2D.Double(position.x + direction * Math.sqrt(stickSquared - sqr(fieldHeight - MARGIN - position.y)), fieldHeight - MARGIN); 
		} else if(topOrBottomWall == BOTTOM){
			if(leftOrRightWall == LEFT){
				if(direction > 0)
					//smooth against left wall
					return new Point2D.Double(MARGIN, position.y + direction * Math.sqrt(stickSquared - sqr(position.x - MARGIN)));
				else
					//smooth against bottom wall
					return new Point2D.Double(position.x - direction * Math.sqrt(stickSquared - sqr(position.y - MARGIN)), MARGIN);
			} else if(leftOrRightWall == RIGHT){
				if(direction > 0)
					//smooth against bottom wall
					return new Point2D.Double(position.x - direction * Math.sqrt(stickSquared - sqr(position.y - MARGIN)), MARGIN);
				else
					//smooth against right wall
					return new Point2D.Double(fieldWidth - MARGIN, position.y - direction * Math.sqrt(stickSquared - sqr(fieldWidth - MARGIN - position.x)));
	 
			}
			//Smooth against bottom wall
			return new Point2D.Double(position.x - direction * Math.sqrt(stickSquared - sqr(position.y - MARGIN)), MARGIN);
		}
	 
		if(leftOrRightWall == LEFT){
			//smooth against left wall
			return new Point2D.Double(MARGIN, position.y + direction * Math.sqrt(stickSquared - sqr(position.x - MARGIN)));
		} else if(leftOrRightWall == RIGHT){
			//smooth against right wall
			return new Point2D.Double(fieldWidth - MARGIN, position.y - direction * Math.sqrt(stickSquared - sqr(fieldWidth - MARGIN - position.x)));
		}
	 
		throw new RuntimeException("This code should be unreachable. position = " + position.x + ", " + position.y + "  orbitCenter = " + orbitCenter.x + ", " + orbitCenter.y + " direction = " + direction);
	}
	 
	public static Point2D.Double projectPoint(Point2D.Double origin, double angle, double distance){
		return new Point2D.Double(origin.x + distance * Math.sin(angle), origin.y + distance * Math.cos(angle));
	}
	
	public static double absoluteAngle(Point2D.Double origin, Point2D.Double target) {
	    return Math.atan2(target.x - origin.x, target.y - origin.y);
	}
	 
}
