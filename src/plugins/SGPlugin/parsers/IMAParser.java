package plugins.SGPlugin.parsers;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;

import element.sequence.SequenceGroup;
import element.sequence.Sequence;
import element.sequence.StringSequence;
import errorHandling.ErrorWindow;
import errorHandling.FileParseException;

/**
 * A not-very-good parser for ima files. This just writes files right now. No reading. 
 * @author brendan
 *
 */
public class IMAParser extends SequenceFileParser {
	
	static final double VERSION = 1.0;

	String[] suffices = {"im", "ima"};
	
    boolean fileOK = false;

    public IMAParser(SunFishFrame parent) {
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
    
	@Override
	public String getName() {
		return "IMA file parser";
	}

	@Override
	public String getDescription() {
		return "Reads sequences from IMA-formatted files";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
    
	public Class getDataClass() {
		return SequenceGroup.class;
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
				writer.write( imaString(seq) );
			}
			writer.close();
		}
		catch(IOException ioe) {
			ErrorWindow.showErrorWindow(ioe, SunFishFrame.getSunFishFrame().getLogger());
			//System.err.println("Could not open file " + file.toString() + " for writing, exception : " + ioe.toString());
		}
	}

	private String imaString(Sequence seq) {
		StringBuilder str = new StringBuilder();
		if (seq.getName().length()>9)
			str.append(seq.getName().substring(0, 9));
		else
			str.append(seq.getName());
		
		while(str.length()<10)
		str.append(" ");

		str.append(seq.toString() + "\n");
		return str.toString();
	}
	
    protected Object readFile(BufferedReader file) throws IOException {
          SequenceGroup sg = new SequenceGroup();
          String line = file.readLine();
          int lineNumber = 0;
          while(line != null && lineNumber < 5) {
        	  line = file.readLine();
        	  lineNumber++;
          }
          
          while(line != null) {
        	  line = file.readLine();
        	  if (line != null && line.length()>10) {
        		  String label = line.substring(0, 10);
        		  String sequence = line.substring(10);
        		  if (! sequence.matches("\\d")) {
        			  Sequence seq = new StringSequence(sequence.trim(), label.trim());
        			  sg.add(seq);
        		  }
        		  
        	  }
          }
          
          
          return sg;
    }


    protected int getFilePriority(File file) {
    	//Not sure how to do this now..
    	return 1;
    }
    
	public String[] getMatchingSuffices() {
		return suffices;
	}
}
