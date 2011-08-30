package fileTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransferredFiles implements Transferable {

	List<File> data;
	
	public TransferredFiles(List<File> files) {
		this.data = files;
	}
	
	public TransferredFiles(File file) {
		data = new ArrayList<File>();
		data.add(file);
	}
	
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		
		if (! (flavor == DataFlavor.javaFileListFlavor)) {
			System.err.println("Flavor is not an instance of javaFileListFlavor");
			throw new UnsupportedFlavorException(flavor);

		}
		return data;
	}


	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[1];
		flavors[0] = DataFlavor.javaFileListFlavor;
		return null;
	}


	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor == DataFlavor.javaFileListFlavor)
			return true;
		else
			return false;
	}

}
