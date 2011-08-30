package plugins.textPlugin;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import display.Display;
import display.DisplayData;

import topLevelGUI.SunFishFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;


/**
 *  A special kind of display that displays any text file as unformatted text.
 * TODO: Implement Save,
 * TODO: Cut and Paste popup 
 * @author brendan
 *
 */
public class TextDisplay extends Display {
	

	JScrollPane scrollPane;
	JTextArea area;
	String fileName;

	public TextDisplay(SunFishFrame parent) {
		super(parent);
		fileName = null;
		setLayout(new BorderLayout());
	}
	

    public void construct() {

    }
    
    public void saveToFile(File f) {
		throw new IllegalStateException("Saving is not yet implemented for Single-sequence displays. Sorry.");
	}

    //Called when data is updated/changed
    protected boolean update(SunFishFrame parent, DisplayData data) {
    	myFrame = parent;
    	area = new JTextArea();
    	
    	if (data.getData(0).getClass() == File.class) {
    		File file = (File)data.getData(0);
    		try {
    			BufferedReader reader = new BufferedReader( new FileReader( file) );
    			String line = reader.readLine();
    			
    			while (line != null) {
    				area.append(line +"\n" );
    				//System.out.println(line + "\n");
    				line = reader.readLine();
    			}
    	    	scrollPane = new JScrollPane(area);
    	    	scrollPane.setPreferredSize(new Dimension(700, 300));
    	    	area.setEditable(true);
    			add(scrollPane, BorderLayout.CENTER);
    		}
    		catch (IOException ex) {
    			System.err.println("Error while attempting to disply file " + filename + " in text mode : \n " + ex.toString());
    			return false;
    		}
    		
    		return true;
    	}
    	
    	//If we're here the DisplayData object was not of class File, so we can't display it
    	return false;
    }

    public String getDisplayName() {
    	String str = "Text Display";
    	return str;
    }

	public Display getNew() {
		return new TextDisplay(sunfishParent);
	}

	public String getFileName() {
		return fileName;
	}

	public void lostFocus() {
			
	}

	public String getTitle() {
		return "";
	}


	@Override
	public String getName() {
		return "Text Display";
	}


	@Override
	public String getDescription() {
		return "Displays text files";
	}


	@Override
	public double getVersionNumber() {
		return 1.0;
	}


	@Override
	public Class[] getDisplayableClasses() {
		return new Class[]{File.class};
	}

}
