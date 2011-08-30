package plugins.xmlDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import topLevelGUI.SunFishFrame;

import display.Display;
import display.DisplayData;

import element.sequence.Sequence;
import element.sequence.SequenceGroup;
import element.sequence.StringSequence;


public class XMLDisplay extends Display {

	static final double VERSION = 1.0;
	
	JScrollPane scrollPane;
	JTree tree;
	Logger logger;
	Document doc;
	
	boolean showComments = true;
	
	public XMLDisplay(SunFishFrame parent, Logger logger) {
		super(parent);
		this.sunfishParent = parent;
		this.logger = logger;
	}
	
	@Override
	public String getName() {
		return "XML file display";
	}

	@Override
	public String getDescription() {
		return "Visualize XML data";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
	
	public String getFileName() {
		return null;
	}


	public Display getNew() {
		return new XMLDisplay(sunfishParent, logger);
	}

	public String getTitle() {
		return null;
	}
	
	public void saveToFile(File f) {
		throw new IllegalStateException("Saving xml not implemented yet, sorry");
	}
	

	@Override
	public Class[] getDisplayableClasses() {
		return new Class[]{Document.class};
	}

	protected boolean update(SunFishFrame parent, display.DisplayData displayData) {		
		title = displayData.getFileName();
		doc = (Document)(displayData.getData(0));
		Node documentRoot = doc.getDocumentElement();
		
		DefaultMutableTreeNode treeRoot = addTreeNodes(documentRoot); 
		tree = new JTree(treeRoot);
		tree.setCellRenderer(new XMLTreeCellRenderer());
		this.setLayout(new BorderLayout());
		
		boolean isLamarc = false;
		if ( ((TreeNodeElement)treeRoot.getUserObject()).getNameText().equalsIgnoreCase("lamarc") || doc.getFirstChild().getNodeName().equalsIgnoreCase("lamarc") ) 
			isLamarc = true;
		else {
			//Scan all top-level nodes to see if there's some any have 'lamarc' in the name
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeRoot.getFirstChild();
			while (node != null && isLamarc==false) {
				isLamarc = ((TreeNodeElement)node.getUserObject()).getNameText().equalsIgnoreCase("lamarc");
				node = node.getNextSibling();
			}
		}
		
		if (isLamarc) {
			Object[] options = {"Display sequences", "Display xml", "Display both"};
			int n = JOptionPane.showOptionDialog(parent,
					"This LAMARC file may have embedded sequences, would you like to display the sequences?",
					"LAMARC file detected",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[0]);
			if (n==0) {
				displayLamarcSequences(doc);
				return false; //Don't display this display
			}
			if (n==1) {
				scrollPane = new JScrollPane(tree);
				add(scrollPane, BorderLayout.CENTER);
				GenericXMLSummary xmlSummary = new GenericXMLSummary(this);
				xmlSummary.analyze(displayData.getFileName(), doc);
				parent.displayOutput(xmlSummary);
				this.repaint();
			}
			if (n==2) {
				displayLamarcSequences(doc);
				scrollPane = new JScrollPane(tree);
				add(scrollPane, BorderLayout.CENTER);
				GenericXMLSummary xmlSummary = new GenericXMLSummary(this);
				xmlSummary.analyze(displayData.getFileName(), doc);
				parent.displayOutput(xmlSummary);
				this.repaint();
			}
    		
		}
		else {
			scrollPane = new JScrollPane(tree);
			add(scrollPane, BorderLayout.CENTER);
			GenericXMLSummary xmlSummary = new GenericXMLSummary(this);
			xmlSummary.analyze(displayData.getFileName(), doc);
			parent.displayOutput(xmlSummary);
			this.repaint();
		}
		
		return true;
	}
	
	
	private ArrayList<Sequence> findSequences(Node node) {
		ArrayList<Sequence> foundSequences = new ArrayList<Sequence>();
		
		int type = node.getNodeType();
	
		if (node.getNodeName().equalsIgnoreCase("datablock")) {
			//System.out.println("Found data block node, trying to extract sequence...");
			Sequence newSeq = extractSequence(node);
			if (newSeq != null) 
				foundSequences.add(newSeq);
		}

		//Descend into all element children
		Node child = node.getFirstChild();
		while(child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE )
				foundSequences.addAll( findSequences(child));

			child = child.getNextSibling();
		}
		
		return foundSequences;
	}
	
	private void displayLamarcSequences(Document doc) {
		SequenceGroup seqs = new SequenceGroup();
		ArrayList<Sequence> extractedSequences = findSequences(doc);
		
		for(Sequence seq : extractedSequences) {
			seqs.add(seq);
		}
		
		DisplayData seqData = new DisplayData(null, seqs);
		if (sunfishParent != null && seqs.size()>0) {
			sunfishParent.displayData(seqData, title + " sequences" );
		}
	}
	
	/**
	 * Returns the value of the attribute attr, if an attribute node named attr exists and is an immediate
	 * descendant of this element node. Non-element nodes are ignored.
	 * 
	 * @param node Element node that is the parent of the attribute nodes to search 
	 * @param attr name of the attribute to look for
	 * @return Value of the attribute
	 */
	private String getAttributeValue(Node node, String attr) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
				//System.out.println("Searching for name attribute in node of type : " + typeString(node) + " with name : " + node.getNodeName());
				Node keyChild = node.getAttributes().getNamedItem(attr);
				if (keyChild != null) {
					
					return keyChild.getNodeValue();
				}
		}
		return null;
	}
	
	private Sequence extractSequence(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (node.getNodeName().equalsIgnoreCase("datablock")) {
				Node child = node.getFirstChild();
				while(child != null) {
					if (child.getNodeType()==Node.TEXT_NODE) {
						String seqText = child.getNodeValue().trim();
						String name = getAttributeValue(node.getParentNode(), "name");
						if (name==null) {
							Node gp = node.getParentNode().getParentNode();
							if (gp != null) {
								name = getAttributeValue(gp, "name");
							}
						}
						if (name == null) {
							name = "unknown seq";
						}
						//System.out.println("Extracting sequence with beginning : " + child.getNodeValue().substring(0, 25) + " from parent with name : " + name);
						Sequence seq = new StringSequence(seqText, name); 
						return seq;
					}
					child = child.getNextSibling();
				}
			}

		}

		return null;
	}
	
	private String typeString(Node n) {
		if (n.getNodeType()==Node.ATTRIBUTE_NODE) {
			return "Attribute";
		}
		if (n.getNodeType()==Node.ELEMENT_NODE) {
			return "Element";
		}
		if (n.getNodeType()==Node.TEXT_NODE) {
			return "Text";
		}
		
		return "Other";
	}
	
	private DefaultMutableTreeNode addTreeNodes(Node node) {
		if (node==null) {
			return null;
		}
		
		DefaultMutableTreeNode treeNode = null;
		
		int type = node.getNodeType();
		switch (type) {
		case Node.ELEMENT_NODE:
			treeNode = new DefaultMutableTreeNode();
			TreeNodeElement treeElement = new TreeNodeElement( node );
			treeNode.setUserObject(treeElement);
			
			//Find all attributes of this element and add 'em
			treeElement.addAttributes(node.getAttributes());	
			
			//Descend into all element children
			Node child = node.getFirstChild();
			while(child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE ) {
					DefaultMutableTreeNode childNode = addTreeNodes(child);
					if (childNode != null) {
						treeNode.add(childNode);
						childNode.setParent(treeNode);
					}
				}
				if (showComments && child.getNodeType() == Node.COMMENT_NODE) {
					DefaultMutableTreeNode commentNode = new DefaultMutableTreeNode();
					treeNode.add(commentNode);
					commentNode.setParent(treeNode);
					TreeNodeElement commentElement = new TreeNodeElement(child);

					commentNode.setUserObject(commentElement);
				}
				child = child.getNextSibling();
			}
			
		case Node.ATTRIBUTE_NODE:
			//We handle attributes in the element node block, so don't do anything here
			break;
		case Node.COMMENT_NODE:
		case Node.ENTITY_REFERENCE_NODE:
		case Node.TEXT_NODE:
		case Node.DOCUMENT_NODE:	
		case Node.DOCUMENT_TYPE_NODE:
			//We ignore all these cases now
		}
		
		return treeNode;
	}
		
			
	class TreeNodeElement {
		
		String name;
		String text;
		String attributes;
		boolean isComment = false;
		
		
		
		public TreeNodeElement(Node node) {
			if (node.getNodeType()==Node.COMMENT_NODE) {
				name = "";
				text = node.getNodeValue();
				attributes = "";
				isComment = true;
			}
			else {
				name = node.getNodeName();
				text = "";
				attributes = "";
				addTextInfo(node);
			}
		}
		
		public boolean isComment() {
			return isComment;
		}
		
		
		private void addTextInfo(Node node) {
			Node child = node.getFirstChild();
			while(child != null) {
				if (child.getNodeType()==Node.TEXT_NODE) {
					text += child.getNodeValue() + " ";
				}
				child = child.getNextSibling();
			}
		}
				
		public void addAttributes(NamedNodeMap attrs) {
			for(int i=0; i<attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				attributes += attr.getNodeName() + "=" + attr.getNodeValue() + "  ";
				
			}
		}
		
		public String getNameText() {
			return name;
		}
		
		public String getValueText() {
			return text;
		}
		
		public String getAttributeText() {
			return attributes;
		}
		
		public String toString() {
			return name + "   " + text + "  " + attributes;
		}
	}
	
	
	private class XMLTreeCellRenderer implements TreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object obj,
				boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			JLabel comp = new JLabel();
			if (obj instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
				Object userData = node.getUserObject();

				if (userData instanceof TreeNodeElement) {
					TreeNodeElement tne = (TreeNodeElement)userData;
					String txt = "<html>";
					
					if (tne.isComment()) {
						txt += " <font color=b7b7b7>";
						txt += tne.getNameText();
						txt += tne.getValueText();
						txt += " </font>";
						
					}
					else {
						if (tne.getNameText()!="") {
							txt += "<b>" + tne.getNameText() + "</b>";
						}

						if (tne.getAttributeText()!="") {
							txt += "  <font color=7e33ae>" + tne.getAttributeText() + "</font>";
						}

						if (tne.getValueText()!="") {
							txt += "<b>  <font color=334dae>" + tne.getValueText() + "</font></b>";
						}
					}
					comp.setText(txt);
				}
			}
			else {
				comp.setText(obj.toString());
			}
			
			return comp;
		}
		
	}

}
