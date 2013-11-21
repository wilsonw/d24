package d24;

import java.awt.geom.Point2D;


public class BotUtil {

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


}
