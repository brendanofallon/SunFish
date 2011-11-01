/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.SGPlugin.analyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import display.Display;
import display.DisplayData;

import plugins.SGPlugin.analyzer.statTable.StatTable;
import plugins.SGPlugin.sgStatistics.*;


import figure.series.CategorySeries;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

import element.sequence.*;
import errorHandling.ErrorWindow;
import figure.series.CategoryFigure;
import figure.series.XYSeriesElement;
import figure.series.XYSeriesFigure;
import guiWidgets.RoundedBezelPanel;

/**
 *A panel that displays some basic textual information about a sequence group (or multiple sequence groups)
 * @author brendan
 */
public class SequenceGroupSummary extends Analyzable implements SequenceGroupChangeListener {
	
	private javax.swing.JPanel parentPanel;
	private JScrollPane scrollPane;
	//CategoryFigure baseFreqChart = new CategoryFigure();

	SGStatisticsRegistry sgReg;
	SegregatingSites segSites;
	NucleotideDiversity nucDiversity;
	NumHaplotypes nHaps;
	NumGappedSites numGapped;
	TajimasD  tajD;
	WattersonsTheta thetaW;
	GFreq gFreq;
	AFreq aFreq;
	TFreq tFreq;
	CFreq cFreq;
	NumSitePatterns sitePatterns;
	File sourceFile = null;
	
	
	public SequenceGroupSummary(Display source) {
		super(source);
		initializePanel();
		sgReg = new SGStatisticsRegistry();
	}

	public JComponent getComponent() {
		return this;
	}

	public Class getDataType() {
		return SequenceGroup.class;
	}

	
	
	/**
	 * Called when the SG has been modified somehow, we recalculate the stats associated with the sg 
	 * then. 
	 */
	@Override public void sgChanged(SequenceGroup source, ChangeType type) {
		//Don't think we need to worry about this right now since the individual tables listen
		//to events from all the sg's in 'em
		//TODO, but shouldn't we update the chart?
	}
	
	
	/**
	 * If the object is a SequenceGroup, we add it to both summary tables and to the Base Frequencies 
	 * chart. 
	 */
	@Override public void addObjectData(Object obj) {
		if (obj instanceof SequenceGroup) {
			SequenceGroup sg = (SequenceGroup)obj;
			
			upperTable.addSequenceGroup( sg, sg.getName() );
			lowerTable.addSequenceGroup( sg, sg.getName() );
			addBaseFreqSeries(sg, sg.getName());
		}
	}
	
	/**
	 * Called when a file has been dropped on this analyzer, we see if we can 
	 * parse a sequencegroup from the file and the add it to the upper and lower StatTables
	 * if we can 
	 * 
	 * @param file
	 */
	public void fileDropped(File file) {
		FileParser parser = SunFishFrame.getSunFishFrame().getParserForFileAndClass(file, SequenceGroup.class);
	
		if (parser != null) {
			try {
				Object obj = parser.readFile(file);
				if (obj instanceof SequenceGroup) {
					((SequenceGroup)obj).setName(file.getName());
					addObjectData(obj);
				}

			} catch (Exception ex) {
				ErrorWindow.showErrorWindow(ex, SunFishFrame.getSunFishFrame().getLogger());
			}
			
		}
	}
	
	/**
	 * Called when this display has been dismissed, we should remove this from all listener lists here
	 */
	public void closed() {
		upperTable.clearAllListeners();
		lowerTable.clearAllListeners();
	}
	
	public void addBaseFreqSeries(SequenceGroup seqs, String name) {
		CategorySeries baseFreqs = new CategorySeries();
		SGCalculator aFreq = sgReg.getInstance(SGStatisticsRegistry.AFREQ, seqs);
		SGCalculator cFreq = sgReg.getInstance(SGStatisticsRegistry.CFREQ, seqs);
		SGCalculator gFreq = sgReg.getInstance(SGStatisticsRegistry.GFREQ, seqs);
		SGCalculator tFreq = sgReg.getInstance(SGStatisticsRegistry.TFREQ, seqs);
		baseFreqs.addPoint("A", aFreq.getValue());
		baseFreqs.addPoint("C", cFreq.getValue());
		baseFreqs.addPoint("G", gFreq.getValue());
		baseFreqs.addPoint("T", tFreq.getValue());
		
		baseFreqs.setName(name);
		//baseFreqChart.addDataSeries(baseFreqs);
		//baseFreqChart.setYTickSpacing(0.25);	
	}
	
	public void analyze(String name, Object data) {
		SequenceGroup seqs;
		if ( data instanceof DisplayData) {
			sourceFile = ((DisplayData)data).getFile();
			seqs = (SequenceGroup)(((DisplayData)data).getData(0)); 
		}
		else {
			if (! (data instanceof SequenceGroup)) {
				System.err.println("Got data incompatible with this output type");
				return;
			}
			seqs = (SequenceGroup)data;
		}
		

		seqs.addSGChangeListener(this); 
		
		nameLabel.setText(name);

		segSites = (SegregatingSites)sgReg.getInstance(SGStatisticsRegistry.SEG_SITES, seqs);
		nucDiversity = (NucleotideDiversity)sgReg.getInstance(SGStatisticsRegistry.NUC_DIVERSITY, seqs);
		tajD = (TajimasD)sgReg.getInstance(SGStatisticsRegistry.TAJIMASD, seqs);
		numGapped = (NumGappedSites)sgReg.getInstance(SGStatisticsRegistry.NUM_GAPS, seqs);
		nHaps = new NumHaplotypes(seqs);
		thetaW = (WattersonsTheta)sgReg.getInstance(SGStatisticsRegistry.THETAW, seqs);
		HaplotypeDiversity hd = (HaplotypeDiversity)sgReg.getInstance(SGStatisticsRegistry.HAP_DIVERSITY, seqs);
		sitePatterns = (NumSitePatterns)sgReg.getInstance(SGStatisticsRegistry.NUM_SITEPATTERNS, seqs);
		
		SequenceLength length = (SequenceLength)sgReg.getInstance(SGStatisticsRegistry.SEQ_LENGTH, seqs);
		NumSequences numSeqs = (NumSequences)sgReg.getInstance(SGStatisticsRegistry.NUMSEQS, seqs);
		
		upperTable.addSequenceGroup( seqs, name );
		upperTable.addStatistic(numSeqs);
		upperTable.addStatistic(length);
		upperTable.addStatistic(nHaps);
		upperTable.addStatistic(sitePatterns);
		upperTable.addStatistic(numGapped);
		
		lowerTable.addSequenceGroup(seqs, name);
		lowerTable.addStatistic(segSites);
		lowerTable.addStatistic(hd);
		lowerTable.addStatistic(nucDiversity);
		lowerTable.addStatistic(thetaW);
		lowerTable.addStatistic(tajD);
		
		addBaseFreqSeries(seqs, name);
		
		if (sourceFile != null) {
			Date lastmod = new Date(sourceFile.lastModified());
			SimpleDateFormat dateFormatter = new SimpleDateFormat("K:mm a, EEE, MMM d, yyyy");
			lastModLabel.setText("Last modified : " + dateFormatter.format(lastmod));
		}
	}
	
	
	/**
	 * Basic structure is a flow layout that contains only a left and right panel, both of which
	 * have vertical box layouts
	 */
	private void initializePanel() {
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		parentPanel = new JPanel();
		parentPanel.setOpaque(false);
		
		upperTable = new StatTable();
		lowerTable = new StatTable();
		nameLabel = new JLabel("(unknown name)");
		
		parentPanel.setBackground(Color.white);
		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.X_AXIS));
		parentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		parentPanel.setMinimumSize(new Dimension(500, 400));
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		leftPanel.setOpaque(false);
		rightPanel.setOpaque(false);
		
		parentPanel.add(leftPanel);
		parentPanel.add(rightPanel);
		
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add( nameLabel );
		upperTable.setPreferredSize(new Dimension(500, 150));
		upperTable.setMinimumSize(new Dimension(200, 50));
		upperTable.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Summary"));
		
		lowerTable.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Neutrality Statistics"));
		lowerTable.setPreferredSize(new Dimension(500, 150));
		lowerTable.setMinimumSize(new Dimension(200, 50));
		leftPanel.add(upperTable);
		leftPanel.add(lowerTable);
		
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		//rightPanel.add( baseFreqChart );
		//baseFreqChart.setPreferredSize(new Dimension(200, 200));
		JPanel fileInfo = new JPanel();
		fileInfo.setOpaque(false);
		fileInfo.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "File Information"));
		fileInfo.setLayout(new BoxLayout(fileInfo, BoxLayout.Y_AXIS));
		fileTypeLabel = new JLabel("File type : unknown ");
		lastModLabel = new JLabel("Last modified : unknown");
		fileInfo.add(fileTypeLabel);
		fileInfo.add(lastModLabel);
		fileInfo.setPreferredSize(new Dimension(200, 100));
		rightPanel.add(fileInfo);
		
		scrollPane.setViewportView(parentPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);
		
		
		//Initialize the base frequencies chart
		List<String> labels = new ArrayList<String>();
		labels.add("A");
		labels.add("C");
		labels.add("G");
		labels.add("T");
		//baseFreqChart.setXLabelList(labels);	
		//baseFreqChart.setXLabel("Base Frequencies");

	}
	


	StatTable upperTable;
	StatTable lowerTable;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel lastModLabel;
    private javax.swing.JLabel fileTypeLabel;


	

}
