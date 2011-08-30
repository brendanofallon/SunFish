/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.SGPlugin.parsers;
import display.Display;
import display.DisplayData;
import display.ProgressPanel;
import element.sequence.*;

import java.awt.Color;
import java.awt.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import plugins.SGPlugin.display.SGContentPanelDisplay;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;


import element.*;
import element.sequence.Sequence;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

/**
 * Converts a fasta file to a sequence group
 * @author brendan
 */
public class FastaParser extends SequenceFileParser {

	static final double VERSION = 1.0;
	
	String[] suffices = {"fasta", "fas"};
	
    boolean fileOK = false;

    public FastaParser(SunFishFrame parent) {
    	super(parent, parent.getLogger());
    	
    	if (parent.getProperty("fasta.color")!=null) {
			Color color = parent.parseColor(parent.getProperty("fasta.color"));
			setLabelColor(color);
		}
		if (parent.getProperty("fasta.icon")!=null) {
			ImageIcon icon = new ImageIcon(parent.getIconPath() + parent.getProperty("fasta.icon"));
			setIcon(icon);
		}
    	
    }
    
	public double getVersionNumber() {
		return VERSION;
	}
    
	public Class getDataClass() {
		return SequenceGroup.class;
	}
	
    /**
     * Primary entry point for parsing a file. This default implementation calls readFile to obtain an object representing
     * the data, creates a new DisplayData object from it and the file, and calls sunfishParent.displayData(...) .
     * This method blocks during file parsing, and may not be appropriate for Parsers that take a long time to
     * parse their file.
     * 
     * @param inputFile
     */
	public void parseAndDisplayEXPERIMENTAL(File inputFile) throws IOException, FileParseException {
		infile = inputFile;
		DisplayData data = null;
		
		int lastPos = inputFile.getName().lastIndexOf(".");
		if (lastPos<0)
			lastPos = inputFile.getName().length();
		String title = inputFile.getName().substring(0, lastPos);
		
//		System.out.println("opening new display");
		
//		ProgressPanel progPanel = SunFishFrame.getSunFishFrame().getDisplayPane().openProgressDisplay(title);
//		
//		ParserWorker parser = new ParserWorker(progPanel);
//		parser.execute();
		
		data = parse(inputFile);

		infile = null;
		//sgDisplay.presentDisplay(sunfishParent, data, title);
	}
	
	
	public void writeData(File file, Object data) {
		if (! (data instanceof SequenceGroup )) {
			throw new IllegalArgumentException("Incorrect data type given to fastaParser.writeData");
		}
		
		SequenceGroup seqs = (SequenceGroup)data;
		try {
			FileWriter writer = new FileWriter(file);
			for(int i=0; i<seqs.size(); i++) {
				Sequence seq = seqs.get(i);
				writer.write( fastaString(seq) +"\n");
			}
			writer.close();
		}
		catch(IOException ioe) {
			ErrorWindow.showErrorWindow(ioe, SunFishFrame.getSunFishFrame().getLogger());
			//System.err.println("Could not open file " + file.toString() + " for writing, exception : " + ioe.toString());
		}
	}

	private String fastaString(Sequence seq) {
		StringBuffer buf = new StringBuffer();
		buf.append(">" + seq.getName()+ "\n");
		buf.append(seq.toString() + "\n");
		return buf.toString();
	}
	
    protected Object readFile(BufferedReader file) throws IOException {
            SequenceGroup seqs = new SequenceGroup();
			int lineNumber = 0;
			
			String line = file.readLine();
			lineNumber++;
			//System.out.println("Trying to read line " + lineNumber + " : " + line);
			while(line!=null && (! line.startsWith(">"))) {
				lineNumber++;
				line = file.readLine();
			}

			if (line == null)  {
				ErrorWindow.showErrorWindow(new FileParseException("File does not appear to be in fasta format"), SunFishFrame.getSunFishFrame().getLogger());
				return null;
			}

			Sequence seq;
			while(line != null) {
				//System.out.println("Trying to read line " + lineNumber + " : " + line);
				line = line.trim();
				while(line!=null && line.trim().length()==0) {
					line = file.readLine();
					lineNumber++;
				}
				if (line == null)
					seq = null;
				else {
					if (line.charAt(0) != '>')  {
						System.err.println("ERROR: this file does not appear to be in fasta format.");
						return null;
					}

					String id = line.substring(1);
					StringBuffer sq = new StringBuffer();
					line = file.readLine();
					lineNumber++;
					if (line!=null)
						line = line.trim();
					while(line != null && line.length()>0 && line.charAt(0)!='>') {
						sq.append(line);
						line = file.readLine();
						lineNumber++;

						if (line!=null)
							line = line.trim();
					}

					seq = new StringSequence(sq.toString().toUpperCase(), id);
					seqs.add( seq );

					// System.out.println("Appending seq with id : " + seq.getName() + " length : " + seq.length());
				}//else line was not null
			}//while reading new sequences

			
			return seqs;
    }


	public String[] getMatchingSuffices() {
		return suffices;
	}

	@Override
	protected int getFilePriority(File file) {
		try {
			BufferedReader reader = new BufferedReader( new FileReader(file));
			String line = reader.readLine();
			while(line != null && line.trim().length()==0)
				line = reader.readLine();

			if (line != null && line.trim().startsWith(">"))
				return 2;

			reader.close();
			return 0;
		}
		catch (IOException ex) {
			return 0;
		}
	}

	@Override
	public String getName() {
		return "Fasta file parser";
	}

	@Override
	public String getDescription() {
		return "Reads fasta-formatted sequence files";
	}
	
	class ParserWorker extends SwingWorker {

		//Number of display that we will appear in
		ProgressPanel progPanel;
		
		protected ParserWorker(ProgressPanel progPanel) {
			this.progPanel = progPanel;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			progPanel.setNote("Reading file");
			for(int i=0; i<10000000; i++) {
				double y = Math.random()*Math.sqrt( Math.tanh(Math.PI*0.4322252+Math.random()) )*Math.cbrt(Math.random()*0.004);
				if (i%1000000==0) {
					progPanel.setProgress((double)i / 10000000.0);
					System.out.println("Setting progress to " + (double)i / 10000000.0);
				}
			}
			
			System.out.println("Done. now parsing file");
			return null;
		}
		
	}
}
