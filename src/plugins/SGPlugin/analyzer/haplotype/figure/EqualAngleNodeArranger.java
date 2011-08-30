package plugins.SGPlugin.analyzer.haplotype.figure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import element.Point;
import guiWidgets.StringUtilities;

import network.NetworkEdge;
import network.Network;
import network.Network2D;
import network.NetworkNode;
import network.Node2D;

/**
 * A type of node arranger that uses the recursive 'equal angle' algorithm to place nodes in space.
 * This will fail (infinite loop-style) if the network contains any cycles. 
 * @author brendan
 *
 */
public class EqualAngleNodeArranger extends AbstractNodeArranger {

	boolean edgeLengthsProportional = true;
	double edgeLengthPerRadius = 0.1;
	
	//This field determines how much sector size increases with distance of a clade from its parent
	private double sectorSizeExpansion = 1.5;
	
	private double maxWeight = Double.NEGATIVE_INFINITY;
	
	private Node2D root = null;
	
	/**
	 * Sets the length of an edge relative to a unit of radius of the nodes.  
	 * @param length
	 */
	public void setEdgeLengthPerRadius(double length) {
		edgeLengthPerRadius = length;
	}
	
	public Node2D getRoot() {
		return root;
	}
	
	@Override
	public void arrangeNodes(Network2D network) {
		nodes = network.getNode2DList();
		
		System.out.println("Laying out nodes with edge length per radius:  " + edgeLengthPerRadius);
		
		//Find network diameter path and pick its central node to start
		List<Node2D> longBranch = getMaxDiameterList();
		
		root = longBranch.get(0);
		if (longBranch.size()>2)
			root = longBranch.get( longBranch.size()/2);
		
		//We need to know the maximum weight for some computations later
		for(NetworkEdge e : network.getAllEdges()) {
			if (e.getWeight() > maxWeight)
				maxWeight = e.getWeight();
		}
		
		root.setX(0.0);
		root.setY(0.0);
		double initAngle = 0;
		
		List<Node2D> neighbors = root.get2DNeighbors();
		Double[] sectorSizes = new Double[neighbors.size()];
		
		//Make one pass over all neighbors to compute all sector sizes
		double sectorSizeSum = 0;
		int biggestCladeIndex = 0;
		int secondBiggestCladeIndex = 0;
		double biggestCladeSectorSize = 0;
		double secondBiggestSectorSize = 0;
		
		//This stores the indices of the neighbors in the order we want to visit, we initialize to 
		//whatever order is in neighbors, but may change it later
		int[] traversalOrder = new int[neighbors.size()];
		
		for(int i=0; i<neighbors.size(); i++) {
			Node2D kid = neighbors.get(i);
			traversalOrder[i] = i;
			sectorSizes[i] = computeSectorSize(root, kid);
			sectorSizeSum += sectorSizes[i];
			if (sectorSizes[i] > biggestCladeSectorSize) {
				secondBiggestCladeIndex = biggestCladeIndex;
				secondBiggestSectorSize = biggestCladeSectorSize;
				biggestCladeIndex = i;
				biggestCladeSectorSize = sectorSizes[i];
			}
		}
		
		
		
		//We'd like big clades to be as widely separated as possible, thus we need to determine the order
		//of clades we place as we go around the circle. How about we put the biggest clade first, and
		//the next biggest clade at position clades/2 ? It only makes sense if there are more than three clades
		if (neighbors.size()>=4) {
			swap(traversalOrder, 0, biggestCladeIndex);
			
			if (secondBiggestCladeIndex==0)
				secondBiggestCladeIndex = biggestCladeIndex;
			
			int midway = neighbors.size()/2;
			swap(traversalOrder, midway, secondBiggestCladeIndex);
		}
		
		
		
		int index = 0;	
		double angleUsed = 0;
		for(int i=0; i<neighbors.size(); i++) {
			index = traversalOrder[i];
			Node2D kid = neighbors.get(index);
			double mySectorSize = sectorSizes[index]/sectorSizeSum * 360.0;
			arrangeNodes( kid, root, initAngle+angleUsed+mySectorSize/2.0, mySectorSize);
			angleUsed += mySectorSize;
			//((HaplotypeElement)kid).setLabel(StringUtilities.format(sectorSizes[index]));
			
			//System.out.println("Root sector #" + index + " raw :" + sectorSizes[index] + " proportional: " + sectorSizes[index]/sectorSizeSum + " angle: " + mySectorSize);
			index++;
		}
		
		((HaplotypeElement)root).setFillColor(Color.red);
	}

	/**
	 * Recursive helper function that lays out nodes in a clade descending from node, not in the direction of parent
	 * @param node
	 * @param parent
	 * @param angle
	 * @param sectorSize
	 */
	private void arrangeNodes(Node2D node, Node2D parent, double angle, double sectorSize) {
		NetworkEdge e = getEdgeForPair(node, parent);
		double dist = 0.05;
		if (e==null)
			throw new IllegalArgumentException("Hap and parent are not joined by an edge!");

		if (edgeLengthsProportional) {
			dist = edgeLengthPerRadius*e.getWeight();
			sectorSize *= Math.min(1.5, 1.0+sectorSizeExpansion*(1.0-Math.exp(-e.getWeight()/maxWeight)));
			//sectorSize *= 1.0+sectorSizeExpansion*(1.0-Math.exp(-e.getWeight()/maxWeight));
		}
		
	
		placeNode(node, parent, angle, dist);
		
		double initialAngle = angle -sectorSize/2.0;
		double angleUsed = 0;
		
		List<Node2D> neighbors = node.get2DNeighbors();
		Double[] sectorSizes = new Double[neighbors.size()-1];
		
		//Make one pass over all neighbors to compute all sector sizes
		int index = 0;
		double sectorSizeSum = 0;
		for(int i=0; i<neighbors.size(); i++) {
			Node2D kid = neighbors.get(i);
			if (kid != parent) {
				sectorSizes[index] = computeSectorSize(node, kid);
				sectorSizeSum += sectorSizes[index];
				index++;
			}
		}
		
		//Make another recursive pass to arrange nodes within each sector
		index = 0;
		for(Node2D kid : neighbors) {
			if (kid != parent) {
				double kidSectorSize = sectorSizes[index]/sectorSizeSum * sectorSize;		
				arrangeNodes( kid, node, initialAngle+angleUsed+kidSectorSize/2.0, kidSectorSize);
				angleUsed += kidSectorSize;
				//((HaplotypeElement)kid).setLabel(StringUtilities.format(sectorSizes[index]));
				index++;
				
			}
		}
	}
	
	
	/**
	 * Computes the size of the sector assigned to the clade defined by the given parent
	 * and its kid. A simple method would be to just count all tips that descend from parent in the direction
	 * of kid. This is actually not so bad,  
	 * @param root
	 * @param kid
	 * @return
	 */
	private double computeSectorSize(Node2D parent, Node2D kid) {
		//Sector size decreases with the length of the edge connecting parent to kid
		//double edgeLength = getEdgeForPair(parent, kid).getWeight();
		double kids = 2+Math.min(10, countTips(kid, parent)) + countAdjacentTips(kid);
		return kids;
	}
	
	/**
	 * Counts the number of nodes adjacent to the given node that have exactly one neighbor
	 * @param node
	 * @return
	 */
	private static int countAdjacentTips(Node2D node) {
		int count = 0;
		for(Node2D kid : node.get2DNeighbors())
			if (kid.get2DNeighbors().size()==1)
				count++;
		return count;
	}
	
	/**
	 * Calculates the position of this node such that its center will be angle degrees 
	 * from vertical away from its parent and the distance separating their EDGE will
	 * be dist. If dist is zero, this should make it so the edges touch each other.  
	 * @param node Node to place
	 * @param parent Parent node, 
	 * @param angle A
	 * @param dist
	 */
	private void placeNode(Node2D node, 
							Node2D parent,
							double angle, 
							double dist) {
		
		double rad1 = node.getWidth()/2.0;
		double rad2 = parent.getWidth()/2.0;
		dist += rad1 + rad2;
		Point pos = translatePoint(parent.getX()+rad2, parent.getY()+rad2, angle, dist);
		node.setX(pos.x); //Sets the center of X
		node.setY(pos.y); //Sets the center of Y
	}
	
	/**
	 * Returns a new point whose position is shifted by the vector given by 
	 * angle and distance
	 * @param start Starting location
	 * @param angle Angle to move point by
	 * @param distance Distance to move point by
	 * @return a Point whose x,y at angle, distance away from the start point
	 */
	private static Point translatePoint(double startX, double startY, double angle, double distance) {
		if (distance==0)
			return new Point(startX, startY);
		angle = angle*0.0174532778; //Convert to radians = pi/180
		Point p = new Point(startX+distance*Math.sin(angle), startY-distance*Math.cos(angle) );
		return p;
	}
	
	
	/**
	 * Swap content a indices a and b in an integer array
	 */
	private static void swap(int[] arr, int a, int b) {
		int tmp = arr[a];
		arr[a] = arr[b];
		arr[b] = tmp;
	}
	
	
	/**
	 * Counts all 'tips' 'descending' from node target, but not traversing in direction of 'parent'
	 * @param target
	 * @param parent
	 * @return Number of tips 
	 */
	private static double countTips(Node2D target, Node2D parent) {
		return countTips(target, parent, 0);
	}
	
	private static double countTips(Node2D target, Node2D parent, double depth) {
		if (target.getEdges().size()==1)
			return 1;
		
		double dist = getEdgeForPair(target, parent).getWeight();
		double count = 0;
		for(NetworkEdge e : target.getEdges()) {
			NetworkNode adjNode = getOtherNode(target, e); //Returns the node that this edge connects to that is not n
			if (adjNode != parent) {
				double val = countTips( (Node2D)adjNode, (Node2D)target, depth + dist);
				count += val;
			}
		}		
		return count;
	}
	
	
	
	
	
	
}
