package plugins.argPlugin.argFigure;



import java.awt.Color;

import plugins.argPlugin.arg.ARG;
import plugins.argPlugin.argDisplay.ARGDisplay;

import figure.Figure;
import figure.TextElement;

/**
 * A figure to handle ARGs in all their complicated wonder. These are usually embedded in an ARG display, with which the 
 * user can interact.  This Figure doesn't do much on it's own, the actual painting is done by an ARGElement. 
 * @author brendan
 *
 */
public class ARGFigure extends Figure {

	ARGDisplay display;
	ARGElement argElement = new ARGElement(this);
	ARGAxisElement axisElement = new ARGAxisElement(this, argElement);
	
	public ARGFigure(ARGDisplay display) {
		addComponentListener(this);

		setRectangleSelection(true); 	//Turn on rectangle selection
		this.display = display;
		
		argElement.setBounds(0.025, 0.05, 0.95, 0.8);
		addElement(argElement);
		
		axisElement.setBounds(0.025, 0.93, 0.95, 0.05);
		addElement(axisElement);
	}
	
	public ARG getARG() {
		return argElement.getARG();
	}
	
	public void setARG(ARG arg) {
		argElement.setARG(arg);
		axisElement.setARG(arg);
		repaint();
	}
	
	public void setRange(int newMin, int newMax) {
		argElement.setRangeMin(newMin);
		argElement.setRangeMax(newMax);
		repaint();
	}

	
	/**
	 * Return the color the argElement is using to paint the tree for the given region
	 * @param regionIndex
	 * @return
	 */
	public Color getColorForRegion(int regionIndex) {
		return argElement.getColorForRegion(regionIndex);
	}

	public void setShowTipLabels(boolean show) {
		argElement.setShowTipLabels(show);
		repaint();
	}

	public void setShowRecombNodes(boolean show) {
		argElement.setShowRecombNodes(show);
		repaint();
	}

	public void setShowStackedTrees(boolean selected) {
		argElement.setShowStackedTrees(selected);
		repaint();
	}

	public void setShowInternalNodeLabels(boolean selected) {
		argElement.setShowInternalNodeLabels(selected);
		repaint();
	}

	/**
	 * Returns true if we're using the 'stacked marginal trees' method of drawing colorized trees. 
	 * @return
	 */
	public boolean getTreesAreStacked() {
		return argElement.showStackedTrees;
	}
}
