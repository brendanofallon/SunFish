/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.treePlugin.parsers;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import display.DisplayData;

import plugins.treePlugin.tree.DrawableTree;
import plugins.treePlugin.tree.reading.TreeFileForwardReader;
import plugins.treePlugin.tree.reading.TreeFileReader;
import plugins.treePlugin.tree.reading.TreeReader;
import plugins.treePlugin.treeDisplay.MultiTreeDisplay;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;

import element.sequence.Sequence;
import element.sequence.SequenceGroup;
import element.sequence.StringSequence;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

/**
 *
 * @author brendan
 */
public class NexusParser extends FileParser implements TreeReader {

	enum TreeReaderType {TWOWAY_READER, FORWARD_READER};
	
	String[] suffices = {"nex", "treeann"};
	
	boolean containsDataMatrix = false;

	TreeReaderType preferredTreeReaderType = TreeReaderType.TWOWAY_READER; 
	//Flag regarding the tree reader type to use... if this class
	//is being used to build a majority tree, we have no need of storing previously read trees, and
	//also we want to be as memory efficient as possible since we may be dealing with huge files. On
	//the other hand, if we're being used in a MultiTreeDisplay, then we need to be read both forward
	//and backward, and therefore must use a two-way tree reader (TreeFileReader)
		
	
	SequenceGroup sequenceData = null;
	boolean containsTrees = false;
	TreeReader treeReader = null;
	boolean containsTaxa = false;
	boolean containsCharacters = false;
	
	boolean hasTranslationTable = false;
	Map<String, String> translationTable;
	
	public NexusParser(SunFishFrame parent) {
    	super(parent, parent.getLogger());
    	
    	if (parent.getProperty("nexus.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("nexus.color"));
			setLabelColor(color);
		}
    	
    	if (parent.getProperty("nexus.icon")!=null) {
			ImageIcon icon = new ImageIcon(parent.getIconPath() + parent.getProperty("nexus.icon"));
			setIcon(icon);
		}
    }

	@Override
	public String getName() {
		return "Nexus parser";
	}

	@Override
	public String getDescription() {
		return "Parses trees and sequences from nexus-formatted file";
	}

	@Override
	public double getVersionNumber() {
		return 1.0;
	}
	
	public Class getDataClass() {
		return SequenceGroup.class;
	}

	public String[] getMatchingSuffices() {
		return suffices;
	}
	
	public void parseAndDisplay(File inputFile) {
		infile = inputFile;
		DisplayData data = null;
		boolean displaySequences = true;
		boolean displayTrees = true;
        try {
        	identifyBlocks(inputFile);
        	
        	if (containsDataMatrix && containsTrees) {
        		Object[] options = {"Cancel",
        							"Sequences",
        							"Trees",
        							"Both"};
        		int n = JOptionPane.showOptionDialog(sunfishParent,
        				"This file contains both sequences and trees. "
        				+ "What data would you like to display?",
        				"Choose data to display",
        				JOptionPane.YES_NO_CANCEL_OPTION,
        				JOptionPane.QUESTION_MESSAGE,
        				null,
        				options,
        				options[3]);
        		if (n==1) {
        			displaySequences = true;
        			displayTrees = false;
        		}
        		if (n==2) {
        			displayTrees = true;
        			displaySequences = false;
        		}
        		if (n==3) {
        			displaySequences = true;
        			displayTrees = true;
        		}
        	}
            
        	if (containsDataMatrix && displaySequences) {
        		data = new DisplayData(inputFile, sequenceData);
        		data.setIcon(icon);
        		sunfishParent.displayData(data, data.getFileName());
        	}
        	
        	if (containsTrees && displayTrees) {
        		if (hasTranslationTable && translationTable != null) {
        			treeReader.setTranslationTable(translationTable);
        		}
        		data = new DisplayData(inputFile, treeReader, MultiTreeDisplay.class);
        		data.setIcon(icon);
        		sunfishParent.displayData(data, data.getFileName());
        	}
        	
            infile = null;
        }
        catch (IOException e) {
        	ErrorWindow.showErrorWindow(e, logger);
            //System.err.println("Error reading file " + inputFile.toString() + " : " + e.toString());
            infile = null;
        }
        catch (FileParseException fpe) {
        	ErrorWindow.showErrorWindow(fpe, logger);
            //System.err.println("Error parsing file " + inputFile.toString() + " : " + fpe.toString());
            int ll = fpe.getLineNumber();
          //  if (ll>-1)
          //  	System.err.println("The problem seems to be occurring on line number : " + ll);
            infile = null;        	
        }
	}
	
	public Map<String, String> getTranslationTable() {
		return translationTable;
	}
	
	public void identifyBlocks(File inputFile) throws IOException, FileParseException {
		BufferedReader buf = new BufferedReader(new FileReader(inputFile));
		String line = buf.readLine();
		while (line!=null && line.trim().length()==0) {
			line = buf.readLine();
		}
		
		line = line.trim();
		if (! line.startsWith("#NEXUS") ) {
			throw new FileParseException("Invalid file format (first line is not '#NEXUS') ");
		}
		
		while(line != null) {
			line = skipComments(line, buf); //Skips comments and whitespace

			if (line==null)
				break;
			String ucLine = line.toUpperCase();
			if (ucLine.contains("BEGIN DATA;") || ucLine.contains("BEGIN CHARACTERS;")) {
				sequenceData = readMatrix(line, buf);
				containsDataMatrix = true;
			}
			
			if (ucLine.contains("BEGIN TAXA;")) {
				containsTaxa = true;
				readTaxa(line, buf);
			}
			
			if (ucLine.contains("BEGIN TREES")) {
				containsTrees = true;
				//Scan for a translation table
				line = buf.readLine();
				while(line!=null && !line.contains(";")) {
					ucLine = line.toUpperCase();
					if (ucLine.contains("TRANSLATE")) {
						buildTranslationTable(buf);
						hasTranslationTable = true;
						break;
					}
					line = buf.readLine();
				}
				treeReader = getTreeReader(inputFile);
				if (translationTable != null)
					treeReader.setTranslationTable(translationTable);
			}
			
			
			line = buf.readLine();
		}
	}
	
	private void readTaxa(String line, BufferedReader buf) throws IOException, FileParseException {
		if (! (line.toUpperCase().contains("TAXA") )) {
			throw new FileParseException("Somehow we got to readTaxa, but first line doesn't contain TAXA");
		}
		
		//uhh.. does this really need to do anything?
		while(line!=null && ! line.toUpperCase().contains("END")) {
			line = buf.readLine();
		}
	}

	public TreeReader getTreeReader(File inputFile) throws IOException {
		if (preferredTreeReaderType == TreeReaderType.TWOWAY_READER)
			return new TreeFileReader(inputFile);
		if (preferredTreeReaderType == TreeReaderType.FORWARD_READER)
			return new TreeFileForwardReader(inputFile);
		
		//How did we get here?
		return null;
	}
	
	/**
	 * Determines which type of tree reader to use, this will have no effect if set after the call
	 * to .identifyBlocks(), which creates the tree reader
	 * @param type
	 */
	public void setPreferredReaderType(TreeReaderType type) {
		preferredTreeReaderType = type;
	}

	/**
	 * Read the data matrix, ostensibly beginning at the current position in the BufferedReader, into file
	 * @param line
	 * @param file
	 * @return The data read in as a SequenceGroup
	 * @throws IOException
	 * @throws FileParseException
	 */
	private SequenceGroup readMatrix(String line, BufferedReader file) throws IOException, FileParseException {
		SequenceGroup seqs = new SequenceGroup();
		String missingChar = "?";
		String gapChar = "-";
		String matchChar = ".";
		boolean interleave = false;
		String type = "Nucleotide";
		
		if (! (line.toUpperCase().contains("DATA") || line.toUpperCase().contains("CHARACTERS"))) {
			throw new FileParseException("Somehow we got to read matrix, but first line doesn't contain MATRIX");
		}
		
		
		//Attempt to find missing and gap symbols...		
		line = file.readLine();
		while(line!=null && ! line.toUpperCase().contains("MATRIX")  ) {
            line = file.readLine();
            line = skipComments(line, file);
            String ucLine = line.toUpperCase();
            if (ucLine.contains("INTERLEAVE"))
            	interleave = true;
            if (ucLine.contains("TRANSLATE")) {
            	buildTranslationTable(file);
            	hasTranslationTable = true;
            }
            if (ucLine.contains("FORMAT")) {	//Parse the formatting string
            	String[] tokens = line.split("[=\\s+]");
            	for(int i=1; i<tokens.length-1; i++) {
            			if (tokens[i].equalsIgnoreCase("gap"))
            				gapChar = tokens[i+1];
            			if (tokens[i].equalsIgnoreCase("missing"))
            				missingChar = tokens[i+1];
            			if (tokens[i].equalsIgnoreCase("datatype"))
            				type = tokens[i+1];
            			if (tokens[i].equalsIgnoreCase("matchchar"))
            				matchChar = tokens[i+1];
            		
            	}
            }
  
            
		}

		if (line == null) {
			throw new FileParseException("Could not find data matrix ");
		}
		
		while(line!=null && (!line.toUpperCase().contains("MATRIX"))) {
			line = file.readLine();
		}

		
		//  Read lines until we come to something that contains 'end' 
		while(line!=null && ! line.toUpperCase().contains("END")) {
			line = file.readLine();
			while(line!= null && line.indexOf("[")>=0) {
				line = skipComments(line, file);
			}
			if (line.contains(";")) {
				int index = line.indexOf(";");
				line = line.substring(0, index);
			}
			if (line.trim().length()>0) {
				
				String[] parts = line.split("\\s+");
				if (parts.length>=2) {
					int firstNonBlank = 0;
					while(firstNonBlank < parts.length && parts[firstNonBlank].trim().length()==0) {
						firstNonBlank++;
					}
					
					StringBuilder sq = new StringBuilder();
					for(int i=firstNonBlank+1; i<parts.length; i++) {
						parts[i].replaceAll(" ", "");
						sq.append(parts[i]);
					}

					if (seqs.contains(parts[firstNonBlank].trim())) {
						seqs.getSequenceForName(parts[firstNonBlank].trim()).append(sq.toString());
					}
					else {
						Sequence seq = new StringSequence(sq.toString(), parts[firstNonBlank].trim());
						seqs.add(seq);
					}
			}
			}
		}

		if (seqs.size()==0 || seqs.getMaxSeqLength()==0) {
			throw new FileParseException("No data in file");
		}
		
		
		//Use the 'matchChar' to replace matchchars with whatever is at the same position in sequence 0
		for(int i=1; i<seqs.size(); i++) {
			seqs.get(i).replaceMatchingChars(seqs.get(0).toString(), matchChar.charAt(0));
		}
		
		return seqs;
	}
	
	private void buildTranslationTable(BufferedReader buf) throws IOException {
		translationTable = new Hashtable<String, String>();
		
		String line = buf.readLine();
		while(line!=null && ! line.contains(";")) {
			String[] parts = line.split("\\s+");
			if (parts.length>2) {
				int index = 0;
				while(index<parts.length && parts[index].trim().length()==0)
					index++;
				if (index<parts.length-1) {
					parts[index+1] = parts[index+1].replaceAll(",", "");
					translationTable.put(parts[index], parts[index+1]);
					//System.out.println("Associating key " + parts[index] + " with value : " + parts[index+1]);
				}
			}
			line = buf.readLine();
		}
		
		if (treeReader != null)
			treeReader.setTranslationTable(translationTable);
	}

	public boolean hasTranslationTable() {
		return hasTranslationTable;
	}
	
	protected String skipComments(String line, BufferedReader buf) throws IOException, FileParseException {
		if (line == null) 
			return null;
		
		while(line!=null && line.trim().length()==0)
			line = buf.readLine();
		
		if (line==null)
			return null;
		
		if (!line.contains("["))
			return line;
		
		int firstBracketIndex = line.indexOf('[');
		int closeBracketIndex = line.indexOf(']');
		if (closeBracketIndex>firstBracketIndex) {
			String comment = line.substring(firstBracketIndex, closeBracketIndex+1);
			return line.replace(comment, "");
		}
		
		if (closeBracketIndex>-1 && closeBracketIndex<firstBracketIndex) {
			return line.substring(closeBracketIndex+1, firstBracketIndex);
		}
		
		line = buf.readLine();
		while (line!=null && closeBracketIndex<0) {
			closeBracketIndex = line.indexOf(']');
			line = buf.readLine();
		}
		
		if (line==null || closeBracketIndex<0) {
			throw new FileParseException("Read to end of file looking for comment to end.");
		}
		else {
			if (closeBracketIndex<line.length())
				line = line.substring(closeBracketIndex+1);
		}
		return line;
	}
	
	protected Object readFile(BufferedReader file) throws IOException, FileParseException {
		SequenceGroup seqs = new SequenceGroup();
		String missingChar = "?";
		String gapChar = "-";
		boolean interleave = false;
		boolean inComment = false;
		String first = file.readLine();
		if (! first.startsWith("#NEXUS") ) {
			throw new FileParseException("Invalid file format (first line is not '#NEXUS') ");
		}

		String line = file.readLine();
		while(line!=null && ! line.toUpperCase().contains("MATRIX")  ) {
            line = file.readLine();
            if (line.toUpperCase().contains("INTERLEAVE"))
            	interleave = true;
		}

		if (line == null) {
			throw new FileParseException("Could not find data matrix ");
		}
		
		while(line!=null && line.trim().length()==0)
			line = file.readLine();

		while(line!=null && ! line.contains(";")) {
			line = file.readLine();
			int firstBracketIndex = line.indexOf('[');
			int closeBracketIndex = line.indexOf(']');
			if (firstBracketIndex>-1) {
				if (closeBracketIndex<0) {
					closeBracketIndex = line.length();
					inComment = true;
				}
				else if (closeBracketIndex<firstBracketIndex) {
					throw new FileParseException("Misplaced square brackets in data matrix");
				}
					
				line = line.substring(0, firstBracketIndex) + line.substring(closeBracketIndex+1);
			}
			if (firstBracketIndex<0 && closeBracketIndex>-1 && inComment) {
				inComment = false;
				line = line.substring(closeBracketIndex+1);
			}
			
			if (inComment)
				continue;
			
			if (line.trim().length()>0) {
				String[] parts = line.split("[ ]+");
				if (parts.length>=2) {
					StringBuffer sq = new StringBuffer();
					for(int i=1; i<parts.length; i++) {
						parts[i].replaceAll(" ", "");
						sq.append(parts[i]);
					}

					if (seqs.contains(parts[0].trim())) {
						seqs.getSequenceForName(parts[0].trim()).append(sq.toString());
					}
					else {
						Sequence seq = new StringSequence(sq.toString(), parts[0].trim());
						seqs.add(seq);
					}
			}
			}
		}

		if (seqs.size()==0 || seqs.getMaxSeqLength()==0) {
			throw new FileParseException("No data in file");
		}
		
		
		return seqs;

	}
	
	public void writeData(File file, SequenceGroup seqs) {
		try {
			FileWriter writer = new FileWriter(file);
			writer.write("#NEXUS\n\nBEGIN DATA;\n");
			writer.write("    DIMENSIONS NTAX=" + seqs.size() + " NCHAR=" + seqs.getMaxSeqLength() + ";\n");
			writer.write("    FORMAT MISSING=? GAP=- DATATYPE=DNA;\n");
			writer.write("    MATRIX\n");
			for(int i=0; i<seqs.size(); i++)
				writer.write("    " + seqs.get(i).getName() + " " + seqs.get(i).toString() + "\n");
			writer.write("    ;\n");
			writer.write("END;");
			
			writer.close();
		}
		catch(IOException ioe) {
			System.err.println("Could not open file " + file.toString() + " for writing, exception : " + ioe.toString());
		}		
	}

	public DrawableTree getNextTree() {
		if (!containsTrees) {
			return null;
		}
		if (treeReader == null) {
			System.out.println("Something called getNextTree() but treeReader is null.. probably identifyBlocks() has not been called for this NexusParser yet");
			return null;
		}
		
		DrawableTree tree = treeReader.getNextTree();
		treeReader.translate(tree);
		return tree;
	}

	public DrawableTree getPreviousTree() {
		if (!containsTrees) {
			return null;
		}
		if (treeReader == null) {
			logger.warning("Something called getPreviousTree() but treeReader is null.. probably identifyBlocks() has not been called for this NexusParser yet");
			ErrorWindow.showErrorWindow(new NullPointerException("Can not read the previous tree from this nexus file"));
			return null;
		}
		if (hasTranslationTable) {
			//TODO translate label names.. do we need to do this?
		}
		return treeReader.getPreviousTree();
	}

	
	protected boolean fileFormatMatches(File file) throws Exception {
		BufferedReader reader = new BufferedReader( new FileReader(file));
		String line = reader.readLine();
		while(line != null && line.trim().length()==0)
			line = reader.readLine();
		
		if (line != null && (line.trim().startsWith("#NEXUS") || line.trim().startsWith("#Nexus") || line.trim().startsWith("#nexus")))
			return true;
		
		reader.close();
		return false;
	}

	/**
	 * Totally confusing since ideally we should only write to part of the nexus file (the part
	 * that was displayed)
	 * .. maybe this could only just make new files? It never over writes the source file?
	 */
	public void writeData(File file, Object data) throws IOException {
		throw new IllegalArgumentException("writeData not implemented yet for Nexus files");
	}

	
	/************** TreeReader implementation, delegate everything possible to the treeReader object ********************/
	
	@Override
	public void setTranslationTable(Map<String, String> table) {
		this.translationTable = table;
		treeReader.setTranslationTable(table);
		
	}

	@Override
	public void translate(DrawableTree tree) {
		treeReader.translate(tree);
	}

	@Override
	public void advance() {
		treeReader.advance();
	}

	@Override
	public DrawableTree getCurrentTree() {
		if (!containsTrees) {
			return null;
		}
		if (treeReader == null) {
			logger.warning("Something called getCurrentTree() but treeReader is null.. probably identifyBlocks() has not been called for this NexusParser yet");
			ErrorWindow.showErrorWindow(new NullPointerException("Can not read the a tree from this nexus file"));
			return null;
		}
		return treeReader.getCurrentTree();
	}



	

}
