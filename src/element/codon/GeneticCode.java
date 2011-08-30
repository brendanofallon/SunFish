package element.codon;

import java.util.HashMap;

import element.sequence.Sequence;

/**
 * Interface for all things that can translate a codon into an amino acid
 * @author brendan
 *
 */
public interface GeneticCode {

	//A list of all available amino acids
	public enum AminoAcid {
		Phe, Ser, Tyr, Cys, Leu, Trp, 
		Pro, His, Arg, Ile, Thr, Asn, 
		Glu, Lys, Val, Ala, Asp, Gly,
		Met, Gln, Stop
	}
	
	public AminoAcid translate(String codon);
	
	
	public abstract class AbstractGeneticCode implements GeneticCode {
		
		HashMap<AminoAcid, Character> aaCharMap = new HashMap<AminoAcid, Character>();
		
		public AbstractGeneticCode() {
			aaCharMap.put(AminoAcid.Ala, 'A');			
			aaCharMap.put(AminoAcid.Arg, 'R');
			aaCharMap.put(AminoAcid.Asn, 'N');
			aaCharMap.put(AminoAcid.Asp, 'D');
			aaCharMap.put(AminoAcid.Cys, 'C');
			aaCharMap.put(AminoAcid.Glu, 'E');
			aaCharMap.put(AminoAcid.Gln, 'Q');
			aaCharMap.put(AminoAcid.Gly, 'G');
			aaCharMap.put(AminoAcid.His, 'H');
			aaCharMap.put(AminoAcid.Ile, 'I');
			aaCharMap.put(AminoAcid.Leu, 'L');
			aaCharMap.put(AminoAcid.Lys, 'K');
			aaCharMap.put(AminoAcid.Met, 'M');
			aaCharMap.put(AminoAcid.Phe, 'F');
			aaCharMap.put(AminoAcid.Pro, 'P');
			aaCharMap.put(AminoAcid.Ser, 'S');
			aaCharMap.put(AminoAcid.Thr, 'T');
			aaCharMap.put(AminoAcid.Trp, 'W');
			aaCharMap.put(AminoAcid.Tyr, 'Y');
			aaCharMap.put(AminoAcid.Val, 'V');
			aaCharMap.put(AminoAcid.Stop, '*');
		}
		
		/**
		 * Return the one-letter code for the given amino acid
		 * @param aa
		 * @return The one letter character code for the given aa
		 */
		public Character aaToChar(AminoAcid aa) {
			return aaCharMap.get(aa); 
		}
		
		public abstract AminoAcid translate(String codon);
		
		/**
		 * Returns the amino acid that corresponds to the codon beginning at site i in this sequence
		 * @param sequence
		 * @param site
		 * @return
		 */
		public AminoAcid translate(Sequence sequence, int site) {
			if (site>=(sequence.length()-2)) {
				throw new IllegalArgumentException("Cannot translate codon site #" + site + " because sequence is only " + sequence.length() + " sites long.");
			}
			else {
				AminoAcid aa = translate(sequence.toString().substring(site, site+3));
				if (aa==null) {
					System.out.println("Cannot translate codon :" + sequence.toString().substring(site, site+3));
				}
				return aa;
			}
		}
		
		/**
		 * Returns the amino acid that corresponds to the reverse complement of the codon 'beginning' 
		 * at site i in the forward sequence, but with the bases substituted for their complement and
		 * the codon read in the reverse direction. 
		 * @param sequence
		 * @param site
		 * @return The Amino acid corresponding to the reverse complement of the codon beginning at site i
		 */
		public AminoAcid translateRevComp(Sequence sequence, int site) {
			if (site>=(sequence.length()-2)) {
				throw new IllegalArgumentException("Cannot translate codon site #" + site + " because sequence is only " + sequence.length() + " sites long.");
			}
			else {
				String revCod = sequence.getReverseComplement(site, site+3).toString();
				AminoAcid aa = translate(revCod);
				if (aa==null) {
					System.out.println("Cannot translate codon :" + sequence.toString().substring(site, site+3));
				}
				return aa;
			}
		}
		
	}
}
