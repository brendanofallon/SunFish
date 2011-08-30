package topLevelGUI.analyzer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import topLevelGUI.SunFishFrame;

import errorHandling.ErrorWindow;

public class AnalysisPaneTransferHandler  extends TransferHandler {
	SunFishFrame parent;
	
	public AnalysisPaneTransferHandler(SunFishFrame parent) {
		this.parent = parent;
	}
	
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
	        return false;
	    }
	    
		return true;
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
			
			Analyzable analyzer = parent.getCurrentAnalyzer(); 
			if (analyzer != null)
				analyzer.fileDropped(file);
			
		} catch (UnsupportedFlavorException e) {
			ErrorWindow.showErrorWindow(e, parent.getLogger());
		} catch (IOException e) {
			ErrorWindow.showErrorWindow(e, parent.getLogger());
		}	
		
	    return true;
	}
}
