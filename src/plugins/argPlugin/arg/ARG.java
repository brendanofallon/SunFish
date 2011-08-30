package plugins.argPlugin.arg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import network.Node2D;
import network.NodeArranger;
import network.NodeCollection2D;

/**
 * An ancestral recombination graph, implemented as a collection of ARGNodes, and edges which connect two nodes. These are immutable.
 *   
 * Recombinations are assumed to take place between two sites, and a 'breakpoint' is the site immediately left (down in the integers) of
 * the specified node. So a breakpoint at site i indicates that a recombination has taken place between sites i and i+1. This means 
 * that a recombination at site siteMin is legal (because there can be a recombination between 0 and 1),
 * but a recombination at site (siteMin+siteCount-1) is ILLEGAL since there can't be a recombination to the right of the last site.  
 * 
 * In addition to breakpoints, we also break an ARG up into one or more Ranges, which represent the sites between breakpoints. 
 * These are stored in the Regions field. Ranges are half-open and specified with a min and max such that if there's just one 
 * breakpoint at site i there will be two ranges, one from 0..i+1, the other from i+1..end.
 * 
 * Finally, to speed along the finding of edges when given two nodes, we maintain a mapping of node-pairs to edges, where the
 * node-pair is converted to a String which is used as a key in the map. If there's no entry in the map for nodes A and B, there's
 * no edge connecting nodes A and B. This scheme relies on node id's being unique, but since this is also part of the graphml specification
 * we should be OK there. If we wanted to expand to read in ARGs from a greater variety of sources, we should make sure that the args 
 *  
 * @author brendan
 *
 */
public class ARG extends NodeCollection2D {

	
	protected List<ARGNode> tips = new ArrayList<ARGNode>();	

	//Flag to indicate if we should recalculate node positions
	protected boolean recalculateNodePositions = true;
	
	//The object responsible for node position layout
	protected NodeArranger nodeArranger = new ARGNodeArranger();
	
	//Some layout methods require a focal site to lay out the nodes, we keep track of this here
	//Do we really need this here? Could it just be specified whenever we lay out nodes? Seems weird to have
	//this be a property of an ARG
	protected int focalSite = 0;
	
	protected int siteMin = 0; //Minimum site number
	protected int siteCount = 0; //The number of sites tracked by the model. Valid site range is from [siteMin.. siteMin+siteCount-1]
	
	//The deepest of all nodes
	protected ARGNode root = null;
	
	//A mapping from node PAIRS to edges so we can quickly find the edge that connects two nodes, if there is one
	Map<String, Edge> nodeEdgeMap = new HashMap<String, Edge>();
	
	//A sorted list of where all recombination breakpoints occur
	protected List<Integer> breakpoints = new ArrayList<Integer>();
	
	//List of Ranges corresponding to regions between breakpoints. We just construct once. 
	protected Range[] regions;
	
	//Whether or not we know the tips, we don't at first whilst nodes + edges are being added. 
	private boolean tipsKnown = false;
	
	/**
	 * Initialize an ARG with an empty node list
	 */
	public ARG(List<ARGNode> nodes, List<Edge> edges, int siteMin, int siteMax) {
		super(new ArrayList<Node2D>());
		this.siteMin = siteMin;
		this.siteCount = siteMax - siteMin;
		
		for(ARGNode node : nodes) {
			addNode(node);
		}
		
		for(Edge e : edges) {
			addEdge(e);
		}
		
		setFocalSite(0);
		findTips();	//Identify which nodes are tips. It speeds things up to keep a list of these.
		findBreakPoints();	//Locate and collect all breakpoints in a list 
		constructRegions(); //Create a list of Ranges (regions) describing the intervals between breakpoints. 
		
		//Find the root (highest) node
		root = (ARGNode)nodeList.get(0);
		for(Node2D node : nodeList) {
			if (((ARGNode)node).getNodeHeight() > root.getNodeHeight()) {
				root = (ARGNode)node;
			}
		}
		
		System.out.println("Constructing new arg with " + getNumBreakPoints() + " breakpoints...");
		System.out.println("BPs: " + breakpoints);
		emitRegions();
	}

	/**
	 * Fills the regions field with ranges corresponding to the intervals between breakpoints. This always
	 * makes one more Range than there are breakpoints. 
	 * 
	 * Note that there is some inconsistency between how breakpoints are described and how 
	 * Ranges are defined. If we say there's a breakpoint at site i, we mean there's a break
	 * between i and i+1. If we say a range has min x and max y, we mean it includes sites 
	 * from x... y-1. 
	 * 
	 * So if we're talking about the region from zero to the first break point at, say, i, then 
	 * the range's max will be site i+1, (and hence includes site from zero to i)
	 */
	private void constructRegions() {
		List<Range> protoRegions = new ArrayList<Range>(); //Since some ranges may share boundaries, we don't actually know how many there are
		
		int prevBreak = 0;
		for(int i=0; i<breakpoints.size(); i++) {
			if (breakpoints.get(i)+1 > prevBreak) {
				protoRegions.add( new Range(prevBreak, breakpoints.get(i) +1 ));
			}
			prevBreak = breakpoints.get(i)+1;
		}
		
		regions = new Range[protoRegions.size()+1];
		for(int i=0; i<protoRegions.size(); i++) {
			regions[i] = protoRegions.get(i);
		}
		regions[protoRegions.size()] = new Range(prevBreak, siteMin+siteCount);
	}

	/**
	 * Obtain the lowest-index site for this ARG
	 * @return
	 */
	public int getMinSite() {
		return siteMin;
	}
	
	/**
	 * Obtain the max site index for this ARG
	 * @return
	 */
	public int getMaxSite() {
		return siteMin + siteCount;
	}
	
	/**
	 * Creates a list containing all annotation keys present in the nodes (not all
	 * nodes will have an each key). 
	 * @return
	 */
	public List<String> collectAllAnnotationKeys() {
		List<String> allKeys = new ArrayList<String>();
		for(Node2D node : nodeList) {
			ARGNode aNode = (ARGNode)node;
			for(String key : aNode.getAnnotationKeys()) {
				if (!containsString(allKeys, key))
					allKeys.add(key);
			}
		}
		return allKeys;
	}
	
	/**
	 * Scans the given list to see if it contains the given string, 
	 * returns true if so. Small helper for collectAllAnnotationKeys, 
	 * @param list
	 * @param str
	 * @return True if list contains a string equal to str
	 */
	private static boolean containsString(List<String> list, String str) {
		for(String listStr : list) {
			if (listStr.equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Compute the (sorted) list of breakpoints by asking every node with a recombination what its range's min and max are, and adding
	 * the one that isn't at the global site range boundary.
	 */
	private void findBreakPoints() {
		breakpoints.clear();
		for(Node2D node : nodeList) {
			ARGNode aNode = (ARGNode)node;
			if (aNode.hasRecombParent()) {
				if (aNode.getBreakPointMin()>siteMin) { 
					breakpoints.add(aNode.getBreakPointMin());
				}
				else {
					breakpoints.add(aNode.getBreakPointMax()-1);
				}
			}
		}
		Collections.sort(breakpoints);
	}
	
	/**
	 * Return the total number of recombination breakpoints
	 * @return
	 */
	public int getNumBreakPoints() {
		return breakpoints.size();
	}
	
	/**
	 * Return the breakpoint at the given index
	 * @param index
	 * @return
	 */
	public int getBreakPointForIndex(int index) {
		return breakpoints.get(index);
	}
	
	public Range getRangeForSite(int site) {
		if (site < siteMin || site >= (siteMin+siteCount)) {
			return null;
		}
		
		for(int i=0; i<regions.length; i++) {
			if (regions[i].contains(site))
				return regions[i];
		}
	 
		return null;
	}
	
	/**
	 * Return the number of Ranges in this ARG. This should always be one more than the number of breakpoints, right?
	 * @return
	 */
	public int getRangeCount() {
		return regions.length;
	}
	
	/**
	 * Return all break points in this ARG as a list of Integers
	 * @return
	 */
	public List<Integer> getBreakPoints() {
		return breakpoints;
	}
	
	/**
	 * Returns the adjacent range on corresponding to higher site indices
	 * @param range
	 * @return
	 */
	public Range getNextRange(Range range) {
		return getRangeForSite(range.getMax());
	}
	
	public Range getRange(int rangeIndex) {
		return regions[rangeIndex];
	}
	
	public int indexOfRange(Range range) {
		for(int i=0; i<regions.length; i++) {
			if (regions[i] == range) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the adjacent range corresponding to lower site indices
	 * @param range
	 * @return
	 */
	public Range getPreviousRange(Range range) {
		return getRangeForSite(range.getMin()-1);
	}
	
	/**
	 * Write some basic region info to System.out
	 */
	public void emitRegions() {
		System.out.println("Total arg consists of " + siteCount + " sites from " + siteMin + " to " + siteMin + siteCount);
		System.out.println("Number of regions : " + regions.length);
		for(int i=0; i<regions.length; i++) {
			System.out.println("Region #" + i + "\t" + regions[i].getMin() + ".." + regions[i].getMax());
		}
		
	}
	
	public int getFocalSite() {
		return focalSite;
	}


	public void setFocalSite(int focalSite) {
		this.focalSite = focalSite;
		if (nodeArranger instanceof ARGNodeArranger) {
			((ARGNodeArranger)nodeArranger).setFocalSite(focalSite);
		}
		recalculateNodePositions = true;
	}
	
	/**
	 * Set the type of arranger this ARG uses to lay out its nodes. Generally speaking, this should be the ARG
	 * node arranger if you want something legible. 
	 * @param arranger
	 */
	public void setNodeArranger(NodeArranger arranger) {
		this.nodeArranger = arranger;
		setFocalSite(focalSite); //This makes sure the new arranger knows what the focal site is
	}
	
	
	/**
	 * Cause this arg's node arranger to layout all of the nodes. This always lays out, regardless of the state of recalculateNodePositions,
	 * but it sets that field to false when done.  
	 */
	public void recalculateNodePositions() {
		nodeArranger.arrangeNodes(this);
		recalculateNodePositions = false;
	}


	/**
	 * Returns a list of branches representing all of the branches of the marginal
	 * tree corresponding to the given site.  
	 * @return
	 */
	public List<Edge> getBranchesForFocalTree() {
		return getBranchesForTree(focalSite);
	}
	
	/**
	 * Returns a list of branches representing all of the branches of the focal tree. 
	 * @return
	 */
	public List<Edge> getBranchesForTree(int site) {
		if (!tipsKnown) {
			findTips();
		}
		
		List<Edge> branches = new ArrayList<Edge>();
		for(ARGNode tip : tips) {
			ARGNode ref = tip;
			ARGNode parent = tip.getParentForSite(site);
			while(parent != null) {
				Edge e = getEdgeForNodePair(ref, parent);
				if (branches.contains(e)) {
					break;
				}
				branches.add( e );
				ref = parent;
				parent = parent.getParentForSite(site);
			}
			
		}
		return branches;
	}
	
	/**
	 * Returns all nodes with no offspring
	 * @return
	 */
	public List<ARGNode> getTips() {
		if (! tipsKnown) {
			findTips();
		}
		return tips;
	}
	
	/**
	 * Traverse all nodes and figure out which ones are tips and add these to the list of tips 
	 */
	public void findTips() {
		tips.clear();
		for(Node2D node : nodeList) {
			if (node.getEdges().size()==1) {
				tips.add( (ARGNode)node);
			}
			if (node.getEdges().size()==0) {
				System.err.println("Huh, found a node with no edges attached, this shouldn't really happen for an ARG");
			}
		}
		tipsKnown = true;
	}
	
	/**
	 * Get the state of the flag indicating whether or not we should lay out the node positions before painting
	 * @return
	 */
	public boolean getRecalculateNodePositions() {
		return recalculateNodePositions;
	}


	/**
	 * Setting this to true will tell the node arranger to recalculate all node positions before the tree is drawn again
	 * @param recalculateNodePositions
	 */
	public void setRecalculateNodePositions(boolean recalculateNodePositions) {
		this.recalculateNodePositions = recalculateNodePositions;
	}
	
	//The algorithm for this is not clear and depends a bit on what kind of ARG we're given. If the ARG goes all the way to 
	//the MRCA of everyone, then we can always find a root by picking a tip and just tracing back to the root following the
	//focal site. But if we're given some horribly disjoint structure it's not clear what we should do..?
	public ARGNode getRootForSite(int site) {
		if (!tipsKnown) {
			findTips();
		}
		ARGNode node = tips.get(0);
		
		while(node.getParentForSite(site)!=null) {
			node = node.getParentForSite(site);
		}
		return node;
	}
	

	
	/**
	 * Find the number of tips that are descendants of this through the given site. We follow a site
	 * on if the child.getParentForSite(site) = parent. 
	 * @param kid
	 * @return
	 */
	public static int getNumTipsForSite(ARGNode kid, int site) {
		int count = 0;
		Stack<ARGNode> stack = new Stack<ARGNode>();
		stack.push(kid);
		
		while(! stack.isEmpty()) {
			ARGNode node = stack.pop();
			
			if (node.getNumOffspring()==0) {
				count++;
			}
				
			for(ARGNode n : node.getOffspring()) {
				if (n.getParentForSite(site)==node)
					stack.push(n);
			}
		}
		return count;
	}
	
	
	/**
	 * Return a unique (hopefully) key for the given pair of nodes. This will return the same key for (a,b) as it 
	 * does for (b,a)
	 * @param a
	 * @param b
	 * @return
	 */
	public String keyForNodePair(ARGNode a, ARGNode b) {
		if (a.getNumber() < b.getNumber())
			return a.getID() + b.getID();
		else
			return b.getID() + a.getID();
				
	}
	
	/**
	 * Return the edge that connects the two nodes, if there is one, or null otw
	 * @param a
	 * @param b
	 * @return
	 */
	public Edge getEdgeForNodePair(ARGNode a, ARGNode b) {
		return nodeEdgeMap.get( keyForNodePair(a, b));
	}
	
	/**
	 * Add this edge to the ARG. It is an error to refer to a node that isn't already in the list of nodes.
	 * @param e
	 */
	private void addEdge(Edge e) {
		ARGNode source = e.getSource();
		ARGNode target = e.getTarget();

		if (source.getNodeHeight() > target.getNodeHeight()) {
			throw new IllegalArgumentException("Source node cannot be higher than target node for edge " + e.getID());
		}
		
		if (! nodeList.contains(source)) {
			throw new IllegalArgumentException("Edge refers to missing source node " + source.getID());
		}
		
		if (! nodeList.contains(target)) {
			throw new IllegalArgumentException("Edge refers to missing target node " + target.getID());
		}
		
		if (e.getRangeMin()>siteMin && e.getRangeMax() != (siteMin+siteCount)) {
			throw new IllegalArgumentException("Invalid site range for edge " + e.getID() + " : ranges in upper half must extend to one site beyond last site (since ranges are half-open)");
		}
		
		source.addParentEdge(e, e.getRangeMin(), e.getRangeMax());
		target.addChildEdge(e);

		
		nodeEdgeMap.put( keyForNodePair(source, target), e);
		tipsKnown = false;
		edgeList.add(e);
	}
	
	
	/**
	 * Add a node to this network that is not connected to any edge. This checks for redundancy and does not
	 * add the node if it already exists in the network. 
	 * @param node
	 * @returns true if the node was added 
	 */
	private boolean addNode(ARGNode node) {
		tipsKnown = false;
		if (! nodeList.contains(node)) {
			if (getNodeForID(node.getID())!= null) {
				throw new IllegalArgumentException("A node with ID " + node.getID() + " already exists in the node list, but it's not the node we're trying to add");
			}
			nodeList.add(node);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the ARGNode whose id matches the given id, or null if no such node exists
	 * @param id
	 * @return An ARGNode whose ID matches the given id
	 */
	public ARGNode getNodeForID(String id) {
		for(Node2D node : nodeList) {
			ARGNode aNode = (ARGNode)node;
			if (aNode.getID().equals(id)) {
				return aNode;
			}
		}
		
		return null;
	}
	
	/**
	 * Return maximum height of all nodes in tree, ignores tips. 
	 * @return
	 */
	public double getMaxHeight() {
		double max = 0;
		for(Node2D node : nodeList) {
			ARGNode aNode = (ARGNode)node;
			if (aNode.getNodeHeight()>max)
				max = aNode.getNodeHeight();
		}
		return max;
	}

	/**
	 * Return the *most recent* common ancestor of all tips at the given site. This may be the highest node of all,
	 * but it may not be.  
	 * @param site
	 * @return
	 */
	public ARGNode getMRCAForSite(int site) {
		//Find branch with maximum height, start out at root of all nodes
		List<Edge> branches = getBranchesForTree(site);
		ARGNode highestNode = root;
		
		
		if (site==800) {
			System.out.println("Site is : " + site);
		}
		
		List<ARGNode> offspringNodes = findDescendantBranches(highestNode, branches);
		while(offspringNodes.size()==1) {
			highestNode = offspringNodes.get(0);
			offspringNodes = findDescendantBranches(highestNode, branches);
		}
		return highestNode;
	}

	/**
	 * Returns a list of nodes found  among all the SOURCE nodes in the given set of edges, whose target is the given target.
	 * This is a lot like finding the children of a given ARGNode parent, but it's restricted to the given set of edges
	 * @param root2
	 * @param branches
	 * @return
	 */
	private List<ARGNode> findDescendantBranches(ARGNode target,  List<Edge> branches) {
		List<ARGNode> sources = new ArrayList<ARGNode>(3);
		for(Edge edge : branches) {
			if (edge.getTarget()==target) {
				sources.add(edge.getSource());
			}
		}
		return sources;
	}

	/**
	 * Return the *most recent* common ancestor of all tips for the given Range. This may be the highest node of all,
	 * but it may not be.  
	 * @param site
	 * @return
	 */
	public ARGNode getMRCAForRange(Range range) {
		return getMRCAForSite(range.getMin());
	}
	
}
