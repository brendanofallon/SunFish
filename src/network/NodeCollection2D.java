package network;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic implementation of a collection of nodes and edges, where the nodes 
 * all have a position and size in 2D space.
 * @author brendan
 *
 */
public class NodeCollection2D implements Network2D {

	protected List<Node2D> nodeList = new ArrayList<Node2D>();
	protected List<NetworkEdge> edgeList = new ArrayList<NetworkEdge>();

	/**
	 * Create a new nodeCollection with the given list of Node2D's. Edges are added automagically.
	 * @param nodes
	 */
	public NodeCollection2D(List<Node2D> nodes) {
		for(Node2D node : nodes) {
			List<NetworkEdge> nodeEdges = node.getEdges();
			for(NetworkEdge e : nodeEdges) {
				if (!edgeList.contains(e))
					edgeList.add(e);
			}
			nodeList.add(node);
		}
	}
	

	@Override
	public List<NetworkEdge> getEdges(NetworkNode node) {
		return edgeList;
	}

	@Override
	public List<Node2D> getNode2DList() {
		return nodeList;
	}

	@Override
	/**
	 * Get all Node2D neighbors of the given node
	 */
	public List<Node2D> get2DNeighbors(Node2D node) {
		return node.get2DNeighbors();
//		List<Node2D> neighbors = new ArrayList<Node2D>(5);
//		for(Edge edge : node.getEdges()) {
//			//NetworkNode nodeA = edge.getNodeA();
//			if (edge.getNodeA() != node)
//				neighbors.add((Node2D)edge.getNodeA());
//			else
//				neighbors.add((Node2D)edge.getNodeB());
//			
//		}
//		return neighbors;
	}

	
	
	//////////// Implementation of Network interface //////////////////////////////
	
	@Override
	public List<NetworkEdge> getAllEdges() {
		return edgeList;
	}
	
	@Override
	/**
	 * Obtain all nodes in this collection as a list of NetworkNodes
	 */
	public List<NetworkNode> getAllNodes() {
		List<NetworkNode> nodes = new ArrayList<NetworkNode>();
		nodes.addAll(nodeList);
		return nodes;
	}
	

	/**
	 * Return all neighbors of the given node
	 */
	public List<NetworkNode> getNeighbors(NetworkNode node) {
		List<NetworkNode> neighbors = new ArrayList<NetworkNode>(5);
		for(NetworkEdge edge : node.getEdges()) {
			if (edge.getNodeA() != node)
				neighbors.add(edge.getNodeA());
			else
				neighbors.add(edge.getNodeB());
			
		}
		return neighbors;
	}
	
	/**
	 * Return the maximum X-value of all nodes
	 * @return
	 */
	public double getMaxX() {
		double max = 0;
		for(Node2D node : nodeList) {
			if (node.getX() > max) 
				max = node.getX();
		}
		return max;
	}
	
	/**
	 * Return the maximum Y-value of all nodes
	 * @return
	 */
	public double getMaxY() {
		double max = 0;
		for(Node2D node : nodeList) {
			if (node.getY() > max) 
				max = node.getY();
		}
		return max;
	}
	
	/**
	 * Return the minimum X-value of all nodes
	 * @return
	 */
	public double getMinX() {
		double min = Double.POSITIVE_INFINITY;
		for(Node2D node : nodeList) {
			if (node.getX() < min) 
				min = node.getX();
		}
		return min;
	}
	
	/**
	 * Return the maximum X-value of all nodes
	 * @return
	 */
	public double getMinY() {
		double min = Double.POSITIVE_INFINITY;
		for(Node2D node : nodeList) {
			if (node.getY() < min) 
				min = node.getY();
		}
		return min;
	}
	
	/**
	 * Subtracts getMinX from the x-values of all nodes and getMinY from the y-positions of all nodes
	 */
	public void shiftToOrigin() {
		double minX = getMinX();
		double minY = getMinY();
		
		for(Node2D node : nodeList) {
			node.setY( node.getY() - minY );
			node.setX( node.getX() - minX );
		}
	}
	
	/**
	 * Multiply all x and y positions by the given amount
	 * @param factor
	 */
	public void rescale(double factor) {
		for(Node2D node : nodeList) {
			node.setY( node.getY()*factor );
			node.setX( node.getX()*factor );
		}
	}
	
	/**
	 * Multiply all X positions by the given factor
	 * @param factor
	 */
	public void scaleX(double factor) {
		for(Node2D node : nodeList) {
			node.setX( node.getX()*factor );
		}
	}
	
	/**
	 * Multiply all Y positions by the given factor
	 * @param factor
	 */
	public void scaleY(double factor) {
		for(Node2D node : nodeList) {
			node.setY( node.getY()*factor );
		}
	}
	
	/**
	 * Shift all nodes in the x direction by the given amount
	 */
	public void shiftX(double delta) {
		for(Node2D node : nodeList) {
			node.setX( node.getX() + delta );
		}
	}
	
	/**
	 * Shift all nodes in the y direction by the given amount
	 */
	public void shiftY(double delta) {
		for(Node2D node : nodeList) {
			node.setY( node.getY() + delta );
		}
	}
	
}
