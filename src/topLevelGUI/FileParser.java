/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package topLevelGUI;

import display.DisplayData;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Base class of all things that can examine a File and return an object to be displayed in
 * the form of DisplayData. The application will call .parse(File file) with the file to be
 * processed. Simple fileParsers may just need to implement readFile and accept the base .parse(),
 * but fancier parsers that, for instance, may take a long time and need to run in the background
 * (for instance, the TreeLogParser and maybe the NexusParser) will override .parse()  
 * 
 *  
 * @author brendan
 */
public abstract class FileParser {

	protected File infile = null;
	Color labelColor = Color.black;
	protected ImageIcon icon = null;
	protected SunFishFrame sunfishParent;
	protected Logger logger;
	
	public FileParser(SunFishFrame sunfishParent, Logger logger) {
		this.sunfishParent = sunfishParent;
	}

	/**
	 * Return a short, descriptive name for this parser, e.g. "Newick tree parser" 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Returns a longer description of this parser. e.g. "Reads phylogenies (trees) from newick-formatted files. 
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Return the version number of this display. 
	 * @return
	 */
	public abstract double getVersionNumber();
	
	
    /**
     * Primary entry point for parsing a file. This default implementation calls readFile to obtain an object representing
     * the data, creates a new DisplayData object from it and the file, and calls sunfishParent.displayData(...) .
     * This method blocks during file parsing, and may not be appropriate for Parsers that take a long time to
     * parse their file.
     * 
     * @param inputFile
     */
	public void parseAndDisplay(File inputFile) throws IOException, FileParseException {
		infile = inputFile;
		DisplayData data = null;

		data = parse(inputFile);

		infile = null;
		sunfishParent.displayData(data, data.getFileName());
	}
	
	public DisplayData parse(File file) throws IOException, FileParseException {
		Object info = readFile(file);
		DisplayData data = new DisplayData(file, info, getPreferredDisplayClass());
		data.setIcon(icon);
		return data;
	}
	
	/**
	 * Returns an integer representing this parser's confidence that it is the correct parser for the given file. 
	 * no IO error will occur, but ideally we should be able to detect gross deviations from the expected type quickly. 
	 * @param file File to see if we can parse
	 * @return True if the file can probably be converted into a displayable object
	 */
	public int getParserPriority(File file) {
		String filename = file.getName();
		int lastIndex = filename.lastIndexOf(".");
		if (lastIndex<0) {
			return 0;
		}
		
		if (! file.exists())
			return 0;
		
		String fileSuffix = file.getName().substring(lastIndex+1);
		
		//See if the file suffix matches the list of expected suffices. If not, we don't try to parse
		boolean suffixMatches = false;
		for(int i=0; i<getMatchingSuffices().length; i++) {
			String suffix = getMatchingSuffices()[i];
			if (suffix.equalsIgnoreCase(fileSuffix)) {
				suffixMatches = true;
			}
		}
		
		if (! suffixMatches) 
			return 0;
	
		
		try {
			int priority = getFilePriority(file); 
			return priority; 
		} catch (Exception e) {
			return 0;
		}
		
	}
	
	/**
	 * Indicates how 
	 * @param file
	 * @return
	 */
	protected int getFilePriority(File file) {
		return 1;
	}
	
	/**
	 * 
	 * @return A list of file suffixes that designate the types of files this parser can parse
	 */
	public abstract String[] getMatchingSuffices();
		
	
	/**
	 * In most parsers this method actually does the parsing, and converts the file into an Object,
	 * which is then packaged into a DisplayData object by .parse(file)
	 * @param buf BufferedReader from which to read
	 * @return The data object (Tree, Sequence, etc)
	 * @throws IOException If we could not read from file
	 * @throws FileParseException	If the file existed and could be read, but wasn't in a valid format
	 */
	protected abstract Object readFile(BufferedReader buf) throws IOException, FileParseException;
	
	/**
	 * Returns the class of data object associated with this file parser (Tree, Sequence, etc)
	 * @return
	 */
	public abstract Class getDataClass();
	
	public abstract void writeData(File file, Object data) throws IOException;
	
	
	public Color getLabelColor() {
		return labelColor;
	}
	
	public void setLabelColor(Color c) {
		labelColor = c;
	}
	
	public ImageIcon getIcon() {
		return icon;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	/**
	 * FileParsers may optionally specify a preferred display class which supercedes 
	 * whatever might be found in the DisplayRegistry. This is useful for parsers that
	 * return an object specified only by an interface (XML objects do this)
	 * Most parsers can ignore this. The default is Object.class, which means
	 * use the displayRegistry
	 * @return
	 */
	public Class getPreferredDisplayClass() {
		return Object.class;
	}
	
	/**
	 * Subclasses may choose to either override this method, if they need the File, or to use
	 * readFile(bufferedreader) for convenience, if they'd like to work with a buffered reader
	 * @param file File to be parsed
	 * @return Object read from file
	 * @throws IOException
	 * @throws FileParseException
	 */
	public Object readFile(File file) throws IOException, FileParseException {
		BufferedReader buffer = new BufferedReader( new FileReader(file));
		return readFile(buffer);
	}
	
	
	
	/**
	 * Returns the current input file, is only valid during a getData operation
	 */
	public File getInputFile() {
		return infile;
	}
	
	
	
	
}
