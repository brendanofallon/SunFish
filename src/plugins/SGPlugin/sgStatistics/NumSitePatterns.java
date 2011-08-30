package plugins.SGPlugin.sgStatistics;

import java.util.ArrayList;
import java.util.Hashtable;

import element.sequence.*;

public class NumSitePatterns implements SGCalculator {

	ArrayList<String> patterns;
	SequenceGroup sg;
	
	public NumSitePatterns() { }
	
	public NumSitePatterns(SequenceGroup sg) {
		this.sg = sg;
		buildHash();
	}
	
	
	private void buildHash() {
		patterns = new ArrayList<String>();
		
		for(int i=0; i<sg.getMinSeqLength(); i++) {
			String str = new String(sg.getColumn(i));
			if (! patterns.contains(str)) {
				patterns.add(str);
			}
		}
	}
	
	public String getDescription() {
		return "Number of unique site patterns";
	}


	public SGCalculator getInstance(SequenceGroup sg) {
		return new NumSitePatterns(sg);
	}


	public String getName() {
		return SGStatisticsRegistry.NUM_SITEPATTERNS;
	}


	public double getStandardDeviation() {
		return 0;
	}

	public double getStdevRange(int begin, int offset) {
		return 0;
	}


	public double getValue() {
		return patterns.size();
	}

	public double getValueRange(int begin, int offset) {
		return 0;
	}


	public boolean hasRange() {
		return false;
	}


	public boolean hasStandardDeviation() {
		return false;
	}
	
}
