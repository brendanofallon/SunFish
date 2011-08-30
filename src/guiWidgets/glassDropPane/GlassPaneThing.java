package guiWidgets.glassDropPane;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class GlassPaneThing extends JPanel {

	Rectangle rect;
	ArrayList< ArrayList<Component> > compRows; //Could someday hold multiple rows of components, right now all components are added to the first row
	int leftPadding = 4;
	int topPadding = 3;
	int bottomPadding = 2;
	int compSpacing = 8;	//Horizontal space between components
	int rowSpacing = 3;
	boolean hide = false;
	GlassDropPane dropPane;
	Point pos;
	boolean redoLayout = true;
	boolean firstCall = true;
	GlassDropPaneTab tab;
	boolean inCFFrame = true;
	JRootPane rootPane;
	boolean refindRoot = true;
	JComponent parent;
	int activeRow = 0;
	Color borderColor;
	
	Font defaultFont = new Font("Sans", Font.PLAIN, 11);
	
	public GlassPaneThing(GlassDropPane dropPane, JComponent parent) {
		setLayout(null);
		setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		this.dropPane = dropPane;
		rect = new Rectangle(0, 0, 680, 30);
		compRows = new ArrayList< ArrayList<Component>>();
		compRows.add( new ArrayList<Component>() );
		setOpaque(false);
		this.parent = parent;
		borderColor = new Color(0.8f, 0.8f, 0.8f, 0.8f);
	}

	public void setInCFFrame(boolean val) {
		inCFFrame = val;
	}
	
	public void setFont(Font newFont) {
		defaultFont = newFont;
	}
	
	public void setTab(GlassDropPaneTab tab) {
		this.tab = tab;
	}
	
	public void addComponent(Component comp) {
		removeComponent(comp); //This has no effect if comp is not already here, but if the comp has already been added, we want to make sure
		//it has been removed before we add it again.
		compRows.get(activeRow).add(comp);
		comp.setFont(defaultFont);
		add(comp);
	}
	
	//At some point it may be worth expanding this to include having multiple rows in components,
	//but that would probably require knowing how to have combobox popups appear in front of the 
	//glasspane, which could be really tricky
//	public void newRow() {
//		activeRow++;
//		compRows.add( new ArrayList<Component>() );
//	}
	
	public void setMinimumWidth(int width) {
		rect.width = width;
	}
	
	/**
	 * Handles the layout of the components in a really kludgy way by using absolute positioning.
	 * Maybe at some point this should put all of the components in a panel with a non-opaque
	 * background?
	 */
	public void layoutComponents() {
		int x = rect.x + leftPadding;
		int totWidth = leftPadding;
		//System.out.println("Initial width : " + rect.width);
		//rect.width = getPreferredSize().width;
		
		for(int row=0; row<compRows.size(); row++) {
			ArrayList<Component> comps = compRows.get(row);
			for(int i=0; i<comps.size(); i++) {
				Component comp = comps.get(i);
				comp.validate();

				comp.setBounds(x, rect.y+(rect.height/2-comp.getPreferredSize().height/2), comp.getPreferredSize().width, comp.getPreferredSize().height);
				x+=comp.getPreferredSize().width+compSpacing;
				totWidth += comp.getPreferredSize().width+compSpacing; 

				if (comp.getPreferredSize().height+bottomPadding+topPadding > rect.height)
					rect.height = comp.getPreferredSize().height+bottomPadding+topPadding;
			}
			totWidth += leftPadding;
			if (totWidth > rect.width) {
				rect.width = totWidth;
			}
		}
		redoLayout = false;

	}
	
	/**
	 * Remove the given component from this Thing
	 * @param comp
	 */
	public void removeComponent(Component comp) {
		for(int row=0; row<compRows.size(); row++) {
			ArrayList<Component> comps = compRows.get(row);
			comps.remove(comp);
			super.remove(comp);
		}
		layoutComponents();
	}
	
	public void setHide(boolean h) {
		hide = h;
	}
	
	public void setWidth(int newWidth) {
		rect.width = newWidth;
	}
	
	public void setHeight(int newHeight) {
		rect.height = newHeight;
	}
	
	public void hideAll() {
		hide = true;
	}
	
	public void paint(Graphics g) {
		//Have things moved?
		Shape origClip = g.getClip();
		
		if (rootPane == null || refindRoot) {
			rootPane = this.getRootPane();
			redoLayout = true;
		}
		
		Point absPoint = SwingUtilities.convertPoint(dropPane, new Point(0,0), rootPane);
		
		if (pos != absPoint) {
			redoLayout = true;
			pos = absPoint;
		}
		if (redoLayout) {
			layoutComponents();
		}
		
		//System.out.println("Point 0,0 in rootPane coords : " + SwingUtilities.convertPoint(dropPane, new Point(0,0), this.getRootPane()));
//		g.setColor(Color.RED);
		rect.x = pos.x;
		rect.y = pos.y+dropPane.getHeight();
		//System.out.println("rect height : " + rect.height + " width : " + rect.width);
//		Graphics2D g2d = (Graphics2D)g;
//		g2d.draw(rect);
		
		if (firstCall) {
			for(ArrayList<Component> comps : compRows)
				for(Component comp : comps)
					comp.setVisible(false);
			firstCall = false;
		}
		else {
			for(ArrayList<Component> comps : compRows)
				for(Component comp : comps)
					comp.setVisible(true);
		}
		
		Rectangle thisClip = g.getClipBounds();
		if (dropPane.getScrollPane()!=null) {
			if (thisClip.x < rect.x+dropPane.getScrollPane().getViewport().getViewPosition().x)
				thisClip.x = rect.x+dropPane.getScrollPane().getViewport().getViewPosition().x;
			
			//System.out.println("Clip width : " + thisClip.width + " viewport width : " + dropPane.getScrollPane().getViewport().getWidth());
			if (thisClip.width >= dropPane.getScrollPane().getViewport().getWidth()) {
				thisClip.width = dropPane.getScrollPane().getViewport().getWidth();
			}

			//g.setClip(thisClip);
		//	System.out.println("Setting clip x : " + thisClip.x + " width : " + thisClip.width);
		}
		
		if (! hide) {
			g.setClip(origClip);
			super.paint(g);
		}


	}
	
	public void paintComponent(Graphics g) {
		if (! hide) {
			Graphics2D g2d = (Graphics2D)g;
			
			if (rect.width > dropPane.getWidth()) {
				//g2d.setColor(Color.red);
				//g2d.drawLine(rect.x+dropPane.getWidth(), rect.y-dropPane.getHeight(), rect.x+rect.width, rect.y);
				GlassDropBackground.drawBackground(g2d, rect.x+dropPane.getWidth()-1, rect.y-dropPane.getHeight(), rect.width-dropPane.getWidth(), dropPane.getHeight());
				g2d.setColor(borderColor);	
				g2d.drawLine(rect.x+rect.width-1, rect.y-dropPane.getHeight(), rect.x+rect.width-1, rect.y+rect.height-1);
			}
			
			for(int i=0; i<rect.height; i++) {
				float val = 0.9f+0.1f*((float)i/(float)rect.height);
				g2d.setColor(new Color(val, val, val, 0.9f));
				g2d.drawLine(rect.x, rect.y+i, rect.x+rect.width-1, rect.y+i);
			}

			g2d.setColor(borderColor);
			g2d.drawLine(rect.x+rect.width-1, rect.y, rect.x+rect.width-1, rect.y+rect.height-1);
			g2d.drawLine(rect.x, rect.y, rect.x, rect.y+rect.height-1);
			g2d.drawLine(rect.x, rect.y+rect.height-1, rect.x+rect.width-1, rect.y+rect.height-1);

			if (rootPane == null || refindRoot)
				rootPane = this.getRootPane();
			
			super.paintComponent(g);
		}
	}
}
