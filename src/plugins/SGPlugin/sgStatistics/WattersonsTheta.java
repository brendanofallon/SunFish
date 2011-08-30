package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class WattersonsTheta extends BaseCounter {

	public WattersonsTheta() {};
	
	private WattersonsTheta(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public String getDescription() {
		return "Watterson's theta";
	}

	public SGCalculator getInstance(SequenceGroup sg) {
		return new WattersonsTheta(sg);
	}

	public String getName() {
		return SGStatisticsRegistry.THETAW;
	}
	
	public final double getValueRange(int begin, int length, double partitionIndex) {
		double factor = 0;
		for(int i=1; i<sg.size(); i++) {
			factor += 1.0/(double)i;
		}
		
		double sum = 0;
		double counted = 0;
		for(int i=begin; i<(begin+length); i++) {
			if (partitionIndex<0 || sg.getPartitionNumForSite(i)==partitionIndex) {
				sum += freqs[i];
				counted++;
			}
		}
		return sum/factor/counted;
	}
	
	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			if (isPolymorphic(i))
				freqs[i] = 1.0;
			else
				freqs[i] = 0;
		}
	}


}
