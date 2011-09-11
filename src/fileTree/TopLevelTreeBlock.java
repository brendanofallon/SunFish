package fileTree;

import guiWidgets.InsetShadowPanel;
import guiWidgets.RoundedBezelPanel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import topLevelGUI.SunFishFrame;



/**
 * A panel with a jtree in it, representing a group of files that the user can display. We resize the panel every time
 * the tree changes.  Subclasses of this display are associated with directories, recent files, favorites, etc. 
 * @author brendan
 *
 */
public abstract class TopLevelTreeBlock extends TLBPanel {

	final TreeBlockNode rootNode;
	JTree tree;
	
	String currentFilter = ""; //The empty filter specifies that we show everything
	
	FileTreePanel parentPanel;
	JPanel treePanel;
		
	//A few common items for the popup menu, all subclasses should be sure to add these to
	//their popup
	JMenuItem removeBlockItem;
	
	Point mousePos;
	int mouseOverRow = -1; //If the mouse is over a row, this is the row #
	
	TreeListener treeChangeListener; //Listens for expansion / collapse events
	
	int additionalHeightPadding = 14; //Preferred block height will be this + tree preferred height
	
	ImageIcon topLevelIcon = null; //The icon that appears at left of the name of this block
	
	public TopLevelTreeBlock(FileTreePanel ftPanel) {
		this.parentPanel = ftPanel;
		rootNode = new TreeBlockNode();
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		tree = new JTree(rootNode);
		
		treeChangeListener = new TreeListener(this);
		tree.addTreeExpansionListener(treeChangeListener);
		tree.setAlignmentX(LEFT_ALIGNMENT);
				
		JPanel spacePanel = new JPanel();
		spacePanel.setOpaque(false);
		spacePanel.setMinimumSize(new Dimension(10, 4));
		spacePanel.setPreferredSize(new Dimension(10, 4));
		
		this.add(spacePanel, BorderLayout.NORTH);
		
		treePanel = new JPanel();

		treePanel.setOpaque(false);
		treePanel.setLayout(new BorderLayout());
		treePanel.add(tree, BorderLayout.CENTER);
        this.add(treePanel, BorderLayout.CENTER);
		
		
        tree.setTransferHandler( new TreeTransferHandler() );
		tree.setDragEnabled(true);
		
		TreeMouseListener treeListener = new TreeMouseListener();
		tree.addMouseListener(treeListener);
		
        if (! SunFishFrame.getSunFishFrame().onAMac())
        	tree.setUI(new FileTreeUI());
        
		tree.setOpaque(false);
		popup = new JPopupMenu();
		removeBlockItem = new JMenuItem("Remove directory");
		removeBlockItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeMe();
			}
		});
		popup.add(removeBlockItem);
		popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		mousePos = new Point(0, 0);
	}
	
	/**
	 * Obtains the icon to be painted at left of the name of this block
	 */
	public ImageIcon getTopLevelIcon() {
		return topLevelIcon;
	}
	
	/**
	 * Tells the parent panel to move this block.
	 */
	protected void removeMe() {
		parentPanel.removeBlock(this);
	}
	
	public void unselectAll() {
		tree.setSelectionPath(null);
	}

	public TreeBlockNode getRootNode() {
		return rootNode;
	}
	
	public JTree getTree() {
		return tree;
	}
	
	/**
	 * Set the current filter to the new string, then handle the new filter changed event
	 * @param filter
	 */
	public void setCurrentFilter(String filter) {
		currentFilter = filter;
		handleFilterChanged();
	}
	
	
	public String getCurrentFilter() {
		return currentFilter;
	}
	/**
	 * Show only those files that match the given filter
	 */
	public abstract void handleFilterChanged();
	
	/**
	 * Returns the row over which the mouse is hovered, if any
	 * @return
	 */
	public int getMouseOverRow() {
		return mouseOverRow;
	}
	
	public Point getMousePos() {
		return mousePos;
	}
	
	
	/**
	 * Adjust the size of the containing panel to fit the tree using the provided width.
	 */
	protected void resizeBlock(int newWidth) {

		int newHeight = tree.getPreferredSize().height+additionalHeightPadding; 
		//System.out.println("Resizing block to height : " + newHeight);
		Dimension size = new Dimension(newWidth,  newHeight);	

		this.setMinimumSize(size);
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		revalidate();
		parentPanel.recalculateBlockHeights();
	}

	
	/**
	 * Adjust the size of the containing panel to fit the tree, using our current width as the width. This is called
	 * every time a node in the tree is expanded or collapsed. 
	 */
	protected void resizeBlock() {
		resizeBlock(this.getWidth());
		repaint();	
	}
	
	public void reloadParserInfo() {
		reloadParserInfo(rootNode);
	}
	
	/**
	 * Recursively traverse all children from the given node and call loadFileAttributes on them.
	 * @param treeNode
	 */
	private void reloadParserInfo(TreeNode treeNode) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeNode;
		Object userObj = node.getUserObject();
		if (userObj != null && userObj instanceof TreeFile) {
			TreeFile treeFile = (TreeFile)userObj;
			treeFile.loadFileAttributes();
		}
		
		for(int i=0; i<node.getChildCount(); i++) {
			reloadParserInfo( node.getChildAt(i));
		}
		
	}
	
	/**
	 * Cause the tree panel to be highlighted, or not
	 * @param highlight
	 */
//	public void setHighlight(boolean highlight) { }
	
	public void treeMouseClicked(java.awt.event.MouseEvent e) {
        int selRow = tree.getRowForLocation(this.getWidth()/2, e.getY());
        TreePath selPath = tree.getPathForRow(selRow);

//        System.out.println("\n\nMouse clicked on row : " + selRow);
//        if (selPath == null) {
//        	System.out.println(".. but there is no selected path.");
//        }
//        else {
//        	System.out.println(" * And there is a selected path");
//        }
        
        //New strategy: Never tell the tree to expand or collapse. Just listen for events and attempt
        //to calculate how big the tree is
        if(selRow != -1  ) {
        	if (selPath == null)
        		return;
        	
        	Object node = selPath.getLastPathComponent();
    		if (node == null)
    			return;
    		
        	TreeBlockNode treeNode =  (TreeBlockNode)node;
	
        	
    		//Display file only if the node contains a TreeFile that is not a directory, and 
    		//click count is 2
    		if (treeNode.getUserObject() instanceof TreeFile) {
    			File file = ((TreeFile)treeNode.getUserObject()).getFile();
    			if (! file.isDirectory()) {
    				if (e.getClickCount() == 2) {
    					parentPanel.getParentFrame().displayFile(file);
    				}
    			}
    		}
        }
		 
	}
	
	class TreeListener implements ComponentListener, TreeExpansionListener, TreeWillExpandListener {
		
		TopLevelTreeBlock block;
		
		public TreeListener(TopLevelTreeBlock block) {
			this.block = block;
		}
		public void componentHidden(ComponentEvent arg0) {	}

		public void componentMoved(ComponentEvent arg0) {	}

		public void componentResized(ComponentEvent arg0) {	}
		
		public void componentShown(ComponentEvent arg0) { }

		public void treeCollapsed(TreeExpansionEvent arg0) {
			resizeBlock();
		}

		public void treeExpanded(TreeExpansionEvent arg0) {
			resizeBlock();
		}
		
		public void treeWillCollapse(TreeExpansionEvent arg0)
				throws ExpandVetoException {	}
		
		public void treeWillExpand(TreeExpansionEvent arg0)
				throws ExpandVetoException {	}
	}

	

	

	/**
	 * Handles popup menu events
	 * 
	 *
	 */
	protected class TreeMouseListener extends MouseAdapter {
		
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			try {
				treeMouseClicked(evt);
			}
			catch (Exception ex) {
				System.out.println("Caught exception in treeMouseClick : " + ex);
				
			}
		}
		
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    
	    public void mouseExited(MouseEvent e) {	    }
	    
	    public void mouseMoved(MouseEvent e) {	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
	        	tree.setSelectionPath(selPath);
	        	popup.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}
	
	
	JPopupMenu popup;

	

}
