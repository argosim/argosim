package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.FastaSequence.CONV;
import at.cibiv.ngs.tools.fasta.FastaSequenceIterator;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaWriter;
import at.cibiv.ngs.tools.sam.iterator.ParseException;
import at.cibiv.ngs.tools.vcf.SimpleVCFFile;
import at.cibiv.ngs.tools.vcf.SimpleVCFVariant;

/**
 * Takes a VCF file and applies the specified mutations to the target sequence
 * (based on a random number generator)
 * 
 * @author bernhard.knasmueller@univie.ac.at
 * 
 */

public class VCFMutationEvent extends MutationEvent {
	protected File vcfFile;
	protected double mutationProbability;
	protected int globalOffset;

	public enum LogLevel {
		BASIC, FULL
	}

	private LogLevel logLevel;
	private int appliedVariantsCounter;

	public int getAppliedVariantsCounter() {
		return appliedVariantsCounter;
	}

	public void incrementAppliedVariantsCounter() {
		this.appliedVariantsCounter++;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public int getGlobalOffset() {
		return globalOffset;
	}

	public void setGlobalOffset(int globalOffset) {
		this.globalOffset = globalOffset;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	public File getVcfFile() {
		return vcfFile;
	}

	public void setVcfFile(File vcfFile) throws Exception {
		if(!vcfFile.exists()) {
			throw new Exception("provided vcf file does not exist");
		}
		this.vcfFile = vcfFile;
	}

	public VCFMutationEvent(File seqIn, File tempDir, File vcfFile,
			ILogger iLogger) throws Exception {
		super("vcf_mutate", seqIn, tempDir, iLogger);
		setVcfFile(vcfFile);
		setMutationProbability(mutationProbability);
		setGlobalOffset(0);
		setLogLevel(LogLevel.BASIC);
	}

	private String replaceSubsequence(String inputSeq, String replacement,
			double[] alleleFrequencies) {
		/**
		 * decides whether the replacement is applied or not based on a random
		 * number generator
		 */

		int randIndex = 0;
		if (replacement.contains(",")) {
			// if multiple possibilities exist, take one at random
			String[] replacements = replacement.split(",");
			if (alleleFrequencies.length != replacements.length) {
				throw new RuntimeException(
						"Fatal error: one allele frequency per alternate allele expected; check if you have one AF per alternate allele");
			}
			randIndex = (int) (Math.random() * (replacements.length));
			replacement = replacements[randIndex].trim();
			/*
			 * TODO: this is at least questionable - not sure if this is
			 * statistically correct
			 */
		}

		if (Math.random() < alleleFrequencies[randIndex]) {
			incrementAppliedVariantsCounter();
			return replacement;
		} else {
			return inputSeq;
		}
	}

	private double[] getAlleleFrequencies(SimpleVCFVariant variant) throws Exception {
		double[] result = null;
		String AfField = variant.getInfoField("AF");
		if (AfField == null || AfField.isEmpty() || AfField.equals("")) {
			/* try to get AFs from somewhere else */

			// format in 1000genomes:
			
			// CAF=[0.9527,0.04729]
			// CAUTION: here the first entry denotes the Pr of the REFERENCE allele and
			// should therefore NOT be considered for this purpose!
			
			String CafField = variant.getInfoField("CAF");
			if(CafField != null && !CafField.isEmpty() && !CafField.equals("")) {
				CafField = CafField.replace("[", "");
				CafField = CafField.replace("]", "");
				if(!CafField.contains(",")) {
					throw new Exception("Using the CAF field, there must be at least 2 entries where the first one represents the reference allele frequency");
				} else {
					String[] split = CafField.split(",");
					int i=-1;
					result = new double[split.length - 1];
					for(String str : split) {
						if(i==-1) {
							i = 0;
							continue; // first entry is for reference allele - don't care
						}
						result[i++] = Double.parseDouble(str.trim());
					}
					return result;
				}
			}
			
			/*
			 * just generate frequencies such that each allele has same
			 * frequency
			 */
			String alleles = variant.getAltString();
			if (!alleles.contains(",")) {
				return new double[] { 1.0 };
			} else {
				String[] split = alleles.split(",");
				result = new double[split.length];
				int i = 0;
				for (String str : split) {
					result[i++] = 1.0 / (double) split.length;
					System.out.println("freq: " + result[i - 1]);
					if (result[0] > 1 || result[0] < 0) {
						throw new RuntimeException(
								"Unexpected value of Allele Frequency for variant "
										+ variant.getID());
					}
				}
				return result;
			}
		} else {
			if (AfField.contains(",")) {
				/* more than one frequency, return all of them */
				String[] splitAfField = AfField.split(",");
				result = new double[splitAfField.length];
				int i = 0;
				for (String cur : splitAfField) {
					result[i++] = Double.parseDouble(cur);
					if (result[i - 1] > 1 || result[i - 1] < 0) {
						throw new RuntimeException(
								"Unexpected value of Allele Frequency for variant "
										+ variant.getID());
					}
				}
			} else {
				/* just return the one frequency present */
				result = new double[1];
				result[0] = Double.parseDouble(AfField);
				if (result[0] > 1 || result[0] < 0) {
					throw new RuntimeException(
							"Unexpected value of Allele Frequency for variant "
									+ variant.getID());
				}
			}
		}
		return result;
	}

	@Override
	public Object processSequence() throws Exception {
		File resultFile = createTempFile();
		resultFile.createNewFile();
		MultiFastaSequence mf = new MultiFastaSequence(getSeqIn(), true);
		MultiFastaWriter mw = new MultiFastaWriter(resultFile);
		try {

			for (String chr : mf.getChromosomes()) {
				SimpleVCFFile simpleVCFFile = new SimpleVCFFile(getVcfFile());
				List<SimpleVCFVariant> vcfVariants = simpleVCFFile
						.getVariants(chr); // get all variants for the current
											// chromosome

				FastaSequence fa = mf.getSequence(chr);
				FastaSequenceIterator it = fa.iterator(CONV.NONE);
				mw.startChrom(chr, "from " + getSeqIn()
						+ " with applied VCF mutations");
				int currPos = 0;
				if (vcfVariants.size() == 0) {
					while (it.hasNext()) {
						Character c = it.next();
						mw.write(c);
						currPos++;
					}
					continue; // move on to next chr
				} else {
					Iterator<SimpleVCFVariant> vcfIt = vcfVariants.iterator();
					while (vcfIt.hasNext()) {
						SimpleVCFVariant currentVariant = vcfIt.next();

						while (currPos < (currentVariant.getPosition0() + getGlobalOffset())) {
							mw.write(it.next());
							currPos++;
						}
						// apply variant based on random number:
						String toWrite = replaceSubsequence(
								currentVariant.getRefString(),
								currentVariant.getAltString(),
								/* getMutationProbability() */getAlleleFrequencies(currentVariant));

						if (getLogLevel() == LogLevel.FULL) {
							if (!toWrite.equals(currentVariant.getRefString())) {
								this.createLogEntry("replaced "
										+ currentVariant.getRefString()
										+ " with "
										+ toWrite
										+ " @ pos(0) = "
										+ (currentVariant.getPosition0() + getGlobalOffset())
										+ " on chromosome "
										+ currentVariant.getChromosome());
							}
						}
						// toWrite now contains the correct string that will be
						// written
						// to the fasta file
						mw.write(toWrite);
						currPos += toWrite.length();
						it.skip(currentVariant.getRefString().length());

						// handle that insertions and deletions can change the
						// positions of the remaining
						// variants
						int lengthDifference = toWrite.length()
								- currentVariant.getRefString().length();
						setGlobalOffset(getGlobalOffset() + lengthDifference);
					}

					// now write everything from the last variant until the end
					while (it.hasNext()) {
						mw.write(it.next());
						currPos++;
					}
				}

			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mw.close();

		if (getLogLevel() == LogLevel.BASIC) {
			this.createLogEntry("applied " + getAppliedVariantsCounter()
					+ " variant(s) from vcf-file " + getVcfFile());
		}

		return resultFile;
	}

	public static void main(String[] args) throws Exception {
		// File inputSequence = new
		// File("/project/bakk/genomes-test/hg19/chr21.fa");
		// File tmpDir = new File("/project/bakk/genomes-test/tmp");
		// File vcfFile = new File("/project/bakk/vcf/21-12163-TSI.vcf");
		File inputSequence = new File(
				"/project/bakk/genomes-test/ecoli2-test.fa");
		File tmpDir = new File("/project/bakk/genomes-test/tmp");
		File vcfFile = new File("/project/bakk/vcf/experimental_tiny_caf.vcf");
		double mutationProbability = 1;
		FileLogger fileLogger = new FileLogger(new File(
				"/project/bakk/tmp/myLog.txt"));
		VCFMutationEvent vcfMutation = new VCFMutationEvent(inputSequence,
				tmpDir, vcfFile, fileLogger);
		vcfMutation.setLogLevel(LogLevel.FULL);

		try {
			File resultFile = (File) vcfMutation.processSequence();
			System.out.println("ResultFile: " + resultFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
