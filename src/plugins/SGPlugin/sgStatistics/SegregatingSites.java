package plugins.SGPlugin.sgStatistics;

import element.sequence.*;
import errorHandling.ErrorWindow;

public class SegregatingSites extends BaseCounter {

	
	public SegregatingSites() {};
	
	private SegregatingSites(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}

	public SGCalculator getInstance(SequenceGroup sg) {
		return new SegregatingSites(sg);
	}
	
	public String getDescription() {
		return "Fraction of sites segregating";
	}

	public String getName() {
		return SGStatisticsRegistry.SEG_SITES;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			if (isPolymorphic(i))
				freqs[i] = 1.0;
			else
				freqs[i] = 0.0;
		}
	}
	
	public double getValueRange(int begin, int length, double partitionIndex) {
		if (freqs == null || freqs.length == 0) {
			ErrorWindow.showErrorWindow(new NullPointerException("BaseCounter " + getName() + " was not properly initialized"), null);
			return Double.NaN;
		}
		
		double sum = 0;
		for(int i=begin; i<(begin+length); i++) {
			if (partitionIndex<0 || sg.getPartitionNumForSite(i)==partitionIndex) {
				sum += freqs[i];
			}
		}
		return sum;
	}


}
