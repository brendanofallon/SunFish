/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.SGPlugin.analyzer;

import element.DoneListener;
import element.LabelledNumber;
import element.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import figure.series.XYSeries;

import element.sequence.*;


/**
 * Provides a few utilities for calculating things about sequences and sequenceGroups. Most of 
 * these functions have been obsoleted by the sgStatistics package, but there are a few in here
 * that are still relevant (namely, calculating the allele frequency distribution and the distribution
 * of pairwise differences).
 * 
 * @author brendan
 */
public class SequenceGroupCalculator {

	SequenceGroup seqs;

	public SequenceGroupCalculator(SequenceGroup seqs) {
		this.seqs = seqs;
	}

	/**
	 * Constructs a new sequence that is the consensus sequence of the current sequence group
	 * @param useAmbiguities Use ambiguity codes to resolve ties
	 * @return A new sequence that is the consensus of the group
	 */
	public Sequence getConsensusSequence(boolean useAmbiguities) {
		StringBuilder conStr = new StringBuilder();
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			char base = getConsensusBase(i, useAmbiguities);
			conStr.append(base);
		}
		return new StringSequence(conStr.toString(), "Consensus");
	}
	
	/**
	 * Get the most frequently used base at the given site, ignoring all gaps. If useAmbiguities
	 * is true, then ties are resolved by returning the appropriate IUPAC ambiguity code, otherwise
	 * ties are resolved randomly.  
	 * @param site
	 * @param useAmbiguities
	 * @return The most frequenct base or appropriate ambiguity code
	 */
	public char getConsensusBase(int site, boolean useAmbiguities) {
		double[] freqs = getColumnBaseFreqs(site);
		char[] bases = {'A', 'C', 'T', 'G'};
		for(int i=0; i<freqs.length; i++) { //Sort by frequency, but sort bases at the same time
			for(int j=i; j<freqs.length; j++) {
				if (freqs[i]<freqs[j]) {
					double tmpF = freqs[i];
					char tmpC = bases[i];
					freqs[i] = freqs[j];
					bases[i] = bases[j];
					freqs[j] = tmpF;
					bases[j] = tmpC;
				}
			}
		}
		
		if (!useAmbiguities)
			return bases[0]; //This is the most frequent base
		else {
			if (Math.abs(freqs[0]-freqs[1])<1e-6) {
				if (Math.abs(freqs[1]-freqs[2])<1e-6) {
					if (Math.abs(freqs[2]-freqs[3])<1e-6) { //They're all the same
						return 'N';
					}
					else {
						char c = getAmbiguityCode(bases[0], bases[1], bases[2]);
						System.out.println("Code for " + bases[0] + ", " + bases[1] + ", " + bases[2] + " is " + c);
						return c;
					}
				}
				else	{
					char c = getAmbiguityCode(bases[0], bases[1]); 
					System.out.println("Code for " + bases[0] + ", " + bases[1] + " is " + c);
					return c;
				}
			}
			else 
				return bases[0];
		}
	}
	
	public char getAmbiguityCode(char b1, char b2, char b3) {
		if (b1==b2) {
			if (b2==b3)
				return b1;
			else
				return getAmbiguityCode(b2, b3);
		}
		if (b1==b3) {
			return getAmbiguityCode(b1, b2);
		}
		if (b2==b3) {
			return getAmbiguityCode(b1, b2);
		}
		
		if (b1!='T' && b2!='T' && b3!='T') {
			return 'V';
		}
		if (b1!='G' && b2!='G' && b3!='G') {
			return 'H';
		}
		if (b1!='C' && b2!='C' && b3!='C') {
			return 'D';
		}
		if (b1!='A' && b2!='A' && b3!='A') {
			return 'B';
		}
		
		throw new IllegalArgumentException("Could not find the ambiguity code for these bases: " + b1 + ", " + b2 + ", " +b3);
	}
	
	public char getAmbiguityCode(char b1, char b2) {
		if (b1==b2) {
			return b1;
		}
		if (b1=='A') {
			if (b2=='C') return 'M';
			if (b2=='G') return 'R';
			if (b2=='T') return 'W';
		}
		if (b1=='C') {
			if (b2=='G') return 'S';
			if (b2=='T') return 'Y';			
		}
		if (b1=='G') {
			if (b2=='T') return 'K';
		}
		
		//Now test in reverse order..
		char tmp = b1;
		b1 = b2;
		b2 = tmp;
		
		if (b1=='A') {
			if (b2=='C') return 'M';
			if (b2=='G') return 'R';
			if (b2=='T') return 'W';
		}
		if (b1=='C') {
			if (b2=='G') return 'S';
			if (b2=='T') return 'Y';			
		}
		if (b1=='G') {
			if (b2=='T') return 'K';
		}
		
		
		if ((b1!='A' && b1!='C' && b1!='T' && b1!='G')) 
			throw new IllegalArgumentException("Cannot find ambiguity code for base: " + b1);
		if (b2!='A' && b2!='C' && b2!='T' && b2!='G')
			throw new IllegalArgumentException("Cannot find ambiguity code for base: " + b2);
		
		return '?';
	}
	
	public int getS() {
		if (seqs.size()<2)
			return 0;
		int min = seqs.getMinSeqLength();

		int count = 0;

		for(int i=0; i<min; i++) {
			char[] col = seqs.getColumn(i);
			for(int j=1; j<col.length; j++)
				if (col[0]!=col[j]) {
					count++;
					j=col.length;
				}
		}
		return count;
	}

	public int getColumnBaseCount(int colNum) {
		char[] col = seqs.getColumn(colNum);
		int count = 0;
		for(int i=0; i<col.length; i++) {
			if (col[i]=='A'||col[i]=='G'||col[i]=='C'||col[i]=='T')
				count++;
		}
		return count;
	}

	//Returns an array of the base frequencies for this column, in order A C T G
	public double[] getColumnBaseFreqs(int colNum) {
		return Utils.getColumnBaseFreqs(seqs, colNum);
	}
	
	public int[] getColumnBaseTots(int colNum) {
		int[] counts = new int[4];
		char[] col = seqs.getColumn(colNum);
		
		for(int i=0; i<col.length; i++)
			switch (col[i]) {
				case 'A' :
					counts[0]++;
					break;
				case 'C' : 
					counts[1]++;
					break;
				case 'T' : 
					counts[2]++;
					break;
				case 'G' : 
					counts[3]++;
					break;
			}

		return counts;
	}
	
	public double[] getWindowBaseFreqs(int start, int size) {
		double[] freqs = new double[4];
		int i;
		double colsCounted = 0;
		for(i=0; i<4; i++)
			freqs[i] = 0;
		for(i=start; i<Math.min(seqs.getMaxSeqLength(), start+size); i++) {
			double[] colFreqs = getColumnBaseFreqs(i);
			for(int j=0; j<4; j++)
				freqs[j] += colFreqs[j];
			colsCounted++;
		}
		
		for(int j=0; j<4; j++)
			freqs[j] = freqs[j]/colsCounted;
		return freqs;
	}
	
	public boolean isPolymorphic(int site) {
		char base = seqs.get(0).at(site);
		boolean same = true;
		for(int i=1; i<seqs.size() && same; i++)
			if (seqs.get(i).at(site)!=base)
				same = false;
		return same;
	}
	
	public boolean[] getPolyArray() {
		boolean[] polyArr = new boolean[seqs.getMaxSeqLength()];
		int i;
		for(i=0; i<seqs.getMaxSeqLength(); i++)
			polyArr[i] = isPolymorphic(i);
		
		return polyArr;
	}
	
	public XYSeries getSWindowSeries(int windowSize, int stepSize) {
		XYSeries ser;
		ArrayList<Point> vals = new ArrayList<Point>();
		
		boolean[] polyArr = getPolyArray();
		for(int i=0; i+windowSize<seqs.getMaxSeqLength(); i+=stepSize) {
			double percentS = 0;
			double totalSites = 0;
			for(int j=i; j<Math.min(i+windowSize, seqs.getMaxSeqLength()); j++) { 
				if (polyArr[j])
					percentS++;
				totalSites++;
			}

			percentS /= totalSites;
			
			vals.add(new Point((double)i, percentS));
		}
		
		ser = new XYSeries(vals, "Fraction Sites Segregating");
		return ser;
	}
	
	public XYSeries getGCWindowSeries(int windowSize, int windowStep) {
		int i;
		XYSeries ser;
		ArrayList<Point> vals = new ArrayList<Point>();
		for(i=0; i+windowSize<=seqs.getMaxSeqLength(); i+=windowStep) {
			double[] freqs = getWindowBaseFreqs(i, windowSize);
			vals.add(new Point((double)i, freqs[1]+freqs[3]));
		}
		
		ser = new XYSeries(vals, "GC Bias");
		return ser;
	}

	/**
	 * Returns nucleotide diversity (pi) over the whole sequence starting at pos start, continuing for length sites
	 * 
	 * @param start Site at which to start calculation
	 * @param length Number of sites to include
	 * 
	 */
	public double getPi(int start, int length) {
		int i = 0;
		double sum = 0;
		for(i=start; i<Math.min(start+length, seqs.getMaxSeqLength()); i++) {
			double[] freqs = getColumnBaseFreqs(i);
			sum += 1-freqs[0]*freqs[0]-freqs[1]*freqs[1]-freqs[2]*freqs[2]-freqs[3]*freqs[3];
		}

		return sum;
	}
	
	/*
	 * Returns nucleotide diversity pi over the whole sequence
	 * 
	 */
	public double getPi() {
		return getPi(0, seqs.getMaxSeqLength());
	}
	
	private double getPiSite(int i) {
		double[] freqs = getColumnBaseFreqs(i);
		return 1-freqs[0]*freqs[0]-freqs[1]*freqs[1]-freqs[2]*freqs[2]-freqs[3]*freqs[3];
	}

	public XYSeries getPiWindowSeries(int windowSize, int stepSize) {
		XYSeries ser;
		ArrayList<Point> vals = new ArrayList<Point>();
		double[] piArr = new double[seqs.getMaxSeqLength()];
	
		for(int i=0; i<seqs.getMaxSeqLength(); i++)
			piArr[i] = getPiSite(i);
		
		for(int i=0; i+windowSize<seqs.getMaxSeqLength(); i+=stepSize) {
			double tot = 0;
			for(int j=i; j<windowSize+i; j++)
				tot += piArr[j];
			
			tot = tot/(double)windowSize;
			vals.add(new Point((double)i, tot));
		}
		
//		for(int i=0; i+windowSize<seqs.getMaxSeqLength(); i+=stepSize) {
//			double pi = getPi(i, windowSize);
//
//			vals.add(new Point((double)i, pi));
//		}
		
		ser = new XYSeries(vals, "Nucleotide diversity");
		return ser;		
	}
	
	//Total fraction of adenine
	public double getAFreq() {
		double tot = 0;
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			tot += getColumnBaseFreqs(i)[0]*getColumnBaseCount(i);
		}
		return tot;
	}

	//Total fraction of T
	public double getCFreq() {
		double tot = 0;
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			tot += getColumnBaseFreqs(i)[1]*getColumnBaseCount(i);
		}
		return tot;
	}

	//Total fraction of T
	public double getTFreq() {
		double tot = 0;
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			tot += getColumnBaseFreqs(i)[2]*getColumnBaseCount(i);
		}
		return tot;
	}

	//Total fraction of G
	public double getGFreq() {
		double tot = 0;
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			tot += getColumnBaseFreqs(i)[3]*getColumnBaseCount(i);
		}
		return tot;
	}

	
	public double getWattersonsTheta() {
		double S = getS();
		double a1 =0;
		
		for(int i=1; i<(seqs.size()); i++) {
			a1 += 1.0/(double)i;
		}

		return S/a1;		
	}
	
	//Tajima's D and other neutrality tests
	public double getTajimasD() {
		double pi = getPi();
		double S = getS();
		double a1 =0;
		double a2 =0;
		
		double n = seqs.size();
		
		for(int i=1; i<n; i++) {
			a1 += 1.0/(double)i;
			a2 += 1.0/(double)(i*i);
		}
			
		
		double b1 = (n+1.0)/(3.0*(n-1.0));
		double b2 = 2.0*(n*n + n + 3.0)/(9.0*n*(n-1.0));
		double c1 = b1 - 1.0/a1;
		double c2 = b2 - (n+2.0)/(a1*n) + a2/(a1*a1);
	
		double e1 = c1/a1;
		double e2 = c2/(a1*a1 + a2);
		
		
		double dif = pi-S/a1;
		double D = dif / Math.sqrt(e1*S + e2*S*(S-1.0) );
		
		return D;
	}
	
	public XYSeries getTajimasDWindowSeries(int windowSize, int windowStep) {
		int i;
		ArrayList<Point> tdSeries = new ArrayList<Point>();
		
		double[] piArr = new double[seqs.getMaxSeqLength()];
		for(i=0; i<seqs.getMaxSeqLength(); i++)
			piArr[i] = getPiSite(i);
		
		boolean[] polyArr = getPolyArray();
		
		double n = seqs.size();
		
		double a1 =0;
		double a2 =0;
		
		for(i=1; i<(seqs.size()); i++) {
			a1 += 1.0/(double)i;
			a2 += 1.0/(double)(i*i);
		}
		
		double b1 = (n+1.0)/(3.0*(n-1.0));
		double b2 = 2.0*(n*n + n + 3.0)/(9.0*n*(n-1.0));
		double c1 = b1 - 1.0/a1;
		double c2 = b2 - (n+2.0)/(a1*n) + a2/(a1*a1);
	
		double e1 = c1/a1;
		double e2 = c2/(a1*a1 + a2);
		
		for(i=0; i+windowSize<seqs.getMaxSeqLength(); i+=windowStep) {
			double S = 0;
			double pi = 0;
			for(int j=i; j<i+windowSize; j++) {
				if (polyArr[j])
					S++;
				pi += piArr[j];
			}
				
			//** SHOULD S AND PI BE ABSOLUTE OR PER SITE INVESTIGATED? **// 
			double theta_s = S/a1;
			double D = (pi/windowSize-theta_s)/(e1*S + e2*S*(S-1.0));
			tdSeries.add(new Point(i, D));
		}

		XYSeries ser = new XYSeries(tdSeries, "Tajima's D");
		return ser;
	}
	
	public double getFuLiFStar() {
		double pi = getPi();
		double eta = numSingletons();
		int n = seqs.size();
		double a1 =0;
		double a2 =0;
		
		for(int i=1; i<(seqs.size()); i++) {
			a1 += 1.0/(double)i;
			a2 += 1.0/(double)(i*i);
		}

		double b1 = (seqs.size()+1.0)/3.0/(seqs.size()-1.0);
		double b2 = 2.0*(seqs.size()*seqs.size() + seqs.size() + 3.0)/9.0/seqs.size()/(seqs.size()-1.0);
		double c1 = b1 - 1.0/a1;
		double c2 = b2 - (seqs.size()+2.0)/a1/seqs.size() + a2/a1/a1;
	
		double e1 = c1/a1;
		double e2 = c2/(a1*a1 + a2);
		//double F = (pi- (n-1)/n*eta )/(e1*S + e2*S*(S-1.0));
		System.out.println("Can't compute F* since we don't know the variance. Dang.");
		return 0.0;		
	}

	
	public int getMutantBaseCount(int site, char ancestralBase) {
		int count = 0;
		for(int i=0; i<seqs.size(); i++) 
			if (seqs.get(i).at(site)!=ancestralBase)
				count++;
		return count;
	}
	
	public int numHaplotypes() {
		int num = 0;
		if (seqs == null || seqs.size()==0)
			return 0;
		else if (seqs.size()==1)
			return 1;
		
		
		ArrayList<String> types = new ArrayList<String>();
		types.add(seqs.get(0).toString());
		for(int i=0; i<seqs.size(); i++) {
			if (! types.contains(seqs.get(i).toString())) {
				types.add(seqs.get(i).toString());
				num++;
			}
		}
		return num;
	}
	
	public int numSitesWithGaps() {
		int tot = 0;
		for(int i=0; i<seqs.getMaxSeqLength(); i++)
			if (seqs.hasGap(i))
				tot++;
		return tot;
	}
	
	private int max(int[] list) {
		int max = list[0];
		for(int i=1; i<list.length; i++)
			if (max < list[i])
				max = list[i];
		return max;
	}
	
	/**
	 * Returns the frequency of the minor allele at this particular site. There's some ambiguity here,
	 * for instance, if all bases have frequency 0.25, there's no clear minor allele. For now, this 
	 * returns the second-greatest frequency
	 * @param column site at which to calculate MAF
	 * @return Minor allele frequency at one site
	 */
	public Integer getMinorAlleleCount(int column) {
		if (seqs.hasGap(column))
			return 0;
		int[] freqs = getColumnBaseTots(column);
		Arrays.sort(freqs);
		
		return freqs[2];
	}
	
	
	/**
	 * Returns the unfolded allele frequency spectrum as an int array, using the supplied sequence
	 * as the ancestral sequence. 
	 * @param ancSeq
	 * @return
	 */
	public int[] getUnfoldedAlleleFreqSpectrum(Sequence ancSeq) {
		int[] spec = new int[seqs.size()];
		
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			int val = Integer.valueOf( getMutantBaseCount(i, ancSeq.at(i)) );
			spec[ val ]++;
		}
		return spec;
	}
	
	/**
	 * Returns the distribution of allele frequencies as ints.
	 * @return
	 */
	public int[] getFoldedAlleleFreqSpectrum() {
		int[] spec = new int[seqs.size()/2+1];
		
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			int val = Integer.valueOf( getMinorAlleleCount(i) );
			if (val >= (seqs.size()/2+1) ) {
				System.err.println("Huh? Got a minor allele count of " + val + ", but there are only " + seqs.size() + " sequences!");
			}
			else
				spec[ val ]++;
		}
		
		return spec;
	}

	
	/**
	 * Returns the distribution of allele frequencies 
	 * @return Array where the ith element is the frequency of positions where the minor allele frequency is i/seqs.size() 
	 */
	public double[] getFoldedAlleleSpectrumEx(double theta) {
		boolean even = seqs.size() % 2 ==0;
		int possibilities;
		if (even)
			possibilities = seqs.size()/2;
		else
			possibilities = seqs.size()/2+1;
			
		double[] spec = new double[possibilities];
	
		for(int i=0; i<seqs.size()/2; i++) {
			spec[ i ] = theta/(double)(i+1) + theta/(double)(seqs.size()-(i+1));
		}
		
		if (!even)
			spec[ spec.length-1] = theta/(double)(seqs.size()/2);
		

		return spec;
	}

	/**
	 * Returns the distribution of allele frequencies 
	 * @return Array where the ith element is the frequency of positions where the minor allele frequency is i/seqs.size() 
	 */
	public double[] getUnfoldedAlleleSpectrumEx(double theta) {
		double[] spec = new double[seqs.size()];
	
		for(int i=0; i<seqs.size(); i++)
			spec[ i ] = theta/(double)(i+1);
		 
		return spec;
	}
	
	/**
	 * Returns the distribution of allele frequencies as ints.
	 * Gotcha : The functions that return getXXXasSeries do NOT include the zeroth element
	 * even though all the getXXX functions return it. 
	 * @return
	 */
	public XYSeries getFoldedAlleleSpectrumExAsSeries(double theta) {
		double[] spec = getFoldedAlleleSpectrumEx(theta);
		ArrayList<element.Point> ser = new ArrayList<element.Point>();
		for(int i=1; i<spec.length; i++)
			ser.add(new element.Point(i, spec[i]));
		
		XYSeries cData = new XYSeries(ser);
		return cData;
	}
	
	
	/**
	 * Returns the distribution of allele frequencies 
	 * @return Array where the ith element is the frequency of positions where the minor allele frequency is i/seqs.size() 
	 */
	public XYSeries getUnfoldedAlleleSpectrumExAsSeries(double theta) {
		double[] spec = getUnfoldedAlleleSpectrumEx(theta);
		ArrayList<element.Point> ser = new ArrayList<element.Point>();
		for(int i=1; i<spec.length; i++)
			ser.add(new element.Point(i, spec[i]));
		
		XYSeries cData = new XYSeries(ser);
		return cData;
	}
	
	public XYSeries getFoldedAlleleFreqSpectrumAsSeries() {
		int[] spec = getFoldedAlleleFreqSpectrum();
		
		ArrayList<element.Point> ser = new ArrayList<element.Point>();
		for(int i=1; i<spec.length; i++)
			ser.add(new element.Point(i, spec[i]));
		
		XYSeries cData = new XYSeries(ser);
		return cData;
	}
	
	public XYSeries getUnfoldedAlleleFreqSpectrumAsSeries(Sequence ancSeq) {
		int[] spec = getUnfoldedAlleleFreqSpectrum(ancSeq);
		
		ArrayList<element.Point> ser = new ArrayList<element.Point>();
		for(int i=1; i<spec.length; i++)
			ser.add(new element.Point(i, spec[i]));
		
		XYSeries cData = new XYSeries(ser);
		return cData;
	}
	
	/**
	 * Returns the unfolded allele frequency spectrum as an XYSeries, using the 
	 * sequence in seqs at index ancSeq as the ancestral sequence.
	 * @param ancSeq
	 * @return An XYSeries representing an ancestral sequence
	 */
	public XYSeries getUnfoldedAlleleFreqSpectrumAsSeries(int ancSeq) {
		return getUnfoldedAlleleFreqSpectrumAsSeries(seqs.get(ancSeq));
	}
	
	/**
	 * The number of sites which at which the minor allele has frequency 1/seqs.size()
	 */
	public int numSingletons() {
		int tot = 0;
		for(int i=0; i<seqs.getMaxSeqLength(); i++) {
			int count = getMinorAlleleCount(i);
			if (count == 1-seqs.size())
				tot++;
		}
		return tot;
	}
	
	
	
	public int numDifs(Sequence one, Sequence two) {
		int difs = 0;
		for(int i = 0; i<one.length(); i++) {
			if (one.at(i) != two.at(i))
				difs++;
		}
		
		return difs;
	}
	
	
	/**
	 * This version of getting the pairwise dif distribution does not run in the background and can
	 * take a long time. The inner class below runs as a swing worker and can be used in combination
	 * with a progress monitor to display a handy progress dialog (see PairwiseDifChart for an example)
	 * Much of this code is repeated there...
	 *  
	 * @param monitor
	 * @return
	 */
	public XYSeries getPairwiseDifsDistribution() {
		ArrayList<element.Point> ser = new ArrayList<element.Point>();
		int count = 0;
		int total = seqs.size()*(seqs.size()-1)/2;
		int step = total/10+1;
		int[] hist = new int[seqs.getMaxSeqLength()];
		for (int i=0; i<seqs.size(); i++) {
			
			for(int j=i+1; j<seqs.size(); j++) {
				int difs = numDifs(seqs.get(i), seqs.get(j));
				hist[difs]++;
				count++;
			}
		}
		
		int lowerCutoff = 0;
		while(lowerCutoff< hist.length && hist[lowerCutoff]==0)
			lowerCutoff++;
		
		int upperCutoff = hist.length-1;
		while(upperCutoff>lowerCutoff && hist[upperCutoff]==0)
			upperCutoff--;
			
		
		for(int i=lowerCutoff; i<=upperCutoff; i++) {
			ser.add(new element.Point(i, hist[i]));
		}
		XYSeries cData = new XYSeries(ser);

		return cData;
	}
	
	/**
	 * Returns a new SwingWorker that counts the pairwise differences in the background.
	 * The doneListener is a component to notify when we're .done() 
	 * @param doneListener 
	 * @return
	 */
	public PairwiseDifCounter getPairDifCounter( DoneListener doneListener ) {
		return new PairwiseDifCounter( doneListener );
	}
	
	
	public static class Utils {

		//Returns an array of the base frequencies for this column, in order A C T G
		public static double[] getColumnBaseFreqs(SequenceGroup seqs, int colNum) {
			double[] freqs = new double[4];
			char[] col = seqs.getColumn(colNum);
			double counted = 0;
			int A = 0;
			int C = 1;
			int T = 2;
			int G = 3;
			for(int i=0; i<col.length; i++)
				switch (col[i]) {
					case 'A' :
						freqs[A]++;
						counted++;
						break;
					case 'C' : 
						freqs[C]++;
						counted++;
						break;
					case 'T' : 
						freqs[T]++;
						counted++;
						break;
					case 'G' : 
						freqs[G]++;
						counted++;
						break;
					case 'M' :
						freqs[A]+=0.5;
						freqs[C]+=0.5;
						counted++;
						break;
					case 'R' :
						freqs[A]+=0.5;
						freqs[G]+=0.5;
						counted++;
						break;
					case 'W' :
						freqs[A]+=0.5;
						freqs[T]+=0.5;
						counted++;
						break;
					case 'S' :
						freqs[G]+=0.5;
						freqs[C]+=0.5;
						counted++;
						break;
					case 'Y' :
						freqs[C]+=0.5;
						freqs[T]+=0.5;
						counted++;
						break;
					case 'V' :
						freqs[A]+=0.3333;
						freqs[C]+=0.3333;
						freqs[G]+=0.3333;
						counted++;
						break;
					case 'H' :
						freqs[A]+=0.3333;
						freqs[C]+=0.3333;
						freqs[T]+=0.3333;
						counted++;
						break;
					case 'D' :
						freqs[A]+=0.3333;
						freqs[G]+=0.3333;
						freqs[T]+=0.3333;
						counted++;
						break;
					case 'B' :
						freqs[C]+=0.3333;
						freqs[G]+=0.3333;
						freqs[T]+=0.3333;
						counted++;
						break;
					case 'N' :
						freqs[A]+=0.25;
						freqs[C]+=0.25;
						freqs[G]+=0.25;
						freqs[T]+=0.25;
						counted++;
						break;
					case 'X' :
						freqs[A]+=0.25;
						freqs[C]+=0.25;
						freqs[G]+=0.25;
						freqs[T]+=0.25;
						counted++;
						break;
				}

			for(int i=0; i<4; i++)
				freqs[i] /= counted;

			return freqs;
		}
		
	}
	
   public class PairwiseDifCounter extends SwingWorker<Void, Void> {
	   
	   XYSeries difCounts;
	   DoneListener doneListener;
	   
	   public PairwiseDifCounter( DoneListener doneListener) {
		 this.doneListener = doneListener;   
	   }
	   
	   public XYSeries getSeries() {
		   return difCounts;
	   }
        
        public Void doInBackground() {
           
            setProgress(0);

            ArrayList<element.Point> ser = new ArrayList<element.Point>();
            int count = 0;
            int total = seqs.size()*(seqs.size()-1)/2;
            int step = total/10+1;
            int[] hist = new int[seqs.getMaxSeqLength()];
            for (int i=0; i<seqs.size(); i++) {

            	for(int j=i+1; j<seqs.size(); j++) {
            		int difs = numDifs(seqs.get(i), seqs.get(j));
            		hist[difs]++;
            		count++;
            	}
            	setProgress( (int)Math.round(100*(double)count/(double)total));
            }

            int lowerCutoff = 0;
            while(lowerCutoff< hist.length && hist[lowerCutoff]==0)
            	lowerCutoff++;

            int upperCutoff = hist.length-1;
            while(upperCutoff>lowerCutoff && hist[upperCutoff]==0)
            	upperCutoff--;


            for(int i=lowerCutoff; i<=upperCutoff; i++) {
            	ser.add(new element.Point(i, hist[i]));
            }

            setProgress(Math.min(100, 100*count/total));

            difCounts = new XYSeries(ser);

            return null;
        }


        public void done() {
        	if (doneListener != null)
        		doneListener.done();
        }
    }



}
