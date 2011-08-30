/*
 * Contains items that have a presence in the browser and can be visualized in
 * the display panel.
 */

package element.sequence;
import java.util.*;


import element.sequence.Sequence;
import element.sequence.SequenceChangeListener.SequenceEventType;
/**
 * A string of characters that can be interpreted as a DNA or RNA sequence. This should probably be rehashed
 * at some point - it's weird to have everything be a String. An array of Bases may make more sense. At some
 * point we also need move to a derived structure to provide RNA and protein functionality. 
 * @author brendan
 */
public abstract class Sequence {

		protected static int totSequenceCount = 0;
		public final int myNumber = totSequenceCount;
	
		public static final char GAP = '-';
		
		public static final char UNKNOWN = '?';
	
        protected String name;
        
        protected List<SequenceChangeListener> seqListeners = new ArrayList<SequenceChangeListener>();

        protected List<SequenceAnnotation> annotations;

        /**
         * Add a new annotation to the list of annotations for this sequence
         * @param anno
         */
        public void addAnnotation(SequenceAnnotation anno) {
        	if (annotations == null)
        		annotations = new ArrayList<SequenceAnnotation>();
        	
        	annotations.add(anno);
        }
        
        public void addAnnotations(List<SequenceAnnotation> annos) {
        	if (annotations == null)
        		annotations = new ArrayList<SequenceAnnotation>();
        	
        	for(SequenceAnnotation anno : annos)
        		annotations.add(anno);
        }
        
        public void removeAnnotation(SequenceAnnotation anno) {
        	if (annotations == null)
        		return;
        	
        	annotations.remove(anno);
        }
        
        /**
         * Returns true if there is an annotation of the specified type anywhere in this sequence
         * @param type
         * @return
         */
        public boolean hasAnnotationOfType(String type) {
        	if (annotations == null)
        		return false;
        	
        	for(SequenceAnnotation anno : annotations) {
        		if (anno.getType().equals(type))
        			return true;
        	}
        	return false;
        }
        
        /**
         * Removes all annotations whose type.equals(type)
         * @param type
         */
        public void removeAnnotationByType(String type) {
        	if (annotations == null)
        		return;
        	
        	List<SequenceAnnotation> toRemove = new ArrayList<SequenceAnnotation>();
        	for(SequenceAnnotation anno : annotations) {
        		if (anno.getType().equals(type))
        			toRemove.add(anno);
        	}
        	
        	for(SequenceAnnotation removeMe : toRemove) {
        		annotations.remove(removeMe);
        	}
        }
        
        /**
         * Returns true if there's an annotation of type type at site site
         * @param type
         * @param site
         * @return
         */
        public boolean hasAnnotationAtSite(String type, int site) {
        	if (annotations == null)
        		return false;
        	
        	for(SequenceAnnotation anno : annotations) {
        		if (anno.getType().equals(type) && anno.contains(site))
        			return true;
        	}
        	return false;
        }

        
        /**
         * Returns the first annotation encountered at this site of the given type, or null if there's no such annotation
         * @param type
         * @param site
         * @return
         */
        public SequenceAnnotation getAnnotationAtSite(String type, int site) {
        	if (annotations == null)
        		return null;
        	
        	for(SequenceAnnotation anno : annotations) {
        		if (anno.getType().equals(type) && anno.contains(site))
        			return anno;
        	}
        	return null;
        }
        
        /**
         * Returns all annotations whose type matches the given type
         * @param type Type of annotation to retrieve
         * @return a list of all matching annotations
         */
        public List<SequenceAnnotation> getAnnotationsByType(String type) {
        	List<SequenceAnnotation> annos = new ArrayList<SequenceAnnotation>(5);
        	if (annotations == null)
        		return annos;
        	
        	
        	for(SequenceAnnotation anno : annotations) {
        		if (anno.getType().equals(type))
        			annos.add(anno);
        	}
        	return annos;
        }
        
        /**
         * Add a listener to the list of things to be notified of sequence changes. 
         * @param listener
         */
        public void addSequenceChangeListener(SequenceChangeListener listener) {
        	seqListeners.add(listener);
        }
        
        /**
         * Remove the listener from the list. 
         * @param listener
         * @return True if the listener was in the list of sequence change listeners
         */
        public boolean removeSequenceChangeListener(SequenceChangeListener listener) {
        	return seqListeners.remove(listener);
        }
        
        /**
         * Remove all sequence change listeners
         */
        public void removeSequenceChangeListeners() {
        	seqListeners.clear();
        }
        
        /**
         * Notify all interested parties that this sequence has changed.
         * @param type
         */
        public void fireSequenceChangeEvent(SequenceEventType type) {
        	for(SequenceChangeListener listener : seqListeners) {
        		//System.out.println("Sequence " + getName() + " is firing change event to : " + listener);
        		listener.sequenceChanged(this, type);
        	}
        }
        
        /**
         * Return the number of characters in the sequence
         * @return
         */
        public abstract int length();
         

        /**
         * Retrieve the character at position i in this sequence, or a space character if we're beyond the end of the sequence
         * @param i
         * @return
         */
        public abstract char at(int i);

        
        /**
         * Replaces all bases matching 'matchChar' with whatever is at that position in lookupSeq
         * Useful for getting real bases when we read in things from nexus or phylip files with lots
         * of periods
         * @param lookupSeq
         * @param matchChar
         */
        public abstract void replaceMatchingChars(String lookupSeq, char matchChar);

        /**
         * Returns a new sequence whose seq string is the substring starting at site begin and ending at site end-1.
         * @param begin Start site
         * @param end One position after the last site to include
         * @return A new sequence that is a subsequence of this seq
         */
        public abstract Sequence subseq(int begin, int end); 

        public String getName() {
            return name;
        }
        
        /**
         * Returns true if this sequence is a gap character at this site
         * @param site
         * @return
         */
        public boolean isGap(int site) {
        	if (site >= length())
        		return false;
        	else 
        		return (this.at(site)==' ' || this.at(site)==GAP);
        }

        /**
         * Returns true if the site at the given index is equal to the
         * UNKNOWN character, which is currently '?'
         * @param site
         * @return
         */
        public boolean isUnknown(int site) {
        	if (site >= length())
        		return false;
        	else 
        		return (this.at(site)==UNKNOWN);
        }
        
        
        public void setName(String newName) {
            this.name = newName;
            fireSequenceChangeEvent(SequenceEventType.NAME_CHANGE);
        }
        
   
        
        /**
         * Obtain an array of chars corresponding to the sub-sequence from begin..end (end exclusive)
         * @param begin
         * @param end
         * @return
         */
        public char[] toCharArray(int begin, int end) {
        	char[] chars = new char[end-begin];
        	for(int i=begin; i<end; i++) {
        		chars[i-begin] = this.at(i);
        	}
        	return chars;
        }
        
        /**
         * Returns a complete copy of this Sequence, with name, annotations, and seq. listeners
         * preserved
         */
        public abstract Sequence clone();
        
        /**
         * Returns the characters in this sequence at the columns given in the cols[] argument.
         * If a column is specified that is beyond the end of the sequence, a gap is returned.
         * @param cols
         * @return
         */
        public abstract Sequence getCols(int[] cols);
        
        
        
        /**
         * Adds the new sequence data to the end of this sequence, and fires a sequence change event
         * @param newdata
         */
        public abstract void append(String newdata);
        
        /**
         * Insert the given sequence data into this sequence at the given position. Pos indicates the
         * number of bases preceding the newly inserted data, such that 0 means insert at the beginning, etc. 
         * fillWithGaps indicates if, when given a pos > this.length(), we should add a bunch of gaps and the insert 
         * @param newData
         * @param pos
         * @param fillWithGaps
         */
        public abstract void insert(String newData, int pos, boolean fillWithGaps);
        
        
        /**
         * Remove the specified columns (sites) from the sequence, and 
         * fire a sequence length change event. 
         * @param cols List of site numbers to be removed
         */
        public void removeCols(int[] cols) {
        	removeCols(cols, true);
        }
        
        /**
         * Remove the specified columns from the sequence and fire an event only if
         * the boolean variable is true.
         * @param cols
         * @param fireEvent
         */
        public abstract void removeCols(int[] cols, boolean fireEvent);

        
        /**
         * Return a new Sequence that is the reverse complement of this sequence. This currently does not 
         * maintain any annotations or other properties of this sequence. 
         * @return
         */
        public Sequence getReverseComplement() {
        	return getReverseComplement(0, length());
        }
        
        /**
         * Return a new Sequence that is the reverse complement of this sequence, beginning at site i in
         * the forward direction. This currently does not maintain any annotations or other properties 
         * of this sequence. 
         * @return A new sequence that is the reverse complement of this sequence
         */
        public abstract Sequence getReverseComplement(int start, int end);        
        /**
         * Return a new Sequence that is the forward complement of this sequence, beginning at site i in
         * the forward direction. This does not reverse the direction of the strand. 
         * @return A new sequence that is the forward complement of this sequence
         */
       public abstract Sequence getComplement(int start, int end);		
}
