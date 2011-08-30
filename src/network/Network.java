package network;

import java.util.List;

public interface Network {

	public List<NetworkNode> getAllNodes();
	
	public List<NetworkEdge> getAllEdges();
	
	public List<NetworkEdge> getEdges(NetworkNode node);
	
}
