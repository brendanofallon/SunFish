package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public 	class ATFreq extends BaseCounter {

	public ATFreq() {};
	
	public ATFreq(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public ATFreq getInstance(SequenceGroup sg) {
		return new ATFreq(sg);
	}

	public String getDescription() {
		return "Frequency of adenine and thymine";
	}

	public String getName() {
		return SGStatisticsRegistry.ATFREQ;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			
			char[] col = sg.getColumn(i);
			double counted = 0;
			double gs = 0;
			for(int j=0; j<col.length; j++) {
				if (col[j]=='A' || col[j]=='T')
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
