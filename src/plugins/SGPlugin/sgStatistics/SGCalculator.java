package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

/**
 * Interface of all things that calculate a specific statistic from a sequence group
 * @author brendan
 *
 */
public interface SGCalculator {

	/**
	 * A short name for this statistic, e.g. Nucleotide diversity
	 * @return
	 */
	public String getName();
	
	/**
	 * A descriptive phrase for this statistic
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Returns a new instance of the statistic associated with the given sequence group 
	 * @param sg
	 * @return
	 */
	public SGCalculator getInstance(SequenceGroup sg);
	
	
	/**
	 * Return the value of this statistic taken over the entire sequence group
	 * @return
	 */
	public double getValue();
	
	/**
	 * Return the value of this statistic computed over a range of sites. 
	 * @param begin
	 * @param offset
	 * @return
	 */
	public double getValueRange(int begin, int offset);
	
	public boolean hasStandardDeviation();
	
	public double getStandardDeviation();
	
	public double getStdevRange(int begin, int offset);
	
	public boolean hasRange();
	
}
