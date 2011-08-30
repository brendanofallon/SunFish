package plugins.treePlugin.tree.reading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import plugins.treePlugin.tree.*;

/**
 * Base class for objects that can read sequences of trees from a file
 * @author brendan
 *
 */
public abstract class AbstractTreeFileReader implements TreeReader {

	protected BufferedReader buf;
	protected String currentLine; //Always contains most recent line read, never goes backwards
		
	protected Map<String, String> translationTable = null;
	
	public DrawableTree getNextTree() {
		advance();
		return getCurrentTree();
	}


	@Override
	public void setTranslationTable(Map<String, String> table) {
		this.translationTable = table;
	}


	@Override
	public Map<String, String> getTranslationTable() {
		return translationTable;
	}


	@Override
	public void translate(DrawableTree tree) {
		if (translationTable != null) {
			List<Node> tips = tree.getAllTips();
			for(Node tip : tips) {
				DrawableNode dNode = (DrawableNode)tip;
				String key = dNode.getAnnotationValue("Tip");
				if (key==null)
					key = dNode.getLabel();
				String val = translationTable.get(key);
				if (val!=null) {
					dNode.setLabel(val);			//This is a total gotcha.. TreeReaders look at currentLabel to find what to display (since there are many potential labels)
					dNode.setCurrentLabel(dNode.getLabel()); //But the 'label' field was originally crafted to hold whatever the tip label is... so it's													//Not clear what the difference between label and current label is. They seem to do the same thing..
				}
			}
		}
		
	}

	
}
