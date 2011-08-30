package plugins.SGPlugin.analyzer.haplotype;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.SGPlugin.analyzer.haplotype.figure.HaplotypeNetworkFigure;

import display.Display;

import topLevelGUI.analyzer.Analyzable;

import guiWidgets.ColorSwatchButton;
import guiWidgets.SpinArrow;

/**
 * Displays a haplotype network and some controls that affect the presentation of the network.
 * @author brendan
 *
 */
public class HapNetworkAnalyzer extends Analyzable implements PropertyChangeListener {

	JScrollPane scrollPane;
	JPanel mainPanel;
	HaplotypeNetworkFigure hapNetFigure;

	public HapNetworkAnalyzer(Display source) {
		super(source);
		hapNetFigure = new HaplotypeNetworkFigure(source);
		
		initializeComponents();
	}


	
	public void analyze(String name, Object data) {
		if (! (data instanceof HaplotypeNetwork)) {
			throw new IllegalArgumentException("Data was not of type HaplotypeNetwork");
		}
		
		titleLabel.setText(name);
		
		HaplotypeNetwork hapNet = (HaplotypeNetwork)data;
		hapNetFigure.setNetwork(hapNet);
		
		
		double val = zoomSlider.getValue()/50.0;
		hapNetFigure.rescale( val );
		
		val = (double)rotationSlider.getValue() / (double)rotationSlider.getMaximum() * 360.0 - 180.0;
		
		hapNetFigure.setRotationValue(val);
		
		hapNetFigure.setMarkerSizeFactor((double)markerSizeSlider.getValue()/(double)markerSizeSlider.getMaximum());
		hapNetFigure.repaint();
	}


	/**
	 * Called when the marker color button color has been changed;
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("Swatch Color")) {
			hapNetFigure.setMarkerColor(markerColorButton.getColor());
		}
		hapNetFigure.repaint();
	}
	
	public JComponent getComponent() {
		return scrollPane;
	}


	public Class getDataType() {
		return HaplotypeNetwork.class;
	}
	
	
	/**
	 * Construct GUI components 
	 */
	private void initializeComponents() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.setMinimumSize(new Dimension(500, 400));
		mainPanel.setOpaque(false);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		titleLabel = new JLabel("Your name here");
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(titleLabel);
		leftPanel.setMinimumSize(new Dimension(200, 100));
		leftPanel.setPreferredSize(new Dimension(200, 300));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 0));
		leftPanel.setOpaque(false);
		
		
		JButton rotateB = new JButton("Do something");
		rotateB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSomething();
				//hapNetFigure.rotateOptimally();
				repaint();
			}			
		});
		//leftPanel.add(rotateB);
		
		rotationSlider.setAlignmentX(LEFT_ALIGNMENT);
		rotationSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double val = (double)rotationSlider.getValue() / (double)rotationSlider.getMaximum() * 360.0 - 180.0;
				hapNetFigure.setRotationValue(val);
			}
		});
		//leftPanel.add(makeSliderPanel("Rotation", rotationSlider));
		
		zoomSlider.setAlignmentX(LEFT_ALIGNMENT);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double val = zoomSlider.getValue()/50.0;
				hapNetFigure.rescale( val );
			}
		});
		leftPanel.add(makeSliderPanel("Zoom", zoomSlider));
		
		JPanel colorPanel = new JPanel();
		colorPanel.setOpaque(false);
		colorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		markerColorButton = new ColorSwatchButton(hapNetFigure.getInitMarkerColor());
		markerColorButton.addPropertyChangeListener("Swatch Color", this);
		colorPanel.add(markerColorButton);
		colorPanel.add(new JLabel("Marker color"));
		colorPanel.setMaximumSize(new Dimension(125, 26));
		colorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(colorPanel);
		
		mobileNodesBox = new JCheckBox("Movable nodes");
		mobileNodesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		mobileNodesBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				hapNetFigure.setNodeMobility(mobileNodesBox.isSelected());
			}
		});
		leftPanel.add(mobileNodesBox);
		
		edgeLengthsProportionalBox = new JCheckBox("Edge lengths reflect distance");
		edgeLengthsProportionalBox.setSelected(true);
		edgeLengthsProportionalBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		edgeLengthsProportionalBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (edgeLengthsProportionalBox.isSelected() != hapNetFigure.getEdgeLengthsProportional() ) {
					hapNetFigure.setEdgeLengthsProportional(edgeLengthsProportionalBox.isSelected());
					hapNetFigure.repaint();
				}
			}
		});		
		leftPanel.add(edgeLengthsProportionalBox);
		
		
		JPanel tickPanel = new JPanel();
		tickPanel.setOpaque(false);
		tickPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tickPanel.setAlignmentX(LEFT_ALIGNMENT);
		tickPanel.add(new JLabel("Ticks:"));
		String[] choices = {HaplotypeNetworkFigure.NONE, HaplotypeNetworkFigure.TICKS, HaplotypeNetworkFigure.DISTANCES};
		JComboBox tickBox = new JComboBox(choices);
		tickBox.setPreferredSize(new Dimension(100, 24));
		tickBox.setMaximumSize(new Dimension(100, 24));
		tickBox.setAlignmentX(LEFT_ALIGNMENT);
		tickBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				hapNetFigure.setTickState( (String)arg0.getItem());
			}
		});
		tickPanel.add(tickBox);
		leftPanel.add(tickPanel);
		
		layoutOpsPanel = new JPanel();
		layoutOpsPanel.setLayout(new BorderLayout());
		layoutOpsPanel.setOpaque(false);
		layoutOpsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		layoutOpArrow = new SpinArrow("Layout Options");
		layoutOpArrow.setOpaque(false);
		layoutOpArrow.addPropertyChangeListener(SpinArrow.SPIN_ARROW_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				toggleLayoutOptionsVisible();
			}
		});
		layoutOpsPanel.add(layoutOpArrow, BorderLayout.NORTH);
		optionsPanel = new JPanel();
		optionsPanel.setOpaque(false);
		leftPanel.add(layoutOpsPanel);
		
		
		markerSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 1000, 500);
		markerSizeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(Box.createVerticalStrut(20));
		
		markerSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				
				double val = markerSizeSlider.getValue();
				
				hapNetFigure.setMarkerSizeFactor(val/(double)markerSizeSlider.getMaximum());
				hapNetFigure.repaint();
			}
		});
		optionsPanel.add(makeSliderPanel("Marker size", markerSizeSlider));
		
		leftPanel.add(Box.createVerticalGlue());
		mainPanel.add(leftPanel, BorderLayout.WEST);		
		mainPanel.add(hapNetFigure, BorderLayout.CENTER);
		scrollPane = new JScrollPane(mainPanel);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);		
	}
	
	/**
	 * Toggles visible state of the panel displaying advanced layout options, called
	 * when the SpinArrow changes state
	 */
	protected void toggleLayoutOptionsVisible() {
		if (layoutOpArrow.isOpen()) {
			layoutOpsPanel.add(optionsPanel, BorderLayout.CENTER);
			layoutOpsPanel.revalidate();
			layoutOpsPanel.repaint();
		}
		else {
			layoutOpsPanel.remove(optionsPanel);
			layoutOpsPanel.revalidate();
			layoutOpsPanel.repaint();
		}
	}



	protected void doSomething() {
		hapNetFigure.rotateAllCladesDownhill();
	}



	private JPanel makeSliderPanel(String label, JSlider slider) {
		JPanel rPanel = new JPanel();
		rPanel.setOpaque(false);
		rPanel.setAlignmentX(LEFT_ALIGNMENT);
		rPanel.setLayout(new BoxLayout(rPanel, BoxLayout.X_AXIS));
		rPanel.add(Box.createGlue());
		JLabel lab = new JLabel(label);
		lab.setFont(new Font("Sans", Font.PLAIN, 11));
		rPanel.add(lab);
		rPanel.add(Box.createGlue());
		rPanel.setPreferredSize(new Dimension(200, 30));
		slider.setPreferredSize(new Dimension(100, 25));
		slider.setFont(new Font("Sans", Font.PLAIN, 0));
		rPanel.add(slider);
		return rPanel;
	}
	
	JCheckBox drawTicksBox;
	JCheckBox edgeLengthsProportionalBox;
	JCheckBox mobileNodesBox;
	JLabel titleLabel;
	JSlider markerSizeSlider;
	SpinArrow layoutOpArrow;
	JPanel layoutOpsPanel;
	JPanel optionsPanel;
	boolean optionsPanelVisible = false;
	final JSlider zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
	final JSlider rotationSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
	ColorSwatchButton markerColorButton;



}
