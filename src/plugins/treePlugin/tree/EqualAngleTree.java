package plugins.treePlugin.tree;

import java.awt.Graphics2D;
import java.util.ArrayList;

import element.Point;

/**
 * A type of drawable tree that arranges it's nodes in "Equal Angle" fashion
 * @author brendan
 *
 */
public class EqualAngleTree extends DrawableTree {

	double branchScale = 0.2; //Branch length units per pixel
	boolean hasBranchLengths = true;
	
	public EqualAngleTree(DrawableNode root) {
		super(root);
		hasBranchLengths = hasBranchLengths();
		calculateNodePositions();
	}	
	
	public void calculateNodePositions() {
		int leafTotal = getNumLeaves();

		//The tree will get shifted around/rescaled anyway so these values aren't really important, but we have to start somewhere
		((DrawableNode)root).setX(0.5);
		((DrawableNode)root).setY(0.5);		
		
		double initialAngle = 0;

		double angleUsed = 0;
		for(Node kid : root.getOffspring()) {
			int kidTips = getNumTips(kid);
			double mySectorSize = kidTips*360.0/leafTotal;
			calculateNodePositions( (DrawableNode)kid, initialAngle+angleUsed+mySectorSize/2.0, mySectorSize, kidTips);
			angleUsed += mySectorSize;
		}
		
		hasCalculatedNodePositions = true;
		rescale();
	}
	
	public DrawableTree clone() {
		DrawableNode newRoot = ((DrawableNode)root).cloneWithDescendants();
		DrawableTree newTree = new EqualAngleTree(newRoot);
		return newTree;
	}
	
	protected void calculateNodePositions(DrawableNode n, double angle, double sectorSize, int tips) {
		Point pos;
		if (hasBranchLengths && ! ignoreBranchLengths)
			pos = translatePoint( ((DrawableNode)n.getParent()).getPosition(), angle,  n.getDistToParent()*branchScale);
		else
			pos = translatePoint( ((DrawableNode)n.getParent()).getPosition(), angle, branchScale );
			
		n.setPosition(pos);
		
		double initialAngle = angle-sectorSize/2.0;
		double angleUsed = 0;
		for(Node kid : n.getOffspring()) {
			int kidTips = getNumTips(kid);
			double mySectorSize = kidTips*sectorSize/tips;			
			calculateNodePositions( (DrawableNode)kid, initialAngle+angleUsed+mySectorSize/2.0, mySectorSize, kidTips);
			angleUsed += mySectorSize;			
		}
	}
	
	
	public void rotateNode(DrawableNode n) {
		if (n.numOffspring()==2) {
			DrawableNode kid1 = (DrawableNode)n.getOffspring().remove(0);
			n.addOffspring(kid1);
			calculateNodePositions(); 
		}
		fireTreeChangedEvent(TreeListener.ChangeType.NODES_MOVED);
	}


	public void rescale() {
		if (! getHasCalculatedNodePositions())
			calculateNodePositions();
		
		double minX = getMinX();
		double minY = getMinY();

		shiftX(-1.0*getMinX());
		shiftY(-1.0*getMinY());
		
		double maxX = getMaxX();
		double maxY = getMaxY();		

		double factor = 1.0/(Math.max(maxX, maxY));

		rescale(factor);
		
		minX = getMinX();
		minY = getMinY();
		maxX = getMaxX();
		maxY = getMaxY();
		
		double midY = (maxY+minY)/2.0;
		double midX = (maxX+minX)/2.0;
		
		shiftX(-midX +0.5);
		shiftY(-midY + 0.5);

		minX = getMinX();
		minY = getMinY();
		maxX = getMaxX();
		maxY = getMaxY();
		System.out.println("Min x: " + minX + " Max x: " + maxX + " min Y: " + minY + " max y: " + maxY );
		
	}
	


	protected void shiftY(double modX) {
		shiftY(modX, (DrawableNode)root);
	}
	
	private void shiftY(double modY, DrawableNode n) {
		n.setY( n.getY()+modY);
		for(Node kid : n.getOffspring()) 
			shiftY(modY, (DrawableNode)kid );
	}
	
	protected void shiftX(double modX) {
		shiftX(modX, (DrawableNode)root);
	}
	
	private void shiftX(double modX, DrawableNode n) {
		n.setX( n.getX()+modX);
		for(Node kid : n.getOffspring()) 
			shiftX(modX, (DrawableNode)kid );
	}

	
	/**
	 * Post-order traverse the tree, multiplying all branch lengths by the branchScale field
	 * @param n
	 */
	protected void rescale(DrawableNode n) {
		for(Node kid : n.getOffspring()) {
			rescale((DrawableNode)kid);
		}
		
		double angle =  angleFromParent(n);
		
		Point newPos;
		if (hasBranchLengths && ! ignoreBranchLengths)
			newPos = translatePoint( ((DrawableNode)n.getParent()).getPosition(), angle,  n.getDistToParent()*branchScale);
		else
			newPos = translatePoint( ((DrawableNode)n.getParent()).getPosition(), angle, branchScale );
		
		newPos.x -= n.getX();
		newPos.y -= n.getY();
		shiftClade(n, newPos);
	}
	
	/**
	 * Shifts a clade by pos.x and pos.y in the horizontal and vertical directions
	 * @param n
	 * @param pos
	 */
	protected void shiftClade(DrawableNode n, Point pos) {
		n.setPosition(new Point(n.getX()+pos.x, n.getY()+pos.y));
		
		for(Node kid : n.getOffspring())
			shiftClade( (DrawableNode)kid, pos);
	}
	
	/**
	 * Multiply all branch lengths by the specified factor
	 * @param newFactor
	 */
	public void rescale(double newFactor) {
		this.branchScale = branchScale*newFactor;
		
		for(Node n : root.getOffspring()) {
			rescale( (DrawableNode)n);
		}
	}
	
	public double getBranchScale() {
		return branchScale;
	}
	
	/**
	 * Angle is in degrees, and is measured counter-clockwise from straight up
	 * @param start
	 * @param angle
	 * @param distance
	 * @return a Point whose x,y at angle, distance away from the start point
	 */
	private Point translatePoint(Point start, double angle, double distance) {
		if (distance==0)
			return start;
		angle = angle*0.0174532778; //Convert to radians = pi/180
		Point p = new Point(start.x+distance*Math.sin(angle), start.y-distance*Math.cos(angle) );
		return p;
	}
}
