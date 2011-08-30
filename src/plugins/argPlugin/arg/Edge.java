package plugins.argPlugin.arg;

import network.NetworkEdge;
import network.NetworkNode;

/**
 * An edge (aka branch) in an ARG that connects two ARGNodes
 * @author brendan
 *
 */
public class Edge implements NetworkEdge {

	protected ARGNode source;
	protected ARGNode target;
	
	protected int rangeMin;
	protected int rangeMax;
	
	protected String id;
	
	public Edge(String id, ARGNode source, ARGNode target, int rangeMin, int rangeMax) {
		this.id = id;
		this.source = source;
		this.target = target;
		if (rangeMin >= rangeMax) {
			throw new IllegalArgumentException("Invalid rangeMin / rangeMax combo for edge");
		}
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}

	
	public String getID() {
		return id;
	}
	
	public String toString() {
		return "ARG edge " + getID();
	}
	
	/**
	 * Return the lower, child node for this edge
	 * @return
	 */
	public ARGNode getSource() {
		return source;
	}
	
	/**
	 * Return the higher, parent node for this edge
	 * @return
	 */
	public ARGNode getTarget() {
		return target;
	}

	/**
	 * Get the first site that flows through this edge
	 * @return
	 */
	public int getRangeMin() {
		return rangeMin;
	}

	/**
	 * Get one more than the last site that flows through this edge
	 * @return
	 */
	public int getRangeMax() {
		return rangeMax;
	}
	
	@Override
	public NetworkNode getNodeA() {
		return getSource();
	}

	@Override
	public NetworkNode getNodeB() {
		return getTarget();
	}

	@Override
	public boolean isWeighted() {
		return false;
	}

	@Override
	public double getWeight() {
		return 1;
	}
	
}
