package guiWidgets.glassDropPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class GlassDropPane extends javax.swing.JPanel {

	ArrayList<GlassDropPaneTab> tabs;
	JPanel tabsPanel;
	JPanel openPanel;
	JFrame parent;
	int selectedIndex = -1;
	boolean open;
	Dimension defaultSize;
	JScrollPane parentScrollPane;
	
	Font defaultFont = new Font("Sans", Font.PLAIN, 11);
	
	public GlassDropPane(JFrame parent) {
		tabs = new ArrayList<GlassDropPaneTab>();
		defaultSize = new Dimension(600, 22);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		tabsPanel = new GlassDropBackground(this);
		tabsPanel.setPreferredSize(defaultSize);
		tabsPanel.setMaximumSize(defaultSize);
		tabsPanel.setBackground(Color.WHITE);
		tabsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(tabsPanel);
		tabsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		setMinimumSize(new Dimension(1, 22));
		setPreferredSize(new Dimension(defaultSize.width, 22));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		this.parent = parent;
		open = false;
	}
	
	public void setPreferredWidth(int pWidth) {
		defaultSize.width = pWidth;
		setPreferredSize(defaultSize);
		for(GlassDropPaneTab tab : tabs) {
			tab.getPanel().setMinimumWidth(pWidth);
		}
	}
	
	public void addPanel(String title, GlassPaneThing panel) {
		GlassDropPaneTab tab = new GlassDropPaneTab(this, title, panel);
		tabs.add(tab);
		tabsPanel.add(tabs.get(tabs.size()-1));
		tabsPanel.validate();
		panel.setMinimumWidth(defaultSize.width);
		panel.setTab(tab);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setPreferredSize(defaultSize);
		//panel.setWidth(800);
		panel.setVisible(false);
	}
	
	public JScrollPane getScrollPane() {
		return parentScrollPane;
	}
	
	/**
	 * If a display is popped into a new window, we need to know about it
	 * @param newParent The new JFrame that contains this component.
	 */
	public void changeParent(JFrame newParent) {
		parent = newParent;
	}
	
	
	public void closeOthers(GlassDropPaneTab notThisOne) {
		for(GlassDropPaneTab tab : tabs) { 
			if (tab != notThisOne)
				tab.setClosed();
		}
	}
	
	public void closeAllTabs() {
		setSelected();
	}
	
	public void setSelected(GlassDropPaneTab which) {
		int index = tabs.indexOf(which);

		for(int i=0; i<tabs.size(); i++)  {
			GlassDropPaneTab tab = tabs.get(i);
			if (i!=index) {
				tab.setClosed();
			}
		}
		
		GlassDropPaneTab tab = tabs.get(index);
		tab.setOpen();

		parent.setGlassPane(tab.getPanel());
		parent.getGlassPane().setVisible(true);
		
		open = true;

		this.getParent().repaint();
	}
	
	
	//Closes all open tabs
	public void setSelected() {
		for(int i=0; i<tabs.size(); i++) {
			GlassDropPaneTab tab = tabs.get(i);
			if (tab.isOpen()) {
				tab.setClosed();
			}
		}
		open=false;
		parent.getGlassPane().setVisible(false);
		this.getParent().repaint();
	}
	
	public boolean isOpen() {
		return open;
	}


	/**
	 * We need to know if we're in a component that can be scrolled so painting on the glasspane can be updated
	 * @param scrollPane
	 */
	public void setParentForClip(JScrollPane scrollPane) {
		parentScrollPane = scrollPane;
	}
	
	
}
