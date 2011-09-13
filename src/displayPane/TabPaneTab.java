package displayPane;

import guiWidgets.PrettyLabel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import topLevelGUI.SunFishFrame;


import display.Display;

public class TabPaneTab extends JPanel {

	JLabel close;
	JLabel pop;
	JLabel title;
	DisplayPane pane;
	SunFishFrame cfFrame;
	Icon icon;
	static final ImageIcon closeEnabled = getIcon("icons/close_enabled_12x12.png");
	static final ImageIcon closeDisabled = getIcon("icons/close_disabled_12x12.png");
	static final ImageIcon popEnabled = getIcon("icons/popWindow_enabled_16x16.png");
	static final ImageIcon popDisabled = getIcon("icons/popWindow_16x16.png");
	static final ImageIcon unsavedIcon = getIcon("icons/unsavedIcon.png");
	
	protected boolean unsaved = true;
	
	public TabPaneTab(String name, SunFishFrame cfFrame, ImageIcon icon)  {
		super();
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.pane = cfFrame.getDisplayPane();
		this.cfFrame = cfFrame;
		this.icon = icon;
		setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));

		int suffixPos = name.lastIndexOf(".");
		String shortName = name.substring(0, name.length());
		if (suffixPos>0)
			shortName = name.substring(0, suffixPos);
		title = new PrettyLabel(icon, shortName);
		title.setFont(new Font("Sans", Font.PLAIN, 12));

			
		close = new JLabel(closeDisabled);		
		close.setFocusable(false);
		close.setPreferredSize(new Dimension(15, 15));
		close.addMouseListener(new ClickHandler(close));
		close.setToolTipText("Close this tab");
		
		pop = new JLabel(popDisabled);
		
		pop.setFocusable(false);
		
		pop.setPreferredSize(new Dimension(15, 15));

		pop.addMouseListener(new ClickHandler(pop));

		pop.setToolTipText("Display in new window");
		
		TabMouseListener tbListener = new TabMouseListener(this);
		title.addMouseListener(tbListener);
		close.addMouseListener(tbListener);
		
		add(close);
		add(title);
		add(pop);
	}
	
	static ImageIcon getIcon(String url) {
			ImageIcon icon = null;
			try {
				java.net.URL imageURL = TabPaneTab.class.getResource(url);
				icon = new ImageIcon(imageURL);
			}
			catch (Exception ex) {
				SunFishFrame.getSunFishFrame().getLogger().warning("Error loading icon from resouce : " + ex);
			}
			return icon;
	}
	
	public void setTitle(String newTitle) {
		title.setText(newTitle);
		repaint();
	}
	
	public String getTitle() {
		return title.getText();
	}
	
	public void closeThisTab() {
		int i = pane.indexOfTabComponent(this);
	    pane.closeDisplay(i);
	}

	public void popIt() {
		Component display = getMyComponent();
		if (display instanceof Display) {
			((Display)display).pop();
		}
	}
	
	/**
	 * Convenience method that returns the component associated with this tab. 
	 * @return
	 */
	private Component getMyComponent() {
		int i = pane.indexOfTabComponent(this);
		return pane.getComponentAt(i);
	}
	
	
	public void paint(Graphics g) {
		super.paint(g);
		//Graphics2D g2d = (Graphics2D)g;
		
		//If i'm associated with a Display that has unsaved changes, paint a little marker
		Component myComponent = getMyComponent();
		if (myComponent instanceof Display) {
			if (((Display)myComponent).hasUnsavedChanges()) {

				int xVal = this.getWidth()/2 + (title.getWidth())/2-2;

				g.drawImage(unsavedIcon.getImage(), xVal, 1, null); 
			}
		}
	}
	
	private class TabMouseListener implements MouseListener {
		
		JPanel tab;
		
		public TabMouseListener(JPanel tab) {
			this.tab = tab;
		}
		
		public void mouseClicked(MouseEvent arg0) {
			if (arg0.getSource()==title)  {
				int i = pane.indexOfTabComponent(tab);
				pane.setSelectedIndex(i);
			}
		}

		public void mouseEntered(MouseEvent arg0) {
			close.setIcon(closeEnabled);
			pop.setIcon(popEnabled);

			close.repaint();
			pop.repaint();
			repaint();
		}

		public void mouseExited(MouseEvent arg0) {
			close.setIcon(closeDisabled);
			pop.setIcon(popDisabled);
			pane.repaint(0, 0, pane.getWidth(), 20);
			close.repaint();
			pop.repaint();
			repaint();
		}

		public void mousePressed(MouseEvent arg0) {		
		}

		public void mouseReleased(MouseEvent arg0) {
		
		}
		
	}
	
	
	private class ClickHandler implements MouseListener {
		
		JLabel buttonParent;

		
		public ClickHandler(JLabel button) {
			super();
			buttonParent = button;
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getSource() == close) {
				closeThisTab();
			}
			if (e.getSource() == pop) {
				popIt();
			}
		}
		
		public void mousePressed(MouseEvent e) {}
		
		public void mouseReleased(MouseEvent e) {
			buttonParent.repaint();			
		}
		
	}



}
