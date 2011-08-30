/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.SGPlugin.parsers;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;


import element.sequence.SequenceGroup;
import element.sequence.Sequence;
import element.sequence.StringSequence;
import errorHandling.FileParseException;

/**
 *
 * @author brendan
 */
public class PhyParser extends SequenceFileParser {
	
	static final double VERSION = 1.0;

	String[] suffices = {"phy", "ph"};
	
	public PhyParser(SunFishFrame parent) {
    	super(parent, parent.getLogger());
    	
    	if (parent.getProperty("phylip.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("phylip.color"));
			setLabelColor(color);
		}
    }

	@Override
	public String getName() {
		return "Phylip file parser";
	}

	@Override
	public String getDescription() {
		return "Reads Phylip-formatted sequence files";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
	
	public Class getDataClass() {
		return SequenceGroup.class;
	}
	
	public String[] getMatchingSuffices() {
		return suffices;
	}
	
	public void writeData(File file, SequenceGroup seqs) {
		try {
			FileWriter writer = new FileWriter(file);
			int marker = 0;
			int maxLength = seqs.getMaxSeqLength();
			
			writer.write("   " + seqs.size() + "   " + seqs.getMaxSeqLength() + "\n");
			
			for(int i=0; i<seqs.size(); i++) {
				StringBuffer namePad = new StringBuffer();
				String name = seqs.get(i).getName();
				if (name.length()>10)
					name = name.substring(0, 10);
				namePad.append(name);
				while(namePad.length()<11)
					namePad.append(" ");
				writer.write(namePad.toString());
				System.out.print(namePad.toString());
				StringBuffer seqPad = new StringBuffer();
				seqPad.append(seqs.get(i).toString().substring(0, Math.min(seqs.get(i).toString().length(), 60)));
				if (seqs.get(i).toString().length() < 60) {
					while (seqPad.length() < maxLength) {
						seqPad.append("-");
					}
				}
				writer.write(seqPad.toString()+"\n");
				System.out.print(seqPad.toString()+"\n");
			}
			
			if (maxLength > 60) {
				marker = 60;
		
				while(marker+60 < maxLength) {
					writer.write("\n");
					for(int i=0; i<seqs.size(); i++) {
						writer.write(seqs.get(i).toString().substring(marker, Math.min(seqs.get(i).toString().length(), marker+60))+"\n");
						System.out.print(seqs.get(i).toString().substring(marker, Math.min(seqs.get(i).toString().length(), marker+60))+"\n");
					}
					marker+=60;
				}
				
			}
			
			writer.close();
		}
		catch(IOException ioe) {
			System.err.println("Could not open file " + file.toString() + " for writing, exception : " + ioe.toString());
		}		
	}


	protected Object readFile(BufferedReader file) throws IOException, FileParseException {
		SequenceGroup seqs = new SequenceGroup();
		int numSequences;
		int lineNumber = 0;

            String line = file.readLine();
			lineNumber++;
			
            while(line.trim().length() == 0) {
            	lineNumber++;
                line = file.readLine();
            }
			
            String delims = "[ ]+";
            String[] tokens = line.trim().split(delims);
            if (tokens.length != 2) {
            	System.err.println("Uh-oh, didn't find exactly 2 tokens on first non-blank line : ");
            	for(int i=0; i<tokens.length; i++)
            		System.err.println(i + " : " + tokens[i]);
            	System.err.println(" Aborting...\n");
            	
            	throw new FileParseException("Didn't find sequence or character numbers on first line.");
            }
            else {
            	numSequences = Integer.parseInt(tokens[0]);
            	//numCharacters = Integer.parseInt(tokens[1]); never used
            }
            	
            while(line.trim().length() == 0) {
            	lineNumber++;
                line = file.readLine();
            }
            
            //Now read the next numSequences lines
            StringBuffer[] newSeqs = new StringBuffer[numSequences];
            for(int i=0; i<numSequences; i++) {
            	newSeqs[i] = new StringBuffer();
            }
            String[] seqNames = new String[numSequences];
            for(int i=0; i<numSequences; i++) {
            	lineNumber++;
            	line = file.readLine();
            	tokens = line.split(delims);
            	if(tokens.length>=2) {
            		seqNames[i] = tokens[0];
            		for(int j=1; j<tokens.length; j++)
            			newSeqs[i].append(tokens[j]);
            	}
            	else {
//            		System.err.println("Didn't find exactly two tokens at line #" + lineNumber);
//            		for(int j=0; j<tokens.length; j++)
//                		System.err.println(j + " : " + tokens[j]);
                	throw new FileParseException("Error parsing line " + lineNumber, lineNumber);
            	}
            }//for i
            
            line = file.readLine();
            while(line != null) {
                while(line!=null && line.trim().length() == 0) {
                	lineNumber++;
                    line = file.readLine();
                }
                
                for(int i=0; i<numSequences && line != null; i++) {
                	tokens = line.split(delims);
//                	if (tokens.length==1) {
//                		newSeqs[i].append(tokens[0]);
//                	}
            		for(int j=0; j<tokens.length; j++)
            			newSeqs[i].append(tokens[j]);
                	
//                	if(tokens.length==2) {
//                		newSeqs[i].append(tokens[1]);
//                	}
//                	
//                	if (tokens.length > 2){
////                		System.err.println("Didn't find either 1 or 2 tokens at line #" + lineNumber);
////                		for(int j=0; j<tokens.length; j++)
////                    		System.err.println(j + " : " + tokens[j]);
//                    	throw new FileParseException("Error parsing line " + lineNumber, lineNumber);
//                	}
                	
                	lineNumber++;
                	line = file.readLine();
                }//for i
                
            }//while reading new lines
            
            for(int i=0; i<numSequences; i++) {
            	Sequence seq = new StringSequence(newSeqs[i].toString().toUpperCase(), seqNames[i]);
            	seqs.add( seq );
            }

            return seqs;
		
	}
	
	protected int getFilePriority(File file) {
		try {
			BufferedReader reader = new BufferedReader( new FileReader(file));
			String line = reader.readLine();
			while(line != null && line.trim().length()==0)
				line = reader.readLine();

			if (line != null && (line.trim().split("\\s+").length==2))
				return 2;


			reader.close();
			return 0;
		}
		catch (IOException ex) {
			return 0;
		}
	}


	public void writeData(File file, Object data) throws IOException {
		// TODO Auto-generated method stub
		throw new IllegalArgumentException("Phylip parser not implemented yet");
	}
}

	