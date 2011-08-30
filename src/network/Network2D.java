package network;

import java.util.List;

/**
 * A collection of nodes that can be arranged in space,
 * @author brendan
 *
 */
public interface Network2D extends Network {

	/**
	 * Obtain a list of all nodes in this network
	 * @return
	 */
	public List<Node2D> getNode2DList();
	
	/**
	 * Get all nodes2Ds that are adjacent to (joined by an edge to) the given node
	 * @param node
	 * @return
	 */
	public List<Node2D> get2DNeighbors(Node2D node);
	
}
