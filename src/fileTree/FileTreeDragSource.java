package fileTree;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


class FileTreeDragSource implements DragSourceListener, DragGestureListener {

	  DragSource source;

	  DragGestureRecognizer recognizer;

	  TransferableTreeNode transferable;

	  DefaultMutableTreeNode oldNode;

	  JTree sourceTree;

	  public FileTreeDragSource(JTree tree, int actions) {
	    sourceTree = tree;
	    source = new DragSource();
	    recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
	        actions, this);
	  }

	  /*
	   * Drag Gesture Handler
	   */
	  public void dragGestureRecognized(DragGestureEvent dge) {
	    TreePath path = sourceTree.getSelectionPath();
	    if ((path == null) || (path.getPathCount() <= 1)) {
	      // We can't move the root node or an empty selection
	      return;
	    }
	    oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();
	    transferable = new TransferableTreeNode(path);
	    source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);

	    // If you support dropping the node anywhere, you should probably
	    // start with a valid move cursor:
	    //source.startDrag(dge, DragSource.DefaultMoveDrop, transferable,
	    // this);
	  }

	  /*
	   * Drag Event Handlers
	   */
	  public void dragEnter(DragSourceDragEvent dsde) {
	  }

	  public void dragExit(DragSourceEvent dse) {
	  }

	  public void dragOver(DragSourceDragEvent dsde) {
	  }

	  public void dropActionChanged(DragSourceDragEvent dsde) {
	    System.out.println("Action: " + dsde.getDropAction());
	    System.out.println("Target Action: " + dsde.getTargetActions());
	    System.out.println("User Action: " + dsde.getUserAction());
	  }

	  public void dragDropEnd(DragSourceDropEvent dsde) {
	    /*
	     * to support move or copy, we have to check which occurred:
	     */
	    System.out.println("Drop Action: " + dsde.getDropAction());
	    if (dsde.getDropSuccess()
	        && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
	      ((DefaultTreeModel) sourceTree.getModel())
	          .removeNodeFromParent(oldNode);
	    }

	    /*
	     * to support move only... if (dsde.getDropSuccess()) {
	     * ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode); }
	     */
	  }
	}
