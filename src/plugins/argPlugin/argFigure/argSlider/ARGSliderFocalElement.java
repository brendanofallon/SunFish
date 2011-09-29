package plugins.argPlugin.argFigure.argSlider;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import figure.Figure;
import figure.FigureElement;

public class ARGSliderFocalElement extends FigureElement {

	ARGSliderBackgroundElement bgElement;
	int height = 14;
	
	//Following fields describe the location, in 0..1 'figure bounds' of the edges and focal site of this element
	double leftEdge = 0.2; //Fraction of this elements width that left edge is in from
	double rightEdge = 0.75; //Fraction of this element's width that right edge is in from 0
	double focalPoint = 0.50; //Fraction of element's width where the focal point is
	
	boolean leftDragging = false;
	boolean centerDragging = false;
	boolean rightDragging = false;
	
	static int numGrays = 10;
	static Color[] grays;
	
	element.Point prevDragLoc = new element.Point(0, 0);
	
	ARGSliderFigure argSliderParent;
	
	//When true, we draw the actual numbers of the sites. Normally this happens only during dragging. 
	boolean drawSiteNumbers = false;
	Color numberColor = Color.GRAY;
	Font numberFont = new Font("Sans", Font.BOLD, 11);
	
	public ARGSliderFocalElement(ARGSliderFigure parent, ARGSliderBackgroundElement bgElement) {
		super(parent);
		argSliderParent = parent;
		this.bgElement = bgElement;
		super.setBounds(bgElement.getX(), bgElement.getY(), bgElement.getWidth(), bgElement.getHeight());
		
		if (grays == null) {
			grays = new Color[numGrays];
			for(int i=0; i<10; i++) {
				float val = 1.0f - 0.20f*(float)i/(float)numGrays;
				grays[i] = new Color(val, val, val);
			}
		}
	}

	public void setBounds(double x, double y, double w, double z) {
		throw new IllegalArgumentException("ARGSlider focus elements get their bounds from the background element, you can't set them");
	}
	
	@Override
	public void paint(Graphics2D g) {
		
		paintBox(g);
		
		if (drawSiteNumbers) {
			paintSites(g);
		}
	}

	/**
	 * Paint the numbers indicating the exact edges of the range on either side of the box
	 * @param g
	 */
	private void paintSites(Graphics2D g) {
		g.setColor(numberColor);
		g.setFont(numberFont);
		FontMetrics fm = g.getFontMetrics();
		int leftSite = bgElement.sliderToSiteX(leftEdge);
		int rightSite = bgElement.sliderToSiteX(rightEdge);
		//int centerSite = bgElement.sliderToSiteX( (leftEdge+rightEdge)/2.0);
		String leftStr = String.valueOf(leftSite);
		String rightStr = String.valueOf(rightSite);
		//String centerStr = String.valueOf(centerSite);
		int leftWidth = fm.stringWidth( leftStr);
		
		g.drawString(leftStr, toPixelX(leftEdge)-leftWidth-2, bgElement.getBaseLine()-3);
		g.drawString(rightStr, toPixelX(rightEdge)+2, bgElement.getBaseLine()-3);
	}

	/**
	 * This actually paints the fancy box
	 * @param g
	 */
	private void paintBox(Graphics2D g) {
		int baseLine = bgElement.getBaseLine();
		int left = toPixelX(leftEdge);
		int right = toPixelX(rightEdge);
		for(int i=2; i<height; i++) {
			int grayIndex = (int)Math.floor( (double)(height-i)/(double)height * numGrays);
			g.setColor(grays[grayIndex]);
			g.drawLine(left+1, baseLine-i, right, baseLine-i);
		}
		
		//A few grabber / texture marks
		g.setColor(new Color(0f, 0f, 0f, 0.5f));
		g.drawLine(left+3, baseLine-height+3, left+3, baseLine-3);
		g.drawLine(left+6, baseLine-height+3, left+6, baseLine-3);

		g.drawLine(right-3, baseLine-height+3, right-3, baseLine-3);
		g.drawLine(right-6, baseLine-height+3, right-6, baseLine-3);
		
		g.setColor(new Color(1f, 1f, 1f, 0.5f));
		g.drawLine(left+4, baseLine-height+3, left+4, baseLine-3);
		g.drawLine(left+7, baseLine-height+3, left+7, baseLine-3);
		g.drawLine(right-4, baseLine-height+3, right-4, baseLine-3);
		g.drawLine(right-7, baseLine-height+3, right-7, baseLine-3);	
		
		int middle = round( (left+right)/2.0);
		g.setColor(new Color(0f, 0f, 0f, 0.5f));
		g.drawLine(middle-3, baseLine-height+3, middle-3, baseLine-3);
		g.drawLine(middle, baseLine-height+3, middle, baseLine-3);
		g.drawLine(middle+3, baseLine-height+3, middle+3, baseLine-3);
	
		g.setColor(new Color(1f, 1f, 1f, 0.5f));
		g.drawLine(middle-2, baseLine-height+3, middle-2, baseLine-3);
		g.drawLine(middle+1, baseLine-height+3, middle+1, baseLine-3);
		g.drawLine(middle+4, baseLine-height+3, middle+4, baseLine-3);
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(toPixelX(leftEdge), baseLine-height, toPixelX(rightEdge)-toPixelX(leftEdge), height-1);
	}

	/**
	 * Returns true if the supplied point is on the left edge of the box. Pos is assumed to be in Figure (0..1) coords
	 * @param pos
	 * @return
	 */
	private boolean isPointOnLeft(Point2D pos) {
		double dif = pos.getX()*xFactor - toPixelX(leftEdge);
		
		//System.out.println("Pos x: " + pos.x*xFactor + " leftEdge : " + toPixelX(leftEdge) + " dif: " + dif);
		if (dif > 0 && dif < 10) {
			int pixY = round(pos.getY()*yFactor);
			//System.out.println("PixY : " + pixY + " baseLine : " + bgElement.getBaseLine());
			if (pixY < bgElement.getBaseLine() && pixY > bgElement.getBaseLine()-height  )
				return true;
		}
		return false;
	}
	
	
	private boolean isPointOnCenter(Point2D pos) {
		double dif = pos.getX()*xFactor - toPixelX( (leftEdge+rightEdge)/2.0);
		if (dif > -10 && dif < 10) {
			int pixY = round(pos.getY()*yFactor);
			if (pixY < bgElement.getBaseLine() && pixY > bgElement.getBaseLine()-height  )
				return true;
		}
		return false;
	}

	private boolean isPointOnRight(Point2D pos) {
		double dif = pos.getX()*xFactor - toPixelX(rightEdge);
		
		//System.out.println("Pos x: " + pos.x*xFactor + " leftEdge : " + toPixelX(leftEdge) + " dif: " + dif);
		if (dif < 0 && dif > -10) {
			int pixY = round(pos.getY()*yFactor);
			//System.out.println("PixY : " + pixY + " baseLine : " + bgElement.getBaseLine());
			if (pixY < bgElement.getBaseLine() && pixY > bgElement.getBaseLine()-height  )
				return true;
		}
		return false;
	}
	
	/**
	 * The following four methods are called only if this figure element has been 
	 * added to the list of mouseListeningElements in Figure (via Figure.addMouseListeningElement ). 
	 * Do what you will here. 
	 * @param pos The mouse position in bounds (0..1) coordinates
	 */
	protected void mouseMoved(Point2D  pos) {	};
	
	protected void mousePressed(Point2D pos) {	
		if (isPointOnLeft(pos)) {
			leftDragging = true;
			prevDragLoc.x = pos.getX();
			prevDragLoc.y = pos.getY();
		}
		
		if (isPointOnRight(pos)) {
			rightDragging = true;
			prevDragLoc.x = pos.getX();
			prevDragLoc.y = pos.getY();
		}
		
		if (isPointOnCenter(pos)) {
			centerDragging = true;
			prevDragLoc.x = pos.getX();
			prevDragLoc.y = pos.getY();
		}
	}
	
	protected void mouseReleased(Point2D pos) {	
		leftDragging = false;
		centerDragging = false;
		rightDragging = false;
		drawSiteNumbers = false;
	};
	
	/**
	 * Called continuously as the mouse is being dragged, here we assume that the leftDragging, rightDragging, etc. fields have been
	 * set correctly, and simply adjust the positions of the left and right edge accordingly. We also fire new "range changed" events
	 * which are typically listened to by the parent figure, which then dispatches the event to the ARGDisplay so the ARG can be redrawn
	 */
	protected void mouseDragged(Point2D pos) {	
		if (leftDragging) {		
			leftEdge += (pos.getX() - prevDragLoc.x)/bounds.width;
			if (leftEdge < 0)
				leftEdge = 0;
			if (leftEdge > (rightEdge-0.01))
				leftEdge = rightEdge-0.01;
				
			prevDragLoc.x = pos.getX();
			prevDragLoc.y = pos.getY();
			fireNewRangeEvent();
		}
		
		if (rightDragging) {
			rightEdge += (pos.getX() - prevDragLoc.x)/bounds.width;
			if (rightEdge < 0)
				rightEdge = 0;
			if (rightEdge > 1.0) {
				rightEdge = 1.0;
			}
			if (rightEdge < (leftEdge+0.01))
				rightEdge = leftEdge+0.01;
				
			prevDragLoc.x = pos.getX();
			prevDragLoc.y = pos.getY();
			fireNewRangeEvent();
		}
		
		if (centerDragging) {
			double dif = (pos.getX() - prevDragLoc.x)/bounds.width;
			double prevWidth = rightEdge - leftEdge;
			rightEdge += dif;
			leftEdge += dif;
			
			if (leftEdge < 0) {
				rightEdge += -leftEdge;
				leftEdge = 0;
			}
			
			if (rightEdge > 1.0) {
				leftEdge = 1.0 - prevWidth;  
				rightEdge = 1.0;
			}
			
			prevDragLoc.x = pos.getX();
			prevDragLoc.y = pos.getY();
			fireNewRangeEvent();
		}
		
		drawSiteNumbers = true;
	}
	
	/**
	 * Causes the arg slider parent to fire a new range changed event, alerting interested parties that the user has 
	 * changed the slider somehow. 
	 */
	protected void fireNewRangeEvent() {
		argSliderParent.fireRangeChangedEvent(bgElement.sliderToSiteX(leftEdge), bgElement.sliderToSiteX(rightEdge));
	}

	/**
	 * Get the relative position of the right edge of the box, in 0..1 coords 
	 * @return
	 */
	public double getRightEdge() {
		return rightEdge;
	}
	
	
	/**
	 * Get the relative position of the left edge of the box, in 0..1 coords 
	 * @return
	 */
	public double getLeftEdge() {
		return leftEdge;
	}
	
}
