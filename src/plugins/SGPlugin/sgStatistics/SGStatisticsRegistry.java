package plugins.SGPlugin.sgStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import element.sequence.*;


/**
 * Maintains a list of various S.G. statistics. This class is meant to be
 * the primary way to create new SGCalculator objects - get one from the
 * map and then call getInstance(sg) to get one with values calculated
 * @author brendan
 *
 */
public class SGStatisticsRegistry {

	//This list defines the Names of all the various statistics. They're also used
	//as the keys to index instances 
	public static final String AFREQ = "A freq.";
	public static final String CFREQ = "C freq.";
	public static final String TFREQ = "T freq.";
	public static final String GFREQ = "G freq.";
	public static final String GCFREQ = "G+C Freq.";
	public static final String ATFREQ = "A+T Freq.";
	public static final String PURFREQ = "Purine frequency";
	public static final String PYRFREQ = "Pyrimidine frequency";
	public static final String SEG_SITES = "Segregating sites";
	public static final String NUC_DIVERSITY = "Nucleotide diversity";
	public static final String NUM_GAPS = "Number of gapped sites";
	public static final String NUM_HAPS = "Number of haplotypes";
	public static final String NUM_SITEPATTERNS = "Number of site patterns";
	public static final String HAP_DIVERSITY = "Haplotype diversity";
	public static final String TAJIMASD = "Tajima's D";
	public static final String THETAW = "Watterson's theta";
	public static final String NUMSEQS = "Number of sequences";
	public static final String SEQ_LENGTH = "Sequence length";
	
	Map<String, SGCalculator> allStats;
	
	public SGStatisticsRegistry() {
		allStats = new HashMap<String, SGCalculator>();
		allStats.put(AFREQ, new AFreq());
		allStats.put(CFREQ, new CFreq());
		allStats.put(TFREQ, new TFreq());
		allStats.put(GFREQ, new GFreq());
		allStats.put(GCFREQ, new GCFreq());
		allStats.put(ATFREQ, new ATFreq());
		allStats.put(PURFREQ, new PurineFreq());
		allStats.put(PYRFREQ, new PyrimidineFreq());
		allStats.put(SEG_SITES, new SegregatingSites());
		allStats.put(NUC_DIVERSITY, new NucleotideDiversity());
		allStats.put(NUM_GAPS, new NumGappedSites());
		allStats.put(NUM_HAPS, new NumHaplotypes());
		allStats.put(TAJIMASD, new TajimasD());
		allStats.put(THETAW, new WattersonsTheta());
		allStats.put(HAP_DIVERSITY, new HaplotypeDiversity());
		allStats.put(NUM_SITEPATTERNS, new NumSitePatterns());
		allStats.put(NUMSEQS, new NumSequences());
		allStats.put(SEQ_LENGTH, new SequenceLength());
	}
	
	public Map<String, SGCalculator> getAll() {
		return allStats;
	}
	
	public Map<String, BaseCounter> getBaseCounters() {
		Map<String, BaseCounter> baseCounters = new HashMap<String, BaseCounter>();
		for(SGCalculator calc : allStats.values()) {
			if (calc instanceof BaseCounter) {
				baseCounters.put(calc.getName(), (BaseCounter)calc);
			}
		}
		return baseCounters;
	}
	
	public SGCalculator getInstance(String key, SequenceGroup sg) throws IllegalArgumentException {
		SGCalculator sgCalc = allStats.get(key);
		if (sgCalc == null) {
			throw new IllegalArgumentException("No Calculator of type " + key + " found.");
		}
		else {
			return sgCalc.getInstance(sg);
		}
	}
	
	public BaseCounter getBaseCounterInstance(String key, SequenceGroup sg) throws IllegalArgumentException {
		SGCalculator sgCalc = allStats.get(key);
		if (sgCalc == null) {
			throw new IllegalArgumentException("No Calculator of type " + key + " found.");
		}
		else {
			if (sgCalc instanceof BaseCounter) {
				return ((BaseCounter)sgCalc.getInstance(sg));
			}
			throw new IllegalArgumentException("Calculator type " + key + " is not a base counter");
		}
		
	}
 	
}

