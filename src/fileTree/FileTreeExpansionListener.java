package fileTree;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import topLevelGUI.SunFishFrame;


import java.io.File;
import java.util.ArrayList;


public class FileTreeExpansionListener implements TreeWillExpandListener {

	String pathPrefix = "";
	int maxRecursionDepth = 12; //Don't search for filter matches this many levels below root
	DirectoryTreeBlock dtBlock; //The parent block to which I listen
	
	public FileTreeExpansionListener(DirectoryTreeBlock dtBlock, String path) {
		this.pathPrefix = path;
		this.dtBlock = dtBlock;
	}
	
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {
		// TODO Auto-generated method stub

	}

	
	public void treeWillExpand(TreeExpansionEvent event) {
		TreePath expansionPath = event.getPath();
		
		String pathStr = convertPathToString(expansionPath);
		TreeBlockNode expandingNode = (TreeBlockNode)expansionPath.getLastPathComponent(); 
		
		File nodeFile = ((TreeFile)expandingNode.getUserObject()).getFile();
		
		dtBlock.addChildNodes(nodeFile, expandingNode, dtBlock.getCurrentFilter(), true);
	}

	
	private String convertPathToString(TreePath path) {
		Object[] elements = path.getPath();
		String sepStr = System.getProperty("file.seperator");
		char sep;
		if (sepStr != null)
			sep = sepStr.charAt(0);
		else
			sep = '/';
		StringBuffer buf = new StringBuffer();
		buf.append(pathPrefix);
		buf.append(sep);
		for(int i=1; i<elements.length; i++) {
			buf.append(elements[i]);
			buf.append(sep);
		}
		
		return buf.toString();
	}
}
