package plugins.argPlugin.argFigure.argSlider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import plugins.argPlugin.arg.Range;
import plugins.argPlugin.argFigure.ARGFigure;


import figure.Figure;
import figure.FigureElement;

/**
 * This FigureElement is responsible for painting the scale bar and labels and potentially other non-interactive
 * parts of the ARGSlider
 * 
 * @author brendan
 *
 */
public class ARGSliderBackgroundElement extends FigureElement {

	int min = 0;
	int max = 100;
	int ticks = 4;
	List<Integer> breakpoints = new ArrayList<Integer>();
	
	Stroke scaleStroke = new BasicStroke(1.2f);
	Stroke shadowStroke = new BasicStroke(2.0f);
	Stroke normalStroke = new BasicStroke(1.0f);
	Stroke regionColorStroke = new BasicStroke(2.5f);
	
	Color mainColor = Color.DARK_GRAY;
	Font scaleLabelFont = new Font("Sans", Font.PLAIN, 11);
	
	boolean paintRegionColors = true;
	
	ARGFigure argFigure = null;
		
	public ARGSliderBackgroundElement(Figure parent, ARGFigure argFigure) {
		super(parent);
		this.argFigure = argFigure;
	}

	/**
	 * Set the minimum (leftmost) value
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
		parent.repaint();
	}
	
	/**
	 * Set the maximum (rightmost) value
	 * @param max
	 */
	public void setMax(int max) {
		this.max = max;
		parent.repaint();
	}
	
	public int getBaseLine() {
		return toPixelY(1.0)-15;
	}
	
	public int getLeftEdge() {
		return toPixelX(0)+2;
	}
	
	public int getRightEdge() {
		return toPixelX(1.0)-2;
	}
	
	@Override
	public void paint(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(0, 0, round(xFactor), 0);

		int baseLine = getBaseLine();
		int leftEdge = getLeftEdge();
		int width = getRightEdge() - leftEdge;
	
		if (paintRegionColors)
			paintRegionColors(g, baseLine+1, leftEdge+1, width);

	
		g.setColor(mainColor);
		g.setStroke(scaleStroke);
		paintScale(g, baseLine, leftEdge, width);
		paintLabels(g, baseLine, leftEdge, width);
		
		g.setColor(Color.black);
		for(Integer bp : breakpoints) {
			int pX = siteToPixelX(bp);
			g.drawLine(pX, baseLine+4, pX, baseLine-4);
			g.drawLine(pX+1, baseLine+4, pX, baseLine-4);
		}
	}

	private void paintRegionColors(Graphics2D g, int baseLine, int leftEdge, int width) {
		g.setStroke(regionColorStroke);
		if (! argFigure.getTreesAreStacked()) {
			return;
		}
		for(int i=0; i<argFigure.getARG().getRangeCount(); i++) {
			Range range = argFigure.getARG().getRange(i);
			Color col = argFigure.getColorForRegion(i);
			g.setColor(col);
			int x0 = siteToPixelX(range.getMin());
			int x1 = siteToPixelX(range.getMax());
			g.drawLine(x0, baseLine, x1, baseLine);
		}
	}

	private void paintLabels(Graphics2D g, int baseLine, int leftEdge, int width) {
		g.setFont(scaleLabelFont);
		FontMetrics fm = g.getFontMetrics();
		
		for(int i=0; i<=ticks; i++) {
			int pos = leftEdge+round( (double)i/(double)ticks*width);
			int tickSite = round(min+(double)i/(double)ticks*(max-min));
			String labelStr = String.valueOf(tickSite);
			int strWidth = fm.stringWidth( labelStr );
			g.drawString(labelStr, pos-strWidth/2, toPixelY(1.0)-3);
		}
		
	}

	private void paintScale(Graphics2D g, int baseLine, int leftEdge, int width) {
		g.drawLine(leftEdge+1, baseLine, getRightEdge()-1, baseLine);
				
		for(int i=0; i<=ticks; i++) {
			int pos = leftEdge+round( (double)i/(double)ticks*width);
			g.drawLine(pos, baseLine, pos, baseLine-4);
		}
		
	}

	/**
	 * Finds the pixel X value corresponding to the given site
	 * @param site
	 * @return
	 */
	private int siteToPixelX(int site) {
		double siteFrac = (double)(site-min) / (double)(max-min);
		double width = getRightEdge() - getLeftEdge();
		int x = getLeftEdge() + round(siteFrac*width);
		return x;
	}
	
	/**
	 * Converts a slider value in 0..1 range to a site
	 * @param pixel
	 * @return
	 */
	public int sliderToSiteX(double x) {
		return round(min + x*(max-min));
	}
	
	public void setBreakPoints(List<Integer> breakpoints) {
		this.breakpoints.addAll(breakpoints);
	}

}
