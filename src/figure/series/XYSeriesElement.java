package figure.series;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import errorHandling.ErrorWindow;


/**
 * An element that draws a series of points in x-y space. Different markers can be placed
 * on data points, or lines can be drawn through points, or the series can be represented by
 * 'boxes' column graph style. 
 * 
 * @author brendan
 *
 */
public abstract class XYSeriesElement extends SeriesElement {
	
	protected XYSeries xySeries;
	GeneralPath pathShape;
	
	AxesElement axes;
	
	SeriesConfigFrame configFrame;
	
	//Flag to indicate if we should recalculate data bounds
	//False indicates we should recalculate
	boolean dataBoundsSet = false;
	
	boolean scaleHasBeenSet = false;
	
	Color boxOutlineColor = Color.GRAY;
	
	Rectangle2D lineRect; //Use to test if this series contains certain points
	
	//Ensure boxes are linked 
	boolean connectBoxes = true;
	
	//The transformation object that maps 'figure' points (in 0..1 scale) into pixel space
	//We keep track of it to avoid having to make a new one all the time, and 
	//so that, when new transforms are needed, we can call the .invert() method
	//on it to unapply the previous transform before a new one is applied
	AffineTransform currentTransform;
	
	//Draws fancier looking boxes
	protected boolean decorateBoxes = true;
	
	Rectangle2D boxRect = new Rectangle2D.Double(0, 0, 0, 0); //Used repeatedly to draw boxes
	
	//Some allocated space for marker polygon drawing
	int[] xvals;
	int[] yvals;
	
	public XYSeriesElement(XYSeries series, AxesElement axes, XYSeriesFigure parent) {
		super(parent, series);
		this.xySeries = series;
		this.axes = axes;
		this.xFactor = 1;
		this.yFactor = 1;
		dataBoundsSet = false; 
		
		currentTransform = new AffineTransform();
		currentTransform.setToIdentity();
		
		configFrame = new SeriesConfigFrame(this, parent);
		
		normalStroke = new BasicStroke(1.25f);
		highlightStroke = new BasicStroke(1.25f + highlightWidthIncrease);
		
		lineRect = new Rectangle.Double(0, 0, 0, 0);
		
		//Some buffers for drawing marker polygons
		xvals = new int[5];
		yvals = new int[5];
	}
	
	public XYSeries getSeries() {
		return xySeries;
	}
	
	public void setSeries(XYSeries newSeries) {
		super.series = newSeries;
		this.xySeries = newSeries;
		dataBoundsSet = false;
	}
	

	/**
	 * Paint this series using the given graphics object
	 * @param g
	 */
	public abstract void paintSeries(Graphics2D g);
	
	/**
	 * Obtain the SeriesInstantiator object used to create this type of XYSeriesElement
	 * @return
	 */
	public abstract SeriesInstantiator getInstantiator();
	
	
	@Override
	public double getMaxY() {
		return xySeries.getMaxY();
	}

	@Override
	public double getMinY() {
		return xySeries.getMinY();
	}
	
	@Override
	public double getMaxX() {
		return xySeries.getMaxX();
	}

	@Override
	public double getMinX() {
		return xySeries.getMinX();
	}
	
	
	
	
	public void popupConfigureTool(java.awt.Point pos) {
		configFrame.display(this);
	}
	
	/**
	 * Sets the various options (colors, linewidths, etc) of this series to those specified in ops
	 * @param ops Container object for various series options.
	 */
	public void setOptions(SeriesConfigFrame.SeriesOptions ops) {
		boolean resort = false;

		xySeries.setName( ops.name );
		setLineColor(ops.lineColor);
		
		normalStroke =  new BasicStroke((float)ops.lineWidth, normalStroke.getEndCap(), normalStroke.getLineJoin(), normalStroke.getMiterLimit(), normalStroke.getDashArray(), normalStroke.getDashPhase());;
		highlightStroke = new BasicStroke(normalStroke.getLineWidth()+highlightWidthIncrease, normalStroke.getEndCap(), normalStroke.getLineJoin(), normalStroke.getMiterLimit(), normalStroke.getDashArray(), normalStroke.getDashPhase());
		
		if (resort) {
			((XYSeriesFigure)parent).getElementList().resort();
		}
		
		parent.repaint();
	}
	
	/**
	 * This call informs this element of what the data x and y bounds are.  It must be called
	 * prior to any painting operation. Also, calls to this function are relatively expensive since
	 * they involve creating a bunch of new objects, and ideally this should only be called when the
	 * x or y boundaries are changed (but not, for instance, when the figure size has changed).
	 * 
	 * @param dBounds
	 */
	public void setDataBounds() {
		regenerateShape();
		dataBoundsSet = true;
	}
	
	
	/**
	 * Called when we must recompute the pathShape, typically because the 
	 * underlying series has changed somehow
	 */
	protected abstract void regenerateShape();

	/**
	 * Returns true if this series element contains the given point
	 */
	public abstract boolean contains(double x, double y);
	
//	public boolean contains(double x, double y) {		
//		
//		if (currentMode == POINTS_AND_LINES || currentMode == LINES) {
//			double dataX = axes.boundsXtoDataX(x);
//			
//			lineRect.setRect(x*xFactor-4, y*yFactor-4, 7, 7);
//			Point2D[] line = xySeries.getLineForXVal(dataX);
//			if (line==null || Double.isNaN(line[0].getY()) || Double.isNaN(line[1].getY())) {
//				return false;
//			}
//			else {
//				boolean contains = lineRect.intersectsLine(axes.dataXtoBoundsX(line[0].getX())*xFactor, axes.dataYtoBoundsY(line[0].getY())*yFactor, axes.dataXtoBoundsX(line[1].getX())*xFactor, axes.dataYtoBoundsY(line[1].getY())*yFactor);
//				return contains;
//			}
//			
//		}
//		
//		if (currentMode == BOXES) {
//			double boxWidth = calculateBoxWidth();
//
//
//			double yAxis = axes.dataYtoFigureY(0);
//			
//			double dataX = axes.boundsXtoDataX(x+(boxWidth*boxOffset+Math.ceil(boxWidth/2.0))/xFactor);
//			int boxIndex = xySeries.getIndexForXVal(dataX);
//			Rectangle2D rect = getBoxForIndex(boxIndex, yAxis); 
//			//System.out.println( " click x: " + x*xFactor + " data x: " + dataX + "Box index: " + boxIndex + " x: " + rect.getX() + " height: " + rect.getHeight() + " width: " + rect.getWidth() );
//			Point2D pos = new Point2D.Double(x*xFactor, y*yFactor);
//			if (rect == null)
//				return false;
//			else
//				return rect.contains(pos);
//		}
//		
//		if (currentMode == POINTS) {
//			return pathShape.intersects(x*xFactor-3, y*yFactor-3, 5, 5);
//		}
//		
//		return false;
//	}
	
	
	/**
	 * Called (by Figure) when this element is single clicked
	 */
	public void clicked(Point pos) {
		setSelected(true);
	}

	/**
	 * Called (by Figure) when this element is double clicked
	 */
	public void doubleClicked(Point pos) { 
		setSelected(true);
		if (canConfigure) {
			popupConfigureTool(pos);
		}
	};
	
	
	
	private void emitPathShape() {
		AffineTransform transform = new AffineTransform();
		transform.setToIdentity();
		PathIterator pi = pathShape.getPathIterator(transform);
		
		double[] coords = new double[6];
		int index = 0;
		while (! pi.isDone()) {
			pi.currentSegment(coords);
			pi.next();
			index++;
		}
	}
	
	public void setScale(double xFactor, double yFactor, Graphics g) {
		setDataBounds();
				
		currentTransform.setToScale(xFactor, yFactor);
		if (pathShape != null)
			pathShape.transform(currentTransform);
		
		this.xFactor = xFactor;
		this.yFactor = yFactor;	
		scaleHasBeenSet = true;
	}
	
	public void paint(Graphics2D g) {
		
		if (! dataBoundsSet )
			setDataBounds();
	
		Rectangle clipBounds = axes.getGraphAreaBounds();
		clipBounds.x++;
		clipBounds.height++;
		
		g.setClip(clipBounds ); //Make sure we don't draw outside the lines
		
		paintSeries(g);
		
//		if (isSelected) {
//			g.setColor(highlightColor);
//			g.setStroke(highlightStroke);
//			if (currentMode == LINES || currentMode == POINTS_AND_LINES || currentMode == BOXES) 
//				g.draw(pathShape);
//		}
//		
//		g.setStroke(normalStroke);
//		
//		if (currentMode == LINES) {
//			g.setColor(getLineColor());
//			g.draw(pathShape);	
//		}
//		
//		if (currentMode == BOXES) {
//			g.setColor(getLineColor());
//			double yAxis = axes.dataYtoFigureY(0);
//			
//			for(int i=0; i<xySeries.size(); i++) {
//				Rectangle2D rect = getBoxForIndex(i, yAxis);
//				drawBox(g, rect);
//			}
//		}
//
//		
//		if (currentMode == POINTS ) {
//			g.setColor(getLineColor());
//			for(int i=0; i<xySeries.size(); i++) {
//				drawMarker(g, round(axes.dataXtoFigureX(xySeries.getX(i))), round(axes.dataYtoFigureY(xySeries.getY(i))));
//			}
//		}
//		
//		if (currentMode == POINTS_AND_LINES ) {
//			g.setColor(getLineColor());
//			g.draw(pathShape);
//			
//			g.setColor(getLineColor());
//			for(int i=0; i<xySeries.size(); i++) {
//				drawMarker(g, round(axes.dataXtoFigureX(xySeries.getX(i))), round(axes.dataYtoFigureY(xySeries.getY(i)))); 
//			}
//		}	
		
		g.setStroke(normalStroke);
		g.setClip(0, 0, parent.getWidth(), parent.getHeight()); //return clip to usual bounds
		
	}




	
}
