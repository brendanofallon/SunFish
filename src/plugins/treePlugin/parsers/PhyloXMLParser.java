package plugins.treePlugin.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import plugins.treePlugin.tree.*;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

public class PhyloXMLParser extends FileParser {

	static final double VERSION = 1.0;
	
	String[] suffices = {"xml"};
	
	public static final String XML_DOCUMENT_ROOT_NAME = "phyloxml";
	public static final String XML_CLADE = "clade";
	public static final String XML_DATE = "date";
	public static final String XML_PHYLOGENY = "phylogeny";
	public static final String XML_TAXONOMY = "taxonomy";
	public static final String XML_SEQUENCE = "sequence";
	public static final String XML_ID = "id";
	public static final String XML_SCIENCE_NAME = "scientific_name";
	public static final String XML_COMMON_NAME = "common_name";
	public static final String XML_ACCESSION = "accession";
	public static final String XML_NAME = "name";
	public static final String XML_LOCATION = "location";
	public static final String XML_MOLSEQ = "mol_seq";
	public static final String XML_URI = "uri";
	
	DocumentBuilderFactory factory;
	DocumentBuilder builder;
	
	public PhyloXMLParser(SunFishFrame sunfishParent) {
		super(sunfishParent, sunfishParent.getLogger());
		
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ErrorWindow.showErrorWindow(e);
		}
	}

	@Override
	public String getName() {
		return "PhyloXML file parser";
	}

	@Override
	public String getDescription() {
		return "Reads phyloXML-formatted phylogenies (trees)";
	}
	
	public double getVersionNumber() {
		return VERSION;
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
		DrawableTree tree = phyloXMLToTree(data); 
		return tree;
	}

	/**
	 * Primary class for converting the xml into a tree. We recurse through the xml nodes looking for <clade> elements, creating new
	 * kids whenever we see them. Nearly all other information is added as attributes to the tree nodes.
	 * 
	 * @param doc
	 * @return
	 * @throws FileParseException
	 */
	private DrawableTree phyloXMLToTree(org.w3c.dom.Document doc) throws FileParseException {
		Node documentRoot = doc.getDocumentElement();
		Node child = documentRoot.getFirstChild();
		
		DrawableNode root = null;
		
		//Search for (the first) phylogeny element
		while(child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equalsIgnoreCase(XML_PHYLOGENY))
				break;
			
			child = child.getNextSibling();
		}
		 
	
		//Child should now be 'pointing' at phylogeny element, so search within it for the first clade element
		if (child == null) {
			throw new FileParseException("No phylogeny tag found in XML, cannot create tree");
		}
		
		Node grandChild = child.getFirstChild();

		while(grandChild != null) {	
			if (grandChild.getNodeType() == Node.ELEMENT_NODE && grandChild.getNodeName().equalsIgnoreCase(XML_CLADE)) {
				root = new DrawableNode();
				recurseAddNodes( root, grandChild );
				break;
			}

			grandChild = grandChild.getNextSibling();
		}

		if (root == null) {
			throw new FileParseException("No clades found in XML, cannot create tree");
		}

		//TODO add support for multiple trees 
		
		DrawableTree tree = new SquareTree(root);
		convertDatesToBranchLengths(tree);
		return tree;
	}

	/**
	 * The XML parsing scheme does not set branch lengths, it just reads node ages. This converts the node heights
	 * to appropriate branch lenght values
	 * @param root
	 */
	private void convertDatesToBranchLengths(DrawableTree tree) {
		
		Stack<DrawableNode> stack = new Stack<DrawableNode>();
		stack.push((DrawableNode)tree.getRoot());
		
		while (! stack.isEmpty()) {
			DrawableNode node = stack.pop();
			if (node.getParent() != null) {
				node.setDistToParent(getTreeNodeHeight((DrawableNode)node.getParent()) - getTreeNodeHeight((DrawableNode)node) );
			}
			
			for(plugins.treePlugin.tree.Node child : node.getOffspring()) {
				stack.push((DrawableNode)child);
			}
		}
	}

	private double getTreeNodeHeight(DrawableNode node) {
		String dateAttr = node.getAnnotationValue("Date");
		Double date = Double.parseDouble(dateAttr);
		return date;
	}
	
	//Assume xmlNode is a 'clade' node and treeNode is a drawable tree node corresponding to the clade
	//if xmlNode has element child of type clade, we add those to the tree node
	private DrawableNode recurseAddNodes(DrawableNode treeNode, Node xmlNode) {
		if (xmlNode==null) {
			return null;
		}
		
		//xmlNode is assumed to be an node of type 'clade'
		addCladeAttributes(treeNode, xmlNode);
		setNodeLabelFromAttributes(treeNode);
		
		
		int type = xmlNode.getNodeType();
		switch (type) {
		case Node.ELEMENT_NODE:
			
			//Descend into all element children
			Node child = xmlNode.getFirstChild();
			while(child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE ) {
					if (child.getNodeName().equalsIgnoreCase(XML_CLADE)) {
						DrawableNode childTreeNode = new DrawableNode();
						childTreeNode.setParent(treeNode);
						childTreeNode.setDistToParent(1.0);
						treeNode.addOffspring(childTreeNode);
						
						recurseAddNodes(childTreeNode, child);
					}
					
				}
				
				child = child.getNextSibling();
			}
			
		case Node.ATTRIBUTE_NODE:
		case Node.COMMENT_NODE:
		case Node.ENTITY_REFERENCE_NODE:
		case Node.TEXT_NODE:
		case Node.DOCUMENT_NODE:	
		case Node.DOCUMENT_TYPE_NODE:
			//We ignore all these cases now
		}
		
		return treeNode;
	}
	
	/**
	 * Just try to set a decent label from the attributes we've collected.
	 * @param treeNode
	 */
	private void setNodeLabelFromAttributes(DrawableNode treeNode) {

		if (treeNode.getAnnotationValue("Scientific name")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("Scientific name") );
			return;
		}
		if (treeNode.getAnnotationValue("Common name")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("Common name") );
			return;
		}
		if (treeNode.getAnnotationValue("Name")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("Name") );
			return;
		}
		if (treeNode.getAnnotationValue("Id")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("Id") );
			return;
		}
		if (treeNode.getAnnotationValue("Id")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("Id") );
			return;
		}
		if (treeNode.getAnnotationValue("Accession")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("Accession") );
			return;
		}
		
		if (treeNode.getAnnotationValue("URI")!=null) {
			treeNode.setLabel(treeNode.getAnnotationValue("URI") );
			return;
		}
	}

	/**
	 * Adds various attributes such as height, accession number, label, sequence, etc. from the xml to the given tree node
	 * @param treeNode
	 * @param xmlNode
	 */
	private void addCladeAttributes(DrawableNode treeNode, Node xmlNode) {
		
		Node taxInfo = getChildForNodeName(xmlNode, XML_TAXONOMY);
		if (taxInfo != null) {
			String scienceName = getNodeTextContent(taxInfo, XML_SCIENCE_NAME);
			if (scienceName != null) {
				treeNode.addAnnotation("Scientific name", scienceName);
			}
			
			String id = getNodeTextContent(taxInfo, XML_ID); 
			if (id != null) {
				treeNode.addAnnotation("Id", id);
			}
			
			
			String commonName = getNodeTextContent(taxInfo, XML_COMMON_NAME);
			if (commonName != null) {
				treeNode.addAnnotation("Common name", commonName);
			}
			
		}
		
		Node seqInfo = getChildForNodeName(xmlNode, XML_SEQUENCE);
		if (seqInfo != null) {
			String acc = getNodeTextContent(seqInfo, XML_ACCESSION);
			if (acc != null) {
				treeNode.addAnnotation("Accession", acc);
			}
			
			String name = getNodeTextContent(seqInfo, XML_NAME);
			if (name != null) {
				treeNode.addAnnotation("Name", name);
			}
			
			String location = getNodeTextContent(seqInfo, XML_LOCATION);
			if (location != null) {
				treeNode.addAnnotation("Location", location);
			}
			
			String molseq = getNodeTextContent(seqInfo, XML_MOLSEQ);
			if (molseq != null) {
				treeNode.addAnnotation("Sequence", molseq);
			}
			
			String uri = getNodeTextContent(taxInfo, XML_URI);
			if (uri != null) {
				treeNode.addAnnotation("URI", uri);
			}
			
		}
		
		Node date = getChildForNodeName(xmlNode, XML_DATE);
		if (date != null) {
			String dateStr = date.getFirstChild().getNodeValue();
			treeNode.addAnnotation("Date", dateStr);
		}
		
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
		return DrawableTree.class;
	}

	@Override
	public void writeData(File file, Object data) throws IOException {
		
	}

	@Override
	protected int getFilePriority(File file) {
		try {
			org.w3c.dom.Document data = builder.parse(file);
			Node documentElement = data.getDocumentElement();
			if (documentElement.getNodeName().equalsIgnoreCase(XML_DOCUMENT_ROOT_NAME))
				return 5;
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
