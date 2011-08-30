package plugins.treePlugin.treeDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.treePlugin.tree.DrawableNode;
import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.Tree;
import plugins.treePlugin.tree.reading.TreeQueueManager;
import plugins.treePlugin.tree.reading.TreeReader;
import plugins.treePlugin.treeFigure.MultiTreeChart;

import display.Display;
import display.DisplayData;

import java.util.Iterator;

import topLevelGUI.SunFishFrame;

import guiWidgets.glassDropPane.GlassDropPane;
import guiWidgets.glassDropPane.GlassPaneThing;
import guiWidgets.CFButton;

public class MultiTreeDisplay extends Display {

	public static final String NO_NODE_LABELS = "None";
	public static final String DEPTH_NODE_LABELS = "Depth";
	public static final String HEIGHT_NODE_LABELS = "Height";
	public static final String BRANCH_LENGTH_NODE_LABELS = "Length";
	String nodeLabelType = NO_NODE_LABELS;
	
	public static final String RIGHT_POSITION = "Right";
	public static final String UPPER_LEFT_POSITION = "Upper left";
	public static final String LOWER_LEFT_POSITION = "Lower left";
	
	final static String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	final static Integer[] fontSizeChoices = {5,6,7,8,9,10,11,12,13,14,16,18,20,22,24,30,36,40,44,48,64,72};
	NumberFormat formatter = new DecimalFormat("0.0###");
	
	private JPanel parentPanel;
	private GlassDropPane dropPane;
	private GlassPaneThing optionsPane;
	private GlassPaneThing labelsPane;
	private GlassPaneThing layoutPane;
	private GlassPaneThing selectionPane;
	private GlassPaneThing editingPane;
	private CFButton saveButton;
	private JButton reattachButton;
	JSpinner rowsSpinner;
	JSpinner colsSpinner;
	private JScrollPane scrollPane;
	
	String iconPath;
	MultiTreeChart chart;
	
	TreeQueueManager treeQ;
	
	boolean previousAdvance = true;
	private JComboBox nodeLabelBox;
	private JComboBox labelPositionBox;
	private JComboBox fontFaceList;
	private JComboBox fontSizes;

	Font smallFont;
	
	public MultiTreeDisplay(SunFishFrame sunfishParent) {
		super(sunfishParent);
		this.iconPath = sunfishParent.getIconPath();
		smallFont = new Font("Sans", Font.PLAIN, 10);
		setLayout(new BorderLayout());
		setBorder(null);
	}

	@Override
	public String getName() {
		return "Multi-tree display";
	}


	@Override
	public String getDescription() {
		return "Display multiple trees at once";
	}


	@Override
	public double getVersionNumber() {
		return 1.0;
	}


	@Override
	public Class[] getDisplayableClasses() {
		return new Class[]{TreeReader.class};
	}
	
	public String getFileName() {
		return filename;
	}


	public Display getNew() {
		return new MultiTreeDisplay(sunfishParent);
	}


	public void lostFocus() {
		dropPane.closeAllTabs();
		sunfishParent.getGlassPane().setVisible(false);
		myFrame.getGlassPane().setVisible(false);
	}
	
	public void construct() {
		parentPanel = new JPanel();
		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));
		parentPanel.setBackground(Color.white);
		JPanel topPanel = new JPanel();
		topPanel.setBackground(Color.white);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.setMinimumSize(new Dimension(100, 25));
		topPanel.setMaximumSize(new Dimension(5000, 25));
		
		dropPane = new GlassDropPane(sunfishParent);
		
		optionsPane = new GlassPaneThing(dropPane, parentPanel);
		optionsPane.setVisible(false);
		
		labelsPane = new GlassPaneThing(dropPane, parentPanel);
		labelsPane.setVisible(false);
		
		layoutPane = new GlassPaneThing(dropPane, parentPanel);
		layoutPane.setVisible(false);
		
		selectionPane = new GlassPaneThing(dropPane, parentPanel);
		selectionPane.setVisible(false);

		editingPane = new GlassPaneThing(dropPane, parentPanel);
		editingPane.setVisible(false);
		
		Font displayFont = new Font("Sans", Font.PLAIN, 12);
		saveButton = new CFButton(new ImageIcon(iconPath + "save_22x22.png"), "Save image");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                saveButtonPressed();
	            }
	    });
		optionsPane.addComponent(saveButton);


		JLabel nodetext = new JLabel("Label :");
		nodetext.setFont(displayFont);
		labelsPane.addComponent(nodetext);
		
		String[] nodeLabelChoices = {"None", "Depth", "Height", "Length", "User"};
		nodeLabelBox = new JComboBox(nodeLabelChoices);
		nodeLabelBox.setSelectedIndex(0);
		nodeLabelBox.setFont(displayFont);
		nodeLabelBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodeLabelBoxAction();
            }
        });
		labelsPane.addComponent(nodeLabelBox);
		
		labelsPane.addComponent(new JLabel("Position:"));
		String[] labelPosChoices = {RIGHT_POSITION, UPPER_LEFT_POSITION, LOWER_LEFT_POSITION};
		labelPositionBox = new JComboBox(labelPosChoices);
		labelPositionBox.setSelectedIndex(0);
		labelPositionBox.setFont(displayFont);
		labelPositionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelPositionBoxAction();
            }
        });
		labelsPane.addComponent(labelPositionBox);
		
		
		String[] fontChoices = fontNames; 
		fontFaceList = new JComboBox(fontChoices);
		fontFaceList.setRenderer(new FontListCellRenderer(fontChoices,  fontSizeChoices[7]) );
		fontFaceList.setSelectedIndex( findDefaultFontIndex() );
		fontFaceList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	labelFontFaceAction();
            }
        });
		labelsPane.addComponent(fontFaceList);
		

		fontSizes = new JComboBox(fontSizeChoices);
		fontSizes.setSelectedIndex(6); //should be about 11
		fontSizes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	labelFontSizeAction();
            }
        });
		labelsPane.addComponent(fontSizes);
		
		reattachButton = new JButton("Reattach");
		reattachButton.setToolTipText("Move to main window");
		reattachButton.setEnabled(false);
		reattachButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                reattachButtonPressed();
	            }
	    });
		optionsPane.addComponent(reattachButton);
		
		
		JLabel rowsLabel = new JLabel("Rows");
		layoutPane.addComponent(rowsLabel);
		SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 100, 1);
		rowsSpinner = new JSpinner(model);
		rowsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				chart.setRows((Integer)rowsSpinner.getValue());
				updateList();

			}
		});
		layoutPane.addComponent(rowsSpinner);
		
		JLabel colsLabel = new JLabel("Columns " );
		layoutPane.addComponent(colsLabel);
		model = new SpinnerNumberModel(3, 1, 100, 1);
		colsSpinner = new JSpinner(model);
		colsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				chart.setCols((Integer)colsSpinner.getValue());
				updateList();
			}
		});
		layoutPane.addComponent(colsSpinner);
		
		dropPane.addPanel("Options", optionsPane);
		dropPane.addPanel("Labels", labelsPane);
		dropPane.addPanel("Layout", layoutPane);
		//dropPane.addPanel("Selection", selectionPane);
		//dropPane.addPanel("Editing", editingPane);
		dropPane.setPreferredWidth(550);
		chart = new MultiTreeChart(this);
		chart.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPanel.add(dropPane);
		
		topPanel.add(Box.createHorizontalGlue());
		previousTreeButton = new CFButton(new ImageIcon(iconPath + "backward_16x16.png"));
		previousTreeButton.setToolTipText("Display previous tree");
		previousTreeButton.setEnabled(true);
		previousTreeButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                stepBack();
	            }
	    });
		topPanel.add(previousTreeButton);
		
		
		nextTreeButton = new CFButton(new ImageIcon(iconPath + "forward_16x16.png") );
		nextTreeButton.setToolTipText("Display next tree");
		nextTreeButton.setEnabled(true);
		nextTreeButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                advance();
	            }
	    });
		topPanel.add(nextTreeButton);
		
		parentPanel.add(topPanel);
		parentPanel.add(chart);
		scrollPane = new JScrollPane(parentPanel);
		add(scrollPane, BorderLayout.CENTER);
		dropPane.setParentForClip(scrollPane);
	}

	
	public SunFishFrame getSunFishParent() {
		return sunfishParent;
	}
	/**
	 * Called when matrix dimensions in the chart have changed so we can either
	 * load or dispense with the correct number of trees
	 */
	protected void updateList() {
		int currentSize = chart.getQueueSize();
		int matrixSize = chart.getMatrixSize();

		if(matrixSize > currentSize) {
			chart.addTrees(matrixSize - currentSize);
		}
		else {
			chart.removeTrees(currentSize-matrixSize);
		}
		chart.repaint();
	}



	protected String getNodeLabel(DrawableNode n, DrawableTree tree) {
		String nodeLabelType = (String)nodeLabelBox.getSelectedItem();
		if (nodeLabelType == HEIGHT_NODE_LABELS) {
			return formatter.format( tree.getHeight() - Tree.getDistToRoot(n) );

		}
		if (nodeLabelType == DEPTH_NODE_LABELS) {
			return formatter.format( Tree.getDistToRoot(n) );
		}
		if (nodeLabelType == BRANCH_LENGTH_NODE_LABELS) {
			return formatter.format( n.getDistToParent() );
		}
		//If we're here the nodeLabelType isn't a predefined type, so maybe it's an annotation, look for one that matches
		String val = n.getAnnotationValue(nodeLabelType);
		if (val==null) 
			return "";
		try { //If we can parse a double from the string, try to format it
			double x = Double.parseDouble(val);
			val = formatter.format(x);
		} catch (NumberFormatException nfe) {
			
		}
		return val; 
	}
	
	public ArrayList<DrawableNode> getAllSelectedNodes() {
		ArrayList<DrawableNode> selectedNodes = new ArrayList<DrawableNode>();
		for(DrawableTree tree : chart.getCurrentQueue()) {
			selectedNodes.addAll(tree.getSelectedNodes());
		}
		
		return selectedNodes;
	}
	
	public void nodeLabelBoxAction() {
		ArrayList<DrawableNode> selectedNodes; //= new ArrayList<DrawableNode>();
		
		for(DrawableTree tree : chart.getCurrentQueue()) {
			selectedNodes = tree.getSelectedNodes();
			
			for(DrawableNode node : selectedNodes) {
				node.setCurrentLabel( getNodeLabel(node, tree));	
			}
		}
			
		chart.repaint();
	}
	
	protected void labelPositionBoxAction() {
		ArrayList<DrawableNode> nodes = getAllSelectedNodes();	
		
		String labelPosition = (String)labelPositionBox.getSelectedItem();
		
		for(DrawableNode node : nodes) {
			node.setLabelPosition(labelPosition);
		}
		
		chart.repaint();
		
	}

	protected void labelFontSizeAction() {
		ArrayList<DrawableNode> nodes = getAllSelectedNodes();
		
		int sizeIndex = fontSizes.getSelectedIndex();
		float fontSize = fontSizeChoices[sizeIndex];
		
		for(DrawableNode node : nodes) {
			node.setFontSize(fontSize);
		}
		
		chart.repaint();
	}
	
	protected void labelFontFaceAction() {
		ArrayList<DrawableNode> nodes = getAllSelectedNodes();	
		
		int fontIndex = fontFaceList.getSelectedIndex();
		String face = fontNames[fontIndex];
		
		for(DrawableNode node : nodes) {
			node.setFontFace(face);
		}
		
		chart.repaint();
	}
	
	

	
	/**
	 * Applies the current settings of label type, position, font, etc to the node
	 * @param node
	 */
	private void applyCurrentSettings(DrawableTree tree) {
		int fontIndex = fontFaceList.getSelectedIndex();
		String face = fontNames[fontIndex];
		int sizeIndex = fontSizes.getSelectedIndex();
		float fontSize = fontSizeChoices[sizeIndex];
		String labelPosition = (String)labelPositionBox.getSelectedItem();
		
		for(DrawableNode node : tree.getAllInternalDrawableNodes()) {
			node.setCurrentLabel( getNodeLabel(node, tree));
			node.setFontFace(face);
			node.setFontSize(fontSize);
			node.setLabelPosition(labelPosition);
		}
	}
	
	public void advance() {
		if (chart.isBusy())
			return;

		chart.advance();
	}
	
	public void stepBack() {
		if (chart.isBusy())
			return;

		chart.goback();
	}


	protected void reattachButtonPressed() {
		dropPane.closeAllTabs();
		reattach();		
		sunfishParent.getDisplayPane().setSelectedComponent(this);
	}

	
	public void setFrame(JFrame newFrame) {
		this.myFrame = newFrame;
		dropPane.changeParent(myFrame);
		if (myFrame==sunfishParent) {
			reattachButton.setEnabled(false);
		}
		else {
			reattachButton.setEnabled(true);
		}
	}
	
	public void saveButtonPressed() {
    	BufferedImage chartImage = chart.getImage(); 
		
    	JFileChooser saveChart = new JFileChooser();
    	int val = saveChart.showSaveDialog(sunfishParent);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = saveChart.getSelectedFile();
    		try {
    			ImageIO.write(chartImage, "png", file);
    		}
    		catch(IOException ioe) {
    			sunfishParent.getLogger().warning("Error saving chart image to file : " + ioe.toString() );
    		}
    	}		
	}
	
	public void saveToFile(File f) {
		throw new IllegalStateException("Saving is not implemented for MultiTreeDisplays. Sorry.");
	}

	protected boolean update(SunFishFrame parent, DisplayData data) {
		this.sunfishParent = parent;
		this.myFrame = parent;
		Object obj = data.getData(0);
		if (! (obj instanceof TreeReader)) {
			sunfishParent.getLogger().warning("Got non-treeReader for data in MultiTreeDisplay");
			return false;
		}
		
		TreeReader treeReader = (TreeReader)obj;
		treeQ = new TreeQueueManager(treeReader);
		
		DrawableTree tree = null;

		int total = chart.getMatrixSize();
		chart.setTreeQueue(treeQ);
		chart.addTrees(total);
		Iterator<DrawableTree> it = treeQ.iterator();
		while(it.hasNext()) {
			tree = it.next();
			applyCurrentSettings(tree);
		}
		
		chart.repaint();
		return true;
	}

	
	private int findDefaultFontIndex() {
		int index = -1;
		for(int i=0; i<fontNames.length; i++) {
			if (fontNames[i].equals("Lucida Sans") ) {
				index = i;
				i = fontNames.length;
			}
		}
			
		//If we found Lucida Sans, return the index
		//else, look for choice number 2
		if (index>-1)
			return index;
		else {
			for(int i=0; i<fontNames.length; i++) {
				if (fontNames[i].equals("SansSerif") ) {
					index = i;
					i = fontNames.length;
				}
			}	
		}
		
		//If we found SansSerif, return the index
		//else, look for choice number 3
		if (index>-1)
			return index;
		else {
			for(int i=0; i<fontNames.length; i++) {
				if (fontNames[i].equals("Sans") ) {
					index = i;
					i = fontNames.length;
				}
			}	
		}
		
		return 1;
		
	}
	
	
	class FontListCellRenderer extends DefaultListCellRenderer {

		String[] fontNames;
		JLabel label;
		int fontSize;
		
		public FontListCellRenderer(String[] fontNames, int fontSize) {
			super();
			this.fontSize = fontSize;
			this.fontNames = fontNames;
			label = new JLabel();
		}
		
		
		public Component getListCellRendererComponent(JList list,
	            Object value,
	            int index,
	            boolean isSelected,
	            boolean cellHasFocus) {
			
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (index>=0 && index<fontNames.length) {
				Font newFont = new Font(fontNames[index], Font.PLAIN, 12);

				if (newFont == null || ! newFont.canDisplay('e'))
					newFont = new Font("Sans", Font.PLAIN, 12);

				comp.setFont( newFont );
			}

			return comp;
		}
	}

	
	JButton nextTreeButton;
	JButton previousTreeButton;



}
