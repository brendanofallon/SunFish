package figure.series;

import figure.Figure;
import figure.FigureElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * A generic element that displays a series of data, either ordinal or non-ordinal. Subclasses implement
 * drawing schemes for XYSeries and CategorySeries data types. 
 * @author brendan
 *
 */
public abstract class SeriesElement extends FigureElement {
	
	protected AbstractSeries series; //Stores the data we're representing
	
	protected Color color;
	
	//List of marker shapes
	//If you change this list, you must also add corresponding code to drawMarker
	public static final String[] markerTypes = {"Circle", "Square", "Diamond", "Plus", "X"};
	
	
	//The strokes used to paint the line and the highlighted line
	BasicStroke normalStroke;
	BasicStroke highlightStroke;
	protected float highlightWidthIncrease = 3.25f; //Width increase of highlight stroke over normal stroke
	
	//If we want to display multiple series with box shapes, then we divide their 
	//size by a certain factor, and offset them by a certain amount
	protected int boxWidthDivisor = 1;
	protected double boxOffset = 0;
	
	public SeriesElement(Figure parent, AbstractSeries series) {
		super(parent);
		this.series = series;
	}
	
	public abstract double getMinY();
	
	public abstract double getMaxY();
	
	public abstract double getMinX();
	
	public abstract double getMaxX();
	
	/**
	 * Draw the small 'preview' of this series at the given location. The preview is the small
	 * image used to identify this series in the legend
	 * @param g
	 * @param x
	 * @param y
	 */
	public abstract void drawPreview(Graphics2D g, int x, int y);
	
	
	public AbstractSeries getSeries() {
		return series;
	}
	
	/**
	 * Set both both the stroke used to paint this series to the new value (and the highlight stroke to
	 * something a bit bigger)
	 * @param width
	 */
	public void setLineWidth(float width) {
		normalStroke = new BasicStroke(width);
		highlightStroke = new BasicStroke(width+highlightWidthIncrease);		
	}
	
	/**
	 * Set the line stroke property to be the given stroke. The highlight stroke is automagically set to be something a bit wider
	 * @param newStroke
	 */
	public void setStroke(BasicStroke newStroke) {
		normalStroke = newStroke;
		highlightStroke = new BasicStroke(newStroke.getLineWidth()+highlightWidthIncrease, newStroke.getEndCap(), newStroke.getLineJoin(), newStroke.getMiterLimit(), newStroke.getDashArray(), newStroke.getDashPhase());
	}
	
	/**
	 * Primary color of this series element
	 * @return
	 */
	public Color getLineColor() {
		return color;
	}

	/**
	 * The default width of the line used to draw this series - this is interpreted somewhat differently
	 * by different element types
	 * @return
	 */
	public float getLineWidth() {
		return normalStroke.getLineWidth();
	}
	
	/**
	 * Return the name of the series
	 * @return
	 */
	public String getName() {
		return series.getName();
	}
	
	/**
	 * Set the name of the series to the new value
	 * @param name
	 */
	public void setName(String name) {
		series.setName(name);
	}
	
	/**
	 * Set the color of the lines and boxes drawn to represent this series. 
	 * @param c
	 */
	public void setLineColor(Color c) {
		color = c;
		highlightColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 150);
	}
	
	
	/**
	 * Sets the box width divisor value and box offset value for painting multiple
	 * box series' on the same chart (that don't overlap). These values represent
	 * not pixels but the "fractions of box width", a divisor of two shrinks the box by
	 * a factor of two. Similarly an offset of 1 shifts the box by one boxWidth value to
	 * the left.
	 * 
	 * @param boxWidthDivisor
	 * @param offset
	 */
	public void setBoxWidthAndOffset(int boxWidthDivisor, double offset) {
		this.boxWidthDivisor = boxWidthDivisor;
		this.boxOffset = offset;
	}

	
	

}
