package element.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import element.sequence.SequenceChangeListener.SequenceEventType;

/**
 * An implementation of Sequence where characters are stored in a String, probably workable 
 * for short-medium sequences. Nearly all operations involve simply converting the string to a StringBuilder, performing
 * the operation, and then converting back, so one might wonder why we don't just store everything in a StringBuilder. 
 * After all Sequences are mutable, so shouldn't their underlying storage? The reason we don't do this is because the
 * performance of the charAt function is many, many times faster for Strings than for StringBuilders, and we call
 * that method many, many times. 
 * @author brendan
 *
 */
public class StringSequence extends Sequence {

    protected String seq = null;
    
	public StringSequence(String sq, String name) {
        	totSequenceCount++;
            this.name = new String(name.toString());
            seq = sq;
	}
	
	public int length() {
        return seq.length();
    }
	
	/**
     * Return the sequence in String form - just the sequence, no name or listeners or anything
     */
    public String toString() {
        return seq;
    }
    
	/**
	 * Obtain the character at position i in this sequence. This returns a space character if we're beyond the end of the sequence
	 */
	public char at(int i) {
    	if (i >= seq.length()) {
    		char x = ' ';
    		return x;
    	}
    	else
    		return seq.charAt(i);
    }
	
	public void replaceMatchingChars(String lookupSeq, char matchChar) {
    	StringBuilder builder = new StringBuilder();
    	for(int i=0; i<seq.length(); i++) {
    		char base = seq.charAt(i);
    		if (base==matchChar) {
    			base = lookupSeq.charAt(i);
    		}
    		builder.append(base);
    	}
    	seq = builder.toString();
    	fireSequenceChangeEvent(SequenceEventType.DATA_CHANGE);
    }
	
    public Sequence subseq(int begin, int end) {
        return new StringSequence(this.toString().substring(begin, end), name);
    }
	

    public Sequence clone() {
    	Sequence cloned = new StringSequence(seq.substring(0), name.toString().substring(0));
    	if (annotations != null) {
    		for(SequenceAnnotation anno : annotations)
    			cloned.addAnnotation((SequenceAnnotation)anno.clone());
    	}
    	for(SequenceChangeListener listener : seqListeners) {
    		cloned.addSequenceChangeListener(listener);
    	}

    	
    	return cloned;
    }
    
    public Sequence getCols(int[] cols) {
    	StringBuffer buf = new StringBuffer();
    	int i;
    	for(i=0; i<cols.length; i++) {
    		if (cols[i]>=length())
    			buf.append(GAP);
    		else
    			buf.append( this.at(cols[i]));
    	}
    	
    	return new StringSequence(buf.toString(), this.name);
    }
    
    public void append(String newdata) {
    	seq = seq + newdata;
    	fireSequenceChangeEvent(SequenceEventType.LENGTH_CHANGE);
    }
    
    
    public void insert(String newData, int pos, boolean fillWithGaps) {
    	if (pos > seq.length() && ! fillWithGaps) 
    		throw new IllegalArgumentException("Cannot insert sequence at position " + pos + " because sequence has length : " + seq.length());
    	if (pos==0) {
    		seq = newData + seq;
    	}
    	else {
    		StringBuilder strb = new StringBuilder();
    		while (pos > seq.length() && fillWithGaps) {
    			seq = seq + "-";
    		}
    			
    		strb.append( seq.substring(0, pos));
    		strb.append(newData);
    		
    		strb.append( seq.substring(pos));
    		seq = strb.toString();
    	}
    	fireSequenceChangeEvent(SequenceEventType.DATA_CHANGE);
    }
    
    public void removeCols(int[] cols, boolean fireEvent)   {
    	StringBuilder buf = new StringBuilder(seq);

    	Arrays.sort(cols);
    	
    	String empty = "";
    	//Must run from end of columns backward so indices stay the same
    	for(int i=cols.length-1; i>=0; i--) {
    		if (i==0 || cols[i] != cols[i-1])
    			buf.replace(cols[i], cols[i]+1, empty);
    	}

    	seq = buf.toString();
    	if (fireEvent) 
    		fireSequenceChangeEvent(SequenceEventType.LENGTH_CHANGE);
    }
    
    public Sequence getReverseComplement(int start, int end) {
    	StringBuilder newStr = new StringBuilder();
    	
    	for(int i=end-1; i>=start; i--) {
    		char compBase = '-';
    		char theBase = this.at(i);
    		switch(theBase) {
    		case 'A' : compBase = 'T'; break;
    		case 'G' : compBase = 'C'; break;
    		case 'C' : compBase = 'G'; break;
    		case 'T' : compBase = 'A'; break;
    		case 'U' : compBase = 'A'; break;
    		}

    		newStr.append(compBase);
    	}
    	
    	if (newStr.length() != (end-start)) {
    		System.out.println("New seq length is not the same as the usual sequence length, it's: " + newStr.length());
    	}
    	return new StringSequence(newStr.toString(), name);
    }
    
    public Sequence getComplement(int start, int end) {
    	StringBuilder newStr = new StringBuilder();
    	
    	for(int i=start; i<end; i++) {
    		char compBase = '-';
    		char theBase = this.at(i);
    		switch(theBase) {
    		case 'A' : compBase = 'T'; break;
    		case 'G' : compBase = 'C'; break;
    		case 'C' : compBase = 'G'; break;
    		case 'T' : compBase = 'A'; break;
    		case 'U' : compBase = 'A'; break;
    		}

    		newStr.append(compBase);
    	}
    	
    	if (newStr.length() != (end-start)) {
    		System.out.println("New seq length is not the same as the usual sequence length, it's: " + newStr.length());
    	}
    	return new StringSequence(newStr.toString(), name);
    }

    
    
  //Returns the next index in this sequence that is *not* in cols
    private int nextCharacter(int marker, int[] cols) {
    	int huh = Arrays.binarySearch(cols, marker);
    	while( huh >= 0 && marker<length()) {
    		huh = Arrays.binarySearch(cols, marker);
    		marker++;
    	}
    	return marker;
    }
    
    //Returns the next index in this sequence that *is* in cols
    private int nextGap(int marker, int[] cols) {
    	int huh = Arrays.binarySearch(cols, marker);
    	
    	while( huh < 0 && marker<length()) {
    		huh = Arrays.binarySearch(cols, marker);
    		marker++;
    	}
    	return marker;
    }



    
}
