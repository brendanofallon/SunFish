package plugins.SGPlugin.analyzer;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import display.Display;

import plugins.SGPlugin.display.SGContentPanelDisplay;
import plugins.SGPlugin.sgStatistics.BaseCounter;
import plugins.SGPlugin.sgStatistics.BaseCounterSeries;
import plugins.SGPlugin.sgStatistics.SGStatisticsRegistry;


import element.sequence.*;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;
import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;
import figure.series.AbstractSeries;
import figure.series.SeriesListener;
import figure.series.XYSeries;
import figure.series.XYSeriesElement;
import figure.series.XYSeriesFigure;
import fileTree.FileTreePanel;
import guiWidgets.CFButton;
import guiWidgets.ColorSwatchButton;

/**
 * Creates the sliding-window-series line charts pane, uses Figure.series to manage much of
 * the actual drawing.  There's a somewhat too-complex implementation of how we keep track of
 * which data series are associated with particular sequence groups. We maintain a mapping of
 * sequencegroups (sg's) to lists of series. When new sequence groups are added, they're assigned
 * series according to whatever the user has currently selected in the list of series boxes. However,
 * individual series can be removed by the user, from the figure, and this results in the particular
 * series being removed from the map-list. If all the series are removed from a particular sequence group 
 * and that sequence group was NOT the original one used to initiate this chart, then the sg is removed
 * from the list of all sg's and we don't track it again. 
 * 
 * @author brendan
 *
 */
public class SequenceLineChart extends Analyzable implements SeriesListener, PartitionChangeListener, SequenceGroupChangeListener {

	JScrollPane parent;
	XYSeriesFigure chart;
	String currentName;
	JLabel nameLabel;

	Font defaultFont;
	JPopupMenu chartPopup;
	Logger logger;
	
	SGStatisticsRegistry sgReg;

	SequenceGroup originalSG = null; //Marks the SG that this analyzer was originally started with - we never remove this one. 
	
	ArrayList<String> currentCalculators; //The current list of calculators to apply to the current SGs
	
	Map<SequenceGroup, List<AbstractSeries>> sgSeriesMap;
	
	public SequenceLineChart(Display source) {
		super(source);
		defaultFont = new Font("Sans", Font.PLAIN, 11);
		currentName = "(no data to display)";
		this.logger = SunFishFrame.getSunFishFrame().getLogger();
		setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 2));
		setBackground(Color.white);
		
		sgSeriesMap = new HashMap<SequenceGroup, List<AbstractSeries>>();
		
		sgReg = new SGStatisticsRegistry();
		currentCalculators = new ArrayList<String>();
		initializeComponents();
	}
	
	
	/**
	 * If the given data is a SequenceGroup, add it to this line chart.
	 */
	@Override 
	public void addObjectData(Object obj) {
		if (obj instanceof SequenceGroup) {
			SequenceGroup sg = (SequenceGroup)obj;
			addSequenceGroup( sg );
		}
	}
	
	/**
	 * Called when a file has been dropped on this analyzer, we see if we can 
	 * parse a sequencegroup from the file and attempt to display it if possible
	 * @param file
	 */
	public void fileDropped(File file) {
		FileParser parser = SunFishFrame.getSunFishFrame().getParserForFileAndClass(file, SequenceGroup.class);
		if (parser != null && parser.getDataClass()==SequenceGroup.class) {
			try {
				Object obj = parser.readFile(file);
				if (obj instanceof SequenceGroup) {
					SequenceGroup sg = (SequenceGroup)obj;
					sg.setName(file.getName());
					addSequenceGroup( sg );
				}
			} catch (Exception ex) {
				ErrorWindow.showErrorWindow(ex, SunFishFrame.getSunFishFrame().getLogger());
			}
			
		}
	}
	
	/**
	 * Called by AnalysisPane when this analyzer is dismissed, removes this as a listener from all
	 * sg's we're tracking
	 */
	public void closed() {
		originalSG.removePartitionListener(this);
		originalSG.removeSGChangeListener(this);
		for(SequenceGroup sg : sgSeriesMap.keySet()) {
			sg.removeSGChangeListener(this);
			sg.removePartitionListener(this);
		}
	}
	
	public void analyze(String name, Object data) {
		if (data.getClass()!= SequenceGroup.class) {
			ErrorWindow.showErrorWindow(new IllegalArgumentException("Got a incompatible data type for SequenceLineChart: " + data.getClass()));
			return;
		}
		
		SequenceGroup currentSG = (SequenceGroup)data;
		originalSG = currentSG;
		originalSG.addPartitionListener(this);
		originalSG.addSGChangeListener(this);
		sgSeriesMap.put((SequenceGroup)data, new ArrayList<AbstractSeries>());
		currentName = name;
		if (topLabel!=null)
			topLabel.setText(currentName);
		else 
			topLabel = new JLabel(currentName);

		topLabel.setFont(defaultFont);
		

		int maxLength = currentSG.getMaxSeqLength();
		
		//Guess some decent initial values for size of window and step
		int windowSize = Math.min(100, maxLength);
		int windowStep = maxLength>50 ? 10 : 1;
		if (maxLength > 9999) {
			windowSize = 1000;
			windowStep = 100;
		}
		
		SpinnerModel sizeModel = new SpinnerNumberModel(windowSize, 1, maxLength, 1);
        SpinnerModel stepModel = new SpinnerNumberModel(windowStep, 1, maxLength/2, 1);
        
		windowSizeSpinner.setModel(sizeModel);
		windowStepSpinner.setModel(stepModel);
		
		if (currentSG.getPartitionCount()<2) {
			usePartitionsBox.setSelected(false);
			usePartitionsBox.setEnabled(false);
		} else {
			usePartitionsBox.setEnabled(true);
			usePartitionsBox.setSelected(true);
		}
		
		toggleCalculator(SGStatisticsRegistry.NUC_DIVERSITY);
		chart.repaint();
	}

	public JComponent getComponent() {
		return parent;
	}

		
	/**
	 * Called when one of the sliding window size or step values has changed. This attempts to be
	 * 'smart' and instead of recalculating everything from scratch, 
	 */
    private void newWindowSizeAction() {
		if (sgSeriesMap == null)
			return;
		
		XYSeries data;
		int windowSize = (Integer)windowSizeSpinner.getValue();
		int windowStep = (Integer)windowStepSpinner.getValue();
		int tot = 0;

		for(AbstractSeries series : chart.getAllSeries() ) {
			if (series instanceof BaseCounterSeries) {
				BaseCounterSeries bcSer = (BaseCounterSeries)series;
				BaseCounter bc = bcSer.getCalculator();
				int partitionIndex = bcSer.getPartitionIndex();
				//System.out.println("Replacing series for calc with name : " + bcSer.getName() + " and partition : " + bcSer.getPartitionIndex());
				if (partitionIndex>-1) {
					bcSer.replaceSeries( bc.getWindowPointSeries(windowSize, windowStep, partitionIndex) );
				}
				else
					bcSer.replaceSeries( bc.getWindowPointSeries(windowSize, windowStep) );
			}
			else {
				//System.err.println("Hmm, one of the series (" + series.getName() + ") is not a basecounter, cannot recalculate it for the new window size.");/
			}
		}
		
		chart.repaint();
    }
    
    
    public void popupSaveDataAction() {
    	JFileChooser saveChart = new JFileChooser();
    	int val = saveChart.showSaveDialog(parent);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = saveChart.getSelectedFile();
    		try {
    			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    			writer.write("#Chart data for file : " + currentName + "\n");
    			Date now = new Date();
    			SimpleDateFormat dateFormatter = new SimpleDateFormat("K:mm a, EEE, MMM d, yyyy");
    			writer.write("#Created " +  dateFormatter.format(now) + "\n");
    			writer.write("#Sliding window data, window size : " + windowSizeSpinner.getValue() + " window step : " + windowStepSpinner.getValue() + "\n");
    			AbstractSeries data = chart.getSeries(0);
    			if (data instanceof XYSeries) {
    				XYSeries xyData = (XYSeries)data;
    				for(int i=0; i<data.size(); i++)
    					writer.write(xyData.get(i).getX() + ",\t" + xyData.get(i).getY() + "\n");
    			}
    			else {
    				ErrorWindow.showErrorWindow(new IllegalArgumentException("Cannot write non-xy series data (feature not implemented, yet)") );
    			}
    			writer.write("\n");
    			writer.close();
    			logger.info("Wrote data to file : " + file.getAbsolutePath());
    		}
    		catch(IOException ioe) {
    			logger.warning("Error writing chart data to file : " + ioe.toString() );
    		}
    	}
    }
    
    
    public void saveChartImage() {
    	BufferedImage chartImage = chart.getImage(); 
    		
    	JFileChooser saveChart = new JFileChooser();
    	int val = saveChart.showSaveDialog(parent);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = saveChart.getSelectedFile();
    		try {
    			ImageIO.write(chartImage, "png", file);
    		}
    		catch(IOException ioe) {
    			logger.warning("Error saving chart image to file : " + ioe.toString() );
    		}
    	}	
    }

    
    /**
     * Remove all series associated with the class specified
     * @param toRemove
     */
    public void removeSeriesByClass(Class toRemove) {
    	List<AbstractSeries> activeSeries = chart.getAllSeries();
    	for(AbstractSeries series : activeSeries) {
    		if (series instanceof BaseCounterSeries) {
    			BaseCounter bc = ((BaseCounterSeries)series).getCalculator();
    			if (bc.getClass() == toRemove) {
    				chart.removeSeries(series);
    			}
    		}
    	}
    }
    
    /**
     * You guessed it..remove from the chart all series that are a) baseCounters, and
     * b), have the baseCounter name of calcName. This happens when the user toggles
     * off the check box associated with the named calculator. 
     * 
     * @param calcName
     */
    public void removeSeriesByCalculatorName(String calcName) {
    	List<AbstractSeries> activeSeries = chart.getAllSeries();
    	for(AbstractSeries series : activeSeries) {
    		if (series instanceof BaseCounterSeries) {
    			String name = ((BaseCounterSeries)series).getCalculator().getName();
    			if (name.equals(calcName)) {
    				chart.removeSeries(series);
    			}
    		}
    	}
    }
    
    /**
     * Remove all series associated wit the specified partition
     * @param partitionIndex
     */
//    public void removeSeriesByPartition(int partitionIndex) {
//    	List<XYSeries> activeSeries = chart.getAllSeries();
//    	for(XYSeries series : activeSeries) {
//    		if (series instanceof BaseCounterSeries) {
//    			int index = ((BaseCounterSeries)series).getPartitionIndex();
//    			if (index == partitionIndex) {
//    				chart.removeSeries(series);
//    			}
//    		}
//    	}
//    }
    
    
    /**
     * Uses the calculator with name calcName to construct & display a series for the sg. This should be
     * the only way new series' are added to the chart, since we need to keep track of all the current
     * series in the sgSeriesMap
     * @param calcName
     * @param sg
     */
    protected void addSeriesForSG(String calcName, SequenceGroup sg) {
		BaseCounter calc = null;
		try {
			calc = sgReg.getBaseCounterInstance(calcName, sg);
		}
		catch (IllegalArgumentException ex) {
			System.err.println("Could not find base counter of type : " + calcName);
			return;
		}
		
		List<AbstractSeries> seriesList = sgSeriesMap.get(sg);
		
		if (seriesList == null) {
			System.out.println("Uh-oh, this sequence group doesn't have an associated list in the sgSeriesMap...");
		}
		
		//Make a new series from the calculator
		int windowsize = ((Integer)windowSizeSpinner.getValue()).intValue();
		int windowStep = ((Integer)windowStepSpinner.getValue()).intValue();
		
		if (usePartitionsBox.isSelected()) {
			for(int i=0; i<sg.getPartitionCount(); i++) {
				BaseCounterSeries ser = calc.getWindowSeries(windowsize, windowStep, i);
				ser.setPartitionIndex(i);
				seriesList.add(ser);
				ser.setName(calc.getName() + " - " + sg.getPartitionKeyForIndex(i));
				if (ser != null && ser.size() > 0) {
					chart.addDataSeries(ser);
					chart.repaint();
				}
			}
			
		}
		else {
			BaseCounterSeries ser = calc.getWindowSeries(windowsize, windowStep);
			seriesList.add(ser);
			if (sgSeriesMap.size()>1) {
				String name = sg.getName();
				if (name == null) {
					ser.setName(calc.getName() );	
				}
				else {
					ser.setName(calc.getName() + " - " + sg.getName() );
				}
				
			}
			if (ser != null && ser.size() > 0) {
				chart.addDataSeries(ser);
				chart.repaint();
			}
		}
    	
    }
    
    /**
     * This method updates the current list of calculators (currentCalculators) in toggle fashion,
     * meaning that if calcName is in the list, it is removed. If not, it is added. Further, 
     * when the calculator is removed, all series in the chart associated with the calculator are
     * also removed. When a calculator is added, series are added for all active sequenceGroups, and
     * all associated partitions if usePartitions is true.  
     * 
     * @param calcName
     */
	public void toggleCalculator(String calcName) {
		if (currentCalculators.contains(calcName)) {
			currentCalculators.remove(calcName);
			removeSeriesByCalculatorName(calcName);
		}
		else {
			currentCalculators.add(calcName);
				
			//For all active sequence groups, add the new data series
			
			for(SequenceGroup sg : sgSeriesMap.keySet()) {
				addSeriesForSG(calcName, sg);
			}
		}	
		
		chart.inferBoundsFromCurrentSeries();
		repaint();
	}
	
	
	/**
	 * Adds a new sequence group to the list of sg's currently tracked
	 * @param sg The new sequence group to add
	 */
	public void addSequenceGroup(SequenceGroup sg) {
		sgSeriesMap.put(sg, new ArrayList<AbstractSeries>());
		for(String calcName : currentCalculators) {
			addSeriesForSG(calcName, sg);
		}
		repaint();
	}
	
	/**
	 * Clears the all current series from the figure, then re-adds everything
	 * based on the current sg list and current calculators list. 
	 */
	private void repaintAllSeries() {
		chart.removeAllSeriesSilently();
		//System.out.println("Repainting all series!");
		for(String calcName : currentCalculators) {
			for(SequenceGroup sg : sgSeriesMap.keySet()) {
				addSeriesForSG(calcName, sg);
			}
		}
	}

	/**
	 * Called when either the state of the partitions has changed, or when the user
	 * has clicked on the 'Use Partitions..' box. Either way, we recalculate the 
	 * series data here to reflect the change
	 */
	public void partitionStateChanged(Partitionable source, PartitionChangeType type) {
		if (type==PartitionChangeType.NEW_PARTITION) {
			usePartitionsBox.setEnabled(true);
			if (usePartitionsBox.isSelected())
				repaintAllSeries();
			
		}
		if (type==PartitionChangeType.PARTITIONS_CLEARED) {
			//Do all sg's have no partitions? If so, set the box to disabled
			int max = 0;
			for(SequenceGroup sg : sgSeriesMap.keySet()) {
				if (max < sg.getPartitionCount()) 
					max = sg.getPartitionCount();
			}
			if (max<2)
				usePartitionsBox.setEnabled(false);
			
			repaintAllSeries();
		}
		
	}
	
	/**
	 * Re-infer the axes bounds from the current series & repaint
	 */
	protected void restoreDefaultBounds() {
		chart.inferBoundsFromCurrentSeries();
		repaint();
	}
	
	private JScrollPane createStatisticListPanel() { 
		Map<String, BaseCounter> statMap = sgReg.getBaseCounters();
		int count = 0;
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Color stripeColor = new Color(235, 235, 235);
		
		for(final BaseCounter calc : statMap.values() ) {
			JPanel calcPanel = new JPanel();
			calcPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			final String name = calc.getName(); 
			JCheckBox box = new JCheckBox();
			if (name.equals(SGStatisticsRegistry.NUC_DIVERSITY)) //bit of a hack here to make sure this box starts
				box.setSelected(true);							 //out selected
			box.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					toggleCalculator(name);   
				}
			});
			calcPanel.add(box);
			JLabel lab = new JLabel(name);
			lab.setFont(new Font("Sans", Font.PLAIN, 11));
			calcPanel.add(lab);
			calcPanel.setBackground(Color.white);
			calcPanel.setMaximumSize(new Dimension(500, 24));
			calcPanel.setToolTipText(calc.getDescription());
			if (count % 2 == 0) {
				calcPanel.setBackground(stripeColor);
			}
			else {
				calcPanel.setBackground(Color.white);
			}
			panel.add(calcPanel);
			count++;
		}
		
		JScrollPane pane = new JScrollPane(panel);
		pane.getVerticalScrollBar().setAlignmentX(JComponent.LEFT_ALIGNMENT);
		return pane;
	}
	
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = SequenceLineChart.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Error loadind icon from resouce : " + ex);
		}
		return icon;
	}
	
	private void initializeComponents() {
		int leftPanelWidth = 250;
		JPanel parentPanel = new JPanel();
		parent = new JScrollPane(parentPanel);
		chartSize = new Dimension(500, 300);
		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.X_AXIS));
		JPanel leftPanel = new JPanel();
		leftPanel.setMaximumSize(new Dimension(leftPanelWidth, Integer.MAX_VALUE));
		parent.setBorder(BorderFactory.createEmptyBorder());
		parent.setViewportBorder(BorderFactory.createEmptyBorder());

        jSeparator1 = new javax.swing.JSeparator();
        chartTypeLabel = new javax.swing.JLabel();
        windowSizeSpinner = new javax.swing.JSpinner();
        windowStepSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        leftPanel.setPreferredSize(new java.awt.Dimension(leftPanelWidth, 300));
        leftPanel.setBackground(Color.WHITE);

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));

        topLabel = new JLabel();
        topLabel.setText(currentName);
        topLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        leftPanel.add(topLabel);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
        topPanel.setBackground(Color.white);
        topPanel.setMaximumSize(new Dimension(leftPanelWidth, 16));
        
        topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(topPanel);

        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(leftPanelWidth, 40));
        buttonPanel.setPreferredSize(new Dimension(leftPanelWidth, 36));

        saveButton = new JButton(getIcon("icons/save.png"));
        saveButton.setFont(defaultFont);
        saveButton.setToolTipText("Save chart image");
        saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveChartImage();
			}
        });
        saveButton.setAlignmentX(LEFT_ALIGNMENT);
        
        
        JButton restoreButton = new JButton(getIcon("icons/expand.png"));
        restoreButton.setToolTipText("Restore default axes boundaries");
        restoreButton.setFont(defaultFont);
        restoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				restoreDefaultBounds();
			}
        });
        restoreButton.setAlignmentX(LEFT_ALIGNMENT);
        
        JButton zoomButton = new JButton(getIcon("icons/zoom.png"));
        zoomButton.setToolTipText("Zoom to selection region");
        zoomButton.setFont(defaultFont);
        zoomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				zoomToSelection();
			}
        });
        zoomButton.setAlignmentX(LEFT_ALIGNMENT);
        
        JButton selectRangeButton = new JButton(getIcon("icons/selectColumns.png"));
        selectRangeButton.setFont(defaultFont);
        selectRangeButton.setToolTipText("Select range in sequence group");
        selectRangeButton.setAlignmentX(LEFT_ALIGNMENT);
        selectRangeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectSitesFromAxesRange();
			}
        });
        
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(defaultFont);
        //selectRangeButton.setPreferredSize(new Dimension(80, 24));
        clearButton.setAlignmentX(LEFT_ALIGNMENT);
        clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chart.clearRangeSelection();
				chart.repaint();
			}
        });
        
        
        buttonPanel.add(saveButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(zoomButton);
        buttonPanel.add(selectRangeButton);
        leftPanel.add(buttonPanel);

        
        usePartitionsBox = new JCheckBox("Use partition data");
        usePartitionsBox.setFont(defaultFont);
        usePartitionsBox.setSelected(true);
        usePartitionsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaintAllSeries();
			}			
        });
        
        leftPanel.add(usePartitionsBox);
        
        
        SpinnerModel sizeModel = new SpinnerNumberModel(100, 1, 1000, 10);
        SpinnerModel stepModel = new SpinnerNumberModel(100, 1, 1000, 5);
        windowSizeSpinner = new JSpinner(sizeModel);
        windowStepSpinner = new JSpinner(stepModel);
        windowStepSpinner.setMaximumSize(new Dimension(80, 25));
        windowSizeSpinner.setMaximumSize(new Dimension(80, 25));
        windowSizeLabel = new JLabel();
        windowSizeLabel.setFont(defaultFont);
        windowSizeLabel.setText("Window size");
        windowStepLabel = new JLabel();
        windowStepLabel.setFont(defaultFont);
        windowStepLabel.setText("Window step");
        
        JPanel spinnerLabels = new JPanel();
        spinnerLabels.setAlignmentX(LEFT_ALIGNMENT);
        spinnerLabels.setOpaque(false);
        spinnerLabels.setLayout(new BoxLayout(spinnerLabels, BoxLayout.X_AXIS));
        spinnerLabels.add(windowSizeLabel);
        spinnerLabels.add(Box.createHorizontalStrut(16));
        spinnerLabels.add(windowStepLabel);
        spinnerLabels.add(Box.createHorizontalGlue());
        
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.setAlignmentX(LEFT_ALIGNMENT);
        spinnerPanel.setOpaque(false);
        spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));
        spinnerPanel.add(windowSizeSpinner);
        spinnerPanel.add(Box.createHorizontalStrut(25));
        spinnerPanel.add(windowStepSpinner);
        spinnerPanel.add(Box.createHorizontalGlue());
        leftPanel.add(spinnerLabels);
        leftPanel.add(spinnerPanel);

        windowSizeSpinner.setFont(defaultFont);
        windowSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
            	newWindowSizeAction();
            }
        });
        windowStepSpinner.setFont(defaultFont); 
        windowStepSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
            	newWindowSizeAction();
            }
        });

        
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JScrollPane pane = createStatisticListPanel();
        pane.setPreferredSize(new Dimension(leftPanelWidth, 200));
        pane.setMaximumSize(new Dimension(leftPanelWidth, 400));
        pane.setBackground(Color.white);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(pane);
        

        leftPanel.add(Box.createVerticalGlue());
        
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.white);
		
		
		chart = new XYSeriesFigure();
		chart.setMinimumSize(chartSize);
		chart.setPreferredSize(chartSize);
		chart.addMouseListener(new PopupListener() );

		parentPanel.add(leftPanel);
		parentPanel.add(chart);
		
		chartPopup = new JPopupMenu();
		
		JMenuItem popupExport = new JMenuItem("Save as image");
		popupExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveChartImage();
            }
        });
		chartPopup.add(popupExport);
		
		JMenuItem popupSaveData = new JMenuItem("Save data as .csv");
		popupSaveData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupSaveDataAction();
            }
        });
		chartPopup.add(popupSaveData);
		chartPopup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));	
		
		setLayout(new BorderLayout());
		add(parent, BorderLayout.CENTER);
		
		chart.addSeriesListener(this);
	}

	private JPanel makeSliderPanel(JSlider slider, String name) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel lab = new JLabel(name);
		lab.setOpaque(false);
		p.add(lab);
		p.add(slider);
		return p;
	}

	/**
	 * Adjusts the x min and max bounds of the chart to match the selection region. Has no effect
	 * if there is no selected region
	 */
	protected void zoomToSelection() {
		if (chart.isRangeSelected()) {
			double[] range = chart.getRangeSelection();
			if (range[0] < range[1]) {
				chart.setXMin(range[0]);
				chart.setXMax(range[1]);
				chart.clearRangeSelection();
				repaint();				
			}
			
		}
	}


	/**
	 * Selects the columns in the associated sequence group that correspond to the selected
	 * range in the current chart. 
	 */
	protected void selectSitesFromAxesRange() {
		if (chart.isRangeSelected()) {
			double[] range = chart.getRangeSelection();
			int rangeStart = (int)Math.round(range[0]);
			int rangeEnd = (int)Math.round(range[1]);

			try {
				SGContentPanelDisplay sgDisplay = (SGContentPanelDisplay)source;
				sgDisplay.selectColumns(rangeStart, rangeEnd);
			}
			catch (ClassCastException ex) {
				//	apparently source wasn't an sgDisplay, not sure what should happen here
			}
		}
	}


	/**
	 * Performs a reverse-lookup of a sequence group from the sgSeriesMap, that is, finds
	 * the sequence group associated with a given series. Pretty slow of course, but
	 * I don't think this is a performance-sensitive area
	 * @param series
	 * @return
	 */
	private SequenceGroup findSGForSeries(AbstractSeries series) {
		for(SequenceGroup keySG : sgSeriesMap.keySet()) {
			List<AbstractSeries> seriesList =  sgSeriesMap.get(keySG);  
			for(AbstractSeries seriesItem : seriesList) {
			if (seriesItem == series) 
				return keySG;
			}
		}
		return null;
	}

	/**
	 * When the last series for a sequence group is removed, we remove the sequence group 
	 * from the list of tracked sequence groups
	 */
	@Override public void seriesRemoved(AbstractSeries removedSeries) {
		SequenceGroup sg = findSGForSeries(removedSeries);
		
		List<AbstractSeries> seriesList = sgSeriesMap.get(sg);
		boolean found = seriesList.remove(removedSeries);
		if (! found) {
			System.out.println("The series was not found in the list of series associated with this sg... things must be getting corrupted");
		}
		if (seriesList.size()==0 && sg != originalSG) {
			sgSeriesMap.remove(sg);
			System.out.println("Removing an sg from the map since it no longer has any associated series");
		}
		
		
//		int count = 0; //Counts the number of series associated with this sequence group, including the removed one
//		if (sg!= null) {
//			for(Object key : seriesSGMap.keySet()) {
//			    if (key == sg) {
//			    	count++;
//			    }
//			}
//			
//			if (count==1) {
//				//Only the removed series was associated with this sg, so remove the sg
//			}
//		}
		
	}

	/** We don't care about these now */
	@Override
	public void seriesChanged(AbstractSeries changedSeries) {

	}
	


	JCheckBox usePartitionsBox;
	private JLabel topLabel;
	private JLabel windowStepLabel;
	private JLabel windowSizeLabel;  
	private JCheckBox gcLineBox;
	private JCheckBox sLineBox;
	private JCheckBox piLineBox;
	private JCheckBox tajimasDLineBox;
    private javax.swing.JLabel chartTypeLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton optionsButton;
    private javax.swing.JButton increaseButton;
    private javax.swing.JButton decreaseButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner windowSizeSpinner;
    private javax.swing.JSpinner windowStepSpinner;
    private JComboBox chartTypeBox;
    private Dimension chartSize;
    ColorSwatchButton gcLineColor;
    ColorSwatchButton sLineColor;
    ColorSwatchButton piLineColor;
    ColorSwatchButton tdLineColor;
    
    
	private class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	chartPopup.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}


	@Override
	/**
	 * This is called when the original sequence group has been modified,
	 */
	public void sgChanged(SequenceGroup source, ChangeType type) {
		repaintAllSeries();
	}


}
