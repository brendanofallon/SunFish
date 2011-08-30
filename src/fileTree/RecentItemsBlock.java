package fileTree;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


/**
 * A type of top level block that remembers recent files listed. It's not heirarchical, it just
 * maintains a list of recent files and presents them. 
 * 
 * @author brendan
 *
 */
public class RecentItemsBlock extends TopLevelTreeBlock {

	List<TreeBlockNode> recentItems;
	DefaultTreeModel model; 
	int capacity = 6; //The number of items to remember
	
	public RecentItemsBlock(FileTreePanel ftPanel) {
		super(ftPanel);

		topLevelIcon = FileTreePanel.getIcon("icons/clock.png");
		
		recentItems = new ArrayList<TreeBlockNode>(10);
		rootNode.setUserObject("Recent Items");
		
		tree.setCellRenderer(new FileTreeCellRenderer(this, parentPanel.getParentFrame()));
		model = (DefaultTreeModel)tree.getModel();
	}
	
	/**
	 * Adds a new file to the list, if the list does not already contain an item
	 * with the same absolute path. If the new size of the list is > capacity, the
	 * item at the bottom of the list (the oldest item) is removed. 
	 * @param file
	 * @throws  
	 */
	public void addItem(File file) {
		TreeBlockNode node = new TreeBlockNode(new TreeFile(file));
		boolean alreadyInThere = false;
		TreeBlockNode nodeToMove = null;

		//If it's already in the list, move it to the top position (.. but we cant do this in the loop.. so mark it and move it afterward)
		try {
			
			for(TreeBlockNode n : recentItems) {
				File otherFile = ((TreeFile)n.getUserObject()).getFile();
				if (file.getCanonicalPath().equals(otherFile.getCanonicalPath())) {
					nodeToMove = n;
					alreadyInThere = true;
				}
			}

		}
		catch (IOException ioe) {
			
		}
		
		//If not already in the list, add it to the top
		if (! alreadyInThere) {
			recentItems.add(0, node);
			//System.out.println("Added one node to recent items, list size is now : " + recentItems.size());
		}
		else if (nodeToMove!=null) { 
			recentItems.remove(nodeToMove);
			recentItems.add(0, nodeToMove);
			//System.out.println("Moved node to top, list size is now : " + recentItems.size());
		}
		
		
		//If the list is bigger than capacity, remove the last element 
		if (recentItems.size()>capacity) {
			DefaultMutableTreeNode lastElement = recentItems.remove(recentItems.size()-1);
			rootNode.remove(lastElement);
			//System.out.println("Size is bigger than capacity, removing last element: " + lastElement);
		}
		
		reAddNodes();
	}
	
	/**
	 * Since reAddNodes pays attention to the filter, we just call that function. 
	 */
	public void handleFilterChanged() {
		//We don't care about filtering events
	}
	
	
	/**
	 * So we can write the list of recent items to the properties store
	 * @return
	 */
	public List<File> getRecentFiles() {
		ArrayList<File> files = new ArrayList<File>();
		int count = 0;
		//Read in reverse order so when items are read back in they appear in correct order
		for(int i=recentItems.size()-1; i>=0; i--) {
			DefaultMutableTreeNode node = recentItems.get(i);
			File file = ((TreeFile)(node.getUserObject())).getFile();
			files.add(file);
			count++;
		}
		
		
		return files;
	}
	
	/**
	 * Clears and then re-adds children to the root node in the order they appear
	 * in recent items, this makes sure the order of recent items is preserved, so 
	 * the most recently used files appear near the top. 
	 */
	private void reAddNodes() {
		rootNode.removeAllChildren();
		tree.revalidate();
		Set<String> filesInside = new HashSet<String>();
		
		for(DefaultMutableTreeNode node : recentItems) {
			File file = ((TreeFile)node.getUserObject()).getFile();
			String path;
			try {
				path = file.getCanonicalPath();
				if (!filesInside.contains(path)) {
					if (currentFilter.equals("") || file.getName().contains(currentFilter)) 
						model.insertNodeInto(node, rootNode, rootNode.getChildCount());
					filesInside.add(path);
					//System.out.println("Adding file: " + path + " to recent items list");
				}
			} catch (IOException e) {
				//File doesn't exist, I guess, so don't add it
			}
			
		}
		((DefaultTreeModel) tree.getModel()).reload(rootNode);
		//System.out.println("Re-added nodes, root has " + rootNode.getChildCount() + " children, tree has " + tree.getRowCount() + " total rows");
		repaint();
	}

	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int cap) {
		capacity = cap;
	}


}
