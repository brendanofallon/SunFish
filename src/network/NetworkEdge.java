package network;

public interface NetworkEdge {

	public NetworkNode getNodeA(); //Return a (random, but constant) node that connects to one end of this edge
	
	public NetworkNode getNodeB();
	
	public boolean isWeighted();
	
	public double getWeight();
	
}
