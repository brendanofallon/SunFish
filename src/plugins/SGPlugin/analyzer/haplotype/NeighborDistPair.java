package plugins.SGPlugin.analyzer.haplotype;

/**
 * A utility class that haplotypes use to build the minimum spanning tree
 * @author brendan
 *
 */
public class NeighborDistPair {
	public Haplotype neighbor;
	public int dist;
	
	public NeighborDistPair(Haplotype hap, int d) {
		neighbor = hap;
		dist = d;
	}
}
