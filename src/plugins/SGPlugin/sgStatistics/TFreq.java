package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class TFreq extends BaseCounter {
	
	public TFreq() {};
	
	private TFreq(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new TFreq(sg);
	}

	public String getDescription() {
		return "Frequency of Thymine";
	}

	public String getName() {
		return SGStatisticsRegistry.TFREQ;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			
			char[] col = sg.getColumn(i);
			double counted = 0;
			double gs = 0;
			for(int j=0; j<col.length; j++) {
				if (col[j]=='T')
					gs++;
				if (col[j]!=Sequence.GAP) {
					counted++;
				}
			}
			
			if (counted == 0)
				freqs[i] = 0;
			else
				freqs[i] = gs/counted;
		}
	}
}
