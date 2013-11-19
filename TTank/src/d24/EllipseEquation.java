package d24;

/**
 * f(x,y) =  y^2*a^2 + x^2*b^2 -a^2*b^2;
 * 
 * or 1 = x^2/a^2 + y^2/b^2;
 * 
 * x^2 = (1 - y^2/b^2)*a^2
 * 
 * @author trung
 *
 */
public class EllipseEquation {
	private static EllipseEquation singleton = null;
	
	private double a;
	private double b;
	private double vertexX;
	private double vertexY;
	
	private EllipseEquation(double a, double b, double vX, double vY) {
		System.out.println("-----");
		System.out.println("a=" + a);
		System.out.println("b=" + b);
		System.out.println("vX=" + vX);
		System.out.println("vY=" + vY);
		System.out.println("-----");
		this.a = a;
		this.b = b;
		this.vertexX = vX;
		this.vertexY = vY;
	}

	public static EllipseEquation initialize(double a, double b, double vertexX, double vertexY) {
		if (singleton == null) {
			singleton = new EllipseEquation(a, b, vertexX, vertexY);
		}
		return singleton;
	}
	
	public static EllipseEquation getInstance() {
		if (singleton == null) {
			throw new IllegalArgumentException("Have not initialize");
		}
		return singleton;
	}
	
	private double sqr(double v) {
		return v*v;
	}
	
	/**
	 * 
	 * @param y
	 * @return 
	 */
	public double getX(double y) {
		return Math.sqrt((1 - sqr(y - vertexY)/sqr(b))) * a + vertexX;
	}
	
	/**
	 * 
	 * @param x
	 * @return
	 */
	public double getY(double x) {
		return Math.sqrt((1 - sqr(x - vertexX)/sqr(a))) * b + vertexY;
	}

	public boolean inPath(double x, double y) {
		return Math.round(x) == Math.round(getX(y));
	}
}
