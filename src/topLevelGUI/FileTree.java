package topLevelGUI;

import java.io.File;
import java.util.List;

/**
 * Interface for things that can open / examine files and cause them to be displayed
 * in a sunfish frame 
 * @author brendan
 *
 */
public interface FileTree {

	/**
	 * Allow user to select a new file to display
	 */
	public void openFileAction();
	
	/**
	 * Add a file to the recent files list
	 * @param file
	 */
	public void addRecentFile(File file);
	
	/**
	 * Obtain a list of recently displayed files. Generally this should contain
	 * the files added via calls to addRecentFile, but there's no strict interpretation here
	 * @return
	 */
	public List<File> getRecentFiles();
	
	
	/**
	 * Get a list of currently displayed directories
	 * @return
	 */
	public List<String> getTopLevelPaths();

	/**
	 * Reload all information for the file parsers
	 */
	public void reloadParserInfo();
	
}
