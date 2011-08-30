package plugins.treePlugin.parsers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import errorHandling.ErrorWindow;
import errorHandling.FileParseException;
import guiWidgets.PrettyLabel;



public class TreeLogSetupFrame extends javax.swing.JFrame {
	
	JPanel mainPanel;
	JSlider frequencySlider;
	JComboBox subsampleFreqBox;
	JTextField maxTreesArea;
	JButton cancelButton;
	JButton doneButton;
	JComboBox distBox;
	TreeLogParser parser;
	Font sliderValueFont;
	
	JSpinner skipTreesSpinner;

	public static final String ALL = "100%";
	public static final String HALF = "50%";
	public static final String THIRD = "33%";
	public static final String QUARTER = "25%";
	public static final String TENTH = "10%";
	public static final String TWENTIETH = "5%";
	public static final String HUNDRETH = "1%";
	
	public static final String DIST_FROM_ROOT = "From root";
	public static final String DIST_FROM_TIPS = "From tips";
	
	public TreeLogSetupFrame(TreeLogParser parser) {
		super("Tree Builder Options");
		this.parser = parser;
		
		sliderValueFont = new Font("Sans", Font.PLAIN, 10);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		this.add(mainPanel);
		//mainPanel.setPreferredSize(new Dimension(300, 300));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 8));
		//setPreferredSize(new Dimension(300, 300) );
		
//		mainPanel.add(new PrettyLabel("Max number of trees to include"));
//		maxTreesArea = new JTextField();
//		maxTreesArea.setText("(all)");
//		maxTreesArea.setMaximumSize(new Dimension(200, 25));
//		maxTreesArea.setAlignmentX(Component.LEFT_ALIGNMENT);
//		mainPanel.add(maxTreesArea);
		
		//mainPanel.add(Box.createVerticalStrut(10));
		//mainPanel.add(new PrettyLabel("Calculate branch lengths"));
		//String[] distOptions = {DIST_FROM_TIPS, DIST_FROM_ROOT};
		//distBox = new JComboBox(distOptions);
		//distBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		//mainPanel.add(distBox);
		
		mainPanel.add(Box.createVerticalStrut(10));
		JLabel sslabel = new PrettyLabel("Sample fraction of trees");
		sslabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(sslabel);
		
		String[] freqStrs = {ALL, HALF, THIRD, QUARTER, TENTH, TWENTIETH, HUNDRETH};
		subsampleFreqBox = new JComboBox(freqStrs);
		subsampleFreqBox.setToolTipText("Fraction of trees to include");
		subsampleFreqBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		subsampleFreqBox.setFont(sliderValueFont);
		mainPanel.add(subsampleFreqBox);
		
		mainPanel.add(Box.createVerticalStrut(10));
		JLabel label = new PrettyLabel("Include clades with support above :");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(label);
		frequencySlider = new JSlider(50, 100, 50);
		frequencySlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		frequencySlider.setFont(sliderValueFont);
		frequencySlider.setPaintTicks(true);
		frequencySlider.setPaintLabels(true);
		mainPanel.add(frequencySlider);
		
		
		SpinnerModel skipModel = new SpinnerNumberModel(1000, 0, 1e9, 100);
		skipTreesSpinner = new JSpinner(skipModel);
		skipTreesSpinner.setMinimumSize(new Dimension(80, 10));
		JPanel skipPanel = new JPanel();
		skipPanel.setOpaque(false);
		skipPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		skipPanel.setAlignmentX(LEFT_ALIGNMENT);
		skipPanel.add(new JLabel("Initial trees to skip:"));
		skipPanel.add(skipTreesSpinner);
		mainPanel.add(skipPanel);
		
		mainPanel.add(Box.createVerticalStrut(20));
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancel();
			}
		});
		bottomPanel.add(cancelButton);
		bottomPanel.add(Box.createHorizontalGlue());
		doneButton = new JButton("Build tree");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				done();
			}
		});
		bottomPanel.add(doneButton);
		mainPanel.add(bottomPanel);
		bottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		this.add(mainPanel);
		pack();
		setLocationRelativeTo(null);
	}

	public String getDistMeasure() {
		return (String)distBox.getSelectedItem();
	}
	
	public double getTargetFrequency() {
		return (double)frequencySlider.getValue()/100.0;
	}
	
	public Integer getBurninTrees() {
		return (int)Math.round((Double)skipTreesSpinner.getValue());
	}
	
	public int getSubsampleFrequency() {
		String freq = (String)subsampleFreqBox.getSelectedItem();
		if (freq.equals(ALL)) {
			return 1;
		}
		if (freq.equals(HALF)) {
			return 2;
		}
		if (freq.equals(THIRD)) {
			return 3;
		}
		if (freq.equals(QUARTER)) {
			return 4;
		}
		if (freq.equals(TENTH)) {
			return 10;
		}
		if (freq.equals(TWENTIETH)) {
			return 20;
		}
		if (freq.equals(HUNDRETH)) {
			return 100;
		}
		return 10;
	}
	
//	public int getMaxTreeCount() {
//		int max = Integer.MAX_VALUE;
//	
//		try {
//			 max = Integer.parseInt(maxTreesArea.getText());
//		}
//		catch (NumberFormatException nfe) {
//			// don't do anything
//		}
//		return max;
//	}

	protected void done() {
		setVisible(false);
		parser.parseFile();
		this.dispose();
	}


	protected void cancel() {
		setVisible(false);
		this.dispose();
	}
}
