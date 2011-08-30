package network;


/**
 * Interface for things that place network nodes in 2D space, most likely prior to visualization
 * For now, this ASSUMES THAT THE NETWORKS ARE ACYCLIC! That is, they're like trees.
 * 
 * The basic idea is that many of the same operations are performed on both trees and haplotype
 * networks (and other networks?), so we should have a unified way of accessing some of the common
 * features.
 *   
 * @author brendan
 *
 */
public interface NodeArranger {
	
	public void arrangeNodes(Network2D network);
	
}
