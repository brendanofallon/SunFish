package plugins.SGPlugin.analyzer.haplotype;

import network.GenericEdge;
import network.NetworkNode;

/**
 * A basic edge in a haplotype network
 * @author brendan
 *
 */
public class HaplotypeEdge extends GenericEdge {

	public HaplotypeEdge(NetworkNode aNode, NetworkNode bNode, double weight) {
		super(aNode, bNode, weight);
		
	}

}
