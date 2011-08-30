package plugins.xmlDisplay;

import guiWidgets.PrettyLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

import topLevelGUI.analyzer.Analyzable;

import display.Display;


public class GenericXMLSummary extends Analyzable {

	JScrollPane scrollPane;
	JPanel mainPanel;
	JPanel leftPanel;
	JPanel rightPanel;
	Color bgColor;
	Font labelFont;
	JPanel topLeftPanel;
	PrettyLabel titleLabel;
	
	
	public GenericXMLSummary(Display source) {
		super(source);
		labelFont = new Font("Sans", Font.PLAIN, 12);
		bgColor = new Color(249, 249, 249);
		initComponents();

	}
	
	
	

	public void analyze(String name, Object data) {
		if (! (data instanceof org.w3c.dom.Document)) {
			System.err.println("Unrecognized object sent to GenericXMLSummary, aborting");
		}
		Document doc = (Document)data;
		DocumentType type = doc.getDoctype();
		Node first = doc.getFirstChild();
		titleLabel.setText("   " + name);
		
		if (type != null)
			doctype.setText("Document type : " + type.getNodeValue());
		else
			doctype.setText("Document type : unknown");
		xmlVersion.setText("XML Version : " + doc.getXmlVersion());
		if (first != null)
			rootElementType.setText("Root element : " + doc.getFirstChild().getNodeName());
		else	
			rootElementType.setText("Root element : unknown");
		
		String encoding = doc.getXmlEncoding();
		if (encoding != null)
			xmlEncoding.setText("Encoding : " + encoding);
		else	
			xmlEncoding.setText("Encoding : unknown");
		
		totalElements.setText("Total elements : "  + countElements(doc.getDocumentElement()));
	}
	
	private int countElements(Node n) {
		int count = 1;
		
		Node child = n.getFirstChild();
		while(child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				count += countElements(child);
			}
			child = child.getNextSibling();
		}
		
		return count;
	}
	
	private void initComponents() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(0, 2, 4, 4));
		//mainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		mainPanel.setBackground(bgColor);
		mainPanel.setMinimumSize(new Dimension(400, 200));
		mainPanel.setPreferredSize(new Dimension(400, 300));
		mainPanel.setOpaque(false);
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(bgColor);
		leftPanel.setMinimumSize(new Dimension(100, 100));
		leftPanel.setPreferredSize(new Dimension(100, 100));
		leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		leftPanel.setOpaque(false);
		
		topLeftPanel = new JPanel();
		topLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		topLeftPanel.setMinimumSize(new Dimension(100, 30));
		topLeftPanel.setPreferredSize(new Dimension(100, 30));
		topLeftPanel.setMaximumSize(new Dimension(1000, 30));
		topLeftPanel.setOpaque(false);
		topLeftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topLeftPanel.setBackground(bgColor);
		titleLabel = new PrettyLabel("(no title)");
		titleLabel.setFont(new Font("Sans", Font.BOLD, 13));
		topLeftPanel.add(titleLabel);
		leftPanel.add(topLeftPanel);
		
		JPanel lowerLeftPanel = new JPanel();
		lowerLeftPanel.setLayout(new BoxLayout(lowerLeftPanel, BoxLayout.PAGE_AXIS));
		lowerLeftPanel.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder("Summary"), BorderFactory.createEmptyBorder(0,4,2,2)));
		lowerLeftPanel.setMinimumSize(new Dimension(100, 100));
		lowerLeftPanel.setPreferredSize(new Dimension(300, 200));
		lowerLeftPanel.setMaximumSize(new Dimension(3500, 350));
		lowerLeftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerLeftPanel.setBackground(bgColor);
		lowerLeftPanel.setOpaque(false);
		rootElementType = new PrettyLabel("unknown");
		lowerLeftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		lowerLeftPanel.add(rootElementType);
		doctype = new PrettyLabel("unknown");
		lowerLeftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		lowerLeftPanel.add(doctype);
		xmlVersion = new PrettyLabel("unknown");
		lowerLeftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		lowerLeftPanel.add(xmlVersion);
		xmlEncoding = new PrettyLabel("unknown");
		lowerLeftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		lowerLeftPanel.add(xmlEncoding);
		totalElements = new PrettyLabel("Total elements :");
		lowerLeftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		lowerLeftPanel.add(totalElements);
		leftPanel.add(lowerLeftPanel);
		
		rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder("Interesting Info"), BorderFactory.createEmptyBorder(4,4,2,2)));
		rightPanel.setBackground(bgColor);
		rightPanel.setOpaque(false);
		
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}


	public Class getDataType() {
		return org.w3c.dom.Document.class;
	}

	JLabel doctype;
	JLabel totalElements;
	JLabel xmlVersion;
	JLabel xmlEncoding;
	JLabel numElementTypes;
	JLabel rootElementType;
	
}
