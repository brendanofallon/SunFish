package plugins.textPlugin;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import display.DisplayData;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;

import errorHandling.FileParseException;

public class TextParser extends FileParser {
	
	String[] suffices = {"txt"};
	
	public TextParser(SunFishFrame parent) {
    	super(parent, parent.getLogger());
    	
    	if (parent.getProperty("txt.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("txt.color"));
			setLabelColor(color);
		}
		if (parent.getProperty("txt.icon")!=null) {
			ImageIcon icon = new ImageIcon(parent.getIconPath() + parent.getProperty("txt.icon"));
			setIcon(icon);
		}
    }
	
	public void parseAndDisplay(File file) {
		sunfishParent.displayData(new DisplayData(file, file), file.getName());
	}
	
	public Class getDataClass() {
		return File.class;
	}
	
	public String[] getMatchingSuffices() {
		return suffices;
	}

	/**
	 * Doesn't get used here, since we've overridden parse()
	 */
	protected Object readFile(BufferedReader buf) throws IOException,
			FileParseException {
		
		return null;
	}


	public void writeData(File file, Object data) throws IOException {
		if (! (data instanceof String)) {
			throw new IllegalArgumentException("Non-string found text parser, writeData");
		}
		String text = data.toString();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(text);
		writer.close();
	}
	
	protected boolean fileFormatMatches(File file) throws Exception {
		return true;
	}

	@Override
	public String getName() {
		return  "Text parser";
	}

	@Override
	public String getDescription() {
		return "Reads data from plain text files";
	}

	@Override
	public double getVersionNumber() {
		return 1.0;
	}

}
