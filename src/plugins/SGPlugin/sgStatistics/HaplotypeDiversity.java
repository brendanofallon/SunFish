package plugins.SGPlugin.sgStatistics;

import java.util.ArrayList;
import element.sequence.*;

public class HaplotypeDiversity implements SGCalculator {

	double value;

	public HaplotypeDiversity() { }
	
	public HaplotypeDiversity(SequenceGroup sg) {
		if (sg.size()>1) {
			ArrayList<Double> hapFreqs = new ArrayList<Double>();
			ArrayList<String> seqs = new ArrayList<String>();	

			for(int i=0; i<sg.size(); i++) {
				int index = seqs.indexOf(sg.get(i).toString());
				if (index>=0) {
					hapFreqs.set(index, hapFreqs.get(index)+1);
					
				}
				else {
					seqs.add(sg.get(i).toString());
					hapFreqs.add(new Double(1));
				}
			}

//			System.out.println("Hap Freqs ");
//			for(Double d : hapFreqs) {
//				System.out.println(d);
//			}
				
				
			double sum = 1.0;
			double tot = sg.size();
			for(int i=0; i<hapFreqs.size(); i++)
				sum -= hapFreqs.get(i)*hapFreqs.get(i)/tot/tot;

			value = sg.size()/(sg.size()-1)*sum;
		}
		else {
			value = 0;
		}
	}
	
	public String getDescription() {
		return "Haplotype diversity";
	}


	public SGCalculator getInstance(SequenceGroup sg) {
		return new HaplotypeDiversity(sg);
	}


	public String getName() {
		return SGStatisticsRegistry.HAP_DIVERSITY;
	}


	public double getStandardDeviation() {
		return 0;
	}


	public double getStdevRange(int begin, int offset) {
		return 0;
	}


	public double getValue() {
		return value ;
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
