package plugins.SGPlugin.analyzer.haplotype;

import java.util.ArrayList;
import java.util.List;

import network.NetworkEdge;
import network.Network;
import network.NetworkNode;

import element.sequence.*;
import element.sequence.Sequence;


/**
 * A distance-matrix based representation of a haplotype network of a sequencegroup. The class can construct a network 
 * for a sequencegroup, but it does not perform the layout of the nodes in 2d space, a task which has more to do with 
 * the visual representation of the network and therefore takes place in HaplotypeNetworkFigure (actually, layout 
 * is now performed by a NodeArranger).
 * 
 * This class involves building the network and implements the Network interface,
 * which means it maintains a list of all the 'nodes' and 'edges' of the network. Here NetworkNodes
 * are Haplotypes, and edges are a subclass of 'GenericEdge' called HaplotypeEdges
 * 
 * @author brendan
 *
 */
public class HaplotypeNetwork implements Network {

	int[][] dMatrix; //The distance matrix between haplotypes
	SequenceGroup seqs;
	ArrayList<NetworkNode> haps;
	ArrayList<NetworkEdge> edges;
	
	public HaplotypeNetwork(SequenceGroup sg) {
		this.seqs = sg;
		edges = new ArrayList<NetworkEdge>();
		buildDistanceMatrix();
		constructMinSpanningTree();
	}
	
	/**
	 * For debugging, emit some info to std out
	 */
	private void emitNetwork() {
		System.out.println("Haplotypes size : " + haps.size());
		for(NetworkNode node : haps) {
			Haplotype hap = (Haplotype)node;
			System.out.println("\nHaplotype #" + hap.getIndex() + " has " + hap.getNumNeighbors() + " neighbors");
			ArrayList<NeighborDistPair> neighbors = hap.getNeighbors();
			for(NeighborDistPair nbd : neighbors) {
				System.out.println("\t Hap #" + nbd.neighbor.getIndex() + " dist: " + nbd.dist);
			}
		}
	}


	/**
	 * Constructs a minimum spanning network by adding 'NeighborDistPairs' to the existing haplotypes,
	 * using Prim's O(n^2) algorithm
	 */
	private void constructMinSpanningTree() {
		//Start with a random node, say Haplotype #0
		ArrayList<Haplotype> usedHaps = new ArrayList<Haplotype>();
		ArrayList<Haplotype> unusedHaps = new ArrayList<Haplotype>();
		
		for(NetworkNode n : haps) {
			unusedHaps.add((Haplotype)n);
		}
		Haplotype hap = unusedHaps.remove(0);
		usedHaps.add(hap);
		
		//On each step we add exactly 1 new haplotype to the network ('usedHaps') by selecting
		//the hap that has the closest distance to any of the haps already in the network. We maintain
		//two always disjoint sets, those haps that are already 'used' and those that are not. On
		//each iteration one new hap is selected from the 'unused' pile and to the 'used' pile, and
		//at the same time we construct an edge from the newly added hap to the one already in the 
		//'used' pile that 
		//will be exactly haps.size() such additions, but we've already added 1 to start...
		while(unusedHaps.size()>0) {
			Haplotype[] minPair = findMinimumDistPair(usedHaps, unusedHaps);
			int dist = getDistFromMatrix(minPair[0], minPair[1]);
			minPair[0].addNeighbor(minPair[1], dist);
			minPair[1].addNeighbor(minPair[0], dist);
			makeAndJoinEdge(minPair[0], minPair[1], dist);
			
			unusedHaps.remove(minPair[1]);
			usedHaps.add(minPair[1]);
		}
		
		haps.clear();
		for(Haplotype uhap : usedHaps) {
			haps.add(uhap);
		}
	}
	
	/**
	 * Returns the edge that joins hap a and hap b, if it exists
	 * @param a
	 * @param b
	 * @return
	 */
	public HaplotypeEdge getEdgeForPair(Haplotype a, Haplotype b) {
		for(NetworkEdge e : edges) {
			if (e.getNodeA()==a) {
				if (e.getNodeB()==b)
					return (HaplotypeEdge)e;
				
			}
			else {
				if (e.getNodeB()==a) {
					if (e.getNodeA()==b)
						return (HaplotypeEdge)e;
				}
			}
		}
		return null;
	}
	
	private void makeAndJoinEdge(Haplotype a, Haplotype b, double dist) {
		HaplotypeEdge hapEdge = new HaplotypeEdge(a, b, dist);
		a.addEdge(hapEdge);
		b.addEdge(hapEdge);
		edges.add(hapEdge);
	}
	
	/**
	 * Finds the pair of haplotypes such that one is in group A, the other is in group B, and the distance
	 * between A and B is the minimum distance between all such pairs 
	 * @param groupA
	 * @param groupB
	 * @return
	 */
	private Haplotype[] findMinimumDistPair(ArrayList<Haplotype> groupA, ArrayList<Haplotype> groupB) {
		Haplotype minA = groupA.get(0);
		Haplotype minB = groupB.get(0);
		int minDist = getDistFromMatrix(minA, minB);
		
		for(int i=0; i<groupA.size(); i++) {
			
			for(int j=0; j<groupB.size(); j++) {
				int d = getDistFromMatrix(groupA.get(i), groupB.get(j)); 
				if (d<minDist) {
					minDist = d;
					minA = groupA.get(i);
					minB = groupB.get(j);
				}
			}
		}
		
		Haplotype[] pair = new Haplotype[2];
		pair[0] = minA;
		pair[1] = minB;
		return pair;
	}
	
	/**
	 * Return the NeighborDistPair that contains the haplotype and distance that are 
	 * the minimum distance from the query haplotype
	 * @param haplots
	 * @param query
	 * @return
	 */
	
	private NeighborDistPair findMinimumDistHap(ArrayList<Haplotype> haplots, Haplotype query) {
		Haplotype min = haplots.get(0);
		int minDist = getDistFromMatrix(haplots.get(0), query);
		for(int i=1; i<haplots.size(); i++) {
			int d = getDistFromMatrix(haplots.get(i), query); 
			if (d < minDist) {
				minDist = d;
				min = haplots.get(i);
			}
		}
		return new NeighborDistPair(min, minDist);
	}

	

	/**
	 * Constructs a matrix where i,j is the total number of sites at which sequences i and j differ
	 */
	protected void buildDistanceMatrix() {
		haps = new ArrayList<NetworkNode>();
		int count = 1;
		for(int i=0; i<seqs.size(); i++) {
			int index = hapsContains(seqs.get(i)); 
			if ( index ==-1) {
				Sequence addition = seqs.get(i).clone();
				addition.setName("Haplotype #" + count);
				Haplotype newHap = new Haplotype(addition, count-1);
				newHap.addName(seqs.get(i).getName());
				haps.add(newHap);
				count++;
			}
			else {
				((Haplotype)haps.get(index)).addName(seqs.get(i).getName());
			}
			
			
		}
		
		dMatrix = new int[haps.size()][haps.size()];
		for(int i=0; i<haps.size(); i++) {
			for(int j=i+1; j<haps.size(); j++) {
				int d = getDist(haps.get(i), haps.get(j));
				dMatrix[i][j] = d;
				dMatrix[j][i] = d;
			}
		}
		
	}
	
	protected int hapsContains(Sequence seq) {
		for(int i=0; i<haps.size(); i++) {
			if (isSameSequence(seq, ((Haplotype)haps.get(i)).getSequence())) {
				return i;
			}
		}
		return -1;
	}
	
	protected boolean isSameSequence(Sequence seq1, Sequence seq2) {
		return seq1.toString().equals(seq2.toString());
	}
	
	protected int getDistFromMatrix(Haplotype h1, Haplotype h2) {
		return dMatrix[h1.getIndex()][h2.getIndex()];
	}
	
	protected int getDist(NetworkNode s1, NetworkNode s2) {
		return getDist((Haplotype)s1, (Haplotype)s2);
	}
	
	protected int getDist(Haplotype s1, Haplotype s2) {
		int difs = 0;
		for(int i=0; i<Math.min(s2.getSequence().length(), s1.getSequence().length()); i++) {
			if (s1.getSequence().toString().charAt(i)!=s2.getSequence().toString().charAt(i))
				difs++;
		}
		return difs + Math.abs(s2.getSequence().length()-s1.getSequence().length());
	}

	public ArrayList<NetworkEdge> getAllEdges() {
		return edges;
	}


	public ArrayList<NetworkNode> getAllNodes() {
		return haps;
	}


	@Override
	public List<NetworkEdge> getEdges(NetworkNode node) {
		return node.getEdges();
	}
	
	
}
