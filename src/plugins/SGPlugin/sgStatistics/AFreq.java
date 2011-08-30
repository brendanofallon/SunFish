package plugins.SGPlugin.sgStatistics;


import element.sequence.*;

public class AFreq extends BaseCounter {
	
	public AFreq() {};
	
	private AFreq(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new AFreq(sg);
	}

	public String getDescription() {
		return "Frequency of Adenine";
	}

	public String getName() {
		return SGStatisticsRegistry.AFREQ;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			
			char[] col = sg.getColumn(i);
			double counted = 0;
			double gs = 0;
			for(int j=0; j<col.length; j++) {
				if (col[j]=='A')
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

} //class AFreq

