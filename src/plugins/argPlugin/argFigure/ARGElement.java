package plugins.argPlugin.argFigure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import plugins.argPlugin.arg.ARG;
import plugins.argPlugin.arg.ARGNode;
import plugins.argPlugin.arg.Edge;
import plugins.argPlugin.arg.Range;

import network.NetworkEdge;
import network.NetworkNode;
import network.Node2D;


import figure.Figure;
import figure.FigureElement;

/**
 * An element that actually paints the arg, based on a minimum and maximum range that specify exactly what ARG
 * regions are painted, and a focalSite that determines the layout of the nodes (by specifying a particular marginal
 * tree to use for rearrangement)
 * @author brendan
 *
 */
public class ARGElement extends FigureElement {

	protected ARG arg;
	
	//The stroke used to paint to focal tree
	Stroke focalStroke = new BasicStroke(2.0f);
	
	Stroke normalStroke = new BasicStroke(1.85f);
	
	//Color used to paint the focal tree
	Color focalColor = Color.black;
	
	//Color used to paint nodes
	Color nodeColor = Color.green;
	
	protected final int numColors = 50;
	Color[] treePalette;
	
	Font labelFont = new Font("Sans", Font.PLAIN, 10);
	Font annotationFont = new Font("Sans", Font.PLAIN, 9);
	
	//Vertical displacement of each tree
	int yOffsetSize = 2;
	
	//Used for painting curving branches
	protected CubicCurve2D curve = new CubicCurve2D.Float();
	
	//Following fields specify the range of sites over which to paint the arg
	protected int rangeMin = 0;
	protected int rangeMax = 500;
	
	protected int labelSpace = 100; //How much room to reserve for tip label painting
	protected boolean labelSpaceKnown = false;
	
	protected boolean showTipLabels = true;

	protected boolean showRecombNodes = true;
	
	protected boolean showStackedTrees = true;
	
	protected boolean showInternalNodeLabels = false;

	//Used to collect all interior nodes that will be painted. Used as a field so we dont instantiate with every call to paint
	protected List<ARGNode> nodesToPaint = new ArrayList<ARGNode>();
	
	public ARGElement(Figure parent) {
		super(parent);
	}
	
	/**
	 * Set whether or not we paint the labels for the tips
	 * @param show
	 */
	public void setShowTipLabels(boolean show) {
		if (show != showTipLabels) {
			labelSpaceKnown = false;
		}
		this.showTipLabels = show;
	}
	
	public void setLabelFont(Font font) {
		this.labelFont = font;
		labelSpaceKnown = false;
	}
	
	private void computeLabelSpace(Graphics2D g) {
		if (arg==null)
			return;
		if (!showTipLabels) {
			labelSpace = 0;
			labelSpaceKnown = true;
			return;
		}
		
		int max = 0;
		g.setFont(labelFont);
		FontMetrics fm = g.getFontMetrics();
		for(ARGNode tip : arg.getTips()) {
			int width = fm.stringWidth(tip.getID());
			if (width > max)
				max = width;
		}
		labelSpace = max+10;
		labelSpaceKnown = true;
	}
	
	/**
	 * Return the arg that this element is drawing
	 * @return
	 */
	public ARG getARG() {
		return arg;
	}
	
	public void setARG(ARG arg) {
		this.arg = arg;
		labelSpaceKnown = false;
		fillColors();
	}
	
	/**
	 * Returns the minimum site index that is currently being drawn by this element
	 * @return
	 */
	public int getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(int rangeMin) {
		this.rangeMin = rangeMin;
	}

	/**
	 * Returns the maximum site index that is currently being drawn
	 * @return
	 */
	public int getRangeMax() {
		return rangeMax;
	}

	/**
	 * Set the maximum site boundary for marginal tree drawing
	 * @param rangeMax
	 */
	public void setRangeMax(int rangeMax) {
		this.rangeMax = rangeMax;
	}
	
	/**
	 * Returns the left edge of the tree in pixel values
	 * @return
	 */
	public int getLeftEdge() {
		return toPixelX(0);
	}
	
	/**
	 * Returns the width of the drawing area in pixels
	 * @return
	 */
	public int getDrawWidth() {
		return toPixelX(1)-toPixelX(0);
	}
	
	/**
	 * Obtain the color associated with the given range
	 * @param r
	 * @return
	 */
	public Color colorForRange(Range r) {
		if (!showStackedTrees) {
			return focalColor;
		}
		double val = ((r.getMin() + r.getMax())/2.0-arg.getMinSite())/(arg.getMaxSite()-arg.getMinSite()) ;
		return treePalette[ (int)Math.floor( val*treePalette.length) ];
	}
	
	/**
	 * Return color associated with the given region index
	 * @param rangeIndex
	 * @return
	 */
	public Color getColorForRegion(int rangeIndex) {
		if (!showStackedTrees) {
			return focalColor;
		}
		Range range = arg.getRange(rangeIndex);
		return colorForRange(range);
	}
	
	
	
	@Override
	public void paint(Graphics2D g) {
		if (arg == null) {
			g.setColor(Color.gray);
			g.drawString("ARG not set", round(xFactor/2.0), round(yFactor/2.0));
			return;
		}
		
		//Compute how much space we need for tip labels if not known already
		if (!labelSpaceKnown) {
			computeLabelSpace(g);
		}
		
		
		//Calculate node positions if we don't know them already. 
		if (arg.getRecalculateNodePositions()) {
			arg.recalculateNodePositions();
		}
		
		nodesToPaint.clear();
		
		//First paint all branches in ranges greater than the focal range but less than rangeMax
		int focalSite = arg.getFocalSite();
		Range range = arg.getRangeForSite(focalSite);
		range = arg.getNextRange(range);
		
		int index = 1;
		int actualYOffset = showStackedTrees ? yOffsetSize : 0;
		//System.out.println("Show stacked: " + showStackedTrees + " yOffset: " + actualYOffset);
		while(range != null && range.getMin() < rangeMax) {
			paintBranchesForRange(g, range, -index*actualYOffset, colorForRange(range));
			range = arg.getNextRange(range);
			index++;
		}
		
		//Now paint marginal trees 'back' toward site zero
		range = arg.getRangeForSite(focalSite);
		range = arg.getPreviousRange(range);
		index = 1;
		while(range != null && rangeMin < range.getMax()) {
			paintBranchesForRange(g, range, index*actualYOffset, colorForRange(range));
			range = arg.getPreviousRange(range);
			index++;
		}
		
		//Draw focal tree on top of all other trees
		paintFocalTree(g);
		
		//paint nodes and node labels if necessary
		for(ARGNode node : nodesToPaint) {
			paintNode(g, node);
		}
		
		//Paint all tips
		if (showTipLabels) {
			for(ARGNode tip : arg.getTips()) {
				g.setColor(Color.black);
				g.setFont(labelFont);
				paintTipLabel(g, tip);
			}
		}
	}

	private void paintBranchesForRange(Graphics2D g, Range range, int yOffset, Color col) {
		List<Edge> branches = arg.getBranchesForTree(range.getMin());
		g.setStroke(normalStroke);
		g.setColor(col);
		Range focalRange = arg.getRangeForSite(arg.getFocalSite());
		
		//System.out.println("Painting branches for range: " + range);
		
		for(Edge branch : branches) {
			ARGNode source = branch.getSource();
			ARGNode target = branch.getTarget();
			
			nodesToPaint.add(target);
			
			
			boolean paintCurve = false;
			
			//We only paint a curves if the source has a recombination and the target (the parent node) has only one kid,
			//which must be the source. 
			if (source.hasRecombParent() &&  target.getNumOffspring()==1) {
				//We also only paint a curved branch if the breakpoint associated with the source node is between the range we're
				//currently painting and the focal range...
				int sourceBP = source.getBreakPointMin() > 0 ? source.getBreakPointMin() : source.getBreakPointMax()-1;
				//System.out.println("Focal range min: " + focalRange.getMin() + " rangeMax: " + range.getMax() + " sourceBP : " + sourceBP );
				if (range.getMin() < sourceBP && sourceBP <= focalRange.getMin() ) {
					paintCurve = true;
				}
				if (focalRange.getMin() <= sourceBP && sourceBP < (range.getMax()-1)) {
					paintCurve = true;
				}
				
			}
			
			if (paintCurve) {
				int x0 = toPixelX(target.getX());
				int x1 = toPixelX(source.getX());
				int y0 = toPixelY(target.getY());
				int y1 = toPixelY(source.getY());
				
				double firstDev = 0.6; //Horizontal offset of first control point from source, as fraction of x deviation
				double secondDev = 0.4; //Horizontal offset of second control point from source, as fraction of x deviation
				int vertDev = Math.min(round(10 + 0.1*(x1-x0)), 50); //vertical deviation of curve, in pixels
				if (yOffset<0)
					vertDev *= -1;
		
				int xDif = x1-x0;
				if (xDif==0) 
					xDif =1;
				int xa = round( x0 + firstDev*xDif );  //pixel x location of first control point
				int xb = round( x0 + secondDev*xDif ); //pixel x location of second control point
				int ya = y0 + (xa-x0)*(y1-y0)/xDif + vertDev;
				int yb = y0 + (xb-x0)*(y1-y0)/xDif + vertDev;

				curve.setCurve(toPixelX(source.getX()), toPixelY(source.getY())+yOffset, xa, ya+yOffset, xb, yb+yOffset, toPixelX(target.getX()), toPixelY(target.getY())+yOffset);
				g.draw(curve);
			}
			else {
				paintBranch(g, source, target, yOffset);	
			}
			
		}
	}

	/**
	 * Paint the marginal tree corresponding to the current focal site. We do this by 
	 * grabbing all of the branches as a list (functionality provided by arg), and just
	 * painting a line that connects the nodes for each branch. 
	 * @param g
	 */
	private void paintFocalTree(Graphics2D g) {
		List<Edge> branches = arg.getBranchesForFocalTree();
		g.setStroke(focalStroke);
		g.setColor(focalColor);
		
		for(Edge branch : branches) {
			ARGNode source = branch.getSource();
			ARGNode target = branch.getTarget();
			paintBranch(g, source, target);
			nodesToPaint.add(target);
		}
		
	}
	
	private void paintTipLabel(Graphics2D g, ARGNode tip) {
		int x = toPixelX(tip.getX());
		int y = toPixelY(tip.getY());
		int size = g.getFont().getSize() / 2;
		g.drawString(tip.getID(), x+3, y+size);
	}

	/**
	 * Translate a point in 'FigureElement' coordinates, into a pixel value suitable for passing to a graphics object. 
	 * Passing in a zero here returns the pixel value for the left edge of the element, passing in a one returns a pixel
	 * value for the right edge of the element. 
	 * 
	 * Overrides base class version to provide support for labelSpace
	 * @param x
	 * @return
	 */
	public int toPixelX(double x) {
		return	round((xFactor-labelSpace)*(bounds.x + x*bounds.width));
	}

	
	/**
	 * Paint the given branch and nodes with a y-offset of zero
	 * @param g
	 * @param nodeA
	 * @param nodeB
	 */
	private void paintBranch(Graphics2D g, ARGNode nodeA, ARGNode nodeB) {
		paintBranch(g, nodeA, nodeB, 0);
	}
	
	/**
	 * Paint a single branch connecting the two nodes
	 */
	private void paintBranch(Graphics2D g, ARGNode source, ARGNode target, int yOffset) {
		g.drawLine( toPixelX(source.getX()), toPixelY(source.getY())+yOffset, toPixelX(target.getX()), toPixelY(target.getY())+yOffset);
	}
	
	private void paintNode(Graphics2D g, ARGNode node) {
		if (showRecombNodes && node.hasRecombParent()) {
			g.setColor(nodeColor);
			g.fillRoundRect(toPixelX(node.getX())-3, toPixelY(node.getY())-3, 6, 6, 10, 10);

			g.setColor(Color.black);
			g.setFont(labelFont);
			g.setStroke(normalStroke);
			int bp = node.getBreakPointMin();
			if (bp == arg.getMinSite())
				bp = node.getBreakPointMax();
	
			g.drawString(String.valueOf(bp), toPixelX(node.getX())-2, toPixelY(node.getY())+12);
		}
		if (showInternalNodeLabels) {
			g.setFont(labelFont);
			g.setStroke(normalStroke);			
			int xMod = g.getFontMetrics().stringWidth( node.getID() )/2;
			g.drawString(node.getID(), Math.max(2, toPixelX(node.getX())-xMod), toPixelY(node.getY())-2);

//			String anno = node.getAnnotation();
//			if (anno != null) {
//				g.setColor(Color.GRAY);
//				xMod = g.getFontMetrics().stringWidth( anno )/2;
//				g.drawString(anno, Math.max(2, toPixelX(node.getX())-xMod), toPixelY(node.getY())+ g.getFontMetrics().getHeight());
//			}
			int yStep = 12;
			int yMod = 10;
			g.setFont(annotationFont);
			for(String key : node.getAnnotationKeys()) {
				String val = node.getAnnotation(key);
				String[] lines = val.split("\\n");
				g.drawString(key + " : ", toPixelX(node.getX())-4, toPixelY(node.getY())+yMod);
				for(int i=0; i<lines.length; i++) {
					g.drawString(lines[i], toPixelX(node.getX()), toPixelY(node.getY())+yMod+12);
					yMod += yStep;
				}
			}

		}

	}

	public void setShowRecombNodes(boolean show) {
		showRecombNodes = show;
	}
	
	
	private void fillColors() {
		treePalette = new Color[numColors];

		float rSteps = 4;
		float gSteps = 4;
		float bSteps = 4;
		
		float rVal = 0;
		float gVal = 0;
		float bVal = 0;
		for(int i=0; i<numColors; i++) {
			bVal += 1f/bSteps;
			
			if (bVal>1) {
				bVal = 0;
				gVal += 1f/gSteps;
			}
			
			if (gVal>1) {
				gVal = 0;
				rVal += 1f/rSteps;
			}
			
			if (rVal>1) {
				rVal = 0;
			}
			
			//System.out.println("Colors " + i + " : "+ (rVal+ 0) + "\t" + (gVal+ 0) + "\t" + (bVal+ 0));
			//treePalette[i] = new Color(rVal, gVal, bVal);
			treePalette[i] = new Color(Color.HSBtoRGB((float)i/(float)numColors, 1.0f, 1.0f));
		}
	}

	public void setShowStackedTrees(boolean selected) {
		this.showStackedTrees = selected;
	}

	public void setShowInternalNodeLabels(boolean selected) {
		this.showInternalNodeLabels = selected;
	}

}
