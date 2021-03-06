package figure.series;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import figure.FigureElement;
import guiWidgets.StringUtilities;


/**
 * A small element that paints the indicates the current data position of the mouse pointer if the pointer is over the the associated axes element
 * @author brendan
 *
 */
public class DataPosElement extends FigureElement {

	Point mousePos;
	AxesElement axes;
	double dataX = 0;
	double dataY = 0;
	boolean draw = false;
	
	
	
	Font font = new Font("Sans", Font.PLAIN, 10);
	
	public DataPosElement(AxesElement axes, figure.Figure parent) {
		super(parent);
		parent.addMouseListeningElement(this);
		this.axes = axes;
		bounds.x = axes.getBounds().getX()+0.05;
		bounds.y = Math.min(1.0, axes.getBounds().getY()+axes.getBounds().getHeight()+0.05);
		bounds.height = 0.05;
		bounds.width = 0.1;
	}

	/**
	 * This is called by figure when the mouse has moved. 
	 * @param pos
	 */
	protected void mouseMoved(Point2D pos) {
		//System.out.println("Got poition x: " + pos.x + " y: " + pos.y);
		if (axes.getBounds().contains(pos)) {
			dataX = axes.boundsXtoDataX(pos.getX());
			dataY = axes.boundsYtoDataY(pos.getY());
			if (dataX>= axes.minXVal && dataX <= axes.maxXVal && dataY >= axes.minYVal && dataY <= axes.maxYVal) {
				draw = true;
			}
			else {
				draw = false;
			}
		}
		else {
			draw = false;
		}
		 
	}
	

	
	public void paint(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.setFont(font);
		if (draw) {
			
			g.drawString(StringUtilities.format(dataX) + ", " + StringUtilities.format(dataY), round(bounds.x*xFactor), round(bounds.y*yFactor));
			//g.drawString("Bounds: " + formatter.format(pos.x) + " fig: " + formatter.format(axes. formatter.format(dataX) + ", " + formatter.format(dataY), round(bounds.x*xFactor), round(bounds.y*yFactor));
		}
	}

}
