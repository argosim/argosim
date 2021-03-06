package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.FastaSequence.CONV;
import at.cibiv.ngs.tools.fasta.FastaSequenceIterator;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaWriter;

public class PointMutationEvent extends MutationEvent {
	private double mutationProbability;
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

	public PointMutationEvent(File seqIn, File tempDir,
			double mutationProbability, ILogger iLogger) throws Exception {
		super("point_mutate", seqIn, tempDir, iLogger);
		setMutationProbability(mutationProbability);
	}

	@Override
	public Object processSequence() throws IOException {
		File resultFile = createTempFile();
		resultFile.createNewFile();
		MultiFastaSequence mf = new MultiFastaSequence(getSeqIn(), true);
		MultiFastaWriter mw = new MultiFastaWriter(resultFile);

		if (!isRestrictToRegion()) {
			// mutate whole sequence
			for (String chr : mf.getChromosomes()) {
				FastaSequence fa = mf.getSequence(chr);
				FastaSequenceIterator it = fa.iterator(CONV.NONE);
				mw.startChrom(chr, "from " + getSeqIn()
						+ " point-mutated with Pr(mutation) = "
						+ getMutationProbability());
				while (it.hasNext()) {
					Character c = it.next();
					mw.write(mutate(c, (float) getMutationProbability()));
				}
			}
		} else {
			for (String chr : mf.getChromosomes()) {
				FastaSequence fa = mf.getSequence(chr);
				FastaSequenceIterator it = fa.iterator(CONV.NONE);
				mw.startChrom(chr, "from " + getSeqIn());
				if (getRestrictionChromosome().equals(chr)) {
					System.out.println("restriction on chromosome " + chr);
					for (int currentPosition = 0; it.hasNext(); currentPosition++) {
						Character c = it.next();
						if (currentPosition >= getRestrictionStart()
								&& currentPosition < getRestrictionStart()
										+ getRestrictionLen()) {
							mw.write(mutate(c, (float) getMutationProbability()));
						} else {
							mw.write(c);
						}
					}
				} else {
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
						+ getRestrictionLen())
		: new String("no restrictions");

		this.createLogEntry("applied point mutations with probability "
				+ getMutationProbability() + "; " + restrictionInformation);

		mw.close();
		return resultFile;
	}

	// CODE FROM cibiv ngs-tools (MultiFastaSequence::mutate() )
	// could not use original code because it was private

	static char[] CTG = new char[] { 'C', 'T', 'G' };
	static char[] AGT = new char[] { 'A', 'G', 'T' };
	static char[] GAC = new char[] { 'G', 'A', 'C' };
	static char[] TCA = new char[] { 'T', 'C', 'A' };

	private static Character mutate(Character c, Float mutRate) {
		Random generator = new Random();

		if ((mutRate == null) || (mutRate == 0f))
			return c;
		if (generator.nextFloat() > mutRate)
			return c;
		// mutate
		int dice = generator.nextInt(3);
		if ((c == 'A') || (c == 'a')) {
			c = CTG[dice];
		} else if ((c == 'G') || (c == 'g')) {
			c = TCA[dice];
		} else if ((c == 'C') || (c == 'c')) {
			c = AGT[dice];
		} else if ((c == 'T') || (c == 't')) {
			c = GAC[dice];
		}
		return c;
	}

	// END code from cibiv ngs-tools

	public double getMutationProbability() {
		return mutationProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		if(mutationProbability < 0 || mutationProbability > 1.0) {
			throw new RuntimeException("mutation probability must be between 0.0 and 1.0");
		}
		this.mutationProbability = mutationProbability;
	}

	public static void main(String[] args) {
		File inputSequence = new File(
				"/project/bakk/genomes-test/hg19/chr21.fa");
		File tmpDir = new File("/project/bakk/genomes-test/tmp");
		FileLogger fileLogger = new FileLogger(new File("/project/bakk/tmp/myLog.txt"));
		
		double mutationProbability = 0.0001;
		try {
			PointMutationEvent pointMutationEvent = new PointMutationEvent(
					inputSequence, tmpDir, mutationProbability, fileLogger);
			File resultFile = (File) pointMutationEvent.processSequence();
			System.out.println("ResultFile: " + resultFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
