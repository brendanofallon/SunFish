package plugins.SGPlugin.analyzer.haplotype.figure;

import java.util.ArrayList;
import java.util.List;

import network.NetworkEdge;
import network.Network2D;
import network.NetworkNode;
import network.Node2D;
import network.NodeArranger;

/**
 * Abstract base class of things that place the nodes of a network in space. Assumes the list of
 * all nodes is in the field 'nodes', and provides several methods of general usefulness for dealing 
 * with the collection of nodes. 
 * 
 * @author brendan
 *
 */
public abstract class AbstractNodeArranger implements NodeArranger {

	List<Node2D> nodes;
	
	/**
	 * Return the edge that joins nodeA and nodeB, or null if no such edge exists
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	protected static NetworkEdge getEdgeForPair(Node2D nodeA, Node2D nodeB) {
		for(NetworkEdge e : nodeA.getEdges()) {
			NetworkNode nodeOne = e.getNodeA();
			NetworkNode nodeTwo = e.getNodeB();
			if ((nodeOne==nodeA && nodeTwo==nodeB)||(nodeTwo==nodeA && nodeOne==nodeB)) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of nodes that connect nodeA and nodeB
	 * @param nodeA Start of branch
	 * @param nodeB End of branch
	 * @return
	 */
	private List<Node2D> getConnectionList(Node2D nodeA, Node2D nodeB) {
		for(NetworkEdge e : nodeA.getEdges()) {
			NetworkNode adjNode = getOtherNode(nodeA, e);
			List<Node2D> branch = listToTarget((Node2D)adjNode, nodeA, nodeB);
			if ( branch != null ) {
				branch.add((Node2D)adjNode);
				branch.add(nodeA);
				return branch;
			}
		}
		throw new IllegalArgumentException("NodeA and NodeB do not appear to be connected");
	}
	
	/**
	 * Recursive function that searches the network to find a path going from node to target, and 
	 * returns a list of nodes that describes the path. 
	 * @param node Start of list
	 * @param parent not going in this direction
	 * @param target End of list
	 * @return
	 */
	private List<Node2D> listToTarget(Node2D node, Node2D parent, Node2D target) {
		List<Node2D> list = null; 
		//First scan to see if the target is anywhere ....
		for(NetworkEdge e : node.getEdges()) {
			NetworkNode adjNode = getOtherNode(node, e); //Returns the node that this edge connects to that is not n
			if (adjNode == target) { 
				list = new ArrayList<Node2D>(4);
				list.add((Node2D)adjNode);
				return list;
			}
		}
		
		///...and if not found traverse deeper into structure
		for(NetworkEdge e : node.getEdges()) {
			NetworkNode adjNode = getOtherNode(node, e); //Returns the node that this edge connects to that is not n
			if (adjNode != parent) {
				List<Node2D> branch = listToTarget((Node2D)adjNode, node, target);
				if ( branch != null) {
					list = branch; 
					list.add((Node2D)adjNode);
				}
			}				
		}
		
		//if we're here, we must've reached a tip, return null to poison this branch
		return list;
	}
	
	/**
	 * Returns a list of adjacent nodes such that the longest possible path among all possible nodes is found.
	 * 
	 * @return A list of nodes 
	 */
	protected List<Node2D> getMaxDiameterList() {
		List<Node2D> tips = new ArrayList<Node2D>();
		for(Node2D node : nodes) {
			if (node.getEdges().size()==1) {
				tips.add(node);
			}
		}
		
		double maxDist = 0;
		int maxI = -1;
		int maxJ = -1;
		List<Node2D> longestBranch = null;
		for(int i=0; i<tips.size(); i++) {
			for(int j=i+1; j<tips.size(); j++) {
				List<Node2D> list = getConnectionList(tips.get(i), tips.get(j));
				double dist = getBranchLength(list);
				//System.out.println("Found branch starting from node : " + ((HaplotypeElement)tips.get(i)).label);
				//emitBranch(list);
				if (dist > maxDist) {
					maxDist = dist;
					longestBranch = list;
					maxI = i;
					maxJ = j;
				}
			}
		}
		
		
//		System.out.println("\nFound longest branch connecting tips " + ((HaplotypeElement)tips.get(maxI)).label + " to " + ((HaplotypeElement)tips.get(maxJ)).label);
//		emitBranch(longestBranch);
		
		return longestBranch;
	}
	
	/**
	 * List is assumed to contain a list of nodes where node i and i+1 are connected by an edge for all i
	 * This traverses the list once and adds the weight of each edge connecting the nodes and returns it
	 * @param branch
	 * @return
	 */
	private double getBranchLength(List<Node2D> branch) {
		double sum = 0;
		for(int i=0; i<branch.size()-1; i++) {
			Node2D nodeA = branch.get(i);
			Node2D nodeB = branch.get(i+1);
			NetworkEdge e = getEdgeForPair(nodeA, nodeB);
			sum += e.getWeight();
		}
		return sum;
	}
	
	/**
	 * This will quickly result in an infinite loop if this graph contains cycles. That's why it's
	 * not part of any Network utility
	 * @param n
	 */
	private static void traverse(NetworkNode n,  NetworkNode prevNode) {
		//System.out.println("Visiting node #" + ((Haplotype)n).getIndex() );
		
		if (n.getEdges().size()==0) {
			System.out.println("That node has no edges!");
		}
		for(NetworkEdge e : n.getEdges()) {
			NetworkNode adjNode = getOtherNode(n, e); //Returns the node that this edge connects to that is not n
			if (adjNode != prevNode)
				traverse(adjNode, n);
		}
	}
	
	/**
	 * Returns the node that is connected to edge e and is not 'notThisNode', or null 
	 * if no such node exists.
	 * 
	 * @param notThisOne
	 * @param e
	 * @return
	 */
	protected static NetworkNode getOtherNode(NetworkNode notThisOne, NetworkEdge e) {
		if (e.getNodeA()==notThisOne) {
			return e.getNodeB();
		}
		else {
			if (e.getNodeB()==notThisOne)
				return e.getNodeA();
		}
		
		return null;
	}
	
	public double getMaxX() {
		double max = 0;
		for(Node2D node : nodes) {
			if (node.getX() > max) {
				max = node.getX();
			}
		}
		return max;
	}
	
	public double getMaxY() {
		double max = 0;
		for(Node2D node : nodes) {
			if (node.getY() > max) {
				max = node.getY();
			}
		}
		return max;
	}
	
	public double getMinX() {
		double min = Double.MAX_VALUE;
		for(Node2D node : nodes) {
			if (node.getX() < min) {
				min = node.getX();
			}
		}
		return min;
	}
	
	public double getMinY() {
		double min = Double.MAX_VALUE;
		for(Node2D node : nodes) {
			if (node.getY() < min) {
				min = node.getY();
			}
		}
		return min;
	}

}
