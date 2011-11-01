package figure.series;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import figure.series.LineSeriesElement.LineSeriesInstantiator;
import figure.series.MarkerLineElement.MarkerLineInstantiator;

public class BoxSeriesElement extends XYSeriesElement {

	public BoxSeriesElement(XYSeries series, XYSeriesFigure parent) {
		super(series, parent.getAxes(), parent);
		
	}
	
	/**
	 * Calculate the standard width of the rectangle used to draw a box, if we're drawing boxes. The
	 * default here is to pack the boxes tightly, but that can be controlled if the boxWidthDivisor
	 * variable is set.
	 * @return
	 */
	private double calculateBoxWidth() {
		double boxesShowing = xySeries.size()*(axes.maxXVal-axes.minXVal)/(xySeries.getMaxX()-xySeries.getMinX());
		double boxWidth = axes.getGraphAreaBounds().width / boxesShowing / (double)boxWidthDivisor;
		//System.out.println("Series size: " + xySeries.size() + " boxes showing: " + boxesShowing + " Box width: " + boxWidth);
		return boxWidth;
	}

	@Override
	protected void regenerateShape() {
			//We dont rely on a pathShape so this is a no-op
	}

	@Override
	public boolean contains(double x, double y) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Draws the rectangular box that corresponds to a particular point in this series  
	 * @param rect
	 */
	private void drawBox(Graphics2D g, Rectangle2D rect) {
		g.setColor(getLineColor());
		g.fill(rect);

		if (isSelected) {
			g.setColor(highlightColor);
			g.setStroke(highlightStroke);
			g.draw(rect);
		}
		
		if (rect.getWidth()>4) {
			if (decorateBoxes) {
				int dwidth = (int)Math.round(rect.getWidth()/2.0);
				for(int i=0; i<dwidth; i++) {
					g.setColor(new Color(1.0f, 1.0f, 1.0f, (0.2f)*(1.0f-(float)i/(float)dwidth)));
					g.drawLine((int)Math.round(rect.getX()+i), (int)Math.round(rect.getY()), (int)Math.round(rect.getX()+i), (int)Math.round(rect.getY()+rect.getHeight()));
				}

			}
			
			g.setStroke(normalStroke);
			g.setColor(boxOutlineColor);
			g.draw(rect);
		}		

	}
	
	/**
	 * Returns the rectangular box shape in pixel coordinates associated with the index i in the 
	 * data series. Requires knowing what the box width is and where the y-axis is.
	 */
	protected Rectangle2D getBoxForIndex(int i, double yZero) {
		if (i>=xySeries.size()) {
			return null;
		}
		double boxWidth = calculateBoxWidth();
		
		double halfBox = Math.ceil(boxWidth/2.0);
		double dataY = axes.dataYtoFigureY(xySeries.getY(i));
		double xOffset = boxOffset*boxWidth;
		if (xySeries.get(i).getY()>0) 
			boxRect.setRect(axes.dataXtoFigureX(xySeries.getX(i))-halfBox-xOffset, dataY, boxWidth, yZero-dataY);
		else 
			boxRect.setRect(axes.dataXtoFigureX(xySeries.getX(i))-halfBox-xOffset, yZero, boxWidth, dataY-yZero);

		return boxRect;
	}

	
	@Override
	public void paintSeries(Graphics2D g) {
		g.setStroke(normalStroke);
	
		g.setColor(getLineColor());
		double yAxis = axes.dataYtoFigureY(0);
		
		for(int i=0; i<xySeries.size(); i++) {
			Rectangle2D rect = getBoxForIndex(i, yAxis);
			drawBox(g, rect);
		}
		
	}
	
	public void drawPreview(Graphics2D g, int x, int y) {
		g.setColor(getLineColor());
		g.fillRect(x+3, y-6, x+10, 6);
		g.fillRect(x+12, y-10, x+19, 10);
	}
	
	public static SeriesInstantiator getStaticInstantiator() {
		return new BoxSeriesInstantiator();
	}
	
	public SeriesInstantiator getInstantiator() {
		return new BoxSeriesInstantiator();
	}
	
	public static class BoxSeriesInstantiator implements SeriesInstantiator {

		@Override
		public XYSeriesElement getInstance(XYSeries series, XYSeriesFigure parent) {
			return new BoxSeriesElement(series, parent);
		}

		@Override
		public String getSeriesTypeName() {
			return "Box";
		}
		
	}


}
