package displayPane;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.TransferHandler;

import topLevelGUI.SunFishFrame;

import display.Display;


import errorHandling.ErrorWindow;
import fileTree.TreeFile;


public class DisplayPaneTransferHandler extends TransferHandler {
	
	SunFishFrame parent;
	
	public DisplayPaneTransferHandler(SunFishFrame parent) {
		this.parent = parent;
	}
	
	public boolean canImport(TransferHandler.TransferSupport support) {
		//System.out.println("Display pane transfer handler is getting asked about can import..");
		return support.isDataFlavorSupported(DataFlavor.stringFlavor) || support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	    
	}
	
	
	public boolean importData(TransferSupport support) {
	    if (!canImport(support)) {
	        return false;
	    }

	    // Fetch the Transferable and its data
	    Transferable t = support.getTransferable();
	    
	    String str = "";
		try {
			str = (String)t.getTransferData(DataFlavor.stringFlavor);
			File file = new File(str);
			
		
			Display currentDisplay = parent.getDisplayPane().getCurrentDisplay();
			boolean dropHandledByDisplay = false;
			if (currentDisplay != null) {
				if (currentDisplay.acceptDrop(file)) {
					currentDisplay.fileDropped(file);
					dropHandledByDisplay = true;
				}
			}
			
			//If the drop was not handled by the current display, then we just attempt to
			//open the file in a new display
			if (! dropHandledByDisplay) {
				parent.displayFile(file);
			}
		} catch (UnsupportedFlavorException e) {
			ErrorWindow.showErrorWindow(e, parent.getLogger());
		} catch (IOException e) {
			ErrorWindow.showErrorWindow(e, parent.getLogger());
		}	
		
	    return true;
	}
}

