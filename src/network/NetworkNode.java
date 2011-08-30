package network;

import java.util.List;


/**
 * The nodes in a network.Network must implement this interface
 * @author brendan
 *
 */
public interface NetworkNode {

	public void addEdge(NetworkEdge e);
	
	public List<NetworkEdge> getEdges(); 
	
}
