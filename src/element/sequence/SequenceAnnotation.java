package element.sequence;

import java.util.List;

import element.Range;
import element.Range.SingleRange;


/**
 * A single attribute of a sequence. Right now attributes must have at least two things, a 'type' and
 * a Range. A type is something like CDS or protein_id. The range defines exactly where in this sequence
 * the type is (for instance, a CDS may be between bases a..b). 
 * Optionally, a type may be associated with additional information, such as a value for the protein_id, or
 * a lookup value for a CDS. However, this is not required.
 * 
 * Annotations may also be specified with a particular Direction (specified by an enum), which is
 * forward, backward, both, or unknown.  
 * 
 *  
 * @author brendan
 *
 */
public class SequenceAnnotation {
	
	public enum Direction {Forward, Reverse, Unknown, Both};
	
	Sequence owner;
	Range range;
	
	String attrType; //The type of attribute, for instance CDS or GI or protein_id
	String attrInfo;
	Direction direction = Direction.Unknown;	//If 'forward', the feature is assumed to be on the strand given by the sequence, reverse otw.
	
	
	public SequenceAnnotation(Sequence owner, String type, Range range) {
		this.owner = owner;
		this.range = range;
		attrType = type;
		attrInfo = null;
	}
	
	public SequenceAnnotation(Sequence owner, String type, int startSite, int endSite) {
		this.owner = owner;
		this.range = new Range(startSite, endSite);
		attrType = type;
		attrInfo = null;
	}
	
	public SequenceAnnotation(Sequence owner, String type, String attrInfo, Range range) {
		this.owner = owner;
		this.range = range;
		attrType = type;
		this.attrInfo = attrInfo;
	}
	
	public SequenceAnnotation clone() {
		SequenceAnnotation clone = new SequenceAnnotation(owner, attrType, attrInfo, range);
		return clone;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction newDir) {
		direction = newDir;
	}
	
	/**
	 * Reverse the orientation of the ranges in this annotation, useful if we want to get these coordinates in reverse-complement space
	 * @param length
	 */
	public void reverseRange(int length) {
		range.reverse(length);
	}
	
	public String getType() {
		return attrType;
	}
	
	public boolean hasInfo() {
		return (attrInfo != null);
	}
	
	public String getInfo() {
		return attrInfo;
	}
	
	public Range getRange() {
		return range;
	}
	
	public boolean contains(long where) {
		return range.contains(where);
	}
	
	/**
	 * Returns true if two singles ranges overlap at all
	 * @param one
	 * @param two
	 * @return
	 */
	private boolean rangesOverlap(SingleRange one, SingleRange two) {
		if (one.min < two.min && one.max > two.min) {
			return true;
		}
		if (two.min < one.min && two.max > one.min) {
			return true;
		}
		if (one.min >= two.min && one.max < two.max) {
			return true;
		}
		if (one.min < two.max && one.max > two.min) {
			return true;
		}
		if (two.min < one.max && two.max > one.min) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the intervals given by min1..max1 and min2..max2 overlap 
	 * @param min1 start of interval 1
	 * @param max1 end of interval 1
	 * @param min2 start of interval 2
	 * @param max2 end of interval 2
	 * @return true if the intervals overlap
	 */
	private boolean intervalsOverlap(long min1, long max1, long min2, long max2) {
		if (min1 < min2 && max1 > min2) {
			return true;
		}
		if (min2 < min1 && max2 > min1) {
			return true;
		}
		if (min1 >= min2 && max1 < max2) {
			return true;
		}
		if (min1 < max2 && max1 > min2) {
			return true;
		}
		if (min2 < max1 && max2 > min1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if any site belongs to both this and the supplied annotation
	 * @param other
	 * @return
	 */
	public boolean overlaps(SequenceAnnotation other) {
		List<SingleRange> theseRanges = getRanges();
		List<SingleRange> thoseRanges = other.getRanges();
		for(int i=0; i<theseRanges.size(); i++) {
			for(int j=i; j<thoseRanges.size(); j++) {
				if ( rangesOverlap(theseRanges.get(i), thoseRanges.get(j)) )
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if any site in this orf is in the interval start (inclusive) .. end (exclusive)
	 * @param other
	 * @return
	 */
	public boolean overlaps(int start, int end) {
		List<SingleRange> theseRanges = getRanges();
		
		for(int i=0; i<theseRanges.size(); i++) {
				if ( intervalsOverlap(theseRanges.get(i).min, theseRanges.get(i).max, start, end) )
					return true;
			
		}
		return false;
	}
	
	public List<SingleRange> getRanges() {
		return range.getSingleRanges();
	}
	
	/**
	 * Returns the total number of sites in this annotation
	 * @return
	 */
	public int getLength() {
		return range.getLength();
	}
	
	public long getStart() {
		return range.getMinimum();
	}
	
	public long getEnd() {
		return range.getMaximum();
	}

}
