package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.FastaSequenceIterator;
import at.cibiv.ngs.tools.fasta.MultiFastaWriter;
import at.cibiv.ngs.tools.fasta.FastaSequence.CONV;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.util.GenomicPosition;
import at.cibiv.ngs.tools.util.GenomicPosition.COORD_TYPE;

/**
 * Generates two files: a copy of the input-file without the specified fragment
 * and the cut fragment
 * 
 * @author bernhard.knasmueller@univie.ac.at
 * 
 */

public class CutEvent extends ArgosimEvent {

	private EventPosition start;
	private int len;

	public CutEvent(File seqIn, File tempDir, EventPosition start, int len, ILogger iLogger) {
		super("cut", seqIn, tempDir, iLogger);
		this.start = start;
		this.len = len;
		System.out.println("New CutEvent: seqIn = " + seqIn + ", len = " + len);
	}

	@Override
	public Object processSequence() throws IOException {
		File remainder = createTempFile();
		remainder.createNewFile();
		File cutFragment = createTempFile();
		cutFragment.createNewFile();
		MultiFastaSequence mfasta = new MultiFastaSequence(getSeqIn(), true);
		GenomicPosition pos = start.getGenomicPosition();
		pos = start.getGenomicPosition();
		System.out.println("Position: " + pos);

		FastaSequence seq = mfasta.getSequence(pos.getChromosomeOriginal()); // get
																				// chromosome
																				// name
																				// of
																				// the
																				// input
																				// position
		seq.validate(null);

		String s = seq.getRegion(pos.get0Position(),
				(int) Math.min(len, seq.getLength() - pos.get0Position()),
				CONV.NONE);

		if (s.toString().contains(">"))
			System.err.println("ERROR " + s);
		System.out.println(s);

		MultiFastaSequence mf = new MultiFastaSequence(getSeqIn(), true);
		MultiFastaWriter mwWithoutFragment = new MultiFastaWriter(
				remainder);
		MultiFastaWriter mwFragment = new MultiFastaWriter(cutFragment);

		for (String chr : mf.getChromosomes()) {
			FastaSequence fa = mf.getSequence(chr);
			FastaSequenceIterator it = fa.iterator(CONV.NONE);
			mwWithoutFragment.startChrom(chr, "from " + getSeqIn()
					+ " with cut motif at " + pos);
			int x = 0;
			while (it.hasNext()) {
				Character c = it.next();
				GenomicPosition currentPosition = new GenomicPosition(chr, x,
						COORD_TYPE.ZEROBASED);
				if (currentPosition.compareTo(pos) >= 0
						&& currentPosition.compareTo(pos.add(len)) == -1) {
					mwFragment.write(c);
				} else {
					// write character
					mwWithoutFragment.write(c);
				}

				x++;
			}
		}
		mwWithoutFragment.close();
		mwFragment.close();

		Map<String, File> returnMap = new HashMap<String, File>();
		returnMap.put("cutFragment", cutFragment);
		returnMap.put("remainder", remainder);
		
		this.createLogEntry("cut "
				+ (int) Math.min(len, seq.getLength() - pos.get0Position())
				+ " bp sequence from chromosome = " + pos.getChromosome()
				+ ", position = " + pos.get0Position());
		
		return returnMap;
	}

	public static void main(String[] args) throws IOException {
		File seq = new File("/project/bakk/genomes-test/ecoli2-test.fa");
		File tmp = new File("/project/bakk/genomes-test/tmp");
		
		FileLogger fileLogger = new FileLogger(new File("/project/bakk/tmp/myLog.txt"));

		for (int i = 0; i < 10; i++) {
			CutEvent ce = new CutEvent(seq, tmp, new SpecifiedPosition(seq,
					"chr2", i), 5, fileLogger);
			@SuppressWarnings("unchecked")
			Map<String, File> cutResults = (Map<String, File>) ce
					.processSequence();

			File cutFragment = cutResults.get("cutFragment");
			File remainder = cutResults.get("remainder");

			System.out.println("cutFragment: " + cutFragment + ", remainder: "
					+ remainder);
		}
	}

}
