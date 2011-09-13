package plugins.SGPlugin.sgStatistics;

import element.sequence.*;

public class TajimasD extends BaseCounter {

	int[] thetaWArr;
	double[] piArr;
	
	public TajimasD() {};
	
	private TajimasD(SequenceGroup sg) {
		this.sg = sg;
		buildFreqsArray();
	}
	
	public SGCalculator getInstance(SequenceGroup sg) {
		return new TajimasD(sg);
	}
	
	public String getDescription() {
		return "Tajima's D";
	}


	public String getName() {		
		return SGStatisticsRegistry.TAJIMASD;
	}

	
	/**
	 * We don't really use the normal getSumAndCount here because we keep track of two frequency arrays.
	 * This is confusing because Tajima's D isn't an average over sites, although it can be computed for a 
	 * range. 
	 */
	@Override public Double[] getSumAndCount(int begin, int length, double partitionIndex) {
		double a1 = 0;
		double a2 = 0;
		for(int i=1; i<sg.size(); i++) {
			a1 += 1/(double)i;
			a2 += 1/(double)(i*i);
		}
		
		double sSum = 0;
		double piSum = 0;
		double count = 0;
		for(int i=begin; i<(begin+length); i++) {
			if (partitionIndex<0 || sg.getPartitionNumForSite(i)==partitionIndex) {
				if ( ! Double.isNaN(piArr[i])) {
					sSum += thetaWArr[i];
					piSum += piArr[i];
					count++;
				}
			}
		}
		
		double b1 = (sg.size()+1.0)/(3.0*(sg.size()-1.0));
		double b2 = 2.0*(sg.size()*sg.size() + sg.size() + 3.0)/(9.0*sg.size()*(sg.size()-1.0));
		double c1 = b1 - 1.0/a1;
		double c2 = b2 - (sg.size()+2.0)/(a1*sg.size()) + a2/(a1*a1);
	
		double e1 = c1/a1;
		double e2 = c2/(a1*a1 + a2);
		
		double dif;
		
		if (sSum == 0) {
			dif = 0;
		}
		else {
			dif = piSum-sSum/a1;
			dif /= Math.sqrt(e1*sSum + e2*sSum*(sSum-1) );
		}
		
		//Yes, we multiply the dif*count for the first element since getValueRange(...) will divide the first
		//by the second element to get a value. 
		return new Double[]{dif*count, count};
	}
	
	
	
	
	/**
	 * Currently this treats sites with gaps as non-polymorphic sites... but really we should ignore them entirely
	 */
	private void buildFreqsArray() {
		thetaWArr = new int[sg.getMaxSeqLength()];
		piArr = new double[sg.getMaxSeqLength()];
		int numGaps = 0;
		for(int i=0; i<thetaWArr.length; i++) {
			if ((!hasGap(i)) && isPolymorphic(i))
				thetaWArr[i] = 1;
			else
				thetaWArr[i] = 0;
		}
		
		for(int i=0; i<thetaWArr.length; i++) {
			if (!hasGap(i)) {
				double[] baseFreqs = getColumnBaseCounts(i);

				int count = 0;
				for(int j=0; j<baseFreqs.length; j++)
					count += baseFreqs[j];
				
				for(int j=0; j<baseFreqs.length; j++) {
					baseFreqs[j] = baseFreqs[j]/count * (baseFreqs[j]-1)/(count-1);
				}
				
				piArr[i] = 1.0-baseFreqs[0]-baseFreqs[1]-baseFreqs[2]-baseFreqs[3];
			}
			else {
				piArr[i] = 0;
				numGaps++;
			}
		}		
		
		freqs = piArr; //getValue() uses freqs.length to figure out how long to sequence is, so freqs must be assigned to 
					   //something, even though it's not used in the calculation. 
	}
	

}
