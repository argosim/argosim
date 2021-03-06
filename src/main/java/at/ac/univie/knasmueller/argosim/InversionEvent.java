package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.FastaSequence.CONV;
import at.cibiv.ngs.tools.fasta.FastaSequenceIterator;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaWriter;

public class InversionEvent extends ArgosimEvent {
	private boolean restrictToRegion;
	private int restrictionStart, restrictionLen;
	private String restrictionChromosome;

	public boolean isRestrictToRegion() {
		return restrictToRegion;
	}

	public int getRestrictionStart() {
		return restrictionStart;
	}

	public String getRestrictionChromosome() {
		return restrictionChromosome;
	}

	public int getRestrictionLen() {
		return restrictionLen;
	}

	public void restrictToRegion(int start, int len, String chromosome) {
		this.restrictionChromosome = chromosome;
		this.restrictionLen = len;
		this.restrictionStart = start;
		this.restrictToRegion = true;
	}

	public void removeRestriction() {
		this.restrictToRegion = false;
		this.restrictionChromosome = null;
		this.restrictionLen = 0;
		this.restrictionStart = 0;
	}

	public InversionEvent(File seqIn, File tempDir, ILogger iLogger)
			throws Exception {
		super("invert", seqIn, tempDir, iLogger);
	}

	@Override
	public Object processSequence() throws IOException {
		File resultFile = createTempFile();
		resultFile.createNewFile();
		MultiFastaSequence mf = new MultiFastaSequence(getSeqIn(), true);
		MultiFastaWriter mw = new MultiFastaWriter(resultFile);

		if (!isRestrictToRegion()) {
			// invert whole sequence (all chromosomes)
			for (String chr : mf.getChromosomes()) {
				mw.startChrom(chr, "from " + getSeqIn() + " (inverted)");
				FastaSequence fa = mf.getSequence(chr);
				Long regionLength = fa.getLength();
				if (regionLength == null) {
					throw new IllegalArgumentException(
							"Invalid region length for chromosome " + chr);
				}
				if (regionLength < Integer.MIN_VALUE
						|| regionLength > Integer.MAX_VALUE) {
					throw new IllegalArgumentException("Invalid cast to int: "
							+ regionLength);
				}
				StringBuffer wholeSequence = fa.getRegion(0l,
						regionLength.intValue());
				StringBuffer invertedSequence = wholeSequence.reverse();
				mw.write(invertedSequence.toString());
			}
		} else {
			for (String chr : mf.getChromosomes()) {
				if (getRestrictionChromosome().equals(chr)) {
					mw.startChrom(chr, "from " + getSeqIn() + " (inverted)");
					System.out.println("restriction on chromosome " + chr);
					FastaSequence fa = mf.getSequence(chr);
					int regionLength = getRestrictionLen();
					// first write the sequence before the region to invert...
					StringBuffer firstPart = fa.getRegion(0,
							getRestrictionStart());
					mw.write(firstPart.toString());

					// now invert the region and write that part:
					StringBuffer wholeSequence = fa.getRegion(
							(long) getRestrictionStart(), regionLength);
					StringBuffer invertedSequence = wholeSequence.reverse();
					mw.write(invertedSequence.toString());

					// finally, write the sequence _after_ the region to
					// invert...
					Long seqLength = fa.getLength();
					if (seqLength == null) {
						throw new IllegalArgumentException(
								"Invalid region length for chromosome " + chr);
					}
					if (seqLength < Integer.MIN_VALUE
							|| seqLength > Integer.MAX_VALUE) {
						throw new IllegalArgumentException(
								"Invalid cast to int: " + seqLength);
					}
					StringBuffer lastPart = fa.getRegion(getRestrictionStart()
							+ getRestrictionLen(), seqLength.intValue()
							- (getRestrictionStart() + getRestrictionLen()));
					mw.write(lastPart.toString());
					// for (int currentPosition = 0; it.hasNext();
					// currentPosition++) {
					// Character c = it.next();
					// if (currentPosition >= getRestrictionStart()
					// && currentPosition < getRestrictionStart()
					// + getRestrictionLen()) {
					// mw.write(mutate(c, (float) getMutationProbability()));
					// } else {
					// mw.write(c);
					// }
					// }
				} else {
					FastaSequence fa = mf.getSequence(chr);
					FastaSequenceIterator it = fa.iterator(CONV.NONE);
					mw.startChrom(chr, "from " + getSeqIn());
					System.out.println("not on restricted chromosome (" + chr
							+ ", " + getRestrictionChromosome() + ")");
					while (it.hasNext()) {
						Character c = it.next();
						mw.write(c);
					}
				}
			}
		}

		String restrictionInformation = isRestrictToRegion() ? new String(
				"restricted to region: chromosome = "
						+ getRestrictionChromosome() + ", start = "
						+ getRestrictionStart() + ", len = "
						+ getRestrictionLen()) : new String("no restrictions");

		this.createLogEntry("inverted sequence" + "; " + restrictionInformation);

		mw.close();
		return resultFile;
	}

	public static void main(String[] args) {
		File inputSequence = new File(
				"/project/bakk/genomes-test/ecoli-test2.fa");
		File tmpDir = new File("/tmp/tmp/");
		FileLogger fileLogger = new FileLogger(new File("/tmp/tmp/myLog.txt"));

		try {
			InversionEvent inversionEvent = new InversionEvent(inputSequence,
					tmpDir, fileLogger);
			File resultFile = (File) inversionEvent.processSequence();
			System.out.println("ResultFile: " + resultFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
