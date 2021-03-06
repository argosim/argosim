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
 * Creates random positions within the wrapped sequence. The chromosome can be fixed.
 * @author niko.popitsch@univie.ac.at
 * @author bernhard.knasmueller@univie.ac.at
 *
 */
public class RandomPosition extends EventPosition {

	String fixedChrom = null;

	public RandomPosition(File seqIn) throws FileNotFoundException {
		super(seqIn);
	}

	public String getFixedChrom() {
		return fixedChrom;
	}

	public void setFixedChrom(String fixedChrom) {
		this.fixedChrom = fixedChrom;
	}

	@Override
	public GenomicPosition getGenomicPosition() throws IOException {
		MultiFastaSequence mfasta = new MultiFastaSequence(getSeqIn(), true);
		Set<String> chrs = mfasta.getChromosomes();
		String chr = fixedChrom;
		if (chr == null) {
			chr = (String) chrs.toArray()[rand.nextInt(chrs.size())];
		}
		FastaSequence s = mfasta.getSequence(chr);
		s.validate(null);
		long l = s.getLength();
		int pos0 = rand.nextInt((int) l);
		return new GenomicPosition(chr, pos0, COORD_TYPE.ZEROBASED);
	}

	public static void main(String[] args) throws IOException {
		String seq = "/project/bakk/genomes-test/ecoli2-test.fa";
		for (int i = 0; i < 100; i++) {
			RandomPosition r = new RandomPosition(new File(seq));
			System.out.println(r.getGenomicPosition());
		}
		System.out.println("------------------");
		for (int i = 0; i < 100; i++) {
			RandomPosition r = new RandomPosition(new File(seq));
			r.setFixedChrom("chr1");
			System.out.println(r.getGenomicPosition());
		}
	}

}
