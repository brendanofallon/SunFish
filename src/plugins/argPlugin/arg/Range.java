package plugins.argPlugin.arg;

/**
 * A half-open range of values, which includes values form min...(max-1). These are immutable.
 * 
 * Ranges typically describe the set of sites between two recombination breakpoints. If sites are
 * numbered from 0..9, and breakpoints occur between 2-3 and 7-8, then three ranges exist. Since
 * ranges are half-open the range min and maxes will be 0..3, 3..8, and 8..9.
 * Similarly, if there's a breakpoint between 0..1, the first range will have min 0 and max 1.
 *  
 * @author brendan
 *
 */
public class Range {

	protected int min;
	protected int max;
	
	public Range(int min, int max) {
		if (max<=min)
			throw new IllegalArgumentException("Range max must be strictly greater than min (got min=" + min + " max=" + max + ")");
		this.min= min;
		this.max = max;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public int getLength() {
		return max-min;
	}
	
	public boolean contains(int x) {
		if (x>=min && x<max)
			return true;
		return false;
	}
	
	
	public String toString() {
		return "Range (" + getMin() + " .. " + getMax() + ")";
	}
}
