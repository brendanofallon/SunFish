package plugins.treePlugin.tree.reading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import plugins.treePlugin.tree.*;

/**
 * A class that reads newick-style trees from a file in the forward direction only. This is more memory efficient (since
 * past trees are immediately forgotten), and is used when we're building a consensus / majority tree. 
 * @author brendan
 *
 */
public class TreeFileForwardReader extends AbstractTreeFileReader {
	
	public TreeFileForwardReader(File inputFile) throws IOException {
		buf = new BufferedReader(new FileReader(inputFile));
		currentLine = buf.readLine();
	}
	
	/**
	 * Advance to the next tree but don't try to create it. This happens by reading lines until we find one that contains
	 * an open paren. This line is loaded into 
	 */
	public void advance() {
		while(currentLine != null && !currentLine.contains("(")) {
			try {
				currentLine = buf.readLine();
			} catch (IOException e) {
				//Must be at end of file
			}
		}
	}
	
	public DrawableTree getCurrentTree() {
		DrawableTree tree = null;
		if (currentLine == null)
			return null;

		try {
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

			//TODO can this happen later?
			tree = new SquareTree(treeStrBuilder.toString());
			if (translationTable != null) {
				translate(tree);
			}
		}
		catch (IOException ex) {

		}
		return tree;
	}
	
	public DrawableTree getPreviousTree() {
		throw new IllegalStateException("Forward tree readers do not support reading backwards");
	}
	

}
