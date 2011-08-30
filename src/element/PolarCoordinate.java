package element;

public class PolarCoordinate {

	double magnitude;
	double angle;
	double x;
	double y;
	
	public PolarCoordinate() {
		magnitude = 0;
		angle = 0;
		x = 0;
		y = 0;
	}
	
	public PolarCoordinate(Point p) {
		x = p.x;
		y = p.y;
		angle = calcAngle();
		magnitude = Math.sqrt( x*x + y*y);
	}
	
	public void setX(double x) {
		this.x = x;
		angle = calcAngle();
		magnitude = Math.sqrt( x*x + y*y);		
	}
	
	public void setY(double y) {
		this.x = y;
		angle = calcAngle();
		magnitude = Math.sqrt( x*x + y*y);		
	}
	
	
	public void setAngle(double angle) {
		this.angle = angle;
		x = magnitude*Math.sin(angle);
		y = -1.0*magnitude*Math.cos(angle);
	}
	
	public void setMagnitude(double mag) {
		this.magnitude = mag;
		x = magnitude*Math.sin(angle);
		y = -1.0*magnitude*Math.cos(angle);
	}
	
	
	public double getMagnitude() {
		return magnitude;
	}
	
	public double getAngle() {
		return angle;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	private double calcAngle() {
		if (y==0) {
			if (x==0) return 0;
			if (x<0) return 270;
			if (x>0) return 90;
		}
		
		double val =  -57.2957795*Math.atan( x/y);
		
		if (Double.isNaN(val)) {
			System.out.println("Got NaN in AngleFromPoint, x: " + x + " y:" + y);
		}
		
		if (y<0)
			return 180+val;
		else
			return val;
	}

}
