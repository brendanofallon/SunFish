package plugins.argPlugin.arg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import display.DisplayData;

import plugins.argPlugin.arg.argStats.MaxHeight;
import plugins.argPlugin.argDisplay.ARGDisplay;

import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;

import figure.series.XYSeries;
import figure.series.XYSeriesFigure;

/**
 * An analyzer for ARGs, which displays some basic info in a tablea and shows a chart with a few series'
 * @author brendan
 *
 */
public class ARGAnalyzer extends Analyzable {

	XYSeriesFigure lineChart;
	ARG arg;
	
	public ARGAnalyzer(ARGDisplay source) {
		super(source);
		initializeComponents();
	}

	public void analyze(String name, Object data) {
		if (data instanceof DisplayData) {
			arg = (ARG)((DisplayData)data).getData(0);
		}
		else {
			if (! (data instanceof ARG)) {
				System.err.println("Got data incompatible with this output type");
				return;
			}
			arg = (ARG)data;
		}
		
		MaxHeight mrcaHeight = new MaxHeight();
		
		XYSeries maxHeightSeries = mrcaHeight.computeSeries(arg);
		lineChart.addDataSeries(maxHeightSeries);
		lineChart.repaint();
	}
	
	private void initializeComponents() {
		this.setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.setBackground(Color.white);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

		leftPanel.setMinimumSize(new Dimension(50, 1));
		leftPanel.setPreferredSize(new Dimension(250, 100));
		
		JButton saveData = new JButton("Save data");
		saveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveData();
			}
		});
		leftPanel.add(saveData);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		lineChart = new XYSeriesFigure();
		rightPanel.add(lineChart, BorderLayout.CENTER);
		lineChart.setPreferredSize(new Dimension(600, 300));
		lineChart.setMinimumSize(new Dimension(100, 100));
		
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		
		scrollPane = new JScrollPane(mainPanel);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Save information inmain chart to a file
	 */
	protected void saveData() {
		JFileChooser fileChooser = new JFileChooser();
		int val = fileChooser.showSaveDialog(SunFishFrame.getSunFishFrame());
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = fileChooser.getSelectedFile();
    		try {
    			String data = getDataAsText();
    			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    			writer.write(data);
    			writer.close();
    			SunFishFrame.getSunFishFrame().setInfoLabelText("Wrote data to file " + file.getName());
    		}
    		catch(IOException ioe) {
    			SunFishFrame.getSunFishFrame().getLogger().warning("Error saving chart image to file : " + ioe.toString() );
    		}
    	}		
	}
	
	/**
	 * Converts data in (first series only) of chart into a string
	 * @return
	 */
	private String getDataAsText() {
		String delimiter = ",";
		StringBuilder str = new StringBuilder();
		XYSeries series = (XYSeries)lineChart.getSeries(0);
		for(int i=0; i<series.size(); i++) {
			str.append(series.getX(i) + delimiter + series.getY(i) + "\n");
		}
		
		return str.toString();
	}

	private JScrollPane scrollPane;
}
