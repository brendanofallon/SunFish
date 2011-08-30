package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class PyrimidineFreq extends BaseCounter {
	
	public PyrimidineFreq() {};
	
	private PyrimidineFreq(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new PyrimidineFreq(sg);
	}

	public String getDescription() {
		return "Frequency of pyrimidines (T+C)";
	}

	public String getName() {
		return SGStatisticsRegistry.PYRFREQ;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			
			char[] col = sg.getColumn(i);
			double counted = 0;
			double gs = 0;
			for(int j=0; j<col.length; j++) {
				if (col[j]=='C' || col[j]=='T')
					gs++;
				if (col[j]!='-') {
					counted++;
				}
			}
			freqs[i] = gs/counted;
			if (Double.isNaN(freqs[i]))
				freqs[i] = 0;
		}
	}

}
