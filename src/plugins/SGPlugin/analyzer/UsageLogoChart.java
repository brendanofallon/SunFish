package plugins.SGPlugin.analyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;

import display.Display;

import topLevelGUI.analyzer.Analyzable;

import element.sequence.SequenceGroup;
import figure.Figure;
import guiWidgets.ColorSwatchButton;
import guiWidgets.SpinArrow;

public class UsageLogoChart extends Analyzable implements PropertyChangeListener {

	SequenceGroup currentSG;
	JScrollPane scrollPane; 
	JPanel usageArray; // Container that stores all usageLogoPanels 
	JPanel mainPanel;
	JLabel titleLabel;
	ColorSwatchButton aColor;
	ColorSwatchButton cColor;
	ColorSwatchButton tColor;
	ColorSwatchButton gColor;
	ArrayList<UsageLogoPanel> ulPanels;
	
	
	public UsageLogoChart(Display source) {
		super(source);
		initializePanel();
		ulPanels = new ArrayList<UsageLogoPanel>();
		recolorLabels();
	}
	
	

	private void initializePanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.white);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		titleLabel = new JLabel("(unknown name)");
		leftPanel.add(titleLabel);
		leftPanel.setMinimumSize(new Dimension(200, 1));
		leftPanel.setPreferredSize(new Dimension(200, 300));
		leftPanel.setMaximumSize(new Dimension(200, 10000));
		leftPanel.setOpaque(false);
		leftPanel.add(Box.createVerticalStrut(25));
		
		JPanel aColorPanel = new JPanel();
		aColorPanel.setOpaque(false);
		aColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		aColor = new ColorSwatchButton(Color.green);
		aColor.addPropertyChangeListener("Swatch Color", this);
		aColorPanel.add(aColor);
		aColorPanel.add(new JLabel("A color"));
		
		leftPanel.add(aColorPanel);
		
		JPanel gColorPanel = new JPanel();
		gColorPanel.setOpaque(false);
		gColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		gColor = new ColorSwatchButton(Color.blue);
		gColor.addPropertyChangeListener("Swatch Color", this);
		gColorPanel.add(gColor);
		gColorPanel.add(new JLabel("G color"));
		
		leftPanel.add(gColorPanel);
		
		JPanel cColorPanel = new JPanel();
		cColorPanel.setOpaque(false);
		cColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		cColor = new ColorSwatchButton(Color.yellow);
		cColor.addPropertyChangeListener("Swatch Color", this);
		cColorPanel.add(cColor);
		cColorPanel.add(new JLabel("C color"));
		
		leftPanel.add(cColorPanel);
		
		JPanel tColorPanel = new JPanel();
		tColorPanel.setOpaque(false);
		tColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tColor = new ColorSwatchButton(Color.red);
		tColor.addPropertyChangeListener("Swatch Color", this);
		tColorPanel.add(tColor);
		tColorPanel.add(new JLabel("T color"));
		
		leftPanel.add(tColorPanel);
		leftPanel.add(Box.createGlue());
	
		mainPanel.add(leftPanel, BorderLayout.WEST);
		
		JPanel backPanel = new JPanel();
		backPanel.setLayout(new BorderLayout());
		backPanel.setBackground(Color.white);
		JPanel topPanel = new JPanel();
		topPanel.setMinimumSize(new Dimension(1, 50));
		topPanel.setPreferredSize(new Dimension(100, 50));
		topPanel.setOpaque(false);
		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setMinimumSize(new Dimension(1, 25));
		bottomPanel.setOpaque(false);
		backPanel.add(topPanel, BorderLayout.NORTH);
		backPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		scrollPane = new JScrollPane();
		usageArray = new JPanel();
		usageArray.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 1));
		usageArray.setBackground(Color.white);
		backPanel.add(usageArray, BorderLayout.CENTER);
		scrollPane.setViewportView(backPanel);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(100);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("Swatch Color")) {
			recolorLabels();
		}	
	}
	
	private void recolorLabels() {
		for(UsageLogoPanel ulp : ulPanels) {
			ulp.setAColor(aColor.getColor());
			ulp.setCColor(cColor.getColor());
			ulp.setGColor(gColor.getColor());
			ulp.setTColor(tColor.getColor());
		}
		usageArray.repaint();
	}



	public void analyze(String name, Object data) {
		if (! (data instanceof SequenceGroup)) {
			throw new IllegalArgumentException("Cannot cast data to SequenceGroup in UsageLogoChart");
		}
		
		currentSG = (SequenceGroup)data;
		titleLabel.setText(name);
		SequenceGroupCalculator sgCalc = new SequenceGroupCalculator(currentSG);
		for(int i=0; i<currentSG.getMaxSeqLength(); i++) {
			UsageLogoPanel ulp = new UsageLogoPanel();
			ulp.setMinimumSize(new Dimension(40, 150));
			ulp.setPreferredSize(new Dimension(40, 150));
			ulp.setMaximumSize(new Dimension(40, 150));
			double[] freqs = sgCalc.getColumnBaseFreqs(i);
			ulp.setFrequencies(freqs);
			ulp.setShowPosition(true, i+1);
			usageArray.add(ulp);
			ulPanels.add(ulp);
		}
		
		recolorLabels();
		mainPanel.repaint();
	}
	
	
	
}
