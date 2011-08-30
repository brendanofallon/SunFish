package fileTree;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeBlockNode extends DefaultMutableTreeNode {
	
	//Whether or not this node is expanded...
	//An 'expanded' node is one whose children are showing, if it has children and if all parent
	//states are also 'expanded'. If this node is expanded but a parent is not, then the children
	//should not show. It's not illegal to set expanded to true if there are zero children.
	//This is designed to replace the functionality if tree.isExpanded(tree path...) which does
	//not always seem to work as expected. 
	boolean expanded = false;
	
	public TreeBlockNode() { }
	
	public TreeBlockNode(TreeFile treeFile) {
		setUserObject(treeFile);
	}
	
	public void setExpanded(boolean ex) {
		System.out.println("Setting expansion of node " + this + " to " + ex);
		expanded = ex;
	}
	
	public boolean isExpanded() {
		return expanded;
	}

	
	public TreeFile getTreeFile() {
		if (getUserObject()==null) {
			return null;
		}
		return (TreeFile)getUserObject();
	}
	
}
