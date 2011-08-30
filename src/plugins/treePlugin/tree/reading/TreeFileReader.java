package plugins.treePlugin.tree.reading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import plugins.treePlugin.tree.*;

/**
 *  Returns DrawableTrees from a file, stores the list of already-read in trees (as strings), so that you can back up and read previous
 * trees. Doesn't allow random access to any old tree, as of yet, however. Also, this is not appropriate to use if someone is planning
 * to scan through a huge list of trees, since this thing contains all trees previously read. We should come up with a more memory
 * efficient way of doing this (TreeFileForwardReader just allows access in the forward direction....)
 * 
 * @author brendan
 *
 */
public class TreeFileReader extends AbstractTreeFileReader {

	private ArrayList<String> treeStrings;
	
	private int currentIndex = -1; //Contains index of last tree returned, either by getNextTree() or getPreviousTree(), so goes both up and down
		
	public TreeFileReader(File inputFile) throws IOException {
		buf = new BufferedReader(new FileReader(inputFile));
		currentLine = buf.readLine();
		treeStrings = new ArrayList<String>();
		currentIndex = -1;
	}
	
	
	@Override
	public void advance() {
		readNextTree();
	}
	
	protected boolean readNextTree()  {
		try {
			while(currentLine != null && !currentLine.contains("(")) {
				currentLine = buf.readLine();
			}

			if (currentLine == null)
				return false;

			int index = currentLine.indexOf("(");
			int parenDepth = 1;
			int lastIndex = 0;
			StringBuilder treeStrBuilder = new StringBuilder();
			treeStrBuilder.append("(");
			//If the tree spans multiple lines, parenDepth won't be zero here
			while(currentLine != null && parenDepth>0) {
				for(int i = index+1; i<currentLine.length(); i++) {
					if (currentLine.charAt(i)=='(')
						parenDepth++;
					if (currentLine.charAt(i)==')')
						parenDepth--;
					if (parenDepth==0) {
						lastIndex = i;
						break;
					}
				}
				treeStrBuilder.append(currentLine.substring(index+1, lastIndex+1));
				currentLine = buf.readLine();
			}

			treeStrings.add(treeStrBuilder.toString());
		}
		catch (IOException ioe) {
			System.err.println("Uh-oh, encountered error while reading tree : " + ioe);
			return false;
		}
		return true;
	}
	
	@Override
	public DrawableTree getCurrentTree() {
		if (currentIndex < 0) {
			throw new NullPointerException("Call advance before you call getCurrentTree()");
		}
		DrawableTree tree = new SquareTree(treeStrings.get(currentIndex));
		if (translationTable != null) {
			translate(tree);
		}
		return tree;
	}
	
	public DrawableTree getNextTree() {
		DrawableTree tree;
		boolean hasNext = true;
		if (currentIndex == (treeStrings.size()-1)) {
			hasNext = readNextTree();
		}
		
		if (! hasNext) {
			return null;
		}
		
		currentIndex++;
		return getCurrentTree();
	}
	
	public DrawableTree getPreviousTree() {
		if (currentIndex==0) {
			return null;
		}
		else {
			currentIndex--;
			DrawableTree tree = new SquareTree(treeStrings.get(currentIndex));
			if (translationTable != null)
				translate(tree);
			//System.out.println("Returning previous tree from index : " + currentIndex);
			return tree;
		}

	}

}
