package plugins.argPlugin.arg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import network.NetworkEdge;
import network.Node2D;

/**
 * A single node in an ARG, with one or more edges that connect to other ARGNodes. Currently we support any number of offspring, but
 * only one or two parents. Parents are labelled arbitrarily the 'primaryParent' and 'recombParent', but this designation means
 * nothing besides the fact that the primaryParent is the first parent to be assigned and the recombParent is the second (if any)
 * parent to be assigned. Parents are always associated with a range of sites - if there is no recomParent the primaryParent is
 * associated with the entire site range. However, if a recombParent exists, different sites may be associated with different parents. 
 * 
 * All parent offspring relationships are created via addChildEdge (which adds an edge that connects this node to a child) and
 * addParentEdge (which adds a new parent to this node). 
 * @author brendan
 *
 */
public class ARGNode implements Node2D {
	
	protected static int nodeCount = 0; //Counter for total number of nodes created
	
	//Each node gets a unique, immutable number  
	protected final int myNumber = nodeCount;
	
	protected double xPos;
	protected double yPos;

	//In contrast to tree nodes, ARG nodes are specified by a height, which is a distance from 
	//the node with the lowest height. 
	protected double height;
	
	//A unique and non-mutable id for the node. In general this should come directly from the graphml, 
	//which also specifies that nodes (and edges) have unique ids
	protected String id;
	
	protected ARGNode primaryParent;
	protected ARGNode recombParent;
	protected Range recombRange;
	
	protected List<ARGNode> offspring = new ArrayList<ARGNode>();
	
	protected List<NetworkEdge> edges = new ArrayList<NetworkEdge>();
		
	protected Map<String, String> annotations = new HashMap<String, String>();

	
	public ARGNode(String id, double height) {
		this.id = id;
		this.height = height;
		nodeCount++;
	}
	
	
	/**
	 * Get this node's unique number (not related to ID). 
	 * @return
	 */
	public int getNumber() {
		return myNumber;
	}
	
	/**
	 * Get the unique ID associated with this node, typically this ID was designated by the XML
	 * @return
	 */
	public String getID() {
		return id;
	}
	
	public double getNodeHeight() {
		return height;
	}
	
	public void setNodeHeight(double newHeight) {
		this.height = newHeight;
	}
	
//	public void setParent(ARGNode parent) {
//		this.primaryParent = parent;
//	}
//	
//	public void setRecombParent(ARGNode rParent, Range range) {
//		this.recombParent = rParent;
//		this.recombRange = range;
//	}
	
	/**
	 * Set a recombination parent for this node, with the half-open interval going from rangeMin (inclusive) to rangeMax (exclusive)
	 * @param rParent
	 * @param rangeMin
	 * @param rangeMax
	 */
//	public void setRecombParent(ARGNode rParent, int rangeMin, int rangeMax) {
//		this.recombParent = rParent;
//		this.recombRange = new Range(rangeMin, rangeMax);
//	}

	/**
	 * Returns true if a recombination parent has been set for this node
	 * @return
	 */
	public boolean hasRecombParent() {
		return ( recombParent != null);
	}
	
	/**
	 * Obtain the parent node who donated the given site. This always returns the primary parent when no recomb parent has 
	 * been specified. Otherwise returns the recomb parent is recombRange contains the given site. 
	 * @param site Index of site we want to get the parent for
	 * @return A parent arg node
	 */
	public ARGNode getParentForSite(int site) {
		if (recombParent != null && recombRange.contains(site))
			return recombParent;
		else
			return primaryParent;
	}
	
	/**
	 * Returns a list of all offspring of this node whose getParentForSite() is this node
	 * @param site The site at which to examine parent-offspring relationships
	 * @return
	 */
	public List<ARGNode> getOffspringForSite(int site) {
		List<ARGNode> kids = new ArrayList<ARGNode>(4);
		for(ARGNode kid : getOffspring()) {
			if (kid.getParentForSite(site) == this) {
				kids.add(kid);
			}
		}
		return kids;
	}
	
	/**
	 * Return number of nodes below this node
	 * @return
	 */
	public int getNumOffspring() {
		return offspring.size();
	}
	
	private void addChild(ARGNode child) {
		if (offspring.contains(child)) {
			throw new IllegalArgumentException("Child is already an offspring of arg node " + id);
		}
		offspring.add(child);
	}
	
	/**
	 * Return offspring node with at given index, or null if index >= getNumOffspring
	 * @param which index of child to obtain
	 * @return Another argNode
	 */
	public ARGNode getOffspring(int which) {
		return offspring.get(which);
	}

	public List<ARGNode> getOffspring() {
		return offspring;
	}
	
	//**************** Node2D implementation stuff **************/
	
	/**
	 * Get the relative x-position of this node
	 * @return
	 */
	public double getX() {
		return xPos;
	}

	public void setX(double xPos) {
		this.xPos = xPos;
	}

	/**
	 * Get the relative y-position of this node
	 * @return
	 */
	public double getY() {
		return yPos;
	}

	/**
	 * Set the relative y-position of this node
	 * @param yPos
	 */
	public void setY(double yPos) {
		this.yPos = yPos;
	}
	
	public void addChildEdge(Edge e) {
		if (e.getTarget()!= this) {
			throw new IllegalArgumentException("Attempt to add child edge " + e.getID() + " but edge target is not this node");
		}
		if (! offspring.contains(e.getSource())) {
			addChild(e.getSource());	
		}
		else {
			System.err.println("Warning: Request to add child edge but source is already a child. Right now we think this is OK. Adding edge but not child.");
		}
		edges.add(e);
	}
	
	public void addParentEdge(Edge e, int rangeMin, int rangeMax) {
		if (primaryParent==null) {
			primaryParent = e.getTarget();
			edges.add(e);
		}
		else {
			if (recombParent != null) {
				throw new IllegalArgumentException("ARGNode " + getID() + " already has a primary and recomb. parent, cannot add new parent edge");
			}
			
			recombParent = e.getTarget();
			recombRange = new Range(rangeMin, rangeMax);
			edges.add(e);
		}
	}
	
	public Integer getBreakPointMin() {
		if (recombRange == null)
			return null;
		return recombRange.min;		
	}
	
	public Integer getBreakPointMax() {
		if (recombRange == null)
			return null;
		return recombRange.max;
		
	}
	
	
	/************** Node2D implementation **************************/
	
	@Override
	public void addEdge(NetworkEdge e) {
		throw new IllegalArgumentException("Cannot add edge of class " + e.getClass() + " to an ARG");
	}

	@Override
	public List<NetworkEdge> getEdges() {
		return edges;
	}

	@Override
	/**
	 * Obtain all ARGNodes that are attached to this node via an edge
	 */
	public List<Node2D> get2DNeighbors() {
		List<Node2D> neighbors = new ArrayList<Node2D>();
		for(NetworkEdge edge : edges) {
			Edge e = (Edge)edge;
			if (e.getNodeA()==this)
				neighbors.add((Node2D)e.getNodeB());
			else
				neighbors.add((Node2D)e.getNodeA());
		}

		return neighbors;
	}

	@Override
	/**
	 * Get width of this node - unused for ARG nodes. 
	 */
	public double getWidth() {
		return 0;
	}
	
	@Override
	/**
	 * Obtain the size / height of this element. This is NOT the height of this node in the tree, for that use getNodeHeight. This is
	 * just for Node2D implementation, and this particular variable should not be used. 
	 */
	public double getHeight() {
		return 0;
	}


	
	
	/**************************** Annotation related stuff *************************************/
	
	/**
	 * Add a new key=value annotation to this node
	 * @param key
	 * @param value
	 * @return True if an annotation with the given key already exists
	 */
	public boolean addAnnotation(String key, String value) {
		boolean retVal = annotations.containsKey(key);
		annotations.put(key, value);
		return retVal;
	}
	
	/**
	 * Returns true if this node has the given annotation
	 * @param key
	 * @return
	 */
	public boolean hasAnnotation(String key) {
		return annotations.containsKey(key);
	}
	
	/**
	 * Remove all annotations from this node
	 */
	public void clearAnnotations() {
		annotations.clear();
	}
	
	/**
	 * Get the annotation associated with the given key
	 * @param key
	 * @return
	 */
	public String getAnnotation(String key) {
		return annotations.get(key);
	}
	
	/**
	 * Return all keys used for annotations as a Set
	 * @return
	 */
	public Set<String> getAnnotationKeys() {
		return annotations.keySet();
	}


}
