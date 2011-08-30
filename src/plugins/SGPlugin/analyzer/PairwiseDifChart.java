package plugins.SGPlugin.analyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;

import display.Display;


import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;
import element.DoneListener;
import element.sequence.*;
import errorHandling.ErrorWindow;
import figure.series.XYSeries;
import figure.series.XYSeriesFigure;
import figure.series.XYSeriesElement;
import guiWidgets.CFButton;

/**
 * An analyzer that displays the distribution of pairwise differences in one or more sequence groups
 * @author brendan
 *
 */
public class PairwiseDifChart extends Analyzable implements PropertyChangeListener, DoneListener {

	Logger logger;
	String iconPath;
	SunFishFrame parentFrame;
	JScrollPane parentPane;
	XYSeriesFigure chart;
	
	ArrayList<SequenceGroup> currentSGs = new ArrayList<SequenceGroup>();
	ProgressMonitor progressMonitor;
	SequenceGroupCalculator.PairwiseDifCounter difCounter;
	
	static final Color[] seriesColors = {Color.blue, Color.red, Color.green, Color.orange, Color.magenta, Color.gray, Color.cyan, Color.black};
	
	public PairwiseDifChart(Display source) {
		super(source);
		this.parentFrame = SunFishFrame.getSunFishFrame();
		this.logger = SunFishFrame.getSunFishFrame().getLogger();
		this.iconPath = SunFishFrame.getSunFishFrame().getIconPath();
		initComponents();
	}


	public void analyze(String name, Object data) {
		currentName = name;
		topLabel.setText(currentName);
		
		if (! (data instanceof SequenceGroup) ) {
			logger.warning("Receieved bad data type in PairwiseDifChart, expected SG, got " + data.getClass());
			return;
		}
		currentSGs.add( (SequenceGroup)data );
		
		int total = currentSGs.get(0).size()*(currentSGs.get(0).size()-1)/2;
		progressMonitor = new ProgressMonitor(parentPane, "Tabulating differences", "", 0, total);
		
		SequenceGroupCalculator sgCalc = new SequenceGroupCalculator(currentSGs.get(0));
		difCounter = sgCalc.getPairDifCounter(this);
		difCounter.addPropertyChangeListener(this);
		difCounter.execute();

		chart.setXLabel("Number of differences");
		chart.setYLabel("Frequency");
		parentPane.repaint();
	}
	
	
	/**
	 * If the data provided is sequence group, add it to the chart. 
	 */
	@Override public void addObjectData(Object obj) {
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
		//System.out.println("Got file dropped, parser is : " + parser.getDataClass());
		if (parser != null) {
			try {
				Object obj = parser.readFile(file);
				if (obj instanceof SequenceGroup) {
					SequenceGroup sg = (SequenceGroup)obj;
					sg.setName(file.getName());
					addSequenceGroup( sg );
					//repaint();
				}
			} catch (Exception ex) {
				ErrorWindow.showErrorWindow(ex, SunFishFrame.getSunFishFrame().getLogger());
			}
			
		}
	}
	
	public void addSequenceGroup(SequenceGroup sg) {
		currentSGs.add(sg);
		SequenceGroupCalculator sgCalc = new SequenceGroupCalculator(currentSGs.get( currentSGs.size()-1));
		difCounter = sgCalc.getPairDifCounter(this);
		difCounter.addPropertyChangeListener(this);
		difCounter.execute();
		
	}
		/**
		 * Called when a file has been dropped on this analyzer, we see if we can 
		 * parse a sequencegroup from the file and attempt to display it if possible
		 * @param file
		 */
		
	
	/**
	 * This is called with the difCounter is done calculating all the pairwise differences
	 */
	public void done() {
		XYSeries pairDifs = difCounter.getSeries();
		pairDifs.setName("Pairwise differences");
		if (currentSGs.size()>1) {
			
			String name = currentSGs.get(currentSGs.size()-1).getName();
			if (name!=null) {
				pairDifs.setName("Pair. difs. - " + name);	
			}
				
		}
		if (pairDifs != null) {
			chart.addDataSeries(pairDifs);
			chart.setSeriesMode(pairDifs, XYSeriesElement.BOXES);
			chart.setSeriesLineColor(pairDifs, seriesColors[currentSGs.size()%seriesColors.length-1]);
		}
		progressMonitor.close();
		chart.placeBoxSeries();
		parentPane.repaint();
		
	}
	


	public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
        } 
    }

	
	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.white);
		parentPane = new JScrollPane(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));
		leftPanel = new JPanel();
		chartSize = new Dimension(500, 300);

		
        saveButton = new CFButton("Save image", new ImageIcon(iconPath + "save_24x24.png"));
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveChartImage();
			}
		});
	        
		leftPanel.setMinimumSize(new Dimension(150, 100));
		leftPanel.setPreferredSize(new java.awt.Dimension(150, chartSize.height));
		leftPanel.setMaximumSize(new Dimension(150, Short.MAX_VALUE));
		leftPanel.setBackground(Color.WHITE);

		
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));

		topLabel = new JLabel();
		topLabel.setText(currentName);
		System.out.println("Setting top label to : "  + currentName);
		topLabel.setBackground(Color.green);
		

		leftPanel.add(saveButton);

		leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		leftPanel.add(topLabel);
		leftPanel.add(saveButton);
		leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		leftPanel.add(Box.createVerticalGlue());

		chart = new XYSeriesFigure();
		chart.setPreferredSize(chartSize); 
		leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		mainPanel.add(leftPanel);
		chart.setAlignmentY(Component.TOP_ALIGNMENT);
		mainPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		mainPanel.add(chart);
		//chart.setBorder(BorderFactory.createBevelBorder(1));
		

		setLayout(new BorderLayout());
		add(parentPane, BorderLayout.CENTER);
	}


	protected void saveChartImage() {
		BufferedImage chartImage = chart.getImage(); 
		
    	JFileChooser saveChart = new JFileChooser();
    	int val = saveChart.showSaveDialog(parentFrame);
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


	public JComponent getComponent() {
		return parentPane;
	}


	public Class getDataType() {
		return SequenceGroup.class;
	}
	
	
	private JPanel leftPanel;
	private Dimension chartSize;
	private CFButton saveButton;
	private JLabel topLabel;
	private String currentName;
	
}
