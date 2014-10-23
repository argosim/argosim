package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.util.GenomicPosition;
import at.cibiv.ngs.tools.util.GenomicPosition.COORD_TYPE;

/**
 * Specifies a position on the sequence.
 * @author bernhard.knasmueller@univie.ac.at
 *
 */

public class SpecifiedPosition extends EventPosition {

	String chromosome = null;
	int position;
	
	public SpecifiedPosition(File seqIn, String chromosome, int position) throws FileNotFoundException {
		super(seqIn);
		this.chromosome = chromosome;
		this.position = position;
	}

	public String getChromosome() {
		return chromosome;
	}

	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}

	@Override
	public GenomicPosition getGenomicPosition() throws IOException, RuntimeException {
		MultiFastaSequence mfasta = new MultiFastaSequence(getSeqIn(), true);
		String chr = this.chromosome;
		Set<String> allChromosomes = mfasta.getChromosomes();
		if (!allChromosomes.contains(chr)) {
			System.out.println(allChromosomes);
			throw new RuntimeException("Specified Chromosome Identifier does not exist in the input file");
		}
		FastaSequence s = mfasta.getSequence(chr);
		s.validate(null);
		long l = s.getLength();
		if(this.position > l) { // TODO changed from >= to > - watch if this breaks anything
			 //changed because e.g. PasteEvents requires to use 1 pos AFTER last one
			throw new RuntimeException("Position exceeds sequence length (" + s.getLength() + ")");			
		}
		return new GenomicPosition(chr, this.position, COORD_TYPE.ZEROBASED);

	}

	public static void main(String[] args) throws IOException {
		String seq = "/project/bakk/genomes-test/ecoli2-test.fa";
		for (int i = 0; i < 50; i++) {
			SpecifiedPosition s = new SpecifiedPosition(new File(seq), "chr1", i);
			System.out.println(s.getGenomicPosition());
		}
	}

}
