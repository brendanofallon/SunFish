package plugins.SGPlugin.analyzer.haplotype.figure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import display.Display;

import plugins.SGPlugin.analyzer.haplotype.Haplotype;
import plugins.SGPlugin.analyzer.haplotype.HaplotypeNetwork;
import plugins.SGPlugin.display.SGContentPanelDisplay;

import element.Point;
import figure.Figure;

import network.NetworkEdge;
import network.Network2D;
import network.NetworkNode;
import network.Node2D;
import network.NodeArranger;
import network.NodeCollection2D;


/**
 * Draws a haplotype network to the screen. Each haplotype and
 * edge are represented by an element (HaplotypeElement and HapEdgeElement). This class
 * uses a NodeArranger to handle the arrangement of nodes, although it retains a number
 * of methods that are used for recentering / zooming of the figure. 
 * 
 * This class is set up to swap in other NodeArrangers that can lay out the nodes in 
 * space according to different algorithms. The current arranger is a variant of the Equal-Angle
 * method that is used to plot trees. Swapping in other types of arrangers is easy, just reassign
 * arranger (in the constructor) to be something else. 
 *  
 * 
 *    
 * @author brendan
 *
 */
public class HaplotypeNetworkFigure extends Figure {

	public static final String NONE = "None";
	public static final String TICKS = "Ticks";
	public static final String DISTANCES = "Distance";
	
	HaplotypeNetwork hapNet = null;
	ArrayList<HaplotypeElement> hapElements;
	ArrayList<HapEdgeElement> edgeElements;
	
	boolean edgeLengthProportional = true;
	Color initMarkerColor = Color.blue;
	double initMarkerSize = 1.0;
	
	Display sourceDisplay;
	
	double maxCardinality = 0; //The max cardinality of all haplotypes
	double maxDist = 0;	//The max distance between any two haplotypes
	double initMaxRadius = 4; //So the width of the biggest marker will be 10% of the whole area
	
	double defaultEdgeLengthPerRadius; //Used to set how big marker sizes are per edge length
	double edgeLengthMultiplier = 1.0; //A user-settable multiplier for branch length / marker size ratio
	double adjustedEdgeLengthPerRadius; //The actual edge length multiplier, taking into account the base value and the user factor
	
	boolean recentered = false;
	boolean optimallyRotated = false; //Just happens once, at beginning
	
	double currentRescaleValue = 1.0;
	
	double currentRotationValue = 0.0; //Amount to rotate, in radians
	
	HapElementConfigFrame configFrame;
	
	//A transformation object we use to compute some of the rotation calculations..
	AffineTransform transform = new AffineTransform();

	//A couple of 'working nodes' that are used in the moveNodesDownhill machinery, we
	//don't want to constantly reallocate these
	Point2D p1 = new Point2D.Double();
	Point2D p2 = new Point2D.Double();
	
	Network2D nodeNetwork; //Collection of nodes that are passed to the arranger for layout
	EqualAngleNodeArranger arranger;
	
	public HaplotypeNetworkFigure(Display source) {
		super();
		this.sourceDisplay = source;
		hapElements = new ArrayList<HaplotypeElement>();
		edgeElements = new ArrayList<HapEdgeElement>();
		setRectangleSelection(true); //Turn on rectangle selection
		
		arranger = new EqualAngleNodeArranger();
		super.setAllowClicksOnMultipleSelection(true);
	}

	public Display getDisplay() {
		return sourceDisplay;
	}
	
	/**
	 * Set the network associated with this figure. This creates a new Network2D
	 * @param hapNet
	 */
	public void setNetwork(HaplotypeNetwork hapNet) {
		this.hapNet = hapNet;
		maxCardinality = 0;
		maxDist = 0;
		
		//Go through all haplotypes and create haplotypeElements for each one
		ArrayList<NetworkNode> haps = hapNet.getAllNodes();
		for(NetworkNode node : haps) {
			Haplotype hap = (Haplotype)node;
			if (hap.getCardinality()>maxCardinality)
				maxCardinality = hap.getCardinality();
			HaplotypeElement hapEl = new HaplotypeElement(this, hap);
			hapEl.setScale(1.0, 1.0, null); //We need some sort of values for xFactor and yFactor to calculate node positions, since they depend on marker size
			super.addElement(hapEl);
			hapElements.add(hapEl);
			hapEl.setCanConfigure(true);
		}
		
		//Go through all possible pairs of haplotypes and, if any two are connected, create a new
		//edge element that draws the connection
		for(int i=0; i<hapElements.size(); i++) {
			for(int j=i+1; j<hapElements.size(); j++) {
				if (areConnected(hapElements.get(i), hapElements.get(j))) {
					double dist = hapNet.getEdgeForPair(hapElements.get(i).getHap(), hapElements.get(j).getHap()).getWeight();
					HapEdgeElement edgeEl = new HapEdgeElement(this, hapElements.get(i), hapElements.get(j), (int)dist );
					if (maxDist < dist) {
						maxDist = dist;
					}
					edgeElements.add(edgeEl);
					edgeEl.setZPosition(-5); //paint these underneath
					super.addElement(edgeEl);
				}
			}
		}	
		
		//We need to make an educated guess regarding the appropriate scaling for marker sizes
		// vs. edge lengths. The following value multiplies all edge lengths during node
		//layout, so it makes things leggier and nodes smaller. 
		//If edge lengths and haplotype cardinalities are comparable, then this number should be about
		//one. If cardinalities are big and edge lengths short (low theta-scenario), then this number should
		//be about ten
		defaultEdgeLengthPerRadius =  2*maxCardinality / maxDist; //This should be small if edge lengths are very long
		adjustedEdgeLengthPerRadius = defaultEdgeLengthPerRadius * edgeLengthMultiplier;
		
		arranger.setEdgeLengthPerRadius(adjustedEdgeLengthPerRadius);
	
		for(HaplotypeElement hapEl : hapElements) {
			hapEl.setRadius( Math.sqrt((double)hapEl.getHap().getCardinality()));
			hapEl.setBaseRadius( Math.sqrt((double)hapEl.getHap().getCardinality()));
		}
		
		//The NodeArranger requires that nodes are in a list of type Node2D...
		List<Node2D> nodes = new ArrayList<Node2D>(hapElements.size());
		nodes.addAll(hapElements);
		nodeNetwork = new NodeCollection2D(nodes);
		
		setMarkerColor(initMarkerColor);
		//setMarkerSizeFactor(initMarkerSize); //This calls arrange and rescale(), so we don't need to do it again
		
		recentered = false;
	}

	/**
	 * Returns true if an edge joins the two given haplotype elements
	 * @param aEl
	 * @param bEl
	 * @return
	 */
	private static boolean areConnected(HaplotypeElement aEl, HaplotypeElement bEl) {
		Haplotype a = aEl.getHap();
		Haplotype b = bEl.getHap();
		for(NetworkEdge e : a.getEdges()) {
			if ((e.getNodeA()==a && e.getNodeB()==b)||(e.getNodeA()==b && e.getNodeB()==a))
				return true;
		}
		return false;
	}
	
	public void selectionRectUpdated(Rectangle selRect) {	
		double min = Math.min(getWidth(), getHeight());
//		double xMod = getWidth()/min;
//		double yMod = getHeight()/min;
		for(HaplotypeElement hapEl : hapElements) {
			if (selRect.contains( hapEl.getCenterX()*min, hapEl.getCenterY()*min )) {
				hapEl.setSelected(true);
			}
			else {
				hapEl.setSelected(false);
			}
		}
	}

	public void setRotationValue(double newVal) {
		//System.out.println("Setting rotation to : " + newVal);
		double dif = newVal - currentRotationValue;
		rotateEverything(dif);
		currentRotationValue = newVal;
	}
	
	/**
	 * Computes the distance between two haplotype elements
	 * @param one
	 * @param two
	 * @return
	 */
	private double hapDist(HaplotypeElement one, HaplotypeElement two) {
		return Math.sqrt( (one.getCenterX()-two.getCenterX())*(one.getCenterX()-two.getCenterX()) + (one.getCenterY()-two.getCenterY())*(one.getCenterY()-two.getCenterY())); 
	}
	
	
	/**
	 * Finds the two nodes that are the farthest apart, and then rotates all nodes so that the two nodes 
	 * are aligned along the x or y axis, whichever is longer. 
	 */
	private void rotateOptimally() {
		
		if (hapElements.size()<2) {
			return;
		}
		
		HaplotypeElement one = hapElements.get(0);
		HaplotypeElement two = hapElements.get(1);
		double currentMax = hapDist(one, two);
		for(int i=0; i<hapElements.size(); i++) {
			for(int j=i+1; j<hapElements.size(); j++) {
				double dist = hapDist(hapElements.get(i), hapElements.get(j));
				if (dist>currentMax) {
					currentMax = dist;
					one = hapElements.get(i);
					two = hapElements.get(j);
				}
			}
		}
		
		
		
		double h = Math.sqrt( (one.getX()-two.getX())*(one.getX()-two.getX()) + (one.getY()-two.getY())*(one.getY()-two.getY()) );
		double currentAngle = Math.acos( (one.getX()-two.getX())/h );
		if ( (one.getY() < two.getY()) || (one.getX() > two.getX()) )
			currentAngle *= -1;
		
		if ( (one.getX()>two.getX()) && one.getY() > two.getY()); 
			currentAngle *= -1;
		
		
		currentAngle *= 180 / Math.PI;
		System.out.println("Rotating by " + currentAngle + " degrees.");
		if (getWidth() > getHeight()) { //Rotate so one and two lie along x-axis	
			setRotationValue(currentAngle); //Calling rotate everything means we don't update currentRotationValue, so this is the new 'default'
		}
		else 
			setRotationValue(currentAngle);
		
		double pix = Math.min(getWidth(), getHeight());
		if (pix>0) {
			
			double xFactor = getMaxX()*(double)getWidth();
			double yFactor = getMaxY()*(double)getHeight();
			double val = pix/Math.min(xFactor, yFactor);
			//System.out.println("pix: " + pix + " Rescaling by " + val);
			optimallyRotated = true;
			rescale( val );
		}
		else {
			//System.out.println("Skipping call to rotate optimally because getWidth is not known");
		}
	}
	
	/**
	 * Returns a new point that whose position has been rotated about the centerX and centerY args
	 * by angle degrees. 
	 * 
	 * @param angle
	 * @param p
	 * @param centerX
	 * @param centerY
	 * @return the new point
	 */
	private void rotatePoint(double angle, Point2D p, double centerX, double centerY, Point2D dest) {
		double radAngle = angle*0.0174532925; // number = pi / 180
		transform.setToRotation(radAngle, centerX, centerY);
		transform.transform(p, dest);
	}
	
	/**
	 * Describes how much repelling "force" a node exerts at a certain distance
	 * @param dist
	 * @return
	 */
	private double forceKernel(double dist) {
	//	return 50.0*Math.exp(-50.0*dist);
		return 1.0 / (dist);
	}
	
	/**
	 * Calculates the sum of the force kernel at a specific point over all haplotypes
	 * @param x
	 * @param y
	 * @return The sum of all forces affecting this point
	 */
	private double pointForce(double x, double y, HaplotypeElement hapToSkip) {
		double sum = 0;
		for(HaplotypeElement hapEl : hapElements) {
			if (hapEl != hapToSkip) {
				double dx = (hapEl.getCenterX()-x);
				double dy = (hapEl.getCenterY()-y);
				double dist = Math.sqrt( dx*dx+dy*dy );
				if (dist>0)
					sum += forceKernel(dist);
			}
		}
		return sum;
	}
	
	/**
	 * Returns the sum of the force acting on all members of the clade.
	 * This also computes force among members in the clade 
	 * @param clade
	 * @return
	 */
	private double cladeForce(List<Node2D> clade) {
		double sum = 0;
		for(Node2D node : clade) {
			sum += pointForce(node.getX()+node.getWidth()/2.0, node.getY() + node.getWidth()/2.0, null);
		}
		return sum;
	}
	
	/**
	 * Returns all 'descendants' of node, including node, in the direction away from parent
	 * in the given list
	 * @param node Node at 'root' of clade
	 * @param parent 
	 * @return
	 */
	private void collectClade(Node2D node, Node2D parent, List<Node2D> clade) {
		clade.add(node);
		for(Node2D kid : node.get2DNeighbors())
			if (kid != parent)
				collectClade(kid, node, clade);
	}
	
	/**
	 * Rotates all descendants of the given node (but not in the direction of parent)
	 * by the given number of degrees
	 * @param node
	 * @param parent
	 * @param angle
	 */
	private void rotateClade(Node2D node, Node2D parent, double angle) {
		List<Node2D> clade = new ArrayList<Node2D>();
		collectClade(node, parent, clade);
		rotateClade(clade, angle, node.getX()+node.getWidth()/2.0, node.getY()+node.getWidth()/2.0);
	}
	
	/**
	 * Rotate all members of the given list by angle radians about the given center point 
	 * @param clade
	 * @param angle
	 * @param anchorX
	 * @param anchorY
	 */
	private void rotateClade(List<Node2D> clade, double angle, double anchorX, double anchorY) {
		AffineTransform transform = AffineTransform.getRotateInstance(angle, anchorX, anchorY);
		Point2D src = new Point2D.Double(0, 0);
		Point2D dst = new Point2D.Double(0, 0);
		for(Node2D n : clade) {
			src.setLocation(n.getX() + n.getWidth()/2.0, n.getY()+n.getWidth()/2.0);
			transform.transform(src, dst);
			n.setX(dst.getX());
			n.setY(dst.getY());
		}
	}
	
	/**
	 * Attempts to move this node away from other nodes by calculating the amount of 'force' pushing the node, 
	 * and then moving this node in a direction that minimizes it. We do this stupidly, by just checking 
	 * the amount of force both a little bit left and a little bit right of where we currently are, and moving
	 * to one of the two new spots if it's more minimal than where we currently are. 
	 * @param node Node to move downhill
	 * @param parent The node's parent (we need this to calculate the current parent angle of the node)
	 */
	private void moveNodeDownhill(HaplotypeElement node, HaplotypeElement parent) {
		//double angleFromParent = Math.atan2( (parent.getCenterY()-node.getCenterY()), parent.getCenterX()-node.getCenterX());
		Point2D nodePos = new Point2D.Double(node.getCenterX(), node.getCenterY());

		double currentForce = pointForce(node.getCenterX(), node.getCenterY(), node);
		
		//System.out.println("\n\n Node pos: " + node.getCenterX() + ", " + node.getCenterY() + " current force: " + currentForce );
		rotatePoint(-2.0, nodePos, parent.getCenterX(), parent.getCenterY(), p1 );
		double force1 = pointForce(p1.getX(), p1.getY(), node);
		//System.out.println("Test 1 pos: " + p1.getX() + ", " + p1.getY() + " force : " + force1 );
		rotatePoint(2.0, nodePos, parent.getCenterX(), parent.getCenterY(), p2 );
		double force2 = pointForce(p2.getX(), p2.getY(), node);
		//System.out.println("Test 2 pos: " + p2.getX() + ", " + p2.getY() + " force : " + force2 );
		
		if (force1>force2) {
			if (currentForce>force2) {
				node.setCenterPosition(p2.getX(), p2.getY());
				//System.out.println("Moving to pos 2");
			}
			else {
				//currentForce < force2 and force2 < force1, so no movement is optimal
				//do nothing
				//System.out.println("Not moving");
			}
		}
		else { //force1 < force2
			if (force1<currentForce) { 
				node.setCenterPosition(p1.getX(), p1.getY());
				//System.out.println("Moving to pos 1");
			}
			else {
				 // force2 > force1 and force1 > currentForce, so current force is optimal
				//System.out.println("Not moving");
			}
		}
		
	}
	
	/**
	 * Rotate all nodes around the center by the given angle, in general the outside world should
	 * use setRotationValue( ) to rotate the figure, since that calls this and also sets the 
	 * currentRotationValue field
	 * @param angle
	 */
	private void rotateEverything(double angle) {
		
		double centerX = (getMaxX()+getMinX())/2.0;
		double centerY = (getMaxY()+getMinY())/2.0;
		double radAngle = angle*0.0174532925; // = pi / 180, converts from degrees to radians

		for(HaplotypeElement hapEl : hapElements) {
			double h = Math.sqrt( (hapEl.getX()-centerX)*(hapEl.getX()-centerX) + (hapEl.getY()-centerY)*(hapEl.getY()-centerY)  );
			double currentAngle = Math.acos( (hapEl.getX()-centerX)/h );
			if ( (hapEl.getY() < centerY) || (hapEl.getX() > centerX) )
				currentAngle *= -1;
			
			if ( (hapEl.getX()>centerX) && hapEl.getY() > centerY) 
				currentAngle *= -1;
			
			double newX = centerX + h*Math.cos( radAngle+currentAngle );
			double newY = centerY + h*Math.sin( radAngle+currentAngle );
			
			hapEl.setPosition(newX, newY);	
		}
		
		repaint();
	}
	
	/**
	 * Set whether or not to draw tick marks indicating distance between haplotypes
	 * @param drawTicks
	 */
	public void setDrawTicks(boolean drawTicks) {
		for(HapEdgeElement edge : edgeElements) {
			edge.setDrawTicks(drawTicks);
		}
	}
	
	public Color getInitMarkerColor() {
		return initMarkerColor;
	}

	public void setMarkerColor(Color c) {
		for(HaplotypeElement hapEl : hapElements) {
			hapEl.setFillColor(c);
		}
	}
	
	
	public void showConfigTool() {
		List<HaplotypeElement> selectedElements = new ArrayList<HaplotypeElement>();
		for(HaplotypeElement hapEl : hapElements) {
			if (hapEl.isSelected()) selectedElements.add(hapEl);
		}
		
		SGContentPanelDisplay sourceD = null;
		try {
			sourceD = (SGContentPanelDisplay)getDisplay();	
		}
		catch (Exception ex ){
			//Apparently we can't find the source sg display... this is probably OK. 
		}

		configFrame = new HapElementConfigFrame(selectedElements, sourceD, this);
		configFrame.setVisible(true);
	}
	
	

	/**
	 * Move all elements up and left as much as possible
	 */
	private void shiftToZero() {
		
		double minX = getMinX();
		double minY = getMinY();
		shiftAllX(-(minX*0.98));	//We actually don't want to shift all the way to the edge
		shiftAllY(-(minY*0.98));
	}
	
	/**
	 * If the elements have been recentered since the most recent call to arrangeNode to rescale
	 * @return
	 */
	public boolean getRecentered() {
		return recentered;
	}
	
	/**
	 * Shifts the graph so the average x is at xMiddle and the average y is at yMiddle
	 * @param xMiddle
	 * @param yMiddle
	 */
	public void recenter(double xMiddle, double yMiddle) {
		
//		if (!optimallyRotated)
//			rotateOptimally();
		
		double maxX = getMaxX();
		double maxY = getMaxY();

		double minX = getMinX();
		double minY = getMinY();

		shiftAllX(xMiddle-(maxX+minX)/2.0);
		shiftAllY(yMiddle-(maxY+minY)/2.0);		
		
		recentered = true;
		repaint();
	}
	
	/**
	 * Calls moveNodeDownhill for all elements which are connected to only one other element,
	 * the current implementation is really, really inefficient since we have to look up
	 * adjacencies whenever we want to find a node's neighbor....
	 */
	public void moveAllTipsDownhill() {
		for(int i=0; i<15; i++) {
			for(HaplotypeElement hapEl : hapElements) {
				if (hapEl.getHap().getNumNeighbors()==1) {
					HaplotypeElement parent = getElementForHap( hapEl.getHap().getNeighbors().get(0).neighbor );
					moveNodeDownhill(hapEl, parent);
				}
			}
			repaint();
		}
	}
	
	/**
	 * Rotate all members of the clade about the center of hapEl a small amount in the direction
	 * that reduces the some of the forces (inversely proportional to distance) acting on all
	 * members of the clade. 
	 * @param hapEl Node that defines center point and root of clade
	 * @param parent Parent of focal node
	 */
	public void moveCladeDownhill(HaplotypeElement hapEl, HaplotypeElement parent) {
		double stepSize = 0.04; //In radians
		
		List<Node2D> clade = new ArrayList<Node2D>();
		collectClade(hapEl, parent, clade);
		double currentForce = cladeForce(clade);
		
		rotateClade(clade, stepSize, hapEl.getCenterX(), hapEl.getCenterY());
		
		double positiveForce = cladeForce(clade);
		
		rotateClade(clade, -2*stepSize, hapEl.getCenterX(), hapEl.getCenterY());
		
		double negForce = cladeForce(clade);
		
		if (positiveForce < currentForce) {
			
			if (negForce < positiveForce) {
				//Neg force is lowest and we're already there				
			}
			else {
				//Positive force is lowest, rotate back to that state
				rotateClade(clade, 2*stepSize, hapEl.getCenterX(), hapEl.getCenterY());
			}
		}
		else { //Positive force is greater than currentForce
			if (negForce < currentForce) {
				//Neg force is lowest,
			}
			else {
				//Current force is lowest, rotate to that state
				rotateClade(clade, stepSize, hapEl.getCenterX(), hapEl.getCenterY());
			}
		}
		//hapEl.setLabel(String.valueOf(cladeForce(clade)));
	}
	
	/**
	 * Rescale the size of everything by currentRescaleValue
	 */
	public void rescale() {
		rescale(currentRescaleValue);
	}
	
	/**
	 * Scale everything by multiplying all edge lengths and radii by the given factor. This
	 * also forces a recentering of the image.  
	 * @param sizeMultiplier
	 */
	public void rescale(double sizeMultiplier) {
		shiftToZero();
		double maxX = getMaxX()*1.1;
		double maxY = getMaxY()*1.1;
		currentRescaleValue = sizeMultiplier;
		
		double factor = sizeMultiplier/Math.max(maxX, maxY);
		
		rescalePositions(factor, factor);

		//System.out.println("Size multiplier: " + sizeMultiplier + " max: " + Math.max(maxX, maxY) + " factor: " + factor);
		//System.out.println("Previous factor: " + currentRescaleValue + " new factor: " + factor + " ratio: " + factor/currentRescaleValue);
		for(HaplotypeElement hapEl : hapElements) {
			double newRadius = hapEl.getRadius()*factor;
			hapEl.setRadius( newRadius );
		}
		
		recenter(0.5, 0.5);
		recentered = false;
		
		repaint();
	}
	
//	private void emitPositions() {
//		for(HaplotypeElement hapEl : hapElements) {
//			System.out.println("Hap #" + hapEl.getHap().getIndex() + " pos x: " + hapEl.getX() + " width: " + hapEl.getWidth() + " right edge: " +  (hapEl.getX() + hapEl.getWidth()) + "y: " + hapEl.getY() + " bottom: " + (hapEl.getY() + hapEl.getHeight()));
//		}
//	}
	
	/**
	 * Get the minimum x-value of all elements
	 */
	private double getMinX() {
		double minX = Double.MAX_VALUE;
		for(HaplotypeElement hapEl : hapElements) {
			if (hapEl.getX()< minX)
				minX = hapEl.getX();
		}
		return minX;
	}
	
	private double getMinY() {
		double minY = Double.MAX_VALUE;
		for(HaplotypeElement hapEl : hapElements) {
			if (hapEl.getY() < minY)
				minY = hapEl.getY();
		}
		return minY;
	}
	
	private double getMaxX() {
		double maxX = Double.MIN_VALUE;
		for(HaplotypeElement hapEl : hapElements) {
			if (hapEl.getX()+hapEl.getWidth() > maxX)
				maxX = hapEl.getX()+hapEl.getWidth();
		}
		return maxX;
	}
	
	private double getMaxY() {
		double maxY = Double.MIN_VALUE;
		for(HaplotypeElement hapEl : hapElements) {
			if ((hapEl.getY()+hapEl.getWidth()) > maxY)
				maxY = hapEl.getY()+hapEl.getHeight();
		}
		return maxY;
	}
	
	/**
	 * Sets the relative marker size factor - that is, how big the radius of a marker is
	 * relative to a unit of edge length in the figure
	 * @param factor
	 */
	public void setMarkerSizeFactor(double factor) {
		factor = Math.exp( 3*(factor-0.5) );
		System.out.println("Setting marker size factor to: " + factor);
		
		if (factor > 0 && factor < Double.POSITIVE_INFINITY) {
			adjustedEdgeLengthPerRadius = defaultEdgeLengthPerRadius * factor+1e-10;
			System.out.println("Edge length factor : " + adjustedEdgeLengthPerRadius);
			if (arranger instanceof EqualAngleNodeArranger) {
				((EqualAngleNodeArranger)arranger).setEdgeLengthPerRadius(adjustedEdgeLengthPerRadius);
			}

			layoutNodes();
			rotateEverything(currentRotationValue);
			rescale();
		}
	}
	
	/**
	 * Conduct the primary node arrangement algorithm, using the NodeArranger to perform the
	 * first phase of the algorithm, and then a subsequent modification that moves clades 
	 * away from one another.  
	 */
	private void layoutNodes() {
		arranger.arrangeNodes(nodeNetwork);
		//moveAllTipsDownhill();
		for(int i=0; i<10; i++)
			rotateAllCladesDownhill();
		recentered = false;
	}



	/**
	 * An inefficient way of finding the haplotypeElement associated with a haplotype. These
	 * should probably be stored in a Map, since we call this often enough to make it slow, but the
	 * map wouldn't be that big.
	 * @param adjNode
	 * @return
	 */
	private HaplotypeElement getElementForHap(Haplotype adjNode) {
		for(HaplotypeElement hapEl : hapElements) {
			if (hapEl.getHap() == adjNode)
				return hapEl;
		}
		return null;
	}


	

	/**
	 * Set mobility of all nodes to the provided value
	 * @param isMobile
	 */
	public void setNodeMobility(boolean isMobile) {
		for(HaplotypeElement hel : hapElements) {
			hel.setMobile(isMobile);
		}
	}

	public void setEdgeLengthsProportional(boolean prop) {
		edgeLengthProportional = prop;
		layoutNodes();
		rescale();
	}
	
	public boolean getEdgeLengthsProportional() {
		return edgeLengthProportional;
	}

	public void setTickState(String item) {
		if (item == NONE) {
			for(HapEdgeElement edge : edgeElements) {
				edge.setShowDistance(false);
				edge.setDrawTicks(false);
			}
		}
		
		if (item == TICKS) {
			for(HapEdgeElement edge : edgeElements) {
				edge.setShowDistance(false);
				edge.setDrawTicks(true);
			}
		}
		
		if (item == DISTANCES) {
			for(HapEdgeElement edge : edgeElements) {
				edge.setShowDistance(true);
				edge.setDrawTicks(false);
			}
		}
		repaint();
	}

	/**
	 * Debugging function that looks for a selected node and then rotates it once
	 */
	public void rotateAllCladesDownhill() {
		//rotateSelectedClade((HaplotypeElement)arranger.getRoot(), null);
		HaplotypeElement root = (HaplotypeElement)arranger.getRoot();
		for(Node2D kid : root.get2DNeighbors()) {
			rotateClade(kid, root);
		}
	}
	
	/**
	 * Post-order traverse into structure from node away from parent, rotating entire clades
	 * and nodes in a direction that preserves distance between connected nodes but maximizes
	 * distance between unconnected nodes
	 * 
	 * @param node
	 * @param parent
	 */
	private void rotateClade(Node2D node, Node2D parent) {
		for(Node2D kid : node.get2DNeighbors()) {
			if (kid != parent)
				rotateClade(kid, node);
		}
		if (node.get2DNeighbors().size()==1)
			moveNodeDownhill((HaplotypeElement)node, (HaplotypeElement)parent);
		else
			moveCladeDownhill((HaplotypeElement)node, (HaplotypeElement)parent);
	}
	
	public void rotateSelectedClade(HaplotypeElement hap, HaplotypeElement parent) {
		if (hap.isSelected()) {
//			rotateClade(hap, parent, 0.1);
			moveCladeDownhill(hap, parent);
			return;
		}
		for(Node2D kid : hap.get2DNeighbors()) {
			if (kid != parent)
				rotateSelectedClade((HaplotypeElement)kid, hap);
		}
	}
}
