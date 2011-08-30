package network;

/**
 * A generic edge in a network that connects two nodes and optionally has an associated weight. These do not have 
 * any kind of explicit position. 
 * @author brendan
 *
 */
public class GenericEdge implements NetworkEdge {

	NetworkNode nodeA;
	NetworkNode nodeB;
	double weight = Double.NaN;
	boolean hasWeight = false;
	
	public GenericEdge(NetworkNode aNode, NetworkNode bNode, double weight) {
		this.nodeA = aNode;
		this.nodeB = bNode;
		this.weight = weight;
		hasWeight = true;
	}
	
	public GenericEdge(NetworkNode aNode, NetworkNode bNode) {
		this.nodeA = aNode;
		this.nodeB = bNode;
	}
	
	public NetworkNode getNodeA() {
		return nodeA;
	}


	public NetworkNode getNodeB() {
		return nodeB;
	}


	public double getWeight() {
		return weight;
	}


	public boolean isWeighted() {
		return hasWeight;
	}

}
