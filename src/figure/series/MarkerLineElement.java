package figure.series;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import figure.series.LineSeriesElement.LineSeriesInstantiator;

public class MarkerLineElement extends LineSeriesElement {


	//Tracks current marker type and size
	String currentMarkerType = markerTypes[2];
	int markerSize = 6;
	
	
	public MarkerLineElement(XYSeries series, XYSeriesFigure parent) {
		super(series, parent);
		
	}
	
	/**
	 * Set the marker type for this series to the given type, which should be a member of markerTypes. This
	 * throws an IllegalArgumentException if the supplied type is not a valid type.
	 * @param markerType
	 */
	public void setMarker(String markerType) {
		boolean valid = false;
		for(int i=0; i<markerTypes.length; i++) {
			if (markerTypes[i].equals(markerType)) {
				currentMarkerType = markerType;
				valid = true;
			}
		}
		
		if (!valid) {
			throw new IllegalArgumentException("Cannot set marker type to : " + markerType);
		}
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
	
		currentMarkerType = ops.markerType;
		markerSize = ops.markerSize;
		
		if (resort) {
			((XYSeriesFigure)parent).getElementList().resort();
		}
		
		parent.repaint();
	}
	
	public void paint(Graphics2D g) {
		super.paint(g);
		
		//Paint all the markers
		g.setColor(getLineColor());
		for(int i=0; i<xySeries.size(); i++) {
			drawMarker(g, round(axes.dataXtoFigureX(xySeries.getX(i))), round(axes.dataYtoFigureY(xySeries.getY(i))));
		}

	}
	
	public void drawPreview(Graphics2D g, int x, int y) {
		g.setColor(getLineColor());
		g.drawLine(x, y-5, x+5, y-5);
		drawMarker(g, x, y);
	}
	
	private void drawMarker(Graphics2D g, int x, int y) {
		if (currentMarkerType.equals("Circle")) {
			g.setColor(getLineColor());
			g.fillOval((int)Math.round(x-markerSize/2.0), (int)Math.round(y-markerSize/2.0), markerSize, markerSize);
		}
		if (currentMarkerType.equals("Square")) {
			g.setColor(getLineColor());
			g.fillRect((int)Math.round(x-markerSize/2.0), (int)Math.round(y-markerSize/2.0), markerSize, markerSize);			
		}
		if (currentMarkerType.equals("Diamond")) {
			g.setColor(getLineColor());
			xvals[0] = (int)Math.round(x-markerSize/2.0);
			xvals[1] = x;
			xvals[2] = (int)Math.round(x+markerSize/2.0);
			xvals[3] = x;
			xvals[4] = xvals[0];
			yvals[0] = y;
			yvals[1] = (int)Math.round(y-markerSize/2.0);
			yvals[2] = y;
			yvals[3] = (int)Math.round(y+markerSize/2.0);
			yvals[4] = y;
			g.fillPolygon(xvals, yvals, 5);
		}
		if (currentMarkerType.equals("Plus")) {
			g.setColor(getLineColor());
			g.drawLine((int)Math.round(x-markerSize/2.0), y, (int)Math.round(x+markerSize/2.0), y);
			g.drawLine(x, (int)Math.round(y-markerSize/2.0), x, (int)Math.round(y+markerSize/2.0));
		}
		if (currentMarkerType.equals("X")) {
			g.setColor(getLineColor());
			g.drawLine((int)Math.round(x-markerSize/2.0), (int)Math.round(y-markerSize/2.0), (int)Math.round(x+markerSize/2.0), (int)Math.round(y+markerSize/2.0));
			g.drawLine((int)Math.round(x-markerSize/2.0), (int)Math.round(y+markerSize/2.0), (int)Math.round(x+markerSize/2.0), (int)Math.round(y-markerSize/2.0));
		}
	}

	public String getMarkerType() {
		return currentMarkerType;
	}

	public int getMarkerSize() {
		return markerSize;
	}
	
	public static SeriesInstantiator getStaticInstantiator() {
		return new MarkerLineInstantiator();
	}
	
	public SeriesInstantiator getInstantiator() {
		return new MarkerLineInstantiator();
	}

	public static class MarkerLineInstantiator implements SeriesInstantiator {

		@Override
		public XYSeriesElement getInstance(XYSeries series, XYSeriesFigure parent) {
			return new MarkerLineElement(series, parent);
		}

		@Override
		public String getSeriesTypeName() {
			return "Line with markers";
		}
		
	}


	
}
