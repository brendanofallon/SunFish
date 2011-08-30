package topLevelGUI;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;

import topLevelGUI.SunFishFrame;


/**
 * Contains and dispenses all known parsers. 
 * 
 * @author brendan
 *
 */
public class ParserRegistry {

	List<FileParser> parsers = new ArrayList<FileParser>();
	SunFishFrame sunfish;
	
	public ParserRegistry(SunFishFrame sunfish) {
		this.sunfish = sunfish;
	}
	
	/**
	 * Returns a list containing all registered file parsers
	 * @return
	 */
	public List<FileParser> getAllParsers() {
		return parsers;
	}
	
	/**
	 * Returns a list of all registered file parsers that think they can convert the given file into a displayable object
	 * @param file
	 * @return
	 */
	public List<FileParser> getParsers(File file) {
		List<FileParser> availableParsers = new ArrayList<FileParser>();
		List<Integer> priorities = new ArrayList<Integer>();
		
		for(FileParser parser : parsers) {
			int priority = parser.getParserPriority(file); 
			if (priority > 0) {
				availableParsers.add(parser);
				priorities.add(priority);
			}
		}
		
		//Sort parsers by file priority
		if (availableParsers.size()>1) {
			for(int i=0; i<priorities.size(); i++) {
				for(int j=i+1; j<priorities.size(); j++) {
					if (priorities.get(i)<priorities.get(j)) {
						Integer tmpInt = priorities.get(i);
						priorities.set(i, priorities.get(j));
						priorities.set(j, tmpInt);
						
						FileParser tmpParser = availableParsers.get(i);
						availableParsers.set(i, availableParsers.get(j));
						availableParsers.set(j, tmpParser);
					}
				}
			}
		}
		return availableParsers;
	}
	
	
	/**
	 * Adds all known parsers to a hashtable, keyed by the file suffix the the type of file
	 * each parser can parse.
	 */
	public void addParsers(List<FileParser> parsers) {
		this.parsers.addAll(parsers);
	}


}
