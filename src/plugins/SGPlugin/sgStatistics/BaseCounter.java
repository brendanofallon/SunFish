package plugins.SGPlugin.sgStatistics;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import element.sequence.*;
import errorHandling.ErrorWindow;

/**
 * Base class of statistics that can be computed by looking at an array of frequencies over sites
 * 
 * @author brendan
 *
 */
public abstract class BaseCounter implements SGCalculator {

	SequenceGroup sg;
	double[] freqs;
	double[] sumSquares;
	
	/**
	 * Description of this calculators
	 */
	public abstract String getDescription();
	
	/**
	 * Name of the calculator
	 */
	public abstract String getName();

	/**
	 * Retrieve a new instance of this calculator
	 */
	public abstract SGCalculator getInstance(SequenceGroup sg);
	
	public SequenceGroup getSequenceGroup() {
		return sg;
	}
	
	public double getStandardDeviation() {
		return 0;
	}

	public double getStdevRange(int begin, int offset) {
		return 0;
	}


	/**
	 * Get the value of this statistic over the entire length of the alignment, ignoring partition data
	 */
	public final double getValue() {
		if (freqs == null || freqs.length == 0) {
			//ErrorWindow.showErrorWindow(new NullPointerException("BaseCounter " + getName() + " was not properly initialized"), null);
			return Double.NaN;
		}
		return getValueRange(0, freqs.length);
	}

	
	/**
	 * Return the value of this base counter in the range from begin .. begin+length, ignoring partition data.  
	 */
	public final double getValueRange(int begin, int length) {
		return getValueRange(begin, length, -1.0);
	}
	/**
	 * Get the value of this statistic in a specified range, using only those sites in the 
	 * specified partition.
	 */
	public double getValueRange(int begin, int length, double partitionIndex) {
		if (freqs == null || freqs.length == 0) {
			//ErrorWindow.showErrorWindow(new NullPointerException("BaseCounter " + getName() + " was not properly initialized"), null);
			return Double.NaN;
		}
		
//		double sum = 0;
//		double counted = 0;
//		for(int i=begin; i<(begin+length); i++) {
//			if (partitionIndex<0 || sg.getPartitionNumForSite(i)==partitionIndex) {
//				sum += freqs[i];
//				counted++;
//			}
//		}
//		
////		if (Double.isNaN(sum)) {
////			System.out.println("BaseCounter sum is NaN for calculator " + getName());
////		}
//		return sum/counted;
		Double[] sumCount = getSumAndCount(begin, length, partitionIndex);
		if (sumCount[1]==0)
			return 0;
		
		return sumCount[0] / sumCount[1];
	}

	/**
	 * This works just like getValueRange, in that it computes the sum of the values in the frequencies
	 * array over a the given range. But instead of returning sum / number counted, this returns the two
	 * values separately in the array (sum first, then number counted).
	 * @param begin
	 * @param length
	 * @param partitionIndex
	 * @return
	 */
	public Double[] getSumAndCount(int begin, int length, double partitionIndex) {
		if (freqs == null || freqs.length == 0) {
			//ErrorWindow.showErrorWindow(new NullPointerException("BaseCounter " + getName() + " was not properly initialized"), null);
			return  new Double[]{0.0, Double.NaN};
		}
		
		double sum = 0;
		double counted = 0;
		for(int i=begin; i<(begin+length); i++) {
			if (partitionIndex<0 || sg.getPartitionNumForSite(i)==partitionIndex) {
				sum += freqs[i];
				counted++;
			}
		}
		
		return new Double[]{sum, counted};
	}
	
	public boolean hasStandardDeviation() {
		return false;
	}
	
	public boolean hasRange() {
		return true;
	}

	/**
	 * Retrieve a new window series
	 * @param windowSize
	 * @param stepSize
	 * @return
	 */
	public ArrayList<Point2D> getWindowPointSeries(int windowSize, int stepSize) {
		ArrayList<Point2D> points = new ArrayList<Point2D>();

		for(int i=0; i+windowSize < sg.getMaxSeqLength(); i+=stepSize) {
			points.add(new Point2D.Double(i, getValueRange(i, windowSize)));
		}
		
		return points;
	}
	
	/**
	 * Retrieve a new window series, but only using those sites in the specified partition. This is 
	 * trickier than it should be. We only add a new value if TWO conditions are met:
	 * 1. The central site is in the specified partition. 
	 * 2. At least windowSize / 10 sites in the range are in the partition
	 * 
	 * 
	 * @param windowSize The breadth of the window (in sites) over which to calculate a single value
	 * @param stepSize The amount the window is moved each step
	 * @param partitionIndex The partition to calculate the value for
	 * @return
	 */
	public ArrayList<Point2D> getWindowPointSeries(int windowSize, int stepSize, int partitionIndex) {
		ArrayList<Point2D> points = new ArrayList<Point2D>();

		for(int i=0; i+windowSize < sg.getMaxSeqLength(); i+=stepSize) {
			double xValue = i;
			double yValue = Double.NaN;
			int startSite = Math.max(0, i-windowSize/2);
			if (sg.getPartitionNumForSite(i)==partitionIndex || sg.getPartitionNumForSite(i+1)==partitionIndex || sg.getPartitionNumForSite(i+2)==partitionIndex) {
				
				Double[] sumCount = getSumAndCount(startSite, windowSize, partitionIndex);
				if (sumCount[1] > ((double)windowSize / 10.0))
					yValue = sumCount[0]/sumCount[1];
				else
					yValue = Double.NaN;
			}
			
			points.add(new Point2D.Double(xValue, yValue));
		}
		
		return points;
	}
	
	
	/**
	 * Retrieve a BaseCounterSeries for this calculator using only the data from 
	 * the entire sequence group
	 * 
	 * @param windowSize
	 * @param stepSize
	 * @param partitionIndex
	 * @return A new base counter series with data fromt the entire sequence group
	 */	
	public BaseCounterSeries getWindowSeries(int windowSize, int stepSize) {
		BaseCounterSeries ser;
		ArrayList<Point2D> points = getWindowPointSeries(windowSize, stepSize);
		
		ser = new BaseCounterSeries(points, getName(), this);
		return ser;
	}
	
	/**
	 * Retrieve a BaseCounterSeries for this calculator using only the data in the 
	 * specified partition.
	 * 
	 * @param windowSize
	 * @param stepSize
	 * @param partitionIndex
	 * @return A new base counter series with data reflecting from the specified partition
	 */
	public BaseCounterSeries getWindowSeries(int windowSize, int stepSize, int partitionIndex) {
		BaseCounterSeries ser;
		ArrayList<Point2D> points = getWindowPointSeries(windowSize, stepSize, partitionIndex);
		
		ser = new BaseCounterSeries(points, getName(), this);
		return ser;
	}
	
	
	//A few utilities used by many subclasses
	
	/**
	 * Returns true if there is more than one base in this column
	 * Gap characters (as defined by Sequence.GAP) don't count one way or the other. All gaps in this colum return false
	 */
	public boolean isPolymorphic(int site) {
		if (sg.size()==0)
			return false;
		
		char base = sg.get(0).at(site);
		int seqNum = 0;
		while (seqNum < sg.size() && charIsGap(base) ) {
			base = sg.get(seqNum).at(site);
			seqNum++;
		}
		
		//Every base is a gap in this column, I guess we return false here
		if (seqNum == sg.size()) {
			return false;
		}
		
		boolean same = true;
		for(int i=seqNum; i<sg.size() && same; i++) {
			char compBase = sg.get(i).at(site);
			if ((!charIsGap(compBase)) && compBase!=base)
				same = false;
		}
		return !same;
	}
	
	/**
	 * Returns true if the given base is a gap
	 * @param base
	 * @return
	 */
	public static boolean charIsGap(char base) {
		return (base == Sequence.GAP) || (base == Sequence.UNKNOWN);
	}
	
	/**
	 * Returns true if any sequence has a gap at this site
	 */
	public boolean hasGap(int site) {
		for(int i=0; i<sg.size(); i++)
			if (sg.get(i).isGap(site) )
				return true;
		
		return false;
	}
	
	/**
	 * Returns an array of the base frequencies for this column, in order A C T G
	 * @param colNum
	 * @return
	 */
	public double[] getColumnBaseFreqs(int colNum) {
		double[] counts = getColumnBaseCounts(colNum);

		double counted = 0;
		for(int i=0; i<counts.length; i++)
			counted += counts[i];

		for(int i=0; i<counts.length; i++) {
			counts[i] /= counted;
		}

		return counts;
	}

	
	/**
	 * Returns an array of the base frequencies for this column, in order A C T G
	 * @param colNum
	 * @return
	 */
	public double[] getColumnBaseCounts(int colNum) {
		double[] freqs = new double[4];
		char[] col = sg.getColumn(colNum);
		double counted = 0;
		for(int i=0; i<col.length; i++)
			switch (col[i]) {
				case 'A' :
					freqs[0]++;
					counted++;
					break;
				case 'C' : 
					freqs[1]++;
					counted++;
					break;
				case 'T' : 
					freqs[2]++;
					counted++;
					break;
				case 'G' : 
					freqs[3]++;
					counted++;
					break;
				default:
					counted++;
					break;
			}

		return freqs;
	}
	
}
