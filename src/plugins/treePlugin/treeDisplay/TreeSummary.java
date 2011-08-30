package plugins.treePlugin.treeDisplay;

import guiWidgets.PrettyLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import display.Display;

import plugins.treePlugin.tree.Tree;
import plugins.treePlugin.treeDisplay.treeStatistics.*;

import topLevelGUI.analyzer.Analyzable;


public class TreeSummary extends Analyzable {

	JScrollPane panel;
	Tree tree;
	TreeHeight treeHeight;
	SackinsIndex sackins;
	NodeTotal numNodes;
	CollessIndex colless;
	NumberOfTips numTips;
	TreeHasPolytomies polytomies;
	
	Font labelFont;
	Color bgColor;
	NumberFormat formatter;
	
	public TreeSummary(Display source) {
		super(source);
		labelFont = new Font("Sans", Font.PLAIN, 12);
		bgColor = new Color(249, 249, 249);
		initializePanel();
		formatter = new DecimalFormat("0.0###");
	}
	
	public void analyze(String name, Object data) throws ClassCastException {
		
		this.tree = (Tree)data;
		
		System.out.println("Summary got name : " + name);
		
		titleLabel.setText(name);
		titleLabel.repaint();
		treeHeight = new TreeHeight(tree);
		numNodes = new NodeTotal(tree);
		colless = new CollessIndex(tree);
		sackins = new SackinsIndex(tree);
		numTips = new NumberOfTips(tree);
		polytomies = new TreeHasPolytomies(tree);
		
		height.setText(treeHeight.getName() + " : " + treeHeight.getValue() );
		height.setToolTipText(treeHeight.getDescription());
		
		setLabelProps(height, treeHeight);
		setLabelProps(nodeTotal, numNodes);
		setLabelProps(leafTotal, numTips); 
		setLabelProps(collessIndex, colless); 
		setLabelProps(sackinsIndex, sackins);
		setLabelProps(hasPolytomies, polytomies);
	}
	
	private void setLabelProps(JLabel label, TreeStatistic stat) {
		String str = "";
		
		if (stat.hasDoubleValue()) {
			Double val = stat.getValue();

			if ( Double.isNaN(val)) {
				str = stat.getName() + " : NA";
			}
			else {
				str = stat.getName() + " : " + formatter.format(stat.getValue());
				if (stat.hasStandardDeviation()) {
					str = str + " (" + formatter.format(stat.getStandardDeviation()) + ")";
				}
			}
		}
		
		if (stat.hasBooleanValue()) {
			if (stat.getBooleanValue())
				str = stat.getName() + " : Yes";
			else
				str = stat.getName() + " : No";
		}
		
		label.setText(str);
		label.setToolTipText(stat.getDescription());
		label.setFont(labelFont);
	}

	public JComponent getComponent() {
		return panel;
	}

	public Class getDataType() {
		return Tree.class;
	}
	
	protected void initializePanel() {
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(0, 2, 4, 4));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		innerPanel.setBackground(bgColor);
		innerPanel.setMinimumSize(new Dimension(400, 200));
		innerPanel.setPreferredSize(new Dimension(400, 300));
		innerPanel.setOpaque(false);
		
		nodeTotal = new PrettyLabel();
		leafTotal = new PrettyLabel();
		height = new PrettyLabel();
		collessIndex = new PrettyLabel();
		sackinsIndex = new PrettyLabel();
		hasPolytomies = new PrettyLabel();
		
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(bgColor);
		leftPanel.setMinimumSize(new Dimension(100, 100));
		leftPanel.setPreferredSize(new Dimension(100, 100));
		leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		leftPanel.setOpaque(false);
		
		topLeftPanel = new JPanel();
		topLeftPanel.setMinimumSize(new Dimension(100, 30));
		topLeftPanel.setPreferredSize(new Dimension(100, 30));
		topLeftPanel.setMaximumSize(new Dimension(1000, 30));
		topLeftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		topLeftPanel.setBackground(bgColor);
		titleLabel = new PrettyLabel("(no title)");
		topLeftPanel.add(titleLabel);
		topLeftPanel.setOpaque(false);
		
		upperLeftPanel = new JPanel();
		
		upperLeftPanel.setLayout(new BoxLayout(upperLeftPanel, BoxLayout.PAGE_AXIS));
		upperLeftPanel.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder("Summary"), BorderFactory.createEmptyBorder(0,4,2,2)));
		upperLeftPanel.setMinimumSize(new Dimension(100, 100));
		upperLeftPanel.setPreferredSize(new Dimension(300, 200));
		upperLeftPanel.setMaximumSize(new Dimension(350, 350));
		upperLeftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		upperLeftPanel.setBackground(bgColor);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		upperLeftPanel.add(nodeTotal);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		upperLeftPanel.add(leafTotal);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		upperLeftPanel.add(hasPolytomies);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		upperLeftPanel.add(height);
		upperLeftPanel.setOpaque(false);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		nodeTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
		leafTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
		height.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		lowerLeftPanel = new JPanel();
		lowerLeftPanel.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder("Imbalance"), BorderFactory.createEmptyBorder(4,4,2,2)));
		lowerLeftPanel.setLayout(new BoxLayout(lowerLeftPanel, BoxLayout.PAGE_AXIS));
		lowerLeftPanel.setMinimumSize(new Dimension(100, 100));
		lowerLeftPanel.setPreferredSize(new Dimension(300, 200));
		lowerLeftPanel.setMaximumSize(new Dimension(350, 350));
		lowerLeftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lowerLeftPanel.setBackground(bgColor );
		lowerLeftPanel.setOpaque(false);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		lowerLeftPanel.add(collessIndex);
		upperLeftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		lowerLeftPanel.add(sackinsIndex);
		collessIndex.setAlignmentX(Component.LEFT_ALIGNMENT);
		sackinsIndex.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		leftPanel.add(topLeftPanel);
		leftPanel.add(upperLeftPanel);
		leftPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		leftPanel.add(lowerLeftPanel);
		

		rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder("Interesting Info"), BorderFactory.createEmptyBorder(4,4,2,2)));
		rightPanel.setBackground(bgColor);
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		
		innerPanel.add(leftPanel);
		innerPanel.add(rightPanel);
		
		panel = new JScrollPane(innerPanel);
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setViewportBorder(BorderFactory.createEmptyBorder());
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	
	JLabel nodeTotal;
	JLabel leafTotal;
	JLabel collessIndex;
	JLabel sackinsIndex;
	JLabel height;
	JLabel hasPolytomies;
	
	JLabel titleLabel;
	JPanel topLeftPanel;
	JPanel leftPanel;
	JPanel rightPanel;
	JPanel upperLeftPanel;
	JPanel lowerLeftPanel;
	JPanel upperRightPanel;
	JPanel lowerRightPanel;
}
