package element;

public class DoubleRectangle {

	public double x;
	public double y;
	public double width;
	public double height;
	
	public DoubleRectangle() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
	}
	
	public DoubleRectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(element.Point p) {
		if (p.x < this.x || p.x > (this.x+this.width)) return false;
		if (p.y < this.y || p.y > (this.y+this.height)) return false;
		return true;
	}
	
	public boolean contains(double posX, double posY) {
		if (posX < this.x || posX > (this.x+this.width)) return false;
		if (posY < this.y || posY > (this.y+this.height)) return false;
		return true;
	}
	
	
}
