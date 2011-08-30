package plugins.SGPlugin.analyzer.haplotype;

import java.util.ArrayList;
import java.util.List;

import network.NetworkEdge;
import network.GenericEdge;
import network.NetworkNode;


import element.sequence.Sequence;


/**
 * A genetic sequence which can be referenced by multiple individuals. This class
 * is primarily (right now, exclusively) used by HaplotypeNetwork, and bears some
 * additional methods that relate to building HaplotypeNetworks
 * @author brendan
 *
 */
public class Haplotype implements NetworkNode {

	Sequence seq; //This sequence this haplotype represents;
	ArrayList<String> names; //The names or ids of the individuals that have this haplotype
	ArrayList<NeighborDistPair> neighborDists; //A list of related haplotypes and their total distance to this haplotype
	int index;	//Used to determine index of this haplotype in a distance matrix
	List<NetworkEdge> edges;
	
	
	public Haplotype(Sequence seq, int index) {
		this.seq = seq;
		neighborDists = new ArrayList<NeighborDistPair>();
		edges = new ArrayList<NetworkEdge>();
		names = new ArrayList<String>();
		this.index = index;
	}
	
	
	public int getNumNeighbors() {
		return neighborDists.size();
	}

	
	/**
	 * Returns the list of sequence names associated with this haplotype (presumably these 
	 * are the sequences that have this haplotype).
	 * @return
	 */
	public ArrayList<String> getNames() {
		return names;
	}
	
	public int getCardinality() {
		return names.size();
	}
	
	public boolean removeEdge(HaplotypeEdge edge) {
		boolean removed = 	edges.remove(edge);
		return removed;
	}
	
	public ArrayList<NeighborDistPair> getNeighbors() {
		return neighborDists;
	}
	
	public Sequence getSequence() {
		return seq;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void addName(String name) {
		if (names == null) 
			names = new ArrayList<String>();
		names.add(name);
	}
	
	public void addNeighbor(Haplotype neighbor, int dist) {
		if (neighborDists == null) {
			neighborDists = new ArrayList<NeighborDistPair>();
		}
		
		neighborDists.add(new NeighborDistPair(neighbor, dist));
	}
	
	public boolean hasNeighbor(Haplotype n) {
		for(NeighborDistPair ndp : neighborDists) {
			if (ndp.neighbor.equals(n)) {
				return true;
			}
		}
		
		return false;
	}
	
	public NeighborDistPair getMinimumDistNeighbor() {
		NeighborDistPair min = neighborDists.get(0);
		for(int i=1; i<neighborDists.size(); i++) {
			if (neighborDists.get(i).dist < min.dist)
				min = neighborDists.get(i);
		}
		return min;
	}


	public List<NetworkEdge> getEdges() {
		return edges;
	}


	@Override
	public void addEdge(NetworkEdge e) {
		if (e.getNodeA() != null && !(e.getNodeA() instanceof Haplotype)) {
			throw new IllegalArgumentException("Cannot joing haplotype node to non-haplotype node");
		}
		if (e.getNodeB() != null && !(e.getNodeB() instanceof Haplotype)) {
			throw new IllegalArgumentException("Cannot joing haplotype node to non-haplotype node");
		}
		edges.add(e);
	}
	

}
