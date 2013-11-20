package d24;

import java.awt.geom.Point2D;

import org.junit.Test;

public class SomeTest {
	@Test
	public void testAngle() {
		Point2D p1 = new Point2D.Double(0, 0);
		Point2D p2 = new Point2D.Double(50, 70);
		double angle = BotUtil.getAngleOfLineBetweenTwoPoints(p1, p2);
		double angle2 = BotUtil.absoluteBearing(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		System.out.println(angle2);
		System.out.println(angle);
		System.out.println(normalizeBearing(angle));
		System.out.println("");
		
		p2 = new Point2D.Double(70, -70);
		angle = BotUtil.getAngleOfLineBetweenTwoPoints(p1, p2);
		angle2 = BotUtil.absoluteBearing(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		System.out.println(angle2);
		System.out.println(angle);
		System.out.println(normalizeBearing(angle));
		System.out.println("");
		
		p2 = new Point2D.Double(-70, -70);
		angle = BotUtil.getAngleOfLineBetweenTwoPoints(p1, p2);
		angle2 = BotUtil.absoluteBearing(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		System.out.println(angle2);
		System.out.println(angle);
		System.out.println(normalizeBearing(angle));
		System.out.println("");
		
		p2 = new Point2D.Double(-70, 0);
		angle = BotUtil.getAngleOfLineBetweenTwoPoints(p1, p2);
		angle2 = BotUtil.absoluteBearing(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		System.out.println(angle2);
		System.out.println(angle);
		System.out.println(normalizeBearing(angle));
		System.out.println("");
		
		p2 = new Point2D.Double(-50, 70);
		angle = BotUtil.getAngleOfLineBetweenTwoPoints(p1, p2);
		angle2 = BotUtil.absoluteBearing(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		System.out.println(angle2);
		System.out.println(angle);
		System.out.println(normalizeBearing(angle));
		System.out.println("");
	}
	
	private double normalizeBearing(double angle) {
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
}
