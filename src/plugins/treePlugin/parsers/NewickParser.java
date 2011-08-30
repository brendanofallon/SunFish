package plugins.treePlugin.parsers;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import errorHandling.FileParseException;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import plugins.treePlugin.tree.*;

public class NewickParser extends FileParser {

	static final double VERSION = 1.0;
	
	String[] suffices = {"tre", "newick", "nwk"};
	
	public NewickParser(SunFishFrame parent) {
    	super(parent, parent.getLogger());
    	
    	if (parent.getProperty("newick.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("newick.color"));
			setLabelColor(color);
		}
		if (parent.getProperty("tree.icon")!=null) {
			ImageIcon icon = new ImageIcon(parent.getIconPath() + parent.getProperty("tree.icon"));
			setIcon(icon);
		}
    }

	@Override
	public String getName() {
		return "Newick tree parser";
	}

	@Override
	public String getDescription() {
		return "Reads newick-formatted phylogeny (tree) files";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
	
	public String[] getMatchingSuffices() {
		return suffices;
	}
	
	protected DrawableTree readFile(BufferedReader buf) throws IOException, FileParseException {
		StringBuffer treeString = new StringBuffer();
		int parenDepth = 0;
		char[] c = new char[1];
		boolean reading = false;
		DrawableTree tree;
		
		while( buf.read(c) > -1) {
			if (reading) {
				treeString.append(c);
				if (c[0]=='(')
					parenDepth++;
				if (c[0]==')')
					parenDepth--;
				if (parenDepth==0)
					break;
			}
			else {
				if ( c[0]=='(') {
					reading = true;
					treeString.append(c);
					parenDepth++;
				}
			}
		}
		
		if (parenDepth != 0) {
			logger.warning("Error parsing newick tree: found partial string '" + treeString + "'");
			throw new FileParseException("Error parsing tree, found partial string : " + treeString);
		}
		else {
			tree = new SquareTree(treeString.toString());
						
			return tree;
		}
	}
	
	public Class getDataClass() {
		return DrawableTree.class;
	}

	
	@Override
	protected int getFilePriority(File file) {
		try {
			BufferedReader reader = new BufferedReader( new FileReader(file));
			String line = reader.readLine();
			while(line != null && line.trim().length()==0)
				line = reader.readLine();

			if (line != null && line.trim().startsWith("("))
				return 2;

			reader.close();
			return 0;
		}
		catch (IOException ex) {
			return 0;
		}
	}
	
	/**
	 * TODO: Support for drawable trees with annotations
	 */
	public void writeData(File file, Object data) throws IOException {
		if (! (data instanceof Tree)) {
			throw new IllegalArgumentException("Non-tree found in newick parser writeData");
		}
		
		Tree tree = (Tree)data;
		String newick = tree.getNewick();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(newick);
		writer.close();
		
	}

}
