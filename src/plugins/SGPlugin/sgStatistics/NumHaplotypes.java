package plugins.SGPlugin.sgStatistics;

import java.util.ArrayList;

import element.sequence.SequenceGroup;

public class NumHaplotypes implements SGCalculator {

	SequenceGroup sg;
	int num;
	
	public NumHaplotypes() {
	}
	
	public NumHaplotypes(SequenceGroup seqs) {
		this.sg = seqs;
		num = 0;
		if (sg == null || sg.size()==0) {
			num = 0;
			return;
		}
		else 
			if (sg.size()==1) {
			num = 1;
			return;
		}
			

		ArrayList<String> types = new ArrayList<String>();
		types.add(sg.get(0).toString());
		num = 1;  //Be sure to count the first haplotype
		for(int i=0; i<sg.size(); i++) {
			if (! types.contains(sg.get(i).toString())) {
				types.add(sg.get(i).toString());
				num++;
			}
		} 
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new NumHaplotypes(sg);
	}
	
	public String getDescription() {
		return "Number of haplotypes";	
	}

	public String getName() {
		return SGStatisticsRegistry.NUM_HAPS;	
	}


	public double getStandardDeviation() {
		return 0;
	}


	public double getStdevRange(int begin, int offset) {
		return 0;
	}

	public double getValue() {
		return num;
	}


	public double getValueRange(int begin, int offset) {
		return 0;
	}


	public boolean hasStandardDeviation() {
		return false;
	}
	
	public boolean hasRange() {
		return false;
	}


}
