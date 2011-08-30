package plugins.argPlugin.argFigure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import plugins.argPlugin.arg.ARG;

import figure.Figure;
import figure.FigureElement;
import guiWidgets.StringUtilities;

/**
 * Being of insufficient mettle to write a reusable axis element, ARGs have their own scale-axis element that works just like
 * a tree scale axis element
 * @author brendan
 *
 */
public class ARGAxisElement extends FigureElement {

	private int scaleAxisTicks = 6;
	private boolean useCustomDistance = false;
	private double tickDistance = -1;
	
	Stroke highlightStroke = new BasicStroke(2.5f);
	Stroke normalStroke = new BasicStroke(1.0f);
	
	Color normalColor = Color.black;
	
	boolean drawGridLines = false;
	
	Color gridColor = Color.LIGHT_GRAY;
	
	protected boolean reverse = false;
	
	protected ARG arg;
	
	ARGElement argElement;
	
	public ARGAxisElement(ARGFigure parent, ARGElement argElement) {
		super(parent);
		this.argElement = argElement;
	}

	public void setARG(ARG arg) {
		this.arg = arg;
	}
	
	@Override
	public void paint(Graphics2D g) {
		if (arg == null) {
			return;
		}
		
		if (isSelected()) {
			g.setColor(highlightColor);
			drawHorizontalAxis(g, argElement.getDrawWidth(), argElement.getLeftEdge(), toPixelY(0) );
		}
		g.setStroke(normalStroke);
		g.setColor(Color.black);
				
		drawHorizontalAxis(g, argElement.getDrawWidth(), argElement.getLeftEdge(), toPixelY(0) );
		
		drawHorizontalLabels(g, argElement.getDrawWidth(), argElement.getLeftEdge(), toPixelY(0) );
	}
	
	
	private void drawHorizontalAxis(Graphics2D g2d, int drawWidth, int leftEdge, int topEdge) {
		int ticks = scaleAxisTicks;
		double maxHeight = arg.getMaxHeight();
		
		double tickStep;
		if (useCustomDistance)
			tickStep = tickDistance; 
		else
			tickStep = maxHeight/(double)(ticks-1.0);
		
		g2d.drawLine(leftEdge, topEdge, leftEdge+drawWidth, topEdge);

		double tickX = 0;
		while(tickX <= maxHeight) {
			
			int pixX;
			if (reverse)
				pixX = argElement.toPixelX(tickX/maxHeight);
			else 
				pixX = argElement.toPixelX((maxHeight-tickX)/maxHeight);
			
			if (drawGridLines) {
				g2d.setColor(gridColor);
				g2d.drawLine(pixX, 1, pixX, topEdge);
				g2d.setColor(normalColor);
			}

			g2d.drawLine(pixX, topEdge, pixX, topEdge+5);			
			
			tickX += tickStep;
		}
		
	}
	
	
	protected void drawHorizontalLabels(Graphics2D g2d, int drawWidth, int leftEdge, int topEdge) {
		String label;
		double maxHeight = arg.getMaxHeight();
		
		//int ticks = scaleAxisTicks;
		
		double tickStep;
		if (useCustomDistance)
			tickStep = tickDistance; 
		else
			tickStep = maxHeight/((double)scaleAxisTicks-1.0);
		
		double tickX = 0;
		while(tickX <= maxHeight) {
			int pixX;
			if (reverse) {
				pixX = argElement.toPixelX(tickX/maxHeight);
				label = StringUtilities.format(tickX);
			}
			else { 
				pixX = argElement.toPixelX((maxHeight-tickX)/maxHeight);
				label = StringUtilities.format(tickX);
			}
			
			
			int width = g2d.getFontMetrics().stringWidth(label);
			g2d.drawString(label, Math.max(1, pixX-width/2), topEdge+17);			
			
			tickX += tickStep;
		}
	
	}

}
