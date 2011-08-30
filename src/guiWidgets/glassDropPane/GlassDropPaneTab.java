package guiWidgets.glassDropPane;


import guiWidgets.PrettyLabel;
import guiWidgets.SpinArrow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;



public class GlassDropPaneTab extends JPanel implements MouseListener {
	
	SpinArrow button;
	JLabel label;

	boolean open = false;
	GlassDropPane parentPane;
	GlassPaneThing myPane;

	public GlassDropPaneTab(GlassDropPane parent, String title, GlassPaneThing pane) {
		this.parentPane = parent;
		this.myPane = pane;
		setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		button = new SpinArrow();
		button.setOpaque(false);
		label = new PrettyLabel(title, -2);
		add(button);
		button.addMouseListener(this);
		label.addMouseListener(this);
		add(label);
		setBackground(Color.WHITE);

		setAlignmentY(Component.TOP_ALIGNMENT);
		button.setAlignmentY(Component.TOP_ALIGNMENT);
		label.setAlignmentY(Component.TOP_ALIGNMENT);
		setPreferredSize(new Dimension(label.getPreferredSize().width+20, 27));
		setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 1) );
		setOpaque(false);
	}
	
	public GlassPaneThing getPanel() {
		return myPane;
	}
	
	
	public void setClosed() {
		button.close();
		myPane.setHide(true);
		myPane.setVisible(false);
	}
	
	public void setOpen() {
		button.open();
		myPane.setHide(false);
		myPane.setVisible(true);
	}
	
	public void mouseClicked(MouseEvent arg0) {
		parentPane.closeOthers(this);
		
		if (! isOpen()) {
			parentPane.setSelected(this);
		}
		else {
			parentPane.setSelected();
		}
	}

	public boolean isOpen() {
		return button.isOpen();
	}


	public void mouseEntered(MouseEvent arg0) {
		button.mouseEntered(arg0);
	}

	public void mouseExited(MouseEvent arg0) {
		button.mouseExited(arg0);
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}




	
}
