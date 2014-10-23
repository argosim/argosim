package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;
import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.util.FileUtils;
import at.cibiv.ngs.tools.util.GenomicPosition;

/**
 * Copies a sub sequence to a new file
 * 
 * @author niko.popitsch@univie.ac.at
 * @author bernhard.knasmueller@univie.ac.at
 * 
 */
public class CopyEvent extends ArgosimEvent {

	private EventPosition start;
	private long len;

	public CopyEvent(File seqIn, File tempDir, EventPosition start, long len,
			ILogger iLogger) {
		super("copy", seqIn, tempDir, iLogger);
		this.start = start;
		this.len = len;
		System.out
				.println("New CopyEvent: seqIn = " + seqIn + ", len = " + len);
	}
	
	/**
	 * Version without length input; copies the whole fragment
	 * @param seqIn
	 * @param tempDir
	 * @param start
	 * @param iLogger
	 * @throws IOException 
	 */
	
	public CopyEvent(File seqIn, File tempDir, EventPosition start,
			ILogger iLogger) throws IOException {
		super("copy", seqIn, tempDir, iLogger);
		this.start = start;
		this.len = getLen(seqIn, start);
		System.out
				.println("New CopyEvent: seqIn = " + seqIn + ", len = " + len);
	}

	private long getLen(File seqIn, EventPosition start) throws IOException {
		MultiFastaSequence mfasta = new MultiFastaSequence(seqIn, true);
		GenomicPosition pos = start.getGenomicPosition();
		FastaSequence seq = mfasta.getSequence(pos.getChromosomeOriginal());
		seq.validate(null);
		return seq.getLength() - pos.get0Position();
	}

	@Override
	public Object processSequence() throws IOException {
		File resultFile = createTempFile();
		MultiFastaSequence mfasta = new MultiFastaSequence(getSeqIn(), true);
		GenomicPosition pos = start.getGenomicPosition();
		System.out.println(pos);
		FastaSequence seq = mfasta.getSequence(pos.getChromosomeOriginal());
		seq.validate(null);

		StringBuffer s = seq.getRegion(pos.get0Position(),
				(int) Math.min(len, seq.getLength() - pos.get0Position()));

		if (s.toString().contains(">"))
			System.err.println("ERROR " + s);
		FileUtils.writeToFileFormatted(s, resultFile, 80);

		this.createLogEntry("copied "
				+ (int) Math.min(len, seq.getLength() - pos.get0Position())
				+ " bp sequence from chromosome = " + pos.getChromosome()
				+ ", position = " + pos.get0Position());
		return resultFile;
	}

	public static void main(String[] args) throws IOException {
		File seq = new File("/project/bakk/genomes-test/ecoli2-test.fa");
		File tmp = new File("/project/bakk/tmp/");

		FileLogger fileLogger = new FileLogger(new File(
				"/project/bakk/tmp/myLog.txt"));

		for (int i = 0; i < 10; i++) {
			CopyEvent ce = new CopyEvent(seq, tmp, new SpecifiedPosition(seq,
					"chr2", i), 5, fileLogger);
			File res = (File) ce.processSequence();
			System.out.println("created " + res);
		}
	}

}
