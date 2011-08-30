package plugins.SGPlugin.analyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import display.Display;


import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

import figure.series.XYSeries;

import element.LabelledNumber;
import element.sequence.*;
import errorHandling.ErrorWindow;
import figure.series.XYSeriesFigure;
import figure.series.XYSeriesElement;
import guiWidgets.CFButton;

/** 
 * A chart which displays the distribution of allele frequencies. 
 * @author brendan
 *
 */
public class SequenceBarChart extends Analyzable {

	JScrollPane parent;
	Logger logger;
	String currentName;
	ArrayList<SequenceGroup> currentSGs = new ArrayList<SequenceGroup>();
	Dimension chartSize;
	String iconPath = "./icons/";
	
	ArrayList<XYSeries> currentSpectra = new ArrayList<XYSeries>();
	XYSeries spectrumEx = null;
	//SequenceGroupCalculator sgCalculator;
	NumberFormat formatter = new DecimalFormat("0.0####");
	
	static final Color[] seriesColors = {Color.red, Color.green, Color.orange, Color.magenta, Color.gray, Color.cyan, Color.black};
	
	public SequenceBarChart(Display source) {
		super(source);
		initComponents();
		this.logger = SunFishFrame.getSunFishFrame().getLogger();
		this.iconPath = SunFishFrame.getSunFishFrame().getIconPath();
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
				}
			} catch (Exception ex) {
				ErrorWindow.showErrorWindow(ex, SunFishFrame.getSunFishFrame().getLogger());
			}
			
		}
	}

	private void addSequenceGroup(SequenceGroup sg) {
		currentSGs.add(sg);
		SequenceGroupCalculator sgCalculator = new SequenceGroupCalculator(sg);
		XYSeries newSpec = sgCalculator.getFoldedAlleleFreqSpectrumAsSeries();
		newSpec.setName(sg.getName());
		currentSpectra.add(newSpec);
		chart.addDataSeries(newSpec);
		chart.setSeriesMode(currentSpectra.get(currentSpectra.size()-1), XYSeriesElement.BOXES);
		chart.setSeriesLineColor(currentSpectra.get(currentSpectra.size()-1), seriesColors[currentSGs.size()%seriesColors.length]);
		chart.placeBoxSeries();
	}


	public void analyze(String name, Object data) {
		currentName = name;
		if (! (data instanceof SequenceGroup)) {
			logger.warning("Got non-sequencegroup data in SequenceBarChart.analyze");
			return;
		}
		
		currentSGs.clear();
		currentSGs.add( (SequenceGroup)data );
		
		SequenceGroupCalculator sgCalculator = new SequenceGroupCalculator(currentSGs.get(0));
		currentSpectra.add( sgCalculator.getFoldedAlleleFreqSpectrumAsSeries() );
		String sgName = currentSGs.get(0).getName();
		if (sgName==null) {
			sgName = "Spectrum 1";
		}
		currentSpectra.get(0).setName(sgName);
		
		chart.addDataSeries(currentSpectra.get(0));
		chart.setSeriesMode(currentSpectra.get(0), XYSeriesElement.BOXES);
		chart.setXLabel("Frequency of mutation");
		chart.setYLabel("Number of sites");
		
		int xTicks = (int)Math.ceil(chart.getXMax()+1);
		
		chart.setXMin(0);
		
		if (xTicks <= 10)
			chart.setXTickSpacing(1.0);
		else if (xTicks < 30) 
			chart.setXTickSpacing(2.0);
		else if (xTicks < 50)
			chart.setXTickSpacing(5.0);
		else if (xTicks < 100)
			chart.setXTickSpacing(10);
		else 
			chart.setXTickSpacing(25);
		
		
		
		thetaField.setText(String.valueOf( sgCalculator.getPi()));
		
		chart.repaint();
	}

	public JComponent getComponent() {
		return parent;
	}


	public Class getDataType() {
		return SequenceGroup.class;
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
    
    private double getTheta(SequenceGroupCalculator calc) {
    	double theta = -1;
		if (pickThetaBox.getSelectedIndex()==0)
			theta = calc.getPi();
		if (pickThetaBox.getSelectedIndex()==1)
			theta = calc.getWattersonsTheta();
		if (pickThetaBox.getSelectedIndex()==2) {
			double val = 0;
			try {
			 theta = Double.parseDouble( thetaField.getText() );
			}
			catch (NumberFormatException nfe) {
				theta = 0;
				thetaField.setText("1.0");
				JOptionPane.showMessageDialog(parent,
					    "Please enter a number in the custom theta field",
					    "Number Format Error",
					    JOptionPane.WARNING_MESSAGE);
				return -1;
			}
			
			if (val<0) {
				JOptionPane.showMessageDialog(parent,
					    "Please enter a positive value in the custom theta field",
					    "Number Format Error",
					    JOptionPane.WARNING_MESSAGE);
				return -1;
			}
		}
		
    	return theta;
    }
    
    public void showExpectationAction() {
    	boolean folded = foldedUnfoldedBox.getSelectedIndex()==0;

		chart.removeSeries(spectrumEx);
    	
    	if (showExpectationBox.isSelected()) {
    			SequenceGroupCalculator sgCalculator = new SequenceGroupCalculator(currentSGs.get(0));
    			double theta = getTheta(sgCalculator);
    			
    			if (folded)
    				spectrumEx = sgCalculator.getFoldedAlleleSpectrumExAsSeries( theta);
    			else
    				spectrumEx = sgCalculator.getUnfoldedAlleleSpectrumExAsSeries( theta);
    			
    			
    			spectrumEx.setName("Neutral expectation");

    			XYSeriesElement spectrumEl = chart.addDataSeries(spectrumEx);
    			spectrumEl.setLineColor(Color.RED);
    			chart.setSeriesLineWidth(spectrumEx, 2.2f);
    			
    			double max = 0;
    			for(XYSeries ser : currentSpectra)
    				if (max < ser.getMaxY())
    					max = ser.getMaxY();
    			
    			double newMax = Math.max(max, spectrumEx.getMaxY());
    			if (newMax > chart.getYMax())
    				chart.setYMax( newMax );
    			pickThetaBox.setEnabled(true);
    			pickThetaLabel.setForeground(Color.black);    
    			
    			if (pickThetaBox.getSelectedIndex()!=2) {
    				
    				thetaField.setText( formatter.format(theta) );
    			}
    	}
    	else {
			pickThetaBox.setEnabled(false);
			pickThetaLabel.setForeground(new Color(150, 150, 150));
			thetaField.setText("");
    	}

    	chart.inferBoundsFromCurrentSeries();
		chart.repaint();
    }
    

	
	protected void changeAncestralSequence() {
		for(int i=0; i<currentSGs.size(); i++) {	
			//Color oldColor = currentSpectra.get(i).getColor();
			chart.removeSeries(currentSpectra.get(i));
			SequenceGroupCalculator sgCalc = new SequenceGroupCalculator(currentSGs.get(i));
			currentSpectra.set(i,  sgCalc.getUnfoldedAlleleFreqSpectrumAsSeries( pickSeqBox.getSelectedIndex()));
			
			System.out.println("\n\n *** This doesn't work! It's using the wrong ancestral sequence! You must implement a getUnfoldedSpectrum function with a custom sequence...");
			chart.addDataSeries(currentSpectra.get(i));
			chart.setSeriesMode(currentSpectra.get(i), XYSeriesElement.BOXES);
			//currentSpectra.get(i).setColor(oldColor);
		}
	
		chart.placeBoxSeries();
	}
    
    public void foldedBoxActionPerformed() {
    	boolean folded = foldedUnfoldedBox.getSelectedIndex()==0;
    	
    	ErrorWindow.showErrorWindow(new Exception("This is likely to be broken!"), SunFishFrame.getSunFishFrame().getLogger() );
    	
    	if (!folded) {
    		int totalSize = 0;
    		for(SequenceGroup sg : currentSGs) {
    			totalSize += sg.size();
    		}
    		
    		String[] names = new String[totalSize];
    		DefaultComboBoxModel cModel = new DefaultComboBoxModel();
    	
    		for(int j=0; j<currentSGs.size(); j++) {
    			for(int i=0; i<currentSGs.get(j).size(); i++) {
    				names[i] = currentSGs.get(j).get(i).getName();
    				cModel.addElement(currentSGs.get(j).get(i).getName());
    			}
    		}
    		
    		ancLab.setForeground(Color.black);	
    		pickSeqBox.setModel(cModel);
    		pickSeqBox.setEnabled(true);
    		pickSeqBox.repaint();
    		
      		for(int j=0; j<currentSGs.size(); j++) {
      			SequenceGroupCalculator sgCalculator = new SequenceGroupCalculator(currentSGs.get(j));
      			XYSeries spec = sgCalculator.getUnfoldedAlleleFreqSpectrumAsSeries(0);
      			chart.setSeriesMode(spec, XYSeriesElement.BOXES);
      			chart.setSeriesLineColor(spec, seriesColors[j%seriesColors.length]);
      			chart.addDataSeries(spec);
      		}
    	}
    	else {
    		for(int j=0; j<currentSGs.size(); j++) {
      			SequenceGroupCalculator sgCalculator = new SequenceGroupCalculator(currentSGs.get(j));
      			XYSeries spec = sgCalculator.getFoldedAlleleFreqSpectrumAsSeries();
      			chart.setSeriesMode(spec, XYSeriesElement.BOXES);
      			chart.setSeriesLineColor(spec, seriesColors[j%seriesColors.length]);
      			chart.addDataSeries(spec);
      		}
    		ancLab.setForeground(new Color(150, 150, 150));
    		pickSeqBox.setEnabled(false);
    	}
    	
    	chart.repaint();
    }
    
    private void pickThetaActionPerformed() {
    	thetaField.setEnabled( pickThetaBox.getSelectedIndex()==2);
    	customThetaLabel.setEnabled( pickThetaBox.getSelectedIndex()==2);
    	showExpectationAction();
    	thetaField.repaint();
    }
	
	protected void initComponents() {
		JPanel parentPanel = new JPanel();
		parentPanel.setBackground(Color.white);
		parent = new JScrollPane(parentPanel);
		parent.setBorder(BorderFactory.createEmptyBorder());
		parent.setViewportBorder(BorderFactory.createEmptyBorder());
		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.LINE_AXIS));
		leftPanel = new JPanel();
		leftPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
		chartSize = new Dimension(500, 300);

        saveButton = new CFButton(new ImageIcon(iconPath + "save_24x24.png"), "Save chart image");

		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveChartImage();
			}
		});
	        
		leftPanel.setMinimumSize(new Dimension(150, 100));
		leftPanel.setPreferredSize(new java.awt.Dimension(150, chartSize.height));
		leftPanel.setMaximumSize(new Dimension(150, Short.MAX_VALUE));
		leftPanel.setBackground(Color.WHITE);

		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

		topLabel = new JLabel();
		topLabel.setText(currentName);
		leftPanel.add(topLabel);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		topPanel.setBackground(Color.white);
		topPanel.setMaximumSize(new Dimension(150, 30));

		topPanel.add(saveButton);

		topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(topPanel);
		leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		
		JLabel lab1 = new JLabel("Type of spectrum");
		lab1.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(lab1);
		String[] items = {"Folded", "Unfolded"};
		foldedUnfoldedBox = new JComboBox(items);
		foldedUnfoldedBox.setPreferredSize(new Dimension(100, 24));
		foldedUnfoldedBox.setMaximumSize(new Dimension(150, 200));
		foldedUnfoldedBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		foldedUnfoldedBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldedBoxActionPerformed();
            }
        });
		leftPanel.add(foldedUnfoldedBox);
		
		ancLab = new JLabel("Ancestral sequence");
		ancLab.setAlignmentX(Component.LEFT_ALIGNMENT);
//		ancLab.setForeground(new Color(150, 150, 150));
		leftPanel.add(ancLab);
		pickSeqBox = new JComboBox();
		pickSeqBox.setEnabled(false);
		pickSeqBox.setPreferredSize(new Dimension(100, 24));
		pickSeqBox.setMaximumSize(new Dimension(150, 200));
		pickSeqBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		pickSeqBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeAncestralSequence();
            }
        });
		
		//ArrayList<JMenuItem> seqMenuItems = new ArrayList<JMenuItem>();
		JMenuItem blankItem = new JMenuItem("");
		pickSeqBox.add(blankItem);
		leftPanel.add(pickSeqBox);
		
		showExpectationBox = new JCheckBox("Show neutral ex.");
		showExpectationBox.setFont(new Font("Sans", Font.PLAIN, 11));
		
		leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		leftPanel.add(showExpectationBox);
		showExpectationBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showExpectationBox.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				showExpectationAction();
			}
        }); 
        
        String[] thetas = {"Pi", "Watterson's theta", "Use custom value"};
		pickThetaBox = new JComboBox(thetas);
		pickThetaBox.setEnabled(false);
		pickThetaBox.setPreferredSize(new Dimension(100, 24));
		pickThetaBox.setMaximumSize(new Dimension(150, 200));
		pickThetaBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		pickThetaBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pickThetaActionPerformed();
            }
        });
		pickThetaLabel = new JLabel("Estimate theta from");
		pickThetaLabel.setForeground(new Color(150, 150, 150));
		JPanel thetaPanel = new JPanel();
		thetaPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		
		thetaField = new JTextField();
		thetaField.setEnabled(false);
		thetaField.setText("1.0");
		thetaField.setMinimumSize(new Dimension(100, 1));
		thetaField.setPreferredSize(new Dimension(100, 24));
		thetaField.setMaximumSize(new Dimension(200, 100));
		thetaField.setAlignmentX(Component.LEFT_ALIGNMENT);
		thetaField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showExpectationAction();
            }
        });
		customThetaLabel = new JLabel("Theta :");
		customThetaLabel.setEnabled(false);
		thetaPanel.add( customThetaLabel );
		thetaPanel.add(thetaField);
		thetaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		thetaPanel.setBackground(Color.WHITE);
		leftPanel.add(pickThetaLabel);
		leftPanel.add(pickThetaBox);
		leftPanel.add(thetaPanel);
		
		leftPanel.add(Box.createVerticalGlue());

		chart = new XYSeriesFigure();
		chart.setPreferredSize(chartSize); 
		leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		parentPanel.add(leftPanel);
		chart.setAlignmentY(Component.TOP_ALIGNMENT);
		parentPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		parentPanel.add(chart);
		
		setLayout(new BorderLayout());
		add(parentPanel, BorderLayout.CENTER);
	}




	private XYSeriesFigure chart;
	private JButton saveButton;
	private JLabel topLabel;
	private JPanel leftPanel;
	private JCheckBox showExpectationBox;
	private JComboBox foldedUnfoldedBox;
	private JComboBox pickSeqBox;
	private JComboBox pickThetaBox;
	private JLabel pickThetaLabel;
	private JTextField thetaField;
	private JLabel customThetaLabel;
	private JLabel ancLab;
	
}
