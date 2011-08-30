package plugins.SGPlugin.analyzer.haplotype.figure;

import java.util.ArrayList;
import java.util.List;

import network.NetworkEdge;
import network.Network2D;
import network.NetworkNode;
import network.Node2D;
import network.NodeCollection2D;

/**
 * Arranges the nodes of a network more-or-less at right angles to each other
 * @author brendan
 *
 */
public class RightAngleArranger extends AbstractNodeArranger {

	
	@Override
	public void arrangeNodes(Network2D network) {
//		nodes = new ArrayList<Node2D>();
//		nodes.addAll(network.getNode2DList());
//		
//		List<Node2D> longBranch = getMaxDiameterList();
//		//Lay out long branch...these values are largely arbitrary since everything will get shifted around later...
//		for(int i=0; i<longBranch.size(); i++) {
//			Node2D node = longBranch.get(i);
//			node.setX(i);
//			node.setY(0.5);
//		}
	}
	
	
	private void emitBranch(List<Node2D> branch) {
//		for(int i=0; i<branch.size()-1; i++) {
//			System.out.print( ((HaplotypeElement)branch.get(i) ).label + " - ");
//		}
//		System.out.println( ((HaplotypeElement)branch.get(branch.size()-1) ).label );
//		System.out.println("Total branch dist is: " + getBranchLength(branch));
	}
	

	
	private double getMaxDiameterDist() {
		List<Node2D> tips = new ArrayList<Node2D>();
		for(Node2D node : nodes) {
			if (node.getEdges().size()==1) {
				tips.add(node);
			}
		}
		
		double maxDist = 0;
		Node2D startNode = null;
		Node2D endNode = null;
		int maxI = -1;
		int maxJ = -1;
		for(int i=0; i<tips.size(); i++) {
			for(int j=i+1; j<tips.size(); j++) {
				double dist = getDistance(tips.get(i), tips.get(j));
				if (dist > maxDist) {
					maxDist = dist;
					startNode = tips.get(i);
					endNode = tips.get(j);
					maxI = i;
					maxJ = j;
				}
			}
		}
		
		//System.out.println("\nMax dist pair found between nodes " + maxI + " and " + maxJ + " at dist: " + maxDist );
		
		return maxDist;
	}

	

	
	/**
	 * Returns the distance (include edge distance) between two nodes. 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private double getDistance(Node2D nodeA, Node2D nodeB) {
		for(NetworkEdge e : nodeA.getEdges()) {
			NetworkNode adjNode = getOtherNode(nodeA, e);
			double dist = e.getWeight() + distanceToTarget((Node2D)adjNode, nodeA, nodeB);
			if ( ! Double.isNaN(dist) )
				return dist;
		}
		throw new IllegalArgumentException("NodeA and NodeB do not appear to be connected");
	}
	
	private double distanceToTarget(Node2D node, Node2D parent, Node2D target) {
		//First scan to see if the target is anywhere ....
		for(NetworkEdge e : node.getEdges()) {
			NetworkNode adjNode = getOtherNode(node, e); //Returns the node that this edge connects to that is not n
			if (adjNode == target) 
				return e.getWeight();
		}
		
		///...and if not found traverse deeper into structure
		for(NetworkEdge e : node.getEdges()) {
			NetworkNode adjNode = getOtherNode(node, e); //Returns the node that this edge connects to that is not n
			if (adjNode != parent) {
				Double dist = e.getWeight() + distanceToTarget((Node2D)adjNode, node, target);
				if ( !Double.isNaN(dist)) {
					return dist;
				}
			}				
		}
		//if we're here, we must've reached a tip, return NaN to poison all previous computations
		return Double.NaN;
	}


	
	public static void main(String[] args) {
//		List<Node2D> nodes = new ArrayList<Node2D>();
//		HaplotypeElement a = new HaplotypeElement("a");
//		HaplotypeElement b = new HaplotypeElement("b");
//		HaplotypeElement c = new HaplotypeElement("c");
//		HaplotypeElement d = new HaplotypeElement("d");
//		HaplotypeElement e = new HaplotypeElement("e");
//		HaplotypeElement f = new HaplotypeElement("f");
//		HaplotypeElement g = new HaplotypeElement("g");
//		Edge e1 = new HapEdgeElement(null, a, b, 1);
//		Edge e2 = new HapEdgeElement(null, b, c, 1);
//		Edge e4 = new HapEdgeElement(null, c, e, 1);
//		Edge e3 = new HapEdgeElement(null, c, d, 1);
//		Edge e5 = new HapEdgeElement(null, e, f, 1);
//		Edge e6 = new HapEdgeElement(null, f, g, 1);
//		
//		nodes.add(a);
//		nodes.add(b);
//		nodes.add(c);
//		nodes.add(d);
//		nodes.add(e);
//		nodes.add(f);
//		nodes.add(g);
//		NodeCollection2D network = new NodeCollection2D(nodes);
//		RightAngleArranger arranger = new RightAngleArranger();
//		arranger.arrangeNodes(network);
		
	}
}
