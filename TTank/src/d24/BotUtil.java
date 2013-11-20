package d24;

import java.awt.geom.Point2D;


public class BotUtil {

	public static double BOT_SIZE = 40;
	
	private static double FLD_WIDTH = 100;
	
	private static double distSegWt = 2.5;
	
	public static int MAX_ENERGY_WT = 20;

	public static void setWidth(double botWidth, double battleFieldWidth) {
		BOT_SIZE = botWidth;
		FLD_WIDTH = battleFieldWidth;
		distSegWt = FLD_WIDTH / BOT_SIZE;
	}
	
	public static double getEneryWeight(EnemyBot bot) {
		if (bot.getEnergy() >= 200) {
			return MAX_ENERGY_WT;
		}
		return MAX_ENERGY_WT - (bot.getEnergy() / 10);
	}
	
	public static double getDistanceWeight(EnemyBot bot) {
		if (bot.getDistance() >= FLD_WIDTH) {
			return distSegWt;
		}
		return distSegWt - (bot.getDistance() / BOT_SIZE);
	}
	
	public static double getWeight(EnemyBot bot) {
		return getEneryWeight(bot) + getDistanceWeight(bot);
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
	
	public static double getTurnLeftAngle(Point2D myPos, Point2D futurePos, double heading) {
		double angle = getAngleOfLineBetweenTwoPoints(myPos, futurePos);
		return heading - angle;
	}
	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double absoluteBearing(double x1, double y1, double x2, double y2) {
        double angle = getAngleOfLineBetweenTwoPoints(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2));       
        if (angle > 0 && angle < 90) {
			return 90 - angle;
		}
		if (angle > 90) {
			return 360 - (angle - 90);
		}
		if (angle < 0) {
			return 90 + angle * -1;
		}
		return angle;
    }

	
    /**
     * normalizes a bearing to between +180 and -180
     * @param angle
     * @return
     */
    public static double normalizeBearing(double angle) {
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
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
	
	public static Point2D pointOnEllipse(double width, double height, Point2D centre, Point2D fromPos) {
		double angleInDegrees = getAngleOfLineBetweenTwoPoints(centre, fromPos);
        double ePX = centre.getX() + (int) (width  * Math.cos(Math.toRadians(angleInDegrees)));
        double ePY = centre.getY() + (int) (height * Math.sin(Math.toRadians(angleInDegrees)));
        return new Point2D.Double(ePX, ePY);
	}

}
