package plugins.SGPlugin.sgStatistics;

import java.util.ArrayList;

import element.sequence.*;

/**
 * A calculator that just returns the current number of sequences. 
 * @author brendan
 *
 */
public class NumSequences implements SGCalculator {

		SequenceGroup sg;
	
		public NumSequences() { }
		
		public NumSequences(SequenceGroup sg) {
			this.sg = sg;
		}
		
		public String getDescription() {
			return "Number of sequences";
		}


		public SGCalculator getInstance(SequenceGroup sg) {
			return new NumSequences(sg);
		}


		public String getName() {
			return SGStatisticsRegistry.NUMSEQS;
		}


		public double getStandardDeviation() {
			return 0;
		}


		public double getStdevRange(int begin, int offset) {
			return 0;
		}


		public double getValue() {
			return sg.size();
		}


		public double getValueRange(int begin, int offset) {
			return sg.size();
		}


		public boolean hasRange() {
			return false;
		}


		public boolean hasStandardDeviation() {
			return false;
		}
	
}
