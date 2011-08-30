/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fileTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import topLevelGUI.SunFishFrame;



/**
 *
 * @author brendan
 */
public class TreeTransferHandler extends TransferHandler {
	
	SunFishFrame parent;
	
	public int getSourceActions(JComponent comp) {
	    return COPY_OR_MOVE;
	}

	protected Transferable createTransferable(JComponent jc) {
		JTree tree = (JTree)jc;
		TreePath path = tree.getSelectionPath();
		
		List<File> fList = new ArrayList<File>(2);
		if (path != null) {
			DefaultMutableTreeNode treeNode =  (DefaultMutableTreeNode)(path.getLastPathComponent());
			TreeFile tf = (TreeFile)treeNode.getUserObject();
			//pathString = tf.getFile().getAbsolutePath();
			File file = tf.getFile();
			fList.add(file);
			//System.out.println("Added file " + file.getName() + " to list");
		}
		
		Transferable ft = new FileSelection(fList);
		//System.out.println("Making new file selection transferable");
		return ft;		
	}

	/**
	 * The actual object that gets transferred from the file tree. It contains a list of files, which
	 * is returned when getTransferData(DataFlaver.javaFileListFlavor) is called. 
	 * If getTransferData(DataFlavor.stringFlavor) is called, the first entry in the file list is returned. 
	 * @author brendan
	 *
	 */
	class FileSelection implements Transferable {

		List<File> list = null;
		
		public FileSelection(List<File> fList) {
			this.list = fList;
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.isFlavorJavaFileListType() || flavor.isFlavorTextType();
		}

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			
			
			if (flavor.isFlavorJavaFileListType())
				return list;
			if (flavor.isFlavorTextType()) {
				return list.get(0).getAbsolutePath();
			}
		 
			return null;
		}
		
	}
 

}
