package fileTree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import topLevelGUI.SunFishFrame;
import display.DisplayData;
import errorHandling.ErrorWindow;


/**
 * A top level tree block that represents a directory of files. 
 * @author brendan
 *
 */
public class DirectoryTreeBlock extends TopLevelTreeBlock {

	File rootDirectory;
	int maxRecursionDepth = 12;
	
	FileTreeModelListener treeModelListener;
	FileTreeExpansionListener expansionListener;
	DefaultTreeModel treeModel; 
	
	boolean hideUnrecognizedFiles = false;
	
	public DirectoryTreeBlock(FileTreePanel ftPanel, File rootDirectory) {
		super(ftPanel);
		this.rootDirectory = rootDirectory;
		
		topLevelIcon = FileTreePanel.getIcon("icons/folder2.png");
		
		initializeTree();
		constructPopup();
		
		tree.collapseRow(0);
	}
	
	
	/**
	 * The actual popup is in the superclass TopLevelTreeBlock - here we just add a few items. 
	 */
	private void constructPopup() {

		JMenuItem popupRescan = new JMenuItem("Rescan directory");
		popupRescan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupRescanAction(evt);
            }
        });
		popup.add(popupRescan);
		
		JCheckBoxMenuItem popupHide = new JCheckBoxMenuItem("Hide unrecognized files");
		popupHide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupHideAction(evt);
            }
        });
		popup.add(popupHide);

		
		JMenuItem popupItemDisplay = new JMenuItem("Display");
		popupItemDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupItemDisplayAction(evt);
            }
        });
		popup.add(popupItemDisplay);
		
        JMenuItem popupItemDisplayText = new JMenuItem("Display as text");
		popupItemDisplayText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupItemDisplayText(evt);
            }
        });
		popup.add(popupItemDisplayText);
		
        JMenuItem popupItemRename = new JMenuItem("Rename file");
		popupItemRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupItemRenameAction(evt);
            }
        });
		popup.add(popupItemRename);
		
        JMenuItem popupItemDelete = new JMenuItem("Delete file");
		popupItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupItemDeleteAction(evt);
            }
        });
		popup.add(popupItemDelete);
		
	}
	
	
	protected void popupHideAction(ActionEvent evt) {
		hideUnrecognizedFiles = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
		popupRescanAction(evt);
	}


	/**
	 * Rescan the directory and reload all files. This method is also called by a few
	 * other action listeners to account for other changes (such as showing only parseable
	 * files) 
	 * @param evt ignored.
	 */
	protected void popupRescanAction(ActionEvent evt) {
		rootNode.removeAllChildren();
		addChildNodes(rootDirectory, rootNode);
		treeModel.reload(rootNode);
		TreeCellRenderer cellRenderer = new FileTreeCellRenderer(this, parentPanel.getParentFrame()); 
		tree.setCellRenderer(cellRenderer);
		resizeBlock();
		repaint();
	}


	public File getTreeRoot() {
		return rootDirectory;
	}
	
	/**
	 * Creates the jTree fileTree, which displays all files descending from rootFilePath in a jTree
	 * 
	 * @param rootFile root of the file tree
	 * @param filter If non-empty, display only files matching filter
	 */
    private void initializeTree() {
		tree.setBorder(BorderFactory.createEmptyBorder());
		
		TreeCellRenderer cellRenderer = new FileTreeCellRenderer(this, parentPanel.getParentFrame()); 
		tree.setCellRenderer(cellRenderer);
		
    	if (rootDirectory==null) {
    		System.err.println("Cannot initialize file tree because treeRoot is null.");
    		SunFishFrame.getSunFishFrame().getLogger().severe("initializeTree encountered a null tree root. Aborted tree initialization.");
    		return;
    	}
    	else {
    		if (! rootDirectory.exists()) {
    			System.err.println("Cannot initialize file tree because treeRoot does not point to an existing file.");
    			SunFishFrame.getSunFishFrame().getLogger().severe("initializeTree encountered a tree root that pointed to an non-existing file. Aborted tree initialization.");
    			return;
    		}
    	}
    	
    	FileTreeExpansionListener expansionListener = new FileTreeExpansionListener(this, rootDirectory.getPath());
		tree.addTreeWillExpandListener( expansionListener );
		
		treeModel = new DefaultTreeModel(rootNode);
		tree.setModel(treeModel);
		
        rootNode.setUserObject(new TreeFile(rootDirectory));
		boolean anyMatches = addChildNodes(rootDirectory, rootNode, currentFilter, true);
		
		if (anyMatches == false) {
			rootNode.setUserObject("(No matching files)");
		}

		treeModelListener =  new FileTreeModelListener(parentPanel.getParentFrame(), tree);
		treeModel.addTreeModelListener( treeModelListener );
    }

    
	/**
	 * Returns selected file from file tree, if there is one
	 * @return Selected file
	 */
	public File getSelectedFile() {
		TreePath tp = tree.getSelectionPath();
		if (tp ==null)
			return null;
		TreeBlockNode treeNode =  (TreeBlockNode)(tp.getLastPathComponent());
		if (treeNode == null)
			return null;
        File file = ((TreeFile)treeNode.getUserObject()).getFile();
        return file;
	}
	

	public void handleFilterChanged() {
		rootNode.removeAllChildren();
		addChildNodes(rootDirectory, rootNode, currentFilter, true);
		treeModel.reload();
		revalidate();
		repaint();
	}
	
	
	private void addChildNodes(File file, TreeBlockNode node) {
		addChildNodes(file, node, currentFilter, true);
	}
	

	public boolean addChildNodes(File root, TreeBlockNode node, String filter, boolean recurse) {
		boolean force = true;
		if (filter.equals("")) {
			force = false;
		}
		return addChildNodes(root, node, filter, recurse, force, 0);
	}

	
	private TreeBlockNode getNodeForFilename(List<TreeBlockNode> list, String filePath) {
		for(TreeBlockNode node : list) {
			if (node.getTreeFile()!=null && node.getTreeFile().getFile().getAbsolutePath().equals(filePath))
				return node;
		}
		return null;
	}
	
    /**
     * Recursive function that adds new nodes to fileTree from the file system
     * 
     * @param file File whose descendents should be added to file tree
     * @param node Tree node to which descendents should be added
     * @param filter Regex which restricts added nodes to only those matching filter
     * @param recurse Load child nodes from directories within the root directory as well.
     * @param forceRecurse Load child nodes from all subdirectories
     */
	private boolean addChildNodes(File root, TreeBlockNode node, String filter, boolean recurse, boolean forceRecurse, int depth) {
    	boolean hasMatches = false;
    	if (depth > maxRecursionDepth) {
    		return hasMatches;
    	}
    	
  
    	System.out.println("Adding child nodes from file : " + root.getName());
    	
        File[] files = root.listFiles();
        
        //We first obtain a list of the current children of this node. We don't want to replace these
        //if we don't need to
        List<TreeBlockNode> currentChildren = new ArrayList<TreeBlockNode>(20);
        for(int i=0; i<node.getChildCount(); i++) {
        	currentChildren.add( (TreeBlockNode)node.getChildAt(i));
        }
        
        //Now we remove all children from the node, and re-add them only if they match the current
        //filter or 'hideUnrecognized' state
        node.removeAllChildren();
        
        //The list of nodes to add..
		ArrayList<TreeBlockNode> nodes = new ArrayList<TreeBlockNode>();
        
		if (files==null)
            return false;

    	if (filter.length()==0)
    		hasMatches = true;
    	
        for(int j = 0; j < files.length; j++) {
        	if (! files[j].isHidden()) {
        		String fileName = files[j].getName();
        		TreeBlockNode newNode = getNodeForFilename(currentChildren, files[j].getAbsolutePath());
        		if (newNode == null)
        			newNode = new TreeBlockNode(new TreeFile(files[j]));
        		if (filter.equals("") || fileName.contains(filter)) {
        			if ((!hideUnrecognizedFiles) || (hideUnrecognizedFiles && parentPanel.getParentFrame().getParserList(files[j]).size()>0))
        			nodes.add(newNode);
        			hasMatches = true;
        		}
        		
        		if (files[j].isDirectory() && recurse) {
        			boolean containsMatches = addChildNodes(files[j], newNode, filter, false || forceRecurse, forceRecurse, depth+1);
        			if (containsMatches)
        				nodes.add(newNode);
        			hasMatches = hasMatches || containsMatches;
        		}
        	}
        }


		for(int i=0; i<nodes.size(); i++) {
			for(int j=i+1; j<nodes.size(); j++) {
				if (nodes.get(i).getUserObject().toString().compareTo( nodes.get(j).getUserObject().toString() ) > 0) {
					TreeBlockNode tmp = nodes.get(i);
					nodes.set(i, nodes.get(j));
					nodes.set(j, tmp);
				}
			}
		}

		int[] indices = new int[nodes.size()];
		for(int i=0; i<nodes.size(); i++) {
			//System.out.println("Adding node " + i + " of " + nodes.size() + " which is: " + nodes.get(i));
			node.add(nodes.get(i));
			indices[i] = i;
		}
		
		treeModel.reload(node);
		return hasMatches;
    }

	
	//// PopupMenu Events //////////////////
	
	private void popupItemDisplayAction(java.awt.event.ActionEvent evt ) {
		TreePath tp = tree.getSelectionPath();
		if (tp ==null)
			return;
		TreeBlockNode treeNode =  (TreeBlockNode)(tp.getLastPathComponent());
		if (treeNode == null)
			return;
        File file = ((TreeFile)treeNode.getUserObject()).getFile();
		if (file.isDirectory()) {
			return;
		}
		else {
			parentPanel.getParentFrame().displayFile(file);
		}		
	}
	
	private void popupItemDisplayText(java.awt.event.ActionEvent evt ) {
		TreePath tp = tree.getSelectionPath();
		if (tp ==null)
			return;
		TreeBlockNode treeNode =  (TreeBlockNode)(tp.getLastPathComponent());
		if (treeNode == null)
			return;
        File file = ((TreeFile)treeNode.getUserObject()).getFile();
		if (file.isDirectory()) {
			return;
		}
		else {
			//TextDisplay textDisplay = new TextDisplay(parentPanel.getParentFrame());
			//textDisplay.construct();
			//parentPanel.getParentFrame().display(new DisplayData(file, file), textDisplay, file.getName());
			ErrorWindow.showErrorWindow(new Exception("No text display implemented yet"));
		}		
	}
	
	private void popupItemRenameAction(java.awt.event.ActionEvent evt ) {
		TreePath tp = tree.getSelectionPath();
		if (tp ==null)
			return;
		TreeBlockNode treeNode =  (TreeBlockNode)(tp.getLastPathComponent());
		if (treeNode == null)
			return;
        File file = ((TreeFile)treeNode.getUserObject()).getFile();
        treeModelListener.setCurrentNode(treeNode);
        
        
		//Make tree cell editable to get new name
        tree.setEditable(true);

        tree.startEditingAtPath(tp);
	}
	
	private void popupItemDeleteAction(java.awt.event.ActionEvent evt ) {
		TreePath tp = tree.getSelectionPath();
		if (tp ==null)
			return;
		TreeBlockNode treeNode =  (TreeBlockNode)(tp.getLastPathComponent());
		String name = treeNode.toString().substring( treeNode.toString().lastIndexOf("/")+1);
		if (treeNode == null) {
			System.err.println("Treenode is null, returning without doing nothing");
			return;
		}
        File file = ((TreeFile)treeNode.getUserObject()).getFile();
		//Prompt before deleting
		Object[] options = {"Delete",
                "Cancel"};
		int n = JOptionPane.showOptionDialog(this,
			"Permanently delete " + name + "?", "Delete file",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);

		if (n==0) {
			try {
				file.delete();
				parentPanel.getParentFrame().getLogger().info("Deleted file : " + file.getAbsolutePath());
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(this,
					    "Could not delete file " + name + "\n You may not have the permissions to delete the file, or the file display could be out of sync with the actual file system. Reloading the tree may help.",
					    "File Deletion Failed",
					    JOptionPane.WARNING_MESSAGE);

				parentPanel.getParentFrame().getLogger().warning("Caught exception after attempting to delete file : " + file.getAbsolutePath() + "\n Exception msg : " + e.toString());
			}
			
			try  {
				initializeTree();
			}
			catch (Exception e) {
				parentPanel.getParentFrame().getLogger().warning("Could not reload file tree after deleting file : " + file.getAbsolutePath());
			}
		}
		
	}
	

	private void popupItemUseAsRoot(java.awt.event.ActionEvent evt ) {
		TreePath tp = tree.getSelectionPath();
		if (tp ==null)
			return;
		TreeBlockNode treeNode =  (TreeBlockNode)(tp.getLastPathComponent());
		if (treeNode == null)
			return;
        File file = ((TreeFile)treeNode.getUserObject()).getFile();
		if (file.isDirectory()) {
			rootDirectory = file;
			initializeTree();
		}
		else {
			String path = file.getAbsolutePath();
			int last = path.lastIndexOf("/");
			String dir = path.substring(0, last);
			try {
				File newDir = new File(dir);
				rootDirectory = newDir;
				initializeTree();
			}
			catch (Exception e) {
				System.err.println("Could change root, for some reason : " + e.toString());
				System.err.println("got path : " + path + "\n last index : " + last + " dir: " + dir);
				parentPanel.getParentFrame().getLogger().warning("Could not change tree root, message : " + e.toString());
			}
		}		
	}
	

}
