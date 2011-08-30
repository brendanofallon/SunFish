package plugins.SGPlugin.analyzer.haplotype.figure;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import network.NetworkEdge;
import network.NetworkNode;

import figure.Figure;
import figure.FigureElement;

/**
 * Represents a drawn edge between two haplotype elements
 * @author brendan
 *
 */
public class HapEdgeElement extends FigureElement implements NetworkEdge {

	HaplotypeElement aHap;
	HaplotypeElement bHap;
	
	int tickLength = 10; //in pixels
	
	boolean drawTicks = false;
	boolean showDistance = false;
	
	int numTicks = 0;
	
	Font font = new Font("Sans", Font.PLAIN, 10);
	
	public HapEdgeElement(Figure parent, HaplotypeElement a, HaplotypeElement b, int dist) {
		super(parent);
		this.aHap = a;
		this.bHap = b;
		aHap.addEdge(this);
		bHap.addEdge(this);
		this.numTicks = dist;
	}

	public void setDrawTicks(boolean drawEm) {
		drawTicks = drawEm;
	}
	
	public void setShowDistance(boolean showIt) {
		showDistance = showIt;
	}
	
	private void drawTick(Graphics g, int xc, int yc, double theta) {
		int x1d = round(tickLength*Math.sin(theta));
		int y1d = round(tickLength*Math.cos(theta));
		g.drawLine(xc, yc, xc+x1d, yc-y1d);
		g.drawLine(xc, yc, xc-x1d, yc+y1d);
	}
	
	public void paint(Graphics2D g) {
		g.setColor(foregroundColor);
		
		if (! ((HaplotypeNetworkFigure)parent).getRecentered()) {
			double width = parent.getWidth();
			double height = parent.getHeight();
			double min = Math.min(width, height);
			
			double xMiddle = 0.5*width/min;
			double yMiddle = 0.5*height/min;
			((HaplotypeNetworkFigure)parent).recenter(xMiddle, yMiddle);
		}
		
		
		double min = Math.min(xFactor, yFactor); //Must keep things square...
		double xa = (aHap.getCenterX()*min);
		double ya = (aHap.getCenterY()*min);
		double xb = (bHap.getCenterX()*min);
		double yb = (bHap.getCenterY()*min);
		g.drawLine(round(xa), round(ya), round(xb), round(yb));
		
		if (drawTicks) {
			double theta;
			if (xa!=xb)
				theta = Math.atan((ya-yb)/(xa-xb));
			else
				theta = -5156.62451; // = -90 * 180/Pi

			double totLength = Math.sqrt( (xa-xb)*(xa-xb)+(ya-yb)*(ya-yb));
			double sep = 5+round(totLength/50);

			if (sep * numTicks > 0.8*totLength) {
				sep = 0.8 * totLength/numTicks; 
			}

			double xDiff = sep*Math.cos(theta);
			double yDiff = sep*Math.sin(theta);

			for(double i=0; i<numTicks; i++) {
				int xc = round( (xa+xb)/2 + xDiff*(i-numTicks/2.0)   );
				int yc = round( (ya+yb)/2 + yDiff*(i-numTicks/2.0)   );
				drawTick(g, xc, yc, theta);	
			}

		}
		
	
		if (showDistance) {
			g.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			int height = fm.getHeight();
			String str = String.valueOf(numTicks);
			int width = fm.stringWidth(str);
			
			double theta;
			if (xa!=xb)
				theta = Math.atan((ya-yb)/(xa-xb));
			else
				theta = -3.1416; // = -90 * 180/Pi
			
			int xc = round( (xa+xb)/2 );
			int yc = round( (ya+yb)/2 );
			int xd = round((width+2)*(Math.sin(theta)+1)/2.0);
			int yd = round(height*Math.cos(theta));
			
			if ( (theta>(-1.57/Math.PI) && theta < (1.57/Math.PI))) {
				xd *= -1;
			}
			
			
			if (Math.abs(xa-xb)< 0.0001) {
				xd = 0;
				yd = 0;
			}

			//System.out.println("Theta : " + theta + " xd: " + xd);
			
			g.drawString(String.valueOf(numTicks), xc+xd, yc+yd);
		}
		
	}

	@Override
	public NetworkNode getNodeA() {
		return aHap;
	}

	@Override
	public NetworkNode getNodeB() {
		return bHap;
	}

	@Override
	public boolean isWeighted() {
		return true;
	}

	@Override
	/**
	 * Return the 'weight' of this edge, which is always equal to the number of sites at which this and the 
	 * other haplotype differ. 
	 */
	public double getWeight() {
		return (double)numTicks;
	}


}
