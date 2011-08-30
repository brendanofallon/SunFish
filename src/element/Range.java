package element;

import java.util.ArrayList;
import java.util.List;


/**
 * Specifies a potentially piecewise range over which something exists. 
 * Currently used by SequenceAttributes to specify where in a sequence a particular
 * attribute is. Uses longs in case we get huge values.
 * Boundaries here are INCLUSIVE. 
 * 
 * 
 * @author brendan
 *
 */
public class Range {

	public static final long BEGINNING = Long.MIN_VALUE;
	public static final long END = Long.MAX_VALUE;
	
	ArrayList<SingleRange> ranges;
	
	public Range(int min, int max) {
		ranges = new ArrayList<SingleRange>();
		ranges.add(new SingleRange(min, max));
	}
	
	public Range(long min, long max) {
		ranges = new ArrayList<SingleRange>();
		ranges.add(new SingleRange(min, max));
	}
	
	public void addRange(long min, long max) {
		ranges.add(new SingleRange(min, max));
	}
	
	/**
	 * Reverse all single ranges, computes their values as length-oldVal
	 * @param length
	 */
	public void reverse(int length) {
		for(SingleRange range : ranges) {
			long oldMin = range.min;
			range.min = length-range.max-1;
			range.max = length-oldMin-1;
		}
	}
	
	/**
	 * Returns total number of sites in this range
	 * @return
	 */
	public int getLength() {
		int l =0;
		for(SingleRange r : ranges) 
			l += r.max - r.min;
		return l;
	}
	
	/**
	 * Returns the lowest value in the range
	 * @return
	 */
	public long getMinimum() {
		long min = ranges.get(0).min;
		for(SingleRange range : ranges) {
			if (range.min < min)
				min = range.min;
		}
		return min;
	}
	
	public long getMaximum() {
		long max = ranges.get(0).max;
		for(SingleRange range : ranges) {
			if (range.max > max)
				max = range.max;
		}
		return max;
	}
	
	public boolean hasMultipleRange() {
		return (ranges.size()>1);
	}
	/**
	 * Return true if any singlerange contains this value (
	 * @param val
	 * @return
	 */
	public boolean contains(long val) {
		for(SingleRange range : ranges) {
			if (val >= range.min && val<= range.max) {
				return true;
			}
		}
		return false;
	}
	
	public List<SingleRange> getSingleRanges() {
		return ranges;
	}
	
	public class SingleRange {
		public long min;
		public long max;
		
		public SingleRange(long min, long max) throws IllegalArgumentException {
			this.min = min;
			this.max = max;
			if (min>max) {
				throw new IllegalArgumentException("The minimum value in range must be less than or equal to the maximum value");
			}
		}
	}
}
