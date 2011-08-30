package plugins.argPlugin.argDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.argPlugin.arg.ARG;
import plugins.argPlugin.arg.ARGAnalyzer;
import plugins.argPlugin.arg.Range;
import plugins.argPlugin.argFigure.ARGFigure;
import plugins.argPlugin.argFigure.argSlider.ARGSliderFigure;

import display.Display;
import display.DisplayData;


import topLevelGUI.SunFishFrame;

import guiWidgets.CFButton;
import guiWidgets.glassDropPane.GlassDropPane;
import guiWidgets.glassDropPane.GlassPaneThing;

/**
 * The primary display for viewing an ARG. Mostly just a wrapper for an ARGFigure and an ARGSliderFigure. 
 * 
 * 
 * TODO: node labeling, marginal tree export,
 * 
 * ARGAnalyzer with FC height, breakpoint density, other stats
 * @author brendan
 *
 */
public class ARGDisplay extends Display {

	protected JScrollPane scrollPane;
	protected String iconPath = "./icons";
	private ARGFigure figure;
	protected ARGSliderFigure argSlider;
	
	public ARGDisplay(SunFishFrame parent) {
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
		return "ARG Display";
	}

	@Override
	public String getDescription() {
		return "Displays ancestral recombination graphs";
	}

	@Override
	public double getVersionNumber() {
		return 1.0;
	}
	
	@Override
	public Class[] getDisplayableClasses() {
		return new Class[]{ARG.class};
	}
	
	@Override
	public void saveToFile(File file) {
		throw new IllegalArgumentException("Saving ARGS not supported yet");
		
	}

	@Override
	protected boolean update(SunFishFrame parent, DisplayData data) {
		if (! (data.getData(0) instanceof ARG)) {
			System.err.println("ARG Display got a non-ARG data object");
			SunFishFrame.getSunFishFrame().getLogger().warning(" ARG Display got a non-ARG data object ");
			return false;
		}
		
		ARG arg = (ARG)data.getData(0);
		SunFishFrame.getSunFishFrame().setInfoLabelText("Updating ARG display");
		this.filename = data.getFileName();

		this.sunfishParent = parent;
		figure.setARG(arg);
		argSlider.setRangeBounds(arg.getMinSite(), arg.getMaxSite());
		argSlider.setBreakPoints(arg.getBreakPoints());
		setARGRange( argSlider.getCurrentRange());
		
		ARGAnalyzer analyzer = new ARGAnalyzer(this);
		analyzer.analyze("ARG Analysis", arg);
		SunFishFrame.getSunFishFrame().displayOutput(analyzer);
		return true;
	}

	protected void setARGFocalSite(int site) {
		figure.getARG().setFocalSite(site);
		figure.repaint();
	}
	
	public void saveButtonPressed() {
    	BufferedImage chartImage = figure.getImage(); 
  
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
		}
		else {
			reattachButton.setEnabled(true);
		}
	}
	
	protected void reattachButtonPressed() {
		dropPane.closeAllTabs();
		reattach();		
		sunfishParent.getDisplayPane().setSelectedComponent(this);
	}
	
	public void construct() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBackground(Color.white);
		topPanel.setPreferredSize(new Dimension(1, 22));
		topPanel.setPreferredSize(new Dimension(400, 22));
		topPanel.setMaximumSize(new Dimension(16384, 22));
		
		dropPane = new GlassDropPane(sunfishParent);
		
		optionsPane = new GlassPaneThing(dropPane, topPanel);
		optionsPane.setVisible(false);
		
		labelsPane = new GlassPaneThing(dropPane, topPanel);
		labelsPane.setVisible(false);
		
		appearencePane = new GlassPaneThing(dropPane, topPanel);
		appearencePane.setVisible(false);
		
		selectionPane = new GlassPaneThing(dropPane, topPanel);
		selectionPane.setVisible(false);
		
		Font displayFont = new Font("Sans", Font.PLAIN, 11);
		saveButton = new CFButton(new ImageIcon(iconPath + "save_22x22.png"), "Save image");
		saveButton.setFont(displayFont);
		saveButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                saveButtonPressed();
	            }
	    });
		optionsPane.addComponent(saveButton);


		reattachButton = new JButton("Reattach");
		reattachButton.setFont(displayFont);
		reattachButton.setToolTipText("Move to main window");
		reattachButton.setEnabled(false);
		reattachButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                reattachButtonPressed();
	            }
	    });
		optionsPane.addComponent(reattachButton);
		
		
		showTipLabelsBox = new JCheckBox("Show tip labels");
		showTipLabelsBox.setFont(displayFont);
		showTipLabelsBox.setSelected(true);
		showTipLabelsBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				tipLabelBoxStateChanged();
				
			}
		});
		labelsPane.addComponent(showTipLabelsBox);
		
		nodeLabelsBox = new JCheckBox("Internal node labels");
		nodeLabelsBox.setFont(displayFont);
		nodeLabelsBox.setSelected(false);
		nodeLabelsBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				nodeLabelsBoxStateChanged();
			}
		});
		labelsPane.addComponent(nodeLabelsBox);
		
		
		showRecombNodesBox = new JCheckBox("Show recomb. nodes");
		showRecombNodesBox.setFont(displayFont);
		showRecombNodesBox.setSelected(true);
		showRecombNodesBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				showRecombNodesBoxStateChanged();
				
			}
		});
		appearencePane.addComponent(showRecombNodesBox);

		stackTreesBox = new JCheckBox("Stack trees");
		stackTreesBox.setFont(displayFont);
		stackTreesBox.setSelected(true);
		stackTreesBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				stackTreesBoxStateChanged();
				
			}
		});
		appearencePane.addComponent(stackTreesBox);


		
		
		dropPane.addPanel("Options", optionsPane);
		dropPane.addPanel("Labels", labelsPane);
		dropPane.addPanel("Appearence", appearencePane);
		dropPane.addPanel("Selection", selectionPane);
		
		figure = new ARGFigure(this);

		topPanel.add(dropPane, BorderLayout.WEST);
		
		
		argSlider = new ARGSliderFigure(figure);
		argSlider.setPreferredSize(new Dimension(500, 40));
		argSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		argSlider.addPropertyChangeListener(ARGSliderFigure.RANGE_CHANGED, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent propEvt) {
				if (propEvt.getPropertyName().equals(ARGSliderFigure.RANGE_CHANGED)) {
					Range range = (Range)propEvt.getNewValue();
					setARGRange(range);
				}
			}
		});
		
		JPanel scrollPaneMain = new JPanel();
		scrollPaneMain.setLayout(new BorderLayout());
		scrollPaneMain.add(figure, BorderLayout.CENTER);
		scrollPaneMain.add(argSlider, BorderLayout.SOUTH);
		
		scrollPane = new JScrollPane(scrollPaneMain);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		dropPane.setParentForClip(scrollPane);
	}
	
	protected void nodeLabelsBoxStateChanged() {
		figure.setShowInternalNodeLabels( nodeLabelsBox.isSelected());
	}


	protected void stackTreesBoxStateChanged() {
		figure.setShowStackedTrees( stackTreesBox.isSelected());
		argSlider.repaint(); //Colors may change on the slider, so repaint when this happens. 
	}


	protected void showRecombNodesBoxStateChanged() {
		figure.setShowRecombNodes( showRecombNodesBox.isSelected());
		
	}

	protected void tipLabelBoxStateChanged() {
		if (showTipLabelsBox.isSelected()) {
			figure.setShowTipLabels(true);
		}
		else {
			figure.setShowTipLabels(false);
		}
		
	}

	protected void setARGRange(Range range) {
		//System.out.println("Setting new min: " + range.getMin() + " max: " + range.getMax());
		setARGFocalSite( (range.getMin()+range.getMax())/2 );
		figure.setRange(range.getMin(), range.getMax());
	}

	@Override
	public Display getNew() {
		return new ARGDisplay(sunfishParent);
	}
	
	JButton saveButton;
	JButton reattachButton;
	JCheckBox showTipLabelsBox;
	JCheckBox stackTreesBox;
	JCheckBox showRecombNodesBox;
	JCheckBox nodeLabelsBox;
	protected GlassDropPane dropPane;
	protected GlassPaneThing labelsPane;
	protected GlassPaneThing appearencePane;
	protected GlassPaneThing selectionPane;
	protected GlassPaneThing optionsPane;


}
