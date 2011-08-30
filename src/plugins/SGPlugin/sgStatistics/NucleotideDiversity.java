package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class NucleotideDiversity extends BaseCounter {

	
	public NucleotideDiversity() {};
	
	private NucleotideDiversity(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new NucleotideDiversity(sg);
	}

	public String getDescription() {
		return "Nucleotide diversity (pi)";
	}

	public String getName() {
		return SGStatisticsRegistry.NUC_DIVERSITY;
	}

	private void buildFreqsArray() {
		freqs = new double[sg.getMaxSeqLength()];
		for(int i=0; i<freqs.length; i++) {
			double[] baseFreqs = getColumnBaseCounts(i);
			int count = 0;
			for(int j=0; j<baseFreqs.length; j++)
				count += baseFreqs[j];
			
			for(int j=0; j<baseFreqs.length; j++) {
				baseFreqs[j] = baseFreqs[j]/count * (baseFreqs[j]-1)/(count-1);
			}
			
			freqs[i] = 1.0-baseFreqs[0]-baseFreqs[1]-baseFreqs[2]-baseFreqs[3];
		}
	}
	



}
