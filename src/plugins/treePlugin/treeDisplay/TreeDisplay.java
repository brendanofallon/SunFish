package plugins.treePlugin.treeDisplay;

import display.Display;
import display.DisplayData;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

import guiWidgets.glassDropPane.GlassDropPane;
import guiWidgets.glassDropPane.GlassPaneThing;
import guiWidgets.CFButton;
import guiWidgets.ColorSwatchButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.treePlugin.tree.DrawableTree;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import plugins.treePlugin.tree.*;
import plugins.treePlugin.tree.drawing.BranchPainter;
import plugins.treePlugin.tree.drawing.CurvedBranchPainter;
import plugins.treePlugin.tree.drawing.LineBranchPainter;
import plugins.treePlugin.treeFigure.TreeFigure;
import undoRedo.UndoableAction;

import fontChooser.*;

/**
 * A display for a single tree. We allow selection of individual nodes, changing the appearence
 * of the branches and labels, selecting / removing clades, and many other options.
 * The tree is actually drawn using TreeFigure (which, in turn, uses a TreeDrawer to handle
 * the drawing...)
 * 
 * @author brendan
 *
 */
public class TreeDisplay extends Display {

	static final double VERSION = 1.0;
	
	public static final String NO_NODE_LABELS = "None";
	public static final String DEPTH_NODE_LABELS = "Depth";
	public static final String HEIGHT_NODE_LABELS = "Height";
	public static final String BRANCH_LENGTH_NODE_LABELS = "Length";
	String nodeLabelType = NO_NODE_LABELS;
	
	public static final String RIGHT_POSITION = "Right";
	public static final String UPPER_LEFT_POSITION = "Upper left";
	public static final String LOWER_LEFT_POSITION = "Lower left";
	
	protected JScrollPane scrollPane;
	String iconPath = "./icons";

	protected boolean showPane = false;
	protected JLabel lab1;
	protected GlassPaneThing labelsPane;
	protected GlassPaneThing appearencePane;
	protected GlassPaneThing selectionPane;
	protected GlassPaneThing editingPane;
	protected GlassPaneThing optionsPane;
	protected boolean somethingIsSelected = false;
	protected Color nodeColor = Color.red;
	protected NumberFormat formatter = new DecimalFormat("0.0###");
	
	protected Font nullFont = new Font("Sans", Font.PLAIN, 0);
	
	protected String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	protected Integer[] fontSizeChoices = {5,6,7,8,9,10,11,12,13,14,16,18,20,22,24,30,36,40,44,48,64,72};
	
	protected ArrayList<String> nodeAnnotationKeys = null;
	private JComboBox directionBox;
	
	double zoomFactor = 1.0;
	
	public TreeDisplay(SunFishFrame parent) {
		super(parent);

		this.myFrame = parent;
		filename = "(no name)";
		if (parent.getIconPath()==null || parent.getIconPath()=="")
			this.iconPath = "./icons";
		else
			this.iconPath = parent.getIconPath();

		setLayout(new BorderLayout());
		setBorder(null);
	}

	@Override
	public String getName() {
		return "Tree display";
	}

	@Override
	public String getDescription() {
		return "Visualize and edit phylogenetic trees";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
	
	/**
	 * Called when this display tab is no longer showing, we need to know this so the GlassDropPane
	 * can be hidden
	 */
	public void lostFocus() {
		super.lostFocus();
		dropPane.closeAllTabs();
		sunfishParent.getGlassPane().setVisible(false);
		myFrame.getGlassPane().setVisible(false);
	}
	
	@Override
	public Class[] getDisplayableClasses() {
		return new Class[]{DrawableTree.class};
	}
	
	/**
	 * Called when reattach button is pressed to we can set our parent frame back to the original
	 * sunfish frame (which is required for the glass drop pane to function correctly). We also
	 * use this as a hook to set the size of the chart to fit within the scrollpane, by
	 * calling zoomToParentSize
	 */
	public void setFrame(JFrame newFrame) {
		this.myFrame = newFrame;
		dropPane.changeParent(myFrame);
		if (myFrame==sunfishParent) {
			reattachButton.setEnabled(false);
			zoomToParentSize(); //Restores zoom / chart size to fit exactly within the scrollPane
		}
		else {
			reattachButton.setEnabled(true);
		}
	}
	
	public void saveButtonPressed() {
    	BufferedImage chartImage = chart.getImage(); 
  
    	int val = fileChooser.showSaveDialog(sunfishParent);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = fileChooser.getSelectedFile();
    		try {
    			ImageIO.write(chartImage, "png", file);
    		}
    		catch(IOException ioe) {
    			SunFishFrame.getSunFishFrame().getLogger().warning("Error saving chart image to file : " + ioe.toString() );
    		}
    	}		
	}
	
	public void saveToFile(File file) {
		try {
			int n = JOptionPane.YES_OPTION;
			if (file.exists()) {
				 n = JOptionPane.showConfirmDialog(
					    sunfishParent,
					    "Overwrite existing file " + file.getName() + "?",
					    "File exists",
					    JOptionPane.YES_NO_OPTION);
			}
			
			if (n==JOptionPane.YES_OPTION) {
				FileWriter writer = new FileWriter(file);
				for(DrawableTree tree : chart.getTrees()) {
					String newick = tree.getNewick();
					writer.write(newick + "\n");
				}
				setHasUnsavedChanges(false);
				writer.close();
			}
		}
		catch(IOException ioe) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Error saving chart image to file : " + ioe.toString() );
		}
	}
	
	public void saveNewickButtonPressed() {
    	int val = fileChooser.showSaveDialog(sunfishParent);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = fileChooser.getSelectedFile();
    		saveToFile(file);
    	}		
	}

	
	/**
	 * Returns true if this display can accept drops (as in Drag and Drop) of the specified
	 * file. Returning false (the default) means that this display will ignore the file
	 * 
	 * @param file
	 * @return True if we can handle having this file dropped on us.
	 */
	public boolean acceptDrop(File file) {
		FileParser parser = SunFishFrame.getSunFishFrame().getParserForFileAndClass(file, DrawableTree.class); 
		if (parser==null)
			return false;
		else {
			if (parser.getDataClass() == Tree.class || parser.getDataClass() == DrawableTree.class) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This is called if a file has been dropped on us and we returned 'true' to acceptDrop
	 * @param file
	 */
	public void fileDropped(File file) {
		FileParser parser = SunFishFrame.getSunFishFrame().getParserForFileAndClass(file, DrawableTree.class); 
		if (parser==null)
			return;
		else {
			try {
				Object obj = parser.readFile(file);
				if (obj instanceof DrawableTree) {
					chart.addTree( (DrawableTree)obj );
					repaint();
				}
			} catch (IOException e) {
				SunFishFrame.getSunFishFrame().getLogger().warning("Error reading file to parse for tree drop, ignoring drop of file: " + file.getPath());
			} catch (FileParseException e) {
				SunFishFrame.getSunFishFrame().getLogger().warning("Error parsing file for tree drop, ignoring drop of file: " + file.getPath());
			}
			
		}
	}
	
	public void setSomethingIsSelected(boolean sis) {
		somethingIsSelected = sis;
		if (somethingIsSelected) {
			highlightSelection.setEnabled(true);
			collapseSelection.setEnabled(true);
			rotateNodes.setEnabled(true);
			rerootButton.setEnabled(true);
			removeNodeButton.setEnabled(true);
			removeCladeButton.setEnabled(true);
			extractTreeButton.setEnabled(true);
		}
		else {
			highlightSelection.setEnabled(false);
			collapseSelection.setEnabled(false);
			rotateNodes.setEnabled(false);
			rerootButton.setEnabled(false);
			removeNodeButton.setEnabled(false);
			removeCladeButton.setEnabled(false);			
			extractTreeButton.setEnabled(false);
		}
	}

	public void chooseColorAction() {
		Color newColor = JColorChooser.showDialog(myFrame, "Node Color", nodeColor);
		nodeColor = newColor;
		colorNodeButton.setColor(nodeColor);
		colorNodeButton.repaint();
	}
	
	protected boolean update(SunFishFrame parent, DisplayData data) {
		if (! (data.getData(0) instanceof DrawableTree)) {
			System.err.println("Tree Display got a non-drawabletree data object");
			SunFishFrame.getSunFishFrame().getLogger().warning(" Tree Display got a non-drawabletree data object ");
			return false;
		}
		
		SunFishFrame.getSunFishFrame().setInfoLabelText("Updating tree display");
		this.filename = data.getFileName();

		this.sunfishParent = parent;

		chart.addTree( (DrawableTree)data.getData(0) );
		scaleBoxAction(); //Sets the appropriate scale type for the tree 
		chart.repaint();
		
		nodeAnnotationKeys = collectAllAnnotationKeys();
		String[] nodeLabelChoices = new String[4 + nodeAnnotationKeys.size()];
		nodeLabelChoices[0] = "None";
		nodeLabelChoices[1] = "Depth";
		nodeLabelChoices[2] = "Height";
		nodeLabelChoices[3] = "Length";
		for(int i=0; i<nodeAnnotationKeys.size(); i++) {
			nodeLabelChoices[i+4] = nodeAnnotationKeys.get(i);
		}
		
		ComboBoxModel nodeLabels = new DefaultComboBoxModel(nodeLabelChoices);
		nodeLabelBox.setModel(nodeLabels);
		
		if (parent.getAnalysisPane().getCurrentAnalyzer()==null) {
			TreeSummary summary = new TreeSummary(this);
			summary.analyze(filename, (DrawableTree)data.getData(0));
			parent.displayOutput(summary);
		}
		return true;
	}
	
	/**
	 * Create a non-redundant list of all the annotation keys present in all the trees. 
	 * @return
	 */
	private ArrayList<String> collectAllAnnotationKeys() {
		ArrayList<String> allKeys = new ArrayList<String>();
		for(DrawableTree tree : chart.getTrees()) {
			ArrayList<String> theseKeys = tree.collectAnnotationKeys();
			for(String key : theseKeys) {
				if (! allKeys.contains(key)) {
					allKeys.add(key);
				}
			}
		}
		return allKeys;
	}

	public void ignoreBranchLengthsAction() {
		for(DrawableTree tree : chart.getTrees()) {
			tree.setIgnoreBranchLengths(! ignoreBranchLengths.isSelected());
		}
		chart.repaint();
	}
	
	/**
	 * Return all selected nodes in all trees
	 * @return
	 */
	public List<DrawableNode> getAllSelectedNodes() {
		List<DrawableNode> nodes = new ArrayList<DrawableNode>();
		for(DrawableTree tree : chart.getTrees()) {
			nodes.addAll(tree.getSelectedNodes());
		}
		return nodes;
	}

	/**
	 * Changes the current label type for all selected nodes
	 */
	public void nodeLabelBoxAction() {
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		for(DrawableTree tree : chart.getTrees()) {
			treesBefore.add(tree.clone());
		}
		
		for(DrawableTree tree : chart.getTrees()) {
			List<DrawableNode> nodes = tree.getSelectedNodes();
			for(DrawableNode node : nodes) {
				node.setCurrentLabel( getNodeLabel(tree, node));
			}
		}
		
		TreeUndoableAction typeAction = new TreeUndoableAction(this, treesBefore, "Change labels");
		undoManager.postNewAction(typeAction);

		chart.repaint();
	}
	
	/**
	 * Returns a String representing the label for the given node
	 * @param n
	 * @return
	 */
	protected String getNodeLabel(DrawableTree tree, DrawableNode n) {
		String nodeLabelType = (String)nodeLabelBox.getSelectedItem();

		if (nodeLabelType == HEIGHT_NODE_LABELS) {
			if (n != tree.getRoot()) {
				return formatter.format( tree.getHeight() - Tree.getDistToRoot(n) );
			}
			else 
				return "0.0";
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
	
	public void scaleBoxAction() {
		chart.setScaleType( scaleBox.getSelectedIndex() );
		chart.repaint();
	}

	
	public void selectionBoxAction() {
		chart.setSelectionMode(selectionBox.getSelectedIndex());
		chart.repaint();
	}
	
	public void nodeColorAction() {
		for(DrawableNode n : getAllSelectedNodes()) {
			n.setBranchColor(colorNodeButton.getColor());
		}
		chart.repaint();
	}
	
	public void rotateAction() {
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		for(DrawableTree tree : chart.getTrees()) {
			treesBefore.add(tree.clone());
		}
		
		for(DrawableTree tree : chart.getTrees()) {
			for(DrawableNode n : tree.getSelectedNodes()) {
				tree.rotateNode(n);
				
			}
		}
		
		TreeUndoableAction rotateAction = new TreeUndoableAction(this, treesBefore, "Rotate nodes");
		undoManager.postNewAction(rotateAction);
		chart.repaint();
	}
	
	private void rerootAction() {
		ErrorWindow.showErrorWindow(new Exception("Rerooting isn't implemented yet, sorry"), null);
//		currentTree.reroot(currentTree.getSelectedNodes().get(0));
//		Node newroot = currentTree.getSelectedNodes().get(0);
//		if (newroot == null) {
//			System.err.println("Couldn't get a selected node to reroot...");
//			return;
//		}
//
//		if (newroot.isLeaf()) {
//			currentTree.addNodeBeforeParent(newroot);
//			newroot = newroot.getParent();
//		}
//		currentTree.reroot(newroot);
//		chart.repaint();
	}


	protected void removeCladeAction() {
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		boolean postAction = false;
		for(DrawableTree tree : chart.getTrees()) {
			treesBefore.add(tree.clone());
			while(tree.getSelectedNodes().size()>0) {
				Node n = tree.getSelectedNodes().get(0);
				tree.removeClade(n);
				setHasUnsavedChanges(true);
				postAction = true;
			}
		}
		
		if (postAction) {
			TreeUndoableAction removeAction = new TreeUndoableAction(this, treesBefore, "Remove clades");
			undoManager.postNewAction(removeAction);
		}
		chart.repaint();
	}

	protected void removeSelectedNodes() {
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		boolean postAction = false;
		for(DrawableTree tree : chart.getTrees()) {
			treesBefore.add(tree.clone());
			while(tree.getSelectedNodes().size()>0) {
				Node n = tree.getSelectedNodes().get(0);
				tree.removeNode(n);
				setHasUnsavedChanges(true);
				postAction = true;
			}
		}
		
		if (postAction) {
			TreeUndoableAction removeAction = new TreeUndoableAction(this, treesBefore, "Remove nodes");
			undoManager.postNewAction(removeAction);
		}
		chart.repaint();		
	}
	
	
	public void construct() {
		SunFishFrame.getSunFishFrame().setInfoLabelText("Constructing tree display");
		parentPanel = new JPanel();
		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
		parentPanel.setBackground(Color.white);

		fontChooser = new FontChooser();
		Font textFont = new Font("Sans", Font.PLAIN, 11);
		
		dropPane = new GlassDropPane(sunfishParent);
		
		optionsPane = new GlassPaneThing(dropPane, parentPanel);
		optionsPane.setVisible(false);
		
		labelsPane = new GlassPaneThing(dropPane, parentPanel);
		labelsPane.setVisible(false);
		
		appearencePane = new GlassPaneThing(dropPane, parentPanel);
		appearencePane.setVisible(false);
		
		selectionPane = new GlassPaneThing(dropPane, parentPanel);
		selectionPane.setVisible(false);

		editingPane = new GlassPaneThing(dropPane, parentPanel);
		editingPane.setVisible(false);
		
		Font displayFont = new Font("Sans", Font.PLAIN, 12);
		saveButton = new CFButton(new ImageIcon(iconPath + "save_22x22.png"), "Save image");
		saveButton.setFont(textFont);
		saveButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                saveButtonPressed();
	            }
	    });
		optionsPane.addComponent(saveButton);

		saveNewickButton = new CFButton(new ImageIcon(iconPath + "save_22x22.png"), "Save Newick string");
		saveNewickButton.setFont(textFont);
		saveNewickButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                saveNewickButtonPressed();
	            }
	    });
		optionsPane.addComponent(saveNewickButton);


		reattachButton = new JButton("Reattach");
		reattachButton.setFont(textFont);
		reattachButton.setToolTipText("Move to main window");
		reattachButton.setEnabled(false);
		reattachButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                reattachButtonPressed();
	            }
	    });
		optionsPane.addComponent(reattachButton);
		
		redrawButton = new JButton("Redraw");
		redrawButton.setFont(textFont);
		redrawButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                redrawButtonPressed();
	            }
	    });
		optionsPane.addComponent(redrawButton);

		
		optionsPane.addComponent(new JLabel("Type:"));
		String[] treeTypes = {"Square", "Equal Angle"};
		treeTypeBox = new JComboBox(treeTypes);
		treeTypeBox.setSelectedIndex(0);
		treeTypeBox.setFont(displayFont);
		treeTypeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeTypeBoxAction();
            }
        });
		optionsPane.addComponent(treeTypeBox);
		
		optionsPane.addComponent(new JLabel("Direction:"));
		String[] directions = {"Right", "Left", "Up", "Down"};
		directionBox = new JComboBox(directions);
		directionBox.setSelectedIndex(0);
		directionBox.setFont(displayFont);
		directionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeDirectionBoxAction();
            }
        });
		optionsPane.addComponent(directionBox);		
		

		optionsPane.addComponent(new JLabel("Scale:"));
		String[] scaleChoices = {"None", "Bar", "Axis"};
		scaleBox = new JComboBox(scaleChoices);
		scaleBox.setSelectedIndex(1);
		scaleBox.setFont(displayFont);
		scaleBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleBoxAction();
            }
        });
		optionsPane.addComponent(scaleBox);		
		
		JLabel nodetext = new JLabel("Label type: ");
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
		//fontFaceList.setMinimumSize(new Dimension(50, 24));
		fontFaceList.setSelectedIndex( findDefaultFontIndex() );
		fontFaceList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	labelFontFaceAction();
            }
        });
		labelsPane.addComponent(fontFaceList);
		

		fontSizes = new JComboBox(fontSizeChoices);
		fontSizes.setSelectedIndex(7); //should be about 12
		fontSizes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	labelFontSizeAction();
            }
        });
		labelsPane.addComponent(fontSizes);
	
		ignoreBranchLengths = new JCheckBox("Use branch lengths");
		ignoreBranchLengths.setSelected(true);
		ignoreBranchLengths.setFont(displayFont);
		ignoreBranchLengths.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreBranchLengthsAction();
            }
		});
		appearencePane.addComponent(ignoreBranchLengths);
		
		showErrorBarBox = new JCheckBox("Error bars");
		showErrorBarBox.setSelected(true);
		showErrorBarBox.setFont(displayFont);
		showErrorBarBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showErrorBarBoxAction();
            }
		});
		appearencePane.addComponent(showErrorBarBox);
		
		
		branchRoundnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 85);
		branchRoundnessSlider.setPaintTicks(true);
		branchRoundnessSlider.setPaintLabels(false);
		branchRoundnessSlider.setPreferredSize(new Dimension(80, 20));
		branchRoundnessSlider.setFocusable(false);
		branchRoundnessSlider.setAlignmentY(Component.CENTER_ALIGNMENT);
		branchRoundnessSlider.setFont(nullFont);
		branchRoundnessSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				adjustBranchArc();
			}		
		});
		JLabel roundnessLabel = new JLabel("Branch arc:");
		roundnessLabel.setFont(displayFont);
		appearencePane.addComponent(roundnessLabel);
		appearencePane.addComponent(branchRoundnessSlider);
		
		SpinnerNumberModel numberModel = new SpinnerNumberModel(1.0, 0.01, 20.0, 0.1);
		lineWidthSpinner = new JSpinner(numberModel);
		lineWidthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				changeLineWidth((Double)lineWidthSpinner.getValue());	
			}
		});
		JLabel lineWidthLabel = new JLabel("Branch width:");
		lineWidthLabel.setFont(displayFont);
		appearencePane.addComponent(lineWidthLabel);
		appearencePane.addComponent(lineWidthSpinner);
		
		
		
		
		String[] selectionChoices = {"Nodes", "Clades", "Descendants", "Unselect"};
		selectionBox = new JComboBox(selectionChoices);
		selectionBox.setSelectedIndex(0);
		selectionBox.setFont(displayFont);
		selectionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionBoxAction();
            }
        });
		selectionPane.addComponent(new JLabel("Mode: "));
		selectionPane.addComponent(selectionBox);
		
		highlightSelection = new CFButton("Color: ");
		highlightSelection.setFont(textFont);
		highlightSelection.setEnabled(false);
		highlightSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodeColorAction();
            }
        });
		selectionPane.addComponent(highlightSelection);
		
		colorNodeButton = new ColorSwatchButton(Color.red);
		//colorNodeButton.setMinimumSize(new Dimension(25, 25));
//		colorNodeButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                chooseColorAction();
//            }
//        });
//		colorNodeButton.addPropertyChangeListener(new PropertyChangeListener() {
//			public void propertyChange(PropertyChangeEvent evt) {
//				if (evt.getPropertyName().equals("Swatch Color")) {
//					nodeColorAction();
//				}
//			}			
//		});
		selectionPane.addComponent(colorNodeButton);
		
		collapseSelection = new CFButton("Cartoon", new ImageIcon(iconPath + "cartoonIcon.png") );
		collapseSelection.setFont(textFont);
		collapseSelection.setEnabled(false);
		collapseSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapseAction();
            }
        });
		selectionPane.addComponent(collapseSelection);
		
		rotateNodes = new CFButton("Rotate", new ImageIcon(iconPath + "rotateIcon.png"));
		rotateNodes.setFont(textFont);
		rotateNodes.setEnabled(false);
		rotateNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotateAction();
            }
        });
		selectionPane.addComponent(rotateNodes);
	
		extractTreeButton = new CFButton("Extract");
		extractTreeButton.setFont(textFont);
		extractTreeButton.setEnabled(false);
		extractTreeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exractTreeAction();
            }
        });
		selectionPane.addComponent(extractTreeButton);
		
		rerootButton = new CFButton("Reroot");
		rerootButton.setFont(textFont);
		rerootButton.setEnabled(false);
		rerootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rerootAction();
            }
        });
		editingPane.addComponent(rerootButton);

		removeNodeButton = new CFButton("Remove Node");
		removeNodeButton.setFont(textFont);
		removeNodeButton.setEnabled(false);
		removeNodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedNodes();
            }
        });
		editingPane.addComponent(removeNodeButton);

		removeCladeButton = new CFButton("Remove Clade");
		removeCladeButton.setFont(textFont);
		removeCladeButton.setEnabled(false);
		removeCladeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCladeAction();
            }
        });
		editingPane.addComponent(removeCladeButton);
		
		dropPane.addPanel("Options", optionsPane);
		dropPane.addPanel("Labels", labelsPane);
		dropPane.addPanel("Appearence", appearencePane);
		dropPane.addPanel("Selection", selectionPane);
		dropPane.addPanel("Editing", editingPane);
		chart = new TreeFigure(this);
		chart.setAlignmentX(Component.LEFT_ALIGNMENT);
		chart.addMouseListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent me) {
				requestFocusInWindow();		
			}
			
			public void mouseClicked(MouseEvent me) {
				requestFocusInWindow();		
			}
		});
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBackground(Color.white);
		topPanel.setPreferredSize(new Dimension(1, 22));
		topPanel.setPreferredSize(new Dimension(400, 22));
		topPanel.setMaximumSize(new Dimension(16384, 22));

		topPanel.add(dropPane, BorderLayout.WEST);
		
		zoomSlider = new JSlider();
		zoomSlider.setValue(1);
		zoomSlider.setMinimum(1);
		zoomSlider.setMaximum(100000);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				zoomValueChanged();
			}
		});
		
		zoomSlider.setPreferredSize(new Dimension(100, 20));
		zoomSlider.setFont(new Font("Sans", Font.PLAIN, 0));
		zoomSlider.setPaintLabels(false);
		zoomSlider.setPaintTicks(false);
		
		topPanel.add(zoomSlider, BorderLayout.EAST);
		
		parentPanel.add(topPanel);
		parentPanel.setBackground(Color.white);
		
		parentPanel.add(chart);
		scrollPane = new JScrollPane(chart);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		dropPane.setParentForClip(scrollPane);
	}

	
	
	
	/**
	 * Adjusts the component so that it fits exactly within the boundaries of the scrollPane and
	 * sets the value of the zoom slider to 1.0
	 */
	protected void zoomToParentSize() {
		zoomSlider.setValue(1);
		//Following code ensures that the chart will fit exactly in the scroll pane viewport 
		chart.setPreferredSize(new Dimension(10, 10));
		chart.revalidate();	
	}
	
	public void displayIsPopping() {
		//System.out.println("Display is popping, zooming back to 1...");
    	zoomToParentSize();
    }
	
	 
	protected void displayPopped() {
    	zoomToParentSize();
	}


	/**
	 * Called when zoom slider value has changed, we adjust the size of the tree chart when this happens
	 */
	protected void zoomValueChanged() {
		
		double newFactor = 3.0*(double)zoomSlider.getValue() / (double)zoomSlider.getMaximum()+1;
		
		int curWidth = chart.getWidth();
		int curHeight = chart.getHeight();
		//System.out.println("slider value: " + zoomSlider.getValue() + " old zoomFactor: " + zoomFactor + " new factor: " + newFactor + " ratio: " + newFactor / zoomFactor);
		int newWidth = (int)Math.round( curWidth*newFactor / zoomFactor);
		int newHeight = (int)Math.round( curHeight*newFactor / zoomFactor);
		
		zoomFactor = newFactor;
		Dimension newSize = new Dimension(newWidth, newHeight);
		//chart.setMinimumSize(newSize);
		chart.setPreferredSize(newSize);
		chart.setMaximumSize(newSize);
		System.out.println("Zoom value has changed, zooming to height : " + newHeight);
		chart.revalidate();
		chart.repaint();
	}

	protected void treeDirectionBoxAction() {
		if (directionBox.getSelectedItem().toString().equals("Right")) {
			chart.setTreeOrientation(DrawableTree.Direction.RIGHT);
		}
		
		if (directionBox.getSelectedItem().toString().equals("Left")) {
			chart.setTreeOrientation(DrawableTree.Direction.LEFT);
		}
		
		if (directionBox.getSelectedItem().toString().equals("Up")) {
			chart.setTreeOrientation(DrawableTree.Direction.UP);
		}
		
		if (directionBox.getSelectedItem().toString().equals("Down")) {
			chart.setTreeOrientation(DrawableTree.Direction.DOWN);
		}
	}

	/**
	 * Sets the line width of the selected nodes to 'value'. If there are no selected nodes, 
	 * sets the line with of all nodes.
	 * @param value
	 */
	protected void changeLineWidth(double value) {
		List<DrawableNode> nodes = getAllSelectedNodes();
		if (nodes.size()==0) {
			for(DrawableTree tree : chart.getTrees()) {
				nodes.addAll(tree.getAllDrawableNodes());
			}
		}

		
		for(DrawableNode dn : nodes ) {
			dn.setLineWidth((float)value);
		}
		repaint();
	}

	/**
	 * Sets the branch roundness value of the selected nodes to 'value'. If there
	 * are no selected nodes this affects every node. See CurvedBranchPainter
	 * for the definition of 'value'
	 * @param value
	 */
	protected void adjustBranchArc() {
		List<DrawableNode> nodes = getAllSelectedNodes();
		if (nodes.size()==0) {
			for(DrawableTree tree : chart.getTrees()) {
				nodes.addAll(tree.getAllDrawableNodes());
			}
		}
		
		
		int val = branchRoundnessSlider.getValue();
		BranchPainter branch;
		System.out.println("Adjusting branch arc value to : " + val);
		if (val>0)
			branch = new CurvedBranchPainter((double)val/100.0);
		else 
			branch = new LineBranchPainter();
		for(DrawableNode dn : nodes ) {
			dn.setBranchPainter(branch);
		}
		chart.repaint();
	}

	protected void showErrorBarBoxAction() {
		chart.setShowErrorBars( showErrorBarBox.isSelected());
		chart.repaint();
	}

	protected void labelPositionBoxAction() {
		List<DrawableNode> nodes = getAllSelectedNodes();

		String labelPosition = (String)labelPositionBox.getSelectedItem();

		for(DrawableNode node : nodes) {
			node.setLabelPosition(labelPosition);
		}

		chart.repaint();
	}

	protected void reattachButtonPressed() {
		dropPane.closeAllTabs();
		reattach();		
		sunfishParent.getDisplayPane().setSelectedComponent(this);
	}

	protected void labelFontSizeAction() {
		List<DrawableNode> nodes = getAllSelectedNodes();

		int sizeIndex = fontSizes.getSelectedIndex();
		float fontSize = fontSizeChoices[sizeIndex];

		for(DrawableNode node : nodes) {
			node.setFontSize(fontSize);
		}

		chart.repaint();

	}
	
	protected void labelFontFaceAction() {
		List<DrawableNode> nodes = getAllSelectedNodes();

		int fontIndex = fontFaceList.getSelectedIndex();
		String face = fontNames[fontIndex];

		for(DrawableNode node : nodes) {
			node.setFontFace(face);
		}

		chart.repaint();
	}


	protected void collapseAction() {
		for(DrawableTree tree : chart.getTrees()) {
			tree.changeCollapsedState();
		}
		chart.repaint();
	}

	protected void redrawButtonPressed() {
		for(DrawableTree tree : chart.getTrees()) {
			tree.setHasCalculatedNodePositions(false);
			tree.calculateNodePositions();
		}
		chart.repaint();
	}

	/**
	 * Called when the user has selected the remove tree item from the popup menu, this
	 * tells the chart to remove whatever tree was underneath the popup position
	 */
	public void removeButtonPressed() {
		chart.removeTreeAtPopup();
	}
	
	protected void exractTreeAction() {
		for(DrawableTree tree : chart.getTrees()) {
			Tree extractedTree = tree.extractSelectedTree();
			DisplayData newData = new DisplayData(null, extractedTree);
			sunfishParent.displayData(newData, "Extracted tree");
		}
	}

	protected void treeTypeBoxAction() {
		List<DrawableTree> trees = chart.getTrees();
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		for(DrawableTree tree : trees) {
			treesBefore.add(tree.clone());
		}
		
		chart.removeAllTrees();
		for(DrawableTree tree : trees) {
			DrawableNode root = (DrawableNode)tree.getRoot();
			switch (treeTypeBox.getSelectedIndex()) {
			case 0:	
				tree = new SquareTree( root); 
				break;
			case 1: 
				tree = new EqualAngleTree( root); 
				break;
			}
			chart.addTree(tree);
		}
		
		//EqualAngleTrees must use line branch painter and have scale bar, not axis
		if (treeTypeBox.getSelectedIndex()==1) {
			branchRoundnessSlider.setValue(0);
			adjustBranchArc();

			if (scaleBox.getSelectedIndex()==2) {
				scaleBox.setSelectedIndex(1);
				chart.setScaleType( scaleBox.getSelectedIndex() );
			}
		}
		
		TreeUndoableAction typeAction = new TreeUndoableAction(this, treesBefore, "Tree type");
		undoManager.postNewAction(typeAction);
		
		chart.repaint();
	}

	public String getDisplayName() {
		return "Tree Display";
	}

	public String getFileName() {
		return filename;
	}

	
	public Display getNew() {
		return new TreeDisplay(sunfishParent);
	}

	public String getTitle() {
		return "";
	}

	
	/**
	 * This is called when the user chooses copy from the edit menu and this display is the current
	 * focusOwner (as defined in the TransferActionListener). The default here is to return null. 
	 * Displays which support data copying should return an object representing the copied data here. 
	 * @return The data copied from this display
	 */
	public Transferable copyData() {
		List<DrawableTree> trees = new ArrayList<DrawableTree>( chart.getTrees().size());
		for(DrawableTree tree : chart.getTrees()) {
			if (tree.hasSelectedNodes()) {
				DrawableTree newTree = (DrawableTree) tree.extractSelectedTree();
				trees.add(newTree);
			}
		}
		
		return new TransferableTrees(trees);
	}
	
	/**
	 * Called when the user chooses cut from the edit menu and this Display is the current focusOwner
	 * (as defined in TransferActionListener). Displays which support data cutting should return the cut
	 * data here. 
	 * @return The data cut from this display
	 */
	public Transferable cutData() {
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		for(DrawableTree tree : chart.getTrees()) {
			treesBefore.add(tree.clone());
		}
		
		List<DrawableTree> trees = new ArrayList<DrawableTree>( chart.getTrees().size());
		for(DrawableTree tree : chart.getTrees()) {
			if (tree.hasSelectedNodes()) {
				DrawableTree newTree = (DrawableTree) tree.extractSelectedTree();
				trees.add(newTree);	
			}
		}
		
		removeSelectedNodes();
		
		return new TransferableTrees(trees);
	}
	
	/**
	 * Called when the user pastes data into this display.
	 * @param data
	 */
	public void pasteData(Transferable data) {
		List<DrawableTree> treesBefore = new ArrayList<DrawableTree>(chart.getTrees().size());
		for(DrawableTree tree : chart.getTrees()) {
			treesBefore.add(tree.clone());
		}
		
		try {
			List<DrawableTree> treeList = (List<DrawableTree>) data.getTransferData(TransferableTrees.drawableTreeFlavor);
			for(DrawableTree tree : treeList) {
				chart.addTree(tree);
			}
			chart.repaint();
			
			TreeUndoableAction typeAction = new TreeUndoableAction(this, treesBefore, "Paste trees");
			undoManager.postNewAction(typeAction);
		}
		catch (Exception ex) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Error casting transfer data to drawable tree list, could not paste");
		}
		
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

	/******************* Undo / redo stuff *****************************************/

	public UndoableAction undoAction(UndoableAction action) {
		if (action.getSource() != this) {
			ErrorWindow.showErrorWindow(new IllegalStateException("Undo action not from this source"));
			return null;
		}

		TreeUndoableAction finalState = null;
		
		if (action.getDescription().equals("Final state")) {
			ErrorWindow.showErrorWindow(new IllegalStateException("Hmmm, got a final state object for undo. I don't think this should happen"));
		}
		
		if (action instanceof TreeUndoableAction) {
			finalState = new TreeUndoableAction(this, chart.getTrees(), "Final state");
			TreeUndoableAction treeAction = (TreeUndoableAction)action;
			chart.removeAllTrees();
			for(DrawableTree tree : treeAction.treesBefore) {
				chart.addTree(tree);
			}
			
			repaint();
		}

		if (finalState==null) {
			System.out.println("Hmm, tree display is returning a null object for final state. This means that we tried to undo an action that wasn't a TreeUndoableAction..?");
		}
		
		return finalState;
	}
	
	public void redoAction(UndoableAction action) {
		System.out.println("Redoing action : " + action.getDescription());
		if (action.getDescription().equals("Final state")) {
			List<DrawableTree> trees = ((TreeUndoableAction)action).treesBefore;
			chart.removeAllTrees();
			for(DrawableTree tree : trees) {
				chart.addTree(tree);
			}
			chart.repaint();
			repaint();
			//System.out.println("Redoing final state for a tree with : " + chart.getTrees().get(0).getTotalNodes() + " nodes");
			return;
		}
		
		
		if (action instanceof TreeUndoableAction) {
			TreeUndoableAction treeAction = (TreeUndoableAction)action;
			List<DrawableTree> trees = ((TreeUndoableAction)action).treesBefore;
			chart.removeAllTrees();
			for(DrawableTree tree : trees) {
				chart.addTree(tree);
			}
		//	System.out.println("Redoing " + action.getDescription() + " for a tree with : " + chart.getTrees().get(0).getTotalNodes() + " nodes");
			chart.repaint();
			repaint();
		}
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
				Font newFont = new Font(fontNames[index], Font.PLAIN, 13);

				if (newFont == null || ! newFont.canDisplay('e'))
					newFont = new Font("Sans", Font.PLAIN, 13);

				comp.setFont( newFont );
			}

			return comp;
		}
	}
	
	
	
	
	GlassDropPane dropPane;
	
	int showing = 0;
	TreeFigure chart;
	JPanel parentPanel;

	JSlider zoomSlider;
	JSpinner lineWidthSpinner;
	JSlider branchRoundnessSlider;
	JLabel label2;
	JLabel label3;
	JButton saveButton;
	JButton saveNewickButton;
	JButton button2;
	JButton button3;
	JButton button4;
	JCheckBox ignoreBranchLengths;
	JComboBox nodeLabelBox;
	JComboBox tipLabelBox;
	JComboBox scaleBox;
	JComboBox selectionBox;
	JComboBox treeTypeBox;
	JButton highlightSelection;
	JButton collapseSelection;
	JButton rotateNodes;
	JButton rerootButton;
	JButton removeNodeButton;
	JButton removeCladeButton;
	JButton extractTreeButton;
	JButton redrawButton;
	JButton tipLabelFontButton;
	JButton nodeLabelFontButton;
	JButton reattachButton;
	JComboBox nodeFontsList;
	JComboBox fontFaceList;
	JComboBox nodeFontSizes;
	JComboBox fontSizes;
	JComboBox labelPositionBox;

	
	JCheckBox moveableNodesBox;
	JCheckBox rootTreeBox;
	JCheckBox showErrorBarBox;
	ColorSwatchButton colorNodeButton;
	JSpinner weightSpinner;
	FontChooser fontChooser;

	

}
