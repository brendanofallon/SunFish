package fileTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.TreePath;

class TransferableTreeNode implements Transferable {

	  public static DataFlavor treePathFlavor = new DataFlavor(TreePath.class,
	      "Tree Path");

	  DataFlavor flavors[] = { treePathFlavor };

	  TreePath path;

	  public TransferableTreeNode(TreePath tp) {
	    path = tp;
	  }

	  public synchronized DataFlavor[] getTransferDataFlavors() {
	    return flavors;
	  }

	  public boolean isDataFlavorSupported(DataFlavor flavor) {
	    return (flavor.getRepresentationClass() == TreePath.class);
	  }

	  public synchronized Object getTransferData(DataFlavor flavor)
	      throws UnsupportedFlavorException, IOException {
	    if (isDataFlavorSupported(flavor)) {
	      return (Object) path;
	    } else {
	      throw new UnsupportedFlavorException(flavor);
	    }
	  }
	}
