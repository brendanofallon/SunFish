package element.codon;

import element.codon.GeneticCode.AbstractGeneticCode;

/**
 * A factory class to create & hand out genetic codes. All codes are currently implemented in here,
 * and in general they follow the rules found at http://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi#SG2
 * @author brendan
 *
 */
public class GeneticCodeFactory {

	public enum GeneticCodes { Universal, vertebrateMTDNA, yeastMTDNA, mspMTDNA };
	
	static GeneticCodeFactory thisFactory = null;
	
	public static final char G = 'G';
	public static final char A = 'A';
	public static final char T = 'T';
	public static final char C = 'C';
	public static final char U = 'U';
	
	public GeneticCodeFactory() {
		if (thisFactory==null)
			thisFactory = this;
	}
	
	private AbstractGeneticCode getGeneticCode(GeneticCodes codeType) {
		switch(codeType) {
		case Universal : return new UniversalGeneticCode();
		case vertebrateMTDNA : return new VertebrateMtDNACode();
		case yeastMTDNA : return new YeastMtDNACode();
		case mspMTDNA : return new MPCMtDNAMSCode();
		}
		return null;
	}
	
	public static AbstractGeneticCode createGeneticCode(GeneticCodes codeType) {
		if (thisFactory==null)
			thisFactory = new GeneticCodeFactory();
		
		return thisFactory.getGeneticCode(codeType);
	}
	
	class UniversalGeneticCode extends AbstractGeneticCode {

		public AminoAcid translate(String codon) {
			String rna = codon.replaceAll("T", "U");
			
			if (rna.charAt(0)==U) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Phe;
					else 
						return AminoAcid.Leu;
				}
				
				if (rna.charAt(1)==C)
					return AminoAcid.Ser;
				
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Tyr;
					else
						return AminoAcid.Stop;	//UAU and UAG
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Cys;
					if (rna.charAt(2)==A)
						return AminoAcid.Stop;	//UGA
					if (rna.charAt(2)==G)
						return AminoAcid.Trp;
				}
			}
			
			if (rna.charAt(0)==C) {
				if (rna.charAt(1)==U)
					return AminoAcid.Leu;
				if (rna.charAt(1)==C)
					return AminoAcid.Pro;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.His;
					else
						return AminoAcid.Gln; //Glutamine! Not glutamic acid
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Arg;
			}
			
			if (rna.charAt(0)==A) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==G)
						return AminoAcid.Met;
					else
						return AminoAcid.Ile;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Thr;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asn; //Asparagine, not aspartic acid
					else
						return AminoAcid.Lys;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Ser;
					else
						return AminoAcid.Arg;
				}
			}
			
			if (rna.charAt(0)==G) {
				if (rna.charAt(1)==U) 
					return AminoAcid.Val;
				if (rna.charAt(1)==C)
					return AminoAcid.Ala;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asp; //Aspartic acid, not asparagine
					else
						return AminoAcid.Glu; //Glutamic acid, not glutamine
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Gly; //Glycine
			}
			
			return null;		
		}
		

	}
	
	class VertebrateMtDNACode extends AbstractGeneticCode {

		public AminoAcid translate(String codon) {
			String rna = codon.replaceAll("T", "U");
			
			if (rna.charAt(0)==U) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Phe;
					else 
						return AminoAcid.Leu;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Ser;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==A || rna.charAt(2)==C)
						return AminoAcid.Tyr;
					else
						return AminoAcid.Stop;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Cys;
					if (rna.charAt(2)==A)
						return AminoAcid.Trp;
					if (rna.charAt(2)==G)
						return AminoAcid.Trp;
				}
			}
			
			if (rna.charAt(0)==C) {
				if (rna.charAt(1)==U)
					return AminoAcid.Leu;
				if (rna.charAt(1)==C)
					return AminoAcid.Pro;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.His;
					else
						return AminoAcid.Gln; //Glutamine! Not glutamic acid
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Arg;
			}
			
			if (rna.charAt(0)==A) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==G || rna.charAt(2)==A)
						return AminoAcid.Met;
					else
						return AminoAcid.Ile;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Thr;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asn; //Asparagine, not aspartic acid
					else
						return AminoAcid.Lys;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Ser;
					else
						return AminoAcid.Stop;
				}
			}
			
			if (rna.charAt(0)==G) {
				if (rna.charAt(1)==U) 
					return AminoAcid.Val;
				if (rna.charAt(1)==C)
					return AminoAcid.Ala;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asp; //Aspartic acid, not asparagine
					else
						return AminoAcid.Glu; //Glutamic acid, not glutamine
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Gly; //Glycine
			}
			
			return null;
		}
		
	}
	
	class YeastMtDNACode extends AbstractGeneticCode {


		public AminoAcid translate(String codon) {
			String rna = codon.replaceAll("T", "U");
			
			if (rna.charAt(0)==U) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Phe;
					else 
						return AminoAcid.Leu;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Ser;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==A || rna.charAt(2)==C)
						return AminoAcid.Tyr;
					else
						return AminoAcid.Stop;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Cys;
					if (rna.charAt(2)==A)
						return AminoAcid.Trp;		//Differs from standard
					if (rna.charAt(2)==G)
						return AminoAcid.Trp;
				}
			}
			
			if (rna.charAt(0)==C) {
				if (rna.charAt(1)==U)
					return AminoAcid.Thr;			//Differs from standard
				if (rna.charAt(1)==C)
					return AminoAcid.Pro;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.His;
					else
						return AminoAcid.Gln; //Glutamine! Not glutamic acid
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Arg;
			}
			
			if (rna.charAt(0)==A) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==G || rna.charAt(2)==A)	//Differs from standard
						return AminoAcid.Met;
					else
						return AminoAcid.Ile;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Thr;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asn; //Asparagine, not aspartic acid
					else
						return AminoAcid.Lys;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Ser;
					else
						return AminoAcid.Arg;
				}
			}
			
			if (rna.charAt(0)==G) {
				if (rna.charAt(1)==U) 
					return AminoAcid.Val;
				if (rna.charAt(1)==C)
					return AminoAcid.Ala;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asp; //Aspartic acid, not asparagine
					else
						return AminoAcid.Glu; //Glutamic acid, not glutamine
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Gly; //Glycine
			}
			
			return null;
		}
		
	}
	
	
	
	/**
	 * The mold, protozoan, coelenterate mtDNA and Spiroplasma / mycoplasma code
	 * @author brendan
	 *
	 */
	class MPCMtDNAMSCode extends AbstractGeneticCode {


		public AminoAcid translate(String codon) {
			String rna = codon.replaceAll("T", "U");
			
			if (rna.charAt(0)==U) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Phe;
					else 
						return AminoAcid.Leu;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Ser;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==A || rna.charAt(2)==C)
						return AminoAcid.Tyr;
					else
						return AminoAcid.Stop;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Cys;
					if (rna.charAt(2)==A)
						return AminoAcid.Trp;			//Differs from standard, only difference
					if (rna.charAt(2)==G)
						return AminoAcid.Trp;
				}
			}
			
			if (rna.charAt(0)==C) {
				if (rna.charAt(1)==U)
					return AminoAcid.Leu;
				if (rna.charAt(1)==C)
					return AminoAcid.Pro;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.His;
					else
						return AminoAcid.Gln; //Glutamine! Not glutamic acid
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Arg;
			}
			
			if (rna.charAt(0)==A) {
				if (rna.charAt(1)==U) {
					if (rna.charAt(2)==G)
						return AminoAcid.Met;
					else
						return AminoAcid.Ile;
				}
				if (rna.charAt(1)==C)
					return AminoAcid.Thr;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asn; //Asparagine, not aspartic acid
					else
						return AminoAcid.Lys;
				}
				if (rna.charAt(1)==G) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Ser;
					else
						return AminoAcid.Arg;
				}
			}
			
			if (rna.charAt(0)==G) {
				if (rna.charAt(1)==U) 
					return AminoAcid.Val;
				if (rna.charAt(1)==C)
					return AminoAcid.Ala;
				if (rna.charAt(1)==A) {
					if (rna.charAt(2)==U || rna.charAt(2)==C)
						return AminoAcid.Asp; //Aspartic acid, not asparagine
					else
						return AminoAcid.Glu; //Glutamic acid, not glutamine
				}
				if (rna.charAt(1)==G)
					return AminoAcid.Gly; //Glycine
			}
			
			return null;		
		}
		
	}
}
