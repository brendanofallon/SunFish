package plugins.SGPlugin.analyzer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import display.Display;

import topLevelGUI.analyzer.Analyzable;

import element.sequence.*;
import errorHandling.ErrorWindow;

public class DotPlotAnalyzer extends Analyzable {

	JScrollPane mainScrollPane;
	JPanel mainPanel;
	DotPlotFigure dotPlot;
	
	SequenceGroup currentSG;
	
	public DotPlotAnalyzer(Display source) {
		super(source);
		initializeComponents();
	}

	
	
	public void analyze(String name, Object data) {
		if (!(data instanceof SequenceGroup)) {
			ErrorWindow.showErrorWindow(new Exception("Got a non-sequencegroup object for a dot plot analysis"));
		}
		
		SequenceGroup sg = (SequenceGroup)data;
		currentSG = sg;
		
		titleLabel.setText(name);
		
		DefaultComboBoxModel cModel1 = new DefaultComboBoxModel();
		DefaultComboBoxModel cModel2 = new DefaultComboBoxModel();
	
		for(int j=0; j<sg.size(); j++) {
			cModel1.addElement(sg.get(j).getName());
			cModel2.addElement(sg.get(j).getName());
		}
		seq1Box.setModel(cModel1);
		
		seq2Box.setModel(cModel2);
		
		seq1Box.setSelectedIndex(0);
		seq2Box.setSelectedIndex(1);
		
		dotPlot.setBlockWidth((Integer)blockWidthSpinner.getValue());
		changeSequences();
	}
	
	
	private void initializeComponents() {
		mainPanel = new JPanel();
		mainScrollPane = new JScrollPane(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setMinimumSize(new Dimension(300, 300));
		mainPanel.setBackground(Color.white);
		
		
		mainScrollPane.setBackground(Color.white);
		this.setLayout(new BorderLayout());
		this.add(mainScrollPane, BorderLayout.CENTER);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.white);
		leftPanel.setMinimumSize(new Dimension(200, 10));
		leftPanel.setPreferredSize(new Dimension(200, 10));

		mainPanel.add(leftPanel, BorderLayout.WEST);
		
		
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		titleLabel = new JLabel();
		titleLabel.setAlignmentX(RIGHT_ALIGNMENT);
		leftPanel.add(Box.createVerticalStrut(10));
		leftPanel.add(titleLabel);

		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		buttonPanel.add(saveButton);
		
		JButton restoreButton = new JButton("Restore");
		restoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restoreDefaultBounds();
			}
		});
		
		buttonPanel.add(restoreButton);
		JButton zoomButton = new JButton("Zoom");
		zoomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zoomSelectedBounds();
			}
		});
		buttonPanel.add(zoomButton);
		
		
		leftPanel.add(buttonPanel);
		
		leftPanel.add(Box.createVerticalStrut(50));
		seq1Box = new JComboBox();
		seq1Box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeSequences();
			}
		});
		leftPanel.add(seq1Box);
		seq1Box.setPreferredSize(new Dimension(180, 24));
		seq1Box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		seq1Box.setAlignmentX(RIGHT_ALIGNMENT);
		revComp1Box = new JCheckBox("Use reverse complement");
		revComp1Box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				changeSequences();
			}
		});
		revComp1Box.setAlignmentX(RIGHT_ALIGNMENT);
		leftPanel.add(revComp1Box);

		leftPanel.add(Box.createVerticalStrut(10));
		

		
		seq2Box = new JComboBox();
		seq2Box.setPreferredSize(new Dimension(180, 24));
		seq2Box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		seq2Box.setAlignmentX(RIGHT_ALIGNMENT);
		seq2Box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeSequences();
			}
		});
		leftPanel.add(seq2Box);
		revComp2Box = new JCheckBox("Use reverse complement");
		revComp2Box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				changeSequences();
			}
		});
		revComp2Box.setAlignmentX(RIGHT_ALIGNMENT);
		leftPanel.add(revComp2Box);
		
		leftPanel.add(Box.createVerticalStrut(15));
		blockWidthSpinner = new JSpinner();
		blockWidthSpinner.setModel(new SpinnerNumberModel(3, 1, 100, 1));
		blockWidthSpinner.setToolTipText("The number of sites used per rectangle");
		blockWidthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateBlockWidth();
			}
		});
		JPanel bwPanel = new JPanel();
		bwPanel.setOpaque(false);
		bwPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel lab = new JLabel("Block width : ");
		bwPanel.add(lab);
		bwPanel.add(blockWidthSpinner);
		bwPanel.setAlignmentX(RIGHT_ALIGNMENT);
		leftPanel.add(bwPanel);
		
		leftPanel.add(Box.createVerticalGlue());
		
		
		dotPlot = new DotPlotFigure();
		dotPlot.setPreferredSize(new Dimension(300, 300));
		mainPanel.add(dotPlot, BorderLayout.CENTER);
	}
	
	protected void saveImage() {
		// TODO Auto-generated method stub
		
	}


	/**
	 * Zoom in to the selection box
	 */
	protected void zoomSelectedBounds() {
		dotPlot.zoomToSelection();
	}



	protected void restoreDefaultBounds() {
		dotPlot.restoreDefaultBounds();
	}



	protected void updateBlockWidth() {
		dotPlot.setBlockWidth((Integer)blockWidthSpinner.getValue());
		dotPlot.repaint();
	}



	protected void changeSequences() {
		String name1 = (String)seq1Box.getSelectedItem();
		Sequence seq1 = currentSG.getSequenceForName(name1);
		
		if (revComp1Box.isSelected()) {
			seq1 = seq1.getReverseComplement();
			seq1.setName(name1);
		}
		
		String name2 = (String)seq2Box.getSelectedItem();
		Sequence seq2 = currentSG.getSequenceForName(name2);
		if (revComp2Box.isSelected()) {
			seq2 = seq2.getReverseComplement();
			seq2.setName(name2);
		}
		dotPlot.setSequences(seq1, seq2);
		repaint();
	}


	JSpinner blockWidthSpinner;
	JLabel titleLabel;
	JComboBox seq1Box;
	JCheckBox revComp1Box;
	JComboBox seq2Box;
	JCheckBox revComp2Box;
}
