package plugins.treePlugin.treeDisplay;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import plugins.treePlugin.tree.DrawableTree;


/**
 * A class that wraps a list of trees into a transferable object, suitable for transferring
 * via cut, copy, and paste operations. 
 * @author brendan
 *
 */
public class TransferableTrees implements Transferable {

	List<DrawableTree> trees;
	public static final DataFlavor drawableTreeFlavor = new DataFlavor(DrawableTree.class, "Tree");
	
	public static final DataFlavor[] flavors = {new DataFlavor(DrawableTree.class, "Tree"),
												DataFlavor.stringFlavor};
	
	public TransferableTrees(List<DrawableTree> trees) {
		this.trees = trees;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	/**
	 * Returns true if the flavor's representation class is DrawableTree, or the flavor is a 
	 * text type
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.getRepresentationClass()==DrawableTree.class || flavor.isFlavorTextType();
	}

	@Override
	/**
	 * Obtain the tree list or a series of newick strings representing the trees, depending on
	 * the data flavor given. 
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		
		if (flavor.getRepresentationClass()==DrawableTree.class) {
			return trees;
		}
		
		if (flavor.isFlavorTextType()) {
			StringBuilder strb = new StringBuilder();
			for(DrawableTree tree : trees) {
				strb.append( tree.getNewick() + "\n");
			}
			return strb.toString();
		}
		return null;
	}

}
