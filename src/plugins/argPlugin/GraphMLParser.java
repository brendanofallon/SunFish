package plugins.argPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import network.NetworkNode;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import plugins.argPlugin.arg.ARG;
import plugins.argPlugin.arg.ARGNode;
import plugins.argPlugin.arg.Edge;
import plugins.argPlugin.argDisplay.ARGDisplay;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

public class GraphMLParser extends FileParser {

	final String[] suffices = {"xml"};
	
	public static final String XML_DOCUMENT_ROOT_NAME = "graphml";
	public static final String XML_GRAPH = "graph";
	public static final String XML_NODE = "node";
	public static final String XML_EDGE = "edge";
	public static final String XML_ID = "id";
	public static final String XML_HEIGHT = "height";
	public static final String XML_SOURCE = "source";
	public static final String XML_TARGET = "target";
	public static final String XML_RANGE = "range";
	public static final String XML_START = "start";
	public static final String XML_END = "end";
	public static final String XML_RANGEMIN = "rangemin";
	public static final String XML_RANGEMAX = "rangemax";
	public static final String XML_NODEANNOTATION = "annotation";
	public static final String XML_ANNOTATION_KEY = "key";

	
	public GraphMLParser(SunFishFrame sunfishParent) {
		super(sunfishParent, sunfishParent.getLogger());
	}

	
	@Override
	public String getName() {
		return "ARG and GraphML parser";
	}

	@Override
	public String getDescription() {
		return "Reads ARG files formatted as GraphML";
	}
	
	@Override
	public double getVersionNumber() {
		return 1.1;
	}
	
	public Class getPreferredDisplayClass() {
		return ARGDisplay.class;
	}

	
	@Override
	public String[] getMatchingSuffices() {
		return suffices;
	}

	@Override
	public Object readFile(File file) throws IOException, FileParseException {
		org.w3c.dom.Document data = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			data = builder.parse(file);
		} 
		catch (SAXException se) {
			ErrorWindow.showErrorWindow(se, logger);
			logger.warning("Caught SAX exception while trying to load xml file " + infile.getName() + " : " + se);
			throw new FileParseException(se.toString());
		}
		catch (ParserConfigurationException pe) {
			//Convert this to a file parsing exception
			ErrorWindow.showErrorWindow(pe, logger);
			logger.warning("Caught Parserer configuration exception while trying to load xml file " + infile.getName() + " : " + pe);
			throw new FileParseException(pe.toString());
		}

		if (data == null) 
			return null;
		
		//TODO add support for multiple trees 
		ARG arg = xmlToARG(data); 
		checkARGValidity(arg);
		return arg;
	}

	/**
	 * Perform some error checking on the ARG to ensure that node references are coherent and that 
	 * @param arg
	 * @throws FileParseException 
	 */
	private void checkARGValidity(ARG arg) throws FileParseException {
		List<ARGNode> tips = arg.getTips();
		for(ARGNode tip : tips) {
			if (tip.getParentForSite(0)==null) {
				throw new FileParseException("Tip " + tip.getID() + " has no primary parent");
			}
		}
		
		int nullParentCount = 0;
		List<NetworkNode> nodes = arg.getAllNodes();
		for(NetworkNode node : nodes) {
			ARGNode argNode = (ARGNode)node;
			if (argNode.getParentForSite(0)==null) {
				if (nullParentCount>0) {
					throw new FileParseException("More than one internal node has a null parent.");
				}
				nullParentCount++;
			}
			
		}
	}

	/**
	 * Primary class for converting the xml into a tree. We recurse through the xml nodes looking for <clade> elements, creating new
	 * kids whenever we see them. Nearly all other information is added as attributes to the tree nodes.
	 * 
	 * @param doc
	 * @return
	 * @throws FileParseException
	 */
	private ARG xmlToARG(org.w3c.dom.Document doc) throws FileParseException {
		Node documentRoot = doc.getDocumentElement();
		Node topLevelChild = documentRoot.getFirstChild();
				
		//Search for (the first) element
		while(topLevelChild != null) {
			if (topLevelChild.getNodeType() == Node.ELEMENT_NODE && topLevelChild.getNodeName().equalsIgnoreCase(XML_GRAPH))
				break;
			
			topLevelChild = topLevelChild.getNextSibling();
		}
		 
	
		//Child should now be 'pointing' at 'graph' element, if we're at the end it's an error
		if (topLevelChild == null) {
			throw new FileParseException("No phylogeny tag found in XML, cannot create tree");
		}
		
		String rangeMinStr = getAttributeForNode(topLevelChild, XML_RANGEMIN);
		String rangeMaxStr = getAttributeForNode(topLevelChild, XML_RANGEMAX);
		Integer rangeStart;
		Integer rangeEnd;
		try {
			rangeStart = Integer.parseInt(rangeMinStr);
			rangeEnd = Integer.parseInt(rangeMaxStr);
		}
		catch (NumberFormatException nfe) {
			throw new FileParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
		}
		catch (NullPointerException npe) {
			throw new FileParseException("Graph XML element must specify rangeMin and rangeMax attributes.");
		}
		
		
		Node graphChild = topLevelChild.getFirstChild(); 
		
		List<ARGNode> argNodes = new ArrayList<ARGNode>();
		List<Edge> edges = new ArrayList<Edge>();
		
		//First we read in all nodes, and then all the edges
		while(graphChild != null) {
			addNodeToARG(graphChild, argNodes);
			graphChild = graphChild.getNextSibling();
		}
		
		graphChild = topLevelChild.getFirstChild(); 
		while(graphChild != null) {
			addEdgeToARG(graphChild, argNodes, edges, rangeStart, rangeEnd);
			graphChild = graphChild.getNextSibling();
		}
		
		ARG arg = new ARG(argNodes, edges, rangeStart, rangeEnd);
		return arg;
	}



	private void addNodeToARG(Node child, List<ARGNode> argNodes) {
		if (child.getNodeName().equalsIgnoreCase(XML_NODE)) {
			String id = getAttributeForNode(child, XML_ID);
			String heightStr = getAttributeForNode(child, XML_HEIGHT);
			double height = Double.parseDouble(heightStr);
			String annotation = getAttributeForNode(child, XML_NODEANNOTATION);

			ARGNode argNode = new ARGNode(id, height);
			if (annotation != null)
				argNode.addAnnotation("annot.", annotation);
			
			//Scan for annotations
			Node aChild = child.getFirstChild();
			while(aChild != null) {
				if (aChild.getNodeType() == Node.ELEMENT_NODE && aChild.getNodeName().equalsIgnoreCase(XML_NODEANNOTATION)) {
					
					String key = getAttributeForNode(aChild, XML_ANNOTATION_KEY);
					String val = ((Element)aChild).getTextContent();
					if (key!=null && val != null) {
						argNode.addAnnotation(key, val);
					}
					System.out.println("Found annotation key=" + key + "   value=" + val);
					
				}
					
				aChild = aChild.getNextSibling();
			}
			
			//System.out.println("Adding node with id: " + id + " and height: " + height);
			argNodes.add(argNode);
		}
	}
	
	
	private void addEdgeToARG(Node child, List<ARGNode> argNodes, List<Edge> edges, int rangeMin, int rangeMax) throws FileParseException {
		if (child.getNodeName().equalsIgnoreCase(XML_EDGE)) {
			String sourceID = getAttributeForNode(child, XML_SOURCE);
			String targetID = getAttributeForNode(child, XML_TARGET);
			String edgeID = getAttributeForNode(child, XML_ID);
			ARGNode source = getNodeForID(argNodes, sourceID);
			ARGNode target = getNodeForID(argNodes, targetID);
			
			if (source == null) {
				throw new FileParseException("Could not find node with id " + sourceID + ", referred to by edge");
			}
			if (target == null) {
				throw new FileParseException("Could not find node with id " + targetID + ", referred to by edge");
			}
			
			if (source.getNodeHeight() > target.getNodeHeight()) {
				throw new FileParseException("For edge " + XML_ID + " source node height cannot be greater than target node height");
			}
			
			Node rangeNode = getChildForNodeName(child, XML_RANGE);
			if (rangeNode != null) {
				String startStr = getNodeTextContent(rangeNode, XML_START);
				String endStr = getNodeTextContent(rangeNode, XML_END);
				if (startStr==null || endStr==null) {
					throw new FileParseException("Could not read range start / end information for edge " + XML_ID);
				}
				try {
					Integer start = Integer.parseInt(startStr.trim());
					Integer end = Integer.parseInt(endStr.trim());
					
					edges.add(new Edge(edgeID, source, target, start, end));	
				}
				catch (NumberFormatException nfe) {
					throw new FileParseException("Could not read integer value for start/end information for edge " + XML_ID);
				}
			
				
			}
			else {
				edges.add(new Edge(edgeID, source, target, rangeMin, rangeMax));	
			}
			
			//System.out.println("Adding edge with id " + edgeID + " connecting  source " + sourceID + " to " + targetID);
		}
	}
	
	/**
	 * Find and return the argNode whose ID matches the given id, or null if no node is found 
	 * @param argNodes
	 * @param sourceID
	 * @return
	 */
	private ARGNode getNodeForID(List<ARGNode> argNodes, String id) {
		for(ARGNode node : argNodes) {
			if (node.getID().equals(id)) 
				return node;
		}
		return null;
	}

	/**
	 * Examine the node for an attribute with the given key, and if found return the key's value,
	 * otherwise null.
	 * @param xmlNode
	 * @param key
	 * @return
	 */
	private String getAttributeForNode(Node xmlNode, String key) {
		NamedNodeMap nodeMap = xmlNode.getAttributes();
		Node attrValue = nodeMap.getNamedItem(key);
		if (attrValue == null) {
			return null;
		}
		return attrValue.getNodeValue();
	}

	/**
	 * Examines all immediate descendants of the parent xmlnode to see if they name a nodeName equal
	 * to the name provided. If so this returns that node, or null if no child has the name.
	 * 
	 * @param parent Node whose children we examine
	 * @param name Name to search for
	 * @return A xml node with the specified name, if found
	 */
	private Node getChildForNodeName(Node parent, String name) {
		Node child = parent.getFirstChild();
		while(child != null) {
			if (child.getNodeName().equalsIgnoreCase(name)) {
				return child;
			}
			child = child.getNextSibling();
		}
		
		return null;
	}
	
	/**
	 * Attempts to find an immediate child element node with name 'nodeName', then attempts to find the text content associated with that
	 * node, then returns it. Hence, in the following xml
	 * 
	 * <taxonomy>
	 * 		<scientific_name>
	 * 			Homo sapiens
	 * 		</scientific_name>
	 * </taxonomy>
	 * 
	 * If parent is at 'taxonomy', we might call getNodeTextContent(parent, "scientific_name") to get the String "Homo sapiens"
	 * @param parent An xml node
	 * @param nodeName Node name of child whose text content we want
	 * @return Text content of child with the given name
	 */
	private String getNodeTextContent(Node parent, String nodeName) {
		Node node = getChildForNodeName(parent, nodeName);
		if (node == null)
			return null;
		
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equalsIgnoreCase("#text")) {
				return child.getNodeValue();
			}
			
			child = child.getNextSibling();
		}
		
		return null;
	}
	
	@Override
	public Class getDataClass() {
		return ARG.class;
	}

	@Override
	public void writeData(File file, Object data) throws IOException {
		
	}

	@Override
	protected int getFilePriority(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document data = builder.parse(file);
			Node documentElement = data.getDocumentElement();
			if (documentElement.getNodeName().equalsIgnoreCase(XML_DOCUMENT_ROOT_NAME))
				return 10;
			else 
				return 0;
		} 
		catch (Exception ex) {
			return 0;
		}
	}

	protected Object readFile(BufferedReader buf) throws IOException, FileParseException {
		//This doesn't need to do anything since we've overridden readFile(file)
		return null;
	}




}
