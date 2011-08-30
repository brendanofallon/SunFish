package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class SequenceLength implements SGCalculator {

	SequenceGroup sg;
	
	public SequenceLength() { }
	
	public SequenceLength(SequenceGroup sg) {
		this.sg = sg;
	}
	
	public String getDescription() {
		return "Alignment length";
	}


	public SGCalculator getInstance(SequenceGroup sg) {
		return new SequenceLength(sg);
	}


	public String getName() {
		return SGStatisticsRegistry.SEQ_LENGTH;
	}


	public double getStandardDeviation() {
		return 0;
	}


	public double getStdevRange(int begin, int offset) {
		return 0;
	}


	public double getValue() {
		return sg.getMaxSeqLength();
	}


	public double getValueRange(int begin, int offset) {
		return sg.getMaxSeqLength();
	}


	public boolean hasRange() {
		return false;
	}


	public boolean hasStandardDeviation() {
		return false;
	}
	
}
