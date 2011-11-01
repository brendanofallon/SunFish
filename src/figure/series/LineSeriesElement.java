package figure.series;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class LineSeriesElement extends XYSeriesElement {

	public LineSeriesElement(XYSeries series,
			XYSeriesFigure parent) {
		super(series, parent.getAxes(), parent);
		
	}
	
	protected void regenerateShape() {
		if (xySeries.size()==0) {
			pathShape = new GeneralPath();
			return;
		}

		if (pathShape == null) {
			pathShape = new GeneralPath(new Line2D.Double(xySeries.getX(0), xySeries.getY(0), xySeries.getX(1), xySeries.getY(1)) );
		}
		else 
			pathShape.reset();

		if (xySeries.size()>1) {
			double x1 = axes.dataXtoBoundsX(xySeries.getX(0)  );
			double y1 = axes.dataYtoBoundsY(xySeries.getY(0) );
			double x2 = axes.dataXtoBoundsX( xySeries.getX(1));
			double y2 = axes.dataYtoBoundsY( xySeries.getY(1) );
			pathShape = new GeneralPath(new Line2D.Double(x1, y1, x2, y2) );

			boolean connect = true;

			Point2D p;
			for(int i=1; i<xySeries.size(); i++) {
				p = xySeries.get(i);
				x1 = axes.dataXtoBoundsX( p.getX() );
				y1 = axes.dataYtoBoundsY( p.getY() );

				//We've moved from a undrawn region into an OK one, so just move the 'pointer'
				//to the new site
				if (!connect && !(Double.isNaN(y1))) {
					pathShape.moveTo(x1, y1);
					connect = true;
				}

				//Moving from a good region to an undrawn one
				if (connect && Double.isNaN(y1)) {
					connect = false;
				}


				if (connect)
					pathShape.lineTo(x1, y1);
			}

		}
	
	}


	public void paintSeries(Graphics2D g) {
		
		if (isSelected) {
			g.setColor(highlightColor);
			g.setStroke(highlightStroke); 
			g.draw(pathShape);
		}
		
		g.setStroke(normalStroke);
		
		g.setColor(getLineColor());
		g.draw(pathShape);			
		
	}

	public boolean contains(double x, double y) {		
		
		double dataX = axes.boundsXtoDataX(x);

		lineRect.setRect(x*xFactor-4, y*yFactor-4, 7, 7);
		Point2D[] line = xySeries.getLineForXVal(dataX);
		if (line==null || Double.isNaN(line[0].getY()) || Double.isNaN(line[1].getY())) {
			return false;
		}
		else {
			boolean contains = lineRect.intersectsLine(axes.dataXtoBoundsX(line[0].getX())*xFactor, axes.dataYtoBoundsY(line[0].getY())*yFactor, axes.dataXtoBoundsX(line[1].getX())*xFactor, axes.dataYtoBoundsY(line[1].getY())*yFactor);
			return contains;
		}
		
	}
	
	public void drawPreview(Graphics2D g, int x, int y) {
		g.setColor(getLineColor());
		g.drawLine(x, y-5, x+5, y-5);
	}
	
	public static SeriesInstantiator getInstantiator() {
		return new LineSeriesInstantiator();
	}

	public static class LineSeriesInstantiator implements SeriesInstantiator {

		@Override
		public XYSeriesElement getInstance(XYSeries series, XYSeriesFigure parent) {
			return new LineSeriesElement(series, parent);
		}

		@Override
		public String getSeriesTypeName() {
			return "Line";
		}
		
	}


}
