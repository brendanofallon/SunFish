package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

/**
 * I guess you can consider this a base counter type thing...
 * @author brendan
 *
 */
public class NumGappedSites extends BaseCounter{

	public NumGappedSites() {};
	
	private NumGappedSites(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new NumGappedSites(sg);
	}

	public String getDescription() {
		return "Gapped sites";
	}


	public String getName() {
		return SGStatisticsRegistry.NUM_GAPS;
	}
	
//	public double getValueRange(int begin, int length) {
//		if (freqs == null || freqs.length == 0) {
//			System.out.println(" Frequency array seems not to have been initialized.. returning NaN from valueRange");
//			return Double.NaN;
//		}
//		
//		double sum = 0;
//		for(int i=begin; i<(begin+length); i++) {
//			sum += freqs[i];
//		}
//		
//		return sum;
//	}
	
	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			if (sg.hasGap(i))
				freqs[i] = 1;
			else
				freqs[i] = 0;
		}
	}

}
