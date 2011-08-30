package element.sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import element.codon.GeneticCodeFactory;
import element.codon.GeneticCode.AbstractGeneticCode;
import element.codon.GeneticCode.AminoAcid;
import element.codon.GeneticCodeFactory.GeneticCodes;
import element.sequence.SequenceAnnotation.Direction;

/**
 * A quick-and-dirty orf finder, this thing uses a greedy algorithm that prefers larger ORFs. Note that it
 * doesn't ID ORFs on the reverse complement of the given sequence - although perhaps it should
 * 
 * @author brendan
 *
 */
public class SimpleORFAnnotator implements SequenceAnnotator {

	public static final String annotationType = "ORF";
	
	AbstractGeneticCode geneticCode;
	
	public SimpleORFAnnotator() {
		geneticCode = GeneticCodeFactory.createGeneticCode(GeneticCodes.Universal); 
	}
	
	
	public void setGeneticCode(GeneticCodes codeType) {
		geneticCode = GeneticCodeFactory.createGeneticCode(codeType);
	}
	
	/**
	 * Returns a list of all ORFs in this frame that are > 9 aa's long as a list of 
	 * sequence annotations. This is a 'greedy' algorithm that starts counting as soon 
	 * as it finds a start codon and keeps counting until it comes across the first stop, 
	 * then repeats. 
	 * 
	 * @param seq
	 * @param startSite
	 * @return
	 */
	private List<SequenceAnnotation> getORFsForFrame(Sequence seq, int startSite) {
		List<SequenceAnnotation> orfs = new ArrayList<SequenceAnnotation>();
		StringBuilder codon = new StringBuilder();
		
		boolean inORF = false;
		int currentStart = 0;
		//System.out.println("Starting at site: " + startSite + " base is : " + seq.at(startSite));
		for(int i=startSite; i<seq.length(); i++) {
			
			if (codon.length()==3) {
				AminoAcid aa = geneticCode.translate(codon.toString());
				//System.out.println("Translating codon : " + codon);
				if (aa==AminoAcid.Met && inORF==false) {
					inORF = true;
					currentStart = i-3;
					//System.out.println("Found start codon at pos " + currentStart);
				}
				if (aa==AminoAcid.Stop && inORF==true) {
					inORF = false;
					//System.out.println("Found stop codon at pos : " + (i-2));
					if (i - currentStart > 59){
						SequenceAnnotation orf = new SequenceAnnotation(seq, annotationType, currentStart, i-1);
						orfs.add(orf);
					
					}

				}
				codon.setLength(0);
			}
			
			if (! seq.isGap(i)) {
				codon.append(seq.at(i));
			}
			
		}
		
		return orfs;
	}
	
	
//	private double sitesPerORF(List<SequenceAnnotation> orfs) {
//		int sum = 0;
//		if (orfs.size()==0)
//			return 0;
//		
//		for(SequenceAnnotation orf : orfs)
//			sum += orf.getLength();
//		
//		return (double)sum / (double)orfs.size();
//	}
	
	/**
	 * Adds the new annotation to the list if it doesn't overlap any of the other ranges. If it does,
	 * it is added only if it is larger than all of the other ranges it overlaps with, in this case
	 * all of the overlapped ranges are removed from the list provided
	 * @param list
	 * @param newAnno
	 */
	private void updateList(List<SequenceAnnotation> list, SequenceAnnotation newAnno) {
		List<SequenceAnnotation> rangesOverlapped = new ArrayList<SequenceAnnotation>();
		for(SequenceAnnotation anno : list) {
			if (anno.overlaps(newAnno))
				rangesOverlapped.add(anno);
		}
		
		boolean bigger = true;
		for(SequenceAnnotation anno : rangesOverlapped) {
			if (newAnno.getLength()<anno.getLength())
				bigger = false;
		}
		
		if (bigger) {
			list.removeAll(rangesOverlapped);
			list.add(newAnno);
		}
		
	}
	
	public boolean annotate(Sequence seq) {
		List<SequenceAnnotation> orfs1 = getORFsForFrame(seq, 0);
		List<SequenceAnnotation> orfs2 = getORFsForFrame(seq, 1);
		List<SequenceAnnotation> orfs3 = getORFsForFrame(seq, 2);
	
		Sequence revComp = seq.getReverseComplement();
		List<SequenceAnnotation> orfs4 = getORFsForFrame(revComp, 0);
		List<SequenceAnnotation> orfs5 = getORFsForFrame(revComp, 1);
		List<SequenceAnnotation> orfs6 = getORFsForFrame(revComp, 2);
		
		

		
		//Add every ORF we've come across sequentially to the list... overlapping
		//ORFs will be resolved by preferring the larger orf
		for(SequenceAnnotation anno : orfs1) {
			anno.setDirection(Direction.Forward);
			//updateList(finalORFs, anno);
		}
		for(SequenceAnnotation anno : orfs2) {
			anno.setDirection(Direction.Forward);
			//updateList(finalORFs, anno);
		}
		for(SequenceAnnotation anno : orfs3) {
			anno.setDirection(Direction.Forward);
			//updateList(finalORFs, anno);
		}
		for(SequenceAnnotation anno : orfs4) {
			anno.setDirection(Direction.Reverse);
			anno.reverseRange(seq.length());
			//updateList(finalORFs, anno);
		}
		for(SequenceAnnotation anno : orfs5) {
			anno.setDirection(Direction.Reverse);
			anno.reverseRange(seq.length());
			//updateList(finalORFs, anno);
		}
		for(SequenceAnnotation anno : orfs6) {
			anno.setDirection(Direction.Reverse);
			anno.reverseRange(seq.length());
			//updateList(finalORFs, anno);
		}
		
		List<SequenceAnnotation> finalORFs = new ArrayList<SequenceAnnotation>();
		List<SequenceAnnotation> allORFs = new ArrayList<SequenceAnnotation>();
		allORFs.addAll(orfs1);
		allORFs.addAll(orfs2);
		allORFs.addAll(orfs3);
		allORFs.addAll(orfs4);
		allORFs.addAll(orfs5);
		allORFs.addAll(orfs6);
		
		Collections.sort(allORFs, new ORFLengthComparator());
		
		if (allORFs.size()>1) {
			if (allORFs.get(0).getLength()<allORFs.get(1).getLength()) {
				System.err.println("ORFs are not sorted in ascending order, length1 = " + allORFs.get(0).getLength()  + " length 2: " + allORFs.get(1).getLength());
			}
		}
		
		if (allORFs.size()==0) {
			return false;
		}
		
		finalORFs.add(allORFs.get(0));
		for(int i=1; i<allORFs.size(); i++) {
			boolean overlaps = false;
			for(SequenceAnnotation orf : finalORFs) 
				overlaps = overlaps || orf.overlaps(allORFs.get(i));
			if (!overlaps) {
				finalORFs.add(allORFs.get(i));
			}
		}
		
		seq.addAnnotations(finalORFs);
		if (finalORFs.size()>0)
			return true;
		else
			return false;
	}

	
	/**
	 * Compares annotation length 
	 * @author brendan
	 *
	 */
	class ORFLengthComparator implements Comparator {

		@Override
		public int compare(Object o1, Object o2) {
			return ((SequenceAnnotation)o2).getLength() -((SequenceAnnotation)o1).getLength(); 
		}
		
	}
}
