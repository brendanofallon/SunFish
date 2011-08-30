package plugins.SGPlugin.analyzer.haplotype.figure;

import guiWidgets.ColorSwatchButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import plugins.SGPlugin.analyzer.haplotype.Haplotype;
import plugins.SGPlugin.display.SGContentPanelDisplay;


/**
 * Configuration frame for haplotype elements; this also provides some information to the user
 * regarding the specific sequences that are in this haplotype. 
 * 
 * @author brendan
 *
 */
public class HapElementConfigFrame extends javax.swing.JFrame implements PropertyChangeListener {
	
	List<HaplotypeElement> hapEls;
	HaplotypeNetworkFigure parentFig;
	ColorSwatchButton colorButton;
	
	public HapElementConfigFrame(final List<HaplotypeElement> hapEls, final SGContentPanelDisplay sourceDisplay, HaplotypeNetworkFigure parentFigure) {
		super("Haplotype Configuration");

		parentFig = parentFigure;
		this.hapEls = hapEls; 
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setMinimumSize(new Dimension(100, 130));
		JLabel lab = new JLabel("<html><b>Sequences in this haplotype:</b></html>");
		lab.setAlignmentX(Component.LEFT_ALIGNMENT);

		mainPanel.add(lab);
		lab.setAlignmentX(LEFT_ALIGNMENT);
		centerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JScrollPane namePane = new JScrollPane();
		JList nameList = new JList();
		namePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		namePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		namePane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
		
		DefaultListModel listModel = new DefaultListModel();
		for(HaplotypeElement hapEl : hapEls) {
			Haplotype hap = hapEl.getHap();
			for (String name : hap.getNames()) {
				listModel.add(0, name);
			}
		}
		nameList.setModel(listModel);
		namePane.setViewportView(nameList);
		centerPanel.add(namePane);
		centerPanel.add(Box.createVerticalStrut(10));
		
		JButton selectSeqsButton = new JButton("Select these sequences");
		selectSeqsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	List<String> names = new ArrayList<String>();
            	 if (sourceDisplay != null) {
            		 for(HaplotypeElement hapEl : hapEls) {
            			 Haplotype hap = hapEl.getHap();
            			 names.addAll(hap.getNames());
            		 }
            		
            		 sourceDisplay.selectSequencesByName(names);
            		 
            	 }
            }
        });
		if (sourceDisplay == null)
			selectSeqsButton.setEnabled(false);
		centerPanel.add(selectSeqsButton);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		colorButton = new ColorSwatchButton(hapEls.get(0).getFillColor());
		colorButton.addPropertyChangeListener("Swatch Color", this);
		p1.add(colorButton);
		p1.add(new JLabel("Marker color"));
		centerPanel.add(p1);
		centerPanel.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(centerPanel);
		
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	cancel();
            }
        });
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	done();
            }
        } );
		lowerPanel.add(cancelButton);
		lowerPanel.add(Box.createGlue());
		lowerPanel.add(doneButton);
		lowerPanel.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(lowerPanel);
		mainPanel.add(Box.createVerticalBox());
		this.add(mainPanel);
		pack();
		setLocationRelativeTo(null);
		this.getRootPane().setDefaultButton(doneButton);
	}
	
	

	protected void cancel() {
		setVisible(false);
	}

	protected void done() {
		for(HaplotypeElement hapEl : hapEls)
			hapEl.setFillColor(colorButton.getColor());
		parentFig.repaint();
		setVisible(false);
		
	}



	/**
	 * Called when the color button color has been changed
	 */
	public void propertyChange(PropertyChangeEvent evt) {
//		if (evt.getPropertyName().equals("Swatch Color")) {
//			hapNetFigure.setMarkerColor(colorButton.getColor());
//		}
//		hapNetFigure.repaint();
	}

}
