package plugins.treePlugin.tree.reading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import plugins.treePlugin.tree.DrawableTree;

/**
 * Interface for things that provide a source for drawable trees. Currently implemented by TreeFileReader, which
 * reads newick strings from a file, and TreeFileForwardReader, which is a higher-performance, lower memory 
 * implementation that only reads trees in the forward direction
 * 
 * @author brendan
 *
 */

public interface TreeReader {

	/**
	 * Use the provided table to translate tip labels to something else. This is often necessary when 
	 * dealing with BEAST style nexus files.
	 * @param table
	 */
	public void setTranslationTable(Map<String, String> table);
	
	/**
	 * Obtain the current translation table. This is null if no translation table has been set.
	 * @return
	 */
	public Map<String, String> getTranslationTable();
	
	/**
	 * Use the current translation table to translate the tips of the provided tree. 
	 * @param tree
	 */
	public void translate(DrawableTree tree);
	
	/**
	 * Advance to the next tree in the sequence but do not read it.
	 */
	public void advance();
	
	/**
	 * Read and return the next tree in the sequence
	 */
	public DrawableTree getCurrentTree();
	
	/**
	 * Advance to the next tree in the sequence, then read and return it. In general this should be
	 * equivalent to advance() and then getCurrentTree() 
	 * @return
	 */
	public DrawableTree getNextTree();
	
	/**
	 * Get the previous tree in the sequence.
	 * @return
	 */
	public DrawableTree getPreviousTree();
	

	
}
