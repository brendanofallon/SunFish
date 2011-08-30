package plugins.argPlugin.argFigure.argSlider;

import java.awt.Color;
import java.util.List;

import plugins.argPlugin.arg.Range;
import plugins.argPlugin.argFigure.ARGFigure;

import figure.Figure;

/**
 * A figure that paints the slider-like tool that the user can use to set the range settings for an ARG. This fires
 * propertyChangeEvents with the propertyName RANGE_CHANGED when the user changes the value. This utilizes two
 * different elements to do the painting - one for the scale and labels and other background elements, and one for
 * the slider itself. 
 * @author brendan
 *
 */
public class ARGSliderFigure extends Figure {
	
	public static final String RANGE_CHANGED = "ARG range changed";
	
	ARGSliderBackgroundElement bgElement;
	ARGSliderFocalElement focusElement;
	
	public ARGSliderFigure(ARGFigure argFigure) {
		
		backgroundColor = new Color(0.98f, 0.98f, 0.98f);
		
		bgElement = new ARGSliderBackgroundElement(this, argFigure);
		bgElement.setBounds(0.1, 0.05, 0.8, 0.9);
		bgElement.setMobile(false);
		bgElement.setCanConfigure(false);
		bgElement.setZPosition(5); //Paint scalebar in front. 
		addElement(bgElement);
	
		focusElement = new ARGSliderFocalElement(this, bgElement);
		addElement(focusElement);
		
		
		focusElement.setMobile(false);
		focusElement.setCanConfigure(false);
		super.addMouseListeningElement(focusElement);
	}

	/**
	 * Set the list of breakpoints drawn on the background / scale element
	 * @param breakpoints
	 */
	public void setBreakPoints(List<Integer> breakpoints) {
		bgElement.setBreakPoints(breakpoints);
		repaint();
	}
	
	/**
	 * Set the minimum and maximum values for the background / scale element 
	 * @param min
	 * @param max
	 */
	public void setRangeBounds(int min, int max) {
		bgElement.setMin(min);
		bgElement.setMax(max);
	}
	
	/**
	 * Fire a property change event with name RANGE_CHANGED to notify interested parties that
	 * the user has changed the value(s) of the slider
	 * @param newMin
	 * @param newMax
	 */
	public void fireRangeChangedEvent(int newMin, int newMax) {
		this.firePropertyChange(RANGE_CHANGED, null, new Range(newMin, newMax));
	}

	public Range getCurrentRange() {
		return new Range( bgElement.sliderToSiteX(focusElement.getLeftEdge()), bgElement.sliderToSiteX(focusElement.getRightEdge()));
	}
}
