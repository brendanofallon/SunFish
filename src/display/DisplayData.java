package display;

import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * This class is essentially a wrapper for data that can be displayed in a displaypane
 * FileParsers create these objects and set some data in them, then they're passed
 * to a displaypane for viewing.
 * There's not really a lot of extra information in here, this superclass is mostly just
 * a marker for things that can be displayed
 * @author brendan
 *
 */
public class DisplayData {
	
	File originFile;
	ArrayList<Object> data;
	Class preferredDisplayClass;
	ImageIcon icon = null;
	
	public DisplayData(File origin, Object info) {
		data = new ArrayList<Object>();
		this.originFile = origin;
		data.add(info);
		this.preferredDisplayClass = Object.class;
	}
	
	public DisplayData(File origin, Object info, Class preferredDisplayClass) {
		data = new ArrayList<Object>();
		data.add(info);
		this.originFile = origin;
		this.preferredDisplayClass = preferredDisplayClass;
	}
	
	public Class getPreferredDisplay() {
		return preferredDisplayClass;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
	public ImageIcon getIcon() {
		return icon;
	}
	
	public File getFile() {
		return originFile;
	}
	
	public String getFileName() {
		if (originFile != null)
			return originFile.getName();
		else {
			return "(unknown name)";
		}
	}
	
	public String getFilePath() {
		return originFile.getAbsolutePath();
	}
	
	public Object getData(int index) {
		return data.get(index);
	}
	
	public void addNewData(Object moreInfo) {
		data.add(moreInfo);
	}
	
	public int numObjects() {
		return data.size();
	}
	

}
