package fileTree;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

import topLevelGUI.SunFishFrame;



import java.io.*;
import java.util.logging.Logger;



/**
 * Listens and responds to various file-tree-changing events
 * 
 * @author brendan
 *
 */
public class FileTreeModelListener implements TreeModelListener {
	
	JTree tree;
	DefaultMutableTreeNode currentNode = null;
	File currentFile = null;

	SunFishFrame sunfishParent;
	
	public FileTreeModelListener(SunFishFrame parent, JTree tree) {
		super();
		this.tree = tree;
		sunfishParent = parent;
	}
	
	public void setCurrentNode(DefaultMutableTreeNode node) {
		currentNode = node;
		if (node.getUserObject() instanceof TreeFile) {
			currentFile = ((TreeFile)node.getUserObject()).getFile();
		}
		else {
			System.out.println("Old node did not have a treefile. dang");
		}
	}
	
    public void treeNodesChanged(TreeModelEvent e) {
    	boolean success = false;
    	String newFullName = "(name not set yet)";
    	Exception ex;
    	if (currentFile != null) {
    		System.out.println("Current edited file : " + currentFile.getName() );
    		System.out.println("Old full path : " + currentFile.getAbsolutePath());
    		String directory = currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().lastIndexOf("/")+1);
    		System.out.println("Old directory : " + directory);
    		DefaultMutableTreeNode newNode = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
    		Object userObj = newNode.getUserObject();
    		String newFileName = userObj.toString();
    		
    		try {
    			newFullName = directory + newFileName;
    			System.out.println("New full name : " + newFullName);
    			File newFile = new File(newFullName);
    			success = currentFile.renameTo(newFile);
    			newNode.setUserObject(new TreeFile(newFile));
    		} catch (Exception exc) {
    			ex = exc;
    			sunfishParent.getLogger().warning("Caught exception while renaming file : " + currentFile.getName() + "\n Exception : " + exc.toString() );     	
    			success = false;
    		}
    	}
        
        if (! success) {
        	sunfishParent.getLogger().warning("Error renaming file " + currentFile.getName() + " to " + newFullName);
        	JOptionPane.showMessageDialog(sunfishParent,
        		    "Error renaming file '" + currentFile.getName() + "'",
        		    "Warning",
        		    JOptionPane.WARNING_MESSAGE);

        }

        tree.setEditable(false);
    }
    
    public void treeNodesInserted(TreeModelEvent e) {
    	
    }
    
    public void treeNodesRemoved(TreeModelEvent e) {
    	
    }
    
    public void treeStructureChanged(TreeModelEvent e) {
    	
    }


}
