package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class GCFreq extends BaseCounter {

	
	public GCFreq() {};
	
	private GCFreq(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public GCFreq getInstance(SequenceGroup sg) {
		return new GCFreq(sg);
	}

	public String getDescription() {
		return "Frequency of guanine and cytosine";
	}

	public String getName() {
		return SGStatisticsRegistry.GCFREQ;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			
			char[] col = sg.getColumn(i);
			double counted = 0;
			double gs = 0;
			for(int j=0; j<col.length; j++) {
				if (col[j]=='G' || col[j] == 'C')
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
