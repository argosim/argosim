package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.FastaSequenceIterator;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaWriter;
import at.cibiv.ngs.tools.fasta.FastaSequence.CONV;
import at.cibiv.ngs.tools.util.GenomicPosition;
import at.cibiv.ngs.tools.util.GenomicPosition.COORD_TYPE;

/**
 * Pastes a sequence pasteIn into the file seqIn at positions start and saves
 * the resulting output in tempDir
 * 
 * @author niko.popitsch@univie.ac.at
 * @author bernhard.knasmueller@univie.ac.at
 * 
 */
public class PasteEvent extends ArgosimEvent {

	private EventPosition start;
	private File pasteIn;

	public PasteEvent(File seqIn, File pasteIn, File tempDir,
			EventPosition start, ILogger iLogger) {
		super("paste", seqIn, tempDir, iLogger);
		this.pasteIn = pasteIn;
		this.start = start;
	}

	@Override
	public Object processSequence() throws IOException {
		String paste = IOUtils.toString(new FileReader(pasteIn));
		paste = checkPastedFragment(paste);
		File resultFile = createTempFile();

		MultiFastaSequence mf = new MultiFastaSequence(getSeqIn(), true);
		MultiFastaWriter mw = new MultiFastaWriter(resultFile);
		GenomicPosition ipos = start.getGenomicPosition();
		for (String chr : mf.getChromosomes()) {
			// TODO: change such that chromsomes are in correct order
			FastaSequence fa = mf.getSequence(chr);
			FastaSequenceIterator it = fa.iterator(CONV.NONE);
			mw.startChrom(chr, "from " + getSeqIn()
					+ " with inserted motif at " + ipos);
			int x = 0;
			while (it.hasNext()) {
				Character c = it.next();
				GenomicPosition pos = new GenomicPosition(chr, x,
						COORD_TYPE.ZEROBASED);
				if (pos.equals(ipos)) {
					mw.write(paste);
				}

				// write character
				mw.write(c);
				x++;
			}
			// handle the case that the paste event can be applied to 1 base
			// AFTER the last one
			// (i.e.: insert fragment at the end of the existing file)
			GenomicPosition pos = new GenomicPosition(chr, x,
					COORD_TYPE.ZEROBASED);
			if (pos.equals(ipos)) {
				mw.write(paste);
			}
		}
		mw.close();

		int length = (paste.replaceAll("\n", "")).length();

		this.createLogEntry("inserted motif of length " + length
				+ " at chromosome = " + ipos.getChromosome() + ", position = "
				+ ipos.get0Position());

		return resultFile;
	}

	private String checkPastedFragment(String paste) {
		int count = StringUtils.countMatches(paste, ">");
		if (count > 1) {
			throw new RuntimeException(
					"Tried to paste a fragment, but found a multi-FASTA file. Pasting is only possible with one entry per FASTA file");
		}
		if (count == 0) {
			return paste;
		}
		
		// one entry is there but it starts with a header line; remove that header line
		String pasteNew = paste.substring(paste.indexOf('\n')+1);
		return pasteNew;

	}

	public static void main(String[] args) throws IOException {
		File seqIn = new File(
				"/project/zuse/hulkTemp/0034_150_0.05_40_50.0/tmp/hulkFile_0.tmp");
		File pasteIn = new File(
				"/project/zuse/hulkTemp/0034_150_0.05_40_50.0/tmp/hulkFile_0.tmp0.tmp0.tmp");
		File tempDir = new File("/tmp/hulkTemp/");
		SpecifiedPosition start = new SpecifiedPosition(seqIn, "chr1", 20000);
		FileLogger logger = new FileLogger(new File("/tmp/hulkTemp/mylog.txt"));
		PasteEvent pasteEvent = new PasteEvent(seqIn, pasteIn, tempDir, start,
				logger);
		File resultFile = (File) pasteEvent.processSequence();
		System.out.println("resultFile: " + resultFile);
	}
}
