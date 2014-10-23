package at.ac.univie.knasmueller.argos.explorer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.univie.knasmueller.argosim.SpecifiedPosition;
import at.cibiv.codoc.CompressedCoverageIterator;
import at.cibiv.codoc.CoverageDecompressor;
import at.cibiv.codoc.CoverageHit;
import at.cibiv.codoc.CoverageTools;
import at.cibiv.codoc.CoverageTools.OPERATOR;
import at.cibiv.codoc.utils.CodocException;
import at.cibiv.ngs.tools.util.GenomicPosition;

public class CodocAnalyser {

	public static class FeatureRegion {
		int start, end;
		String chr;
		int thresholdPosition;

		FeatureRegion(int start, int end, String chr) {
			this.start = start;
			this.end = end;
			this.chr = chr;
			thresholdPosition = -1;
		}
	}

	private static final double ISS_THRESHOLD = 60;
	int WINDOW_SIZE;
	double LEVEL_THRESHOLD;
	Map<String, Integer> CHROMOSOME_LENGTHS;
	int ARGOS_READ_LENGTH;

	CoverageDecompressor decompressorISS;

	File fileISS;
	int rightLimit;
	int leftLimit;
	String experimentIndex;

	public CodocAnalyser(String ID, int windowSize, double levelThreshold,
			int readLength, String experimentIndex) {
		WINDOW_SIZE = windowSize;
		this.experimentIndex = experimentIndex;
		LEVEL_THRESHOLD = levelThreshold;
		CHROMOSOME_LENGTHS = new HashMap<String, Integer>();

		// TODO remove:
		CHROMOSOME_LENGTHS.put("chr1", 151000);
		CHROMOSOME_LENGTHS.put("chr2", 150000);
		CHROMOSOME_LENGTHS.put("chr3", 150000);
		// end

		rightLimit = 0;
		leftLimit = 0;

		ARGOS_READ_LENGTH = readLength;

		decompressorISS = null;
		fileISS = new File("/project/zuse/output/" + experimentIndex +"/" + ID
				+ "/result-GENOME.ISS.wig.codoc");

		try {
			decompressorISS = CoverageDecompressor.loadFromFile(fileISS, null);
		} catch (Exception e) {
			System.out.println("File not found.");
		}

	}

	public List<Integer> analyze() {
		List<Integer> features = new ArrayList<Integer>();
		try {
			// find min and max values:
			for (String chr : decompressorISS.getChromosomes()) {
				boolean lastBaseOverThreshold = true;
				CompressedCoverageIterator it = decompressorISS
						.getCoverageIterator();
				while (it.hasNext()) {
					it.next();

					GenomicPosition currentPos = it.getGenomicPosition();
					if (currentPos.getChromosome() == chr) {
						CoverageHit hitQuery = decompressorISS
								.query(currentPos);
						float value = hitQuery.getInterpolatedCoverage();

						if (lastBaseOverThreshold) {
							if (value <= ISS_THRESHOLD) {
								// found feature
								features.add((int) currentPos.get0Position());
								lastBaseOverThreshold = false;
							} else {
								continue;
							}
						} else {
							if (value > ISS_THRESHOLD) {
								// found feature
								features.add((int) currentPos.get0Position());
								lastBaseOverThreshold = true;
							} else {
								continue;
							}
						}

					}
				}
			}
			return features;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				decompressorISS.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

//	public List<FeatureRegion> analyze() {
//		// features is returned after analysis is completed
//		List<FeatureRegion> features = new ArrayList<FeatureRegion>();
//		try {
//			// find min and max values:
//			for (String chr : decompressorISS.getChromosomes()) {
//				float min = Float.MAX_VALUE, max = 0;
//
//				CompressedCoverageIterator it = decompressorISS
//						.getCoverageIterator();
//				while (it.hasNext()) {
//					it.next();
//
//					GenomicPosition currentPos = it.getGenomicPosition();
//					if (currentPos.getChromosome() == chr) {
//
//						CoverageHit hitQuery = decompressorISS
//								.query(currentPos);
//						float value = hitQuery.getInterpolatedCoverage();
//						// check the value WINDOW_SIZE positions after this one:
//						float valueAfterWindow = 0;
//						if (currentPos.get0Position() + WINDOW_SIZE >= CHROMOSOME_LENGTHS
//								.get(chr)) {
//							continue;
//						}
//
//						// fix bug where there is an area at the end of each
//						// chromosome
//						// that is not covered by any signals
//						// this area is as wide as the set read length in ARGOS
//
//						if (currentPos.get0Position() >= CHROMOSOME_LENGTHS
//								.get(chr) - ARGOS_READ_LENGTH * 1.2) {
//							continue;
//						}
//
//						try {
//							valueAfterWindow = (decompressorISS
//									.query(currentPos.add(WINDOW_SIZE)))
//									.getInterpolatedCoverage();
//						} catch (Exception e) {
//							// e.printStackTrace();
//							continue;
//						}
//						if (Math.abs(value - valueAfterWindow) > LEVEL_THRESHOLD) {
//							int featureStart = (int) currentPos.get0Position();
//							// we see that the ISS score is decreasing;
//							// go on until this decrease stops
//							int lastPositionDecreasing = (int) currentPos
//									.get0Position();
//							boolean stillDecreasing = true;
//							do {
//								it.next();
//								currentPos = it.getGenomicPosition();
//								hitQuery = decompressorISS.query(currentPos);
//								value = hitQuery.getInterpolatedCoverage();
//								try {
//									valueAfterWindow = (decompressorISS
//											.query(currentPos.add(WINDOW_SIZE)))
//											.getInterpolatedCoverage();
//								} catch (Exception e) {
//									// e.printStackTrace();
//									stillDecreasing = false;
//									continue;
//								}
//								if (Math.abs(value - valueAfterWindow) > LEVEL_THRESHOLD) {
//									lastPositionDecreasing = (int) currentPos
//											.get0Position();
//								} else {
//									stillDecreasing = false;
//								}
//							} while (currentPos.getChromosome() == chr
//									&& stillDecreasing);
//							FeatureRegion fr = new FeatureRegion(featureStart,
//									lastPositionDecreasing, chr);
//							if (leftLimit != 0 && featureStart < leftLimit)
//								continue;
//							if (rightLimit != 0
//									&& lastPositionDecreasing > rightLimit) {
//								continue;
//							}
//							features.add(fr);
//						}
//					}
//				}
//			}
//			return features;
//
//		} catch (Exception e) {
//
//		} finally {
//			try {
//				decompressorISS.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}

	public static void analyzeExperiment2(String[] args) throws Exception {

		String experimentIndex = "0037";

		double[] mutationFrequencies = { 0, 0.02, 0.04, 0.06, 0.08, 0.10, 0.12,
				0.14, 0.16, 0.18, 0.2, 0.25 };
		int[] fragmentLengths = { 400, 150 };
		double[] percentageReadLengths = { 0.05 };
		int[] readLengths = { 30, 40, 50, 60, 70, 80, 90, 100, 200, 400 };

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				"/project/bakk/mylog_" + experimentIndex + ".txt", true)));

		for (double p : percentageReadLengths) {
			for (int readLength : readLengths) {
				for (int len : fragmentLengths) {
					for (double mutFreq : mutationFrequencies) {

						out.println("fragment len = " + len + ", readLength = "
								+ readLength + ", % = " + p + ", mutFreq " + mutFreq);
						String ID = experimentIndex + "_" + len + "_" + p + "_"
								+ readLength + "_" + mutFreq;
						int windowSize = 30;
						double levelThreshold = 20;

						CodocAnalyser codocAnalyser = new CodocAnalyser(ID,
								windowSize, levelThreshold, readLength, experimentIndex);
						codocAnalyser.setLeftLimit(19000);
						codocAnalyser.setRightLimit(21000);
						List<Integer> results;
						try {
							results = codocAnalyser.analyze();
							codocAnalyser.display(results, out);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		out.close();
	}
	
	public static void analyzeExperimentEcoliGlobal(String[] args) throws Exception {

		String experimentIndex = "0037";

		double[] mutationFrequencies = { 0, 0.02, 0.04, 0.06, 0.08, 0.10, 0.12,
				0.14, 0.16, 0.18, 0.2, 0.25 };
		int[] fragmentLengths = { 400, 150 };
		double[] percentageReadLengths = { 0.05 };
		int[] readLengths = { 30, 40, 50, 60, 70, 80, 90, 100, 200, 400 };

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				"/project/bakk/mylog_" + experimentIndex + ".txt", true)));
		
		File outputFile = new File("/project/bakk/myCodocOutput.codoc");
		File file1 = new File("/project/zuse/output/0040_1000_0.05_50_0.0/result-GENOME.ISS.wig.codoc");
		File file2 = new File("/project/zuse/output/0039_1000_0.05_50_0.0/result-GENOME.ISS.wig.codoc");
		
		try {
			CoverageTools.combineCoverageFiles(file1, null, file2, null, OPERATOR.DIFF, outputFile);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Done. Wrote to " + outputFile);

//		for (double p : percentageReadLengths) {
//			for (int readLength : readLengths) {
//				for (int len : fragmentLengths) {
//					for (double mutFreq : mutationFrequencies) {
//
//						out.println("fragment len = " + len + ", readLength = "
//								+ readLength + ", % = " + p + ", mutFreq " + mutFreq);
//						String ID = experimentIndex + "_" + len + "_" + p + "_"
//								+ readLength + "_" + mutFreq;
//						int windowSize = 30;
//						double levelThreshold = 20;
//
//						CodocAnalyser codocAnalyser = new CodocAnalyser(ID,
//								windowSize, levelThreshold, readLength, experimentIndex);
//						codocAnalyser.setLeftLimit(19000);
//						codocAnalyser.setRightLimit(21000);
//						List<Integer> results;
//						try {
//							results = codocAnalyser.analyze();
//							codocAnalyser.display(results, out);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}
		out.close();
	}

	public static void main(String[] args) throws Exception {
		analyzeExperimentEcoliGlobal(args);
	}

	public static void analyzeExperiment1(String[] args) throws CodocException,
			IOException {
		
		// analyzes the minimal necessary fragment size for detection of a duplication event
		
		String experimentIndex = ""; //TODO fixme

		// // Single value analysis
		// int windowSize = 20;
		// double levelThreshold = 20;
		// int readLength = 60;
		// CodocAnalyser codocAnalyser = new CodocAnalyser("0030_80_0.05_100",
		// windowSize, levelThreshold, readLength);
		// codocAnalyser.setLeftLimit(19000);
		// codocAnalyser.setRightLimit(21000);
		// List<Integer> results;
		// try {
		// results = codocAnalyser.analyze2();
		// codocAnalyser.display2(results, System.out);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// System.out.println("DONE");

		// int[] fragmentLengths = { 750, 500, 250, 200, 175, 150, 125, 100, 90,
		// 80, 70, 60, 50, 40, 30, 20, 10 };

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				"/project/bakk/mylog_0033.txt", true)));

		int[] fragmentLengths = { 150, 120, 80, 70, 60, 50, 40, 30, 20, 15, 10,
				8, 6, 4 };
		double[] percentageReadLengths = { 0.05, 0.1, 0.2 };
		int[] readLengths = { 30, 40, 50, 60, 70, 80, 90, 100, 200, 400 };
		for (double p : percentageReadLengths) {
			for (int readLength : readLengths) {
				for (int len : fragmentLengths) {

					// String ID = "0024_" + len;
					out.println("fragment len = " + len + ", readLength = "
							+ readLength + ", % = " + p);
					String ID = "0033_" + len + "_" + p + "_" + readLength;
					int windowSize = 30;
					double levelThreshold = 20;

					CodocAnalyser codocAnalyser = new CodocAnalyser(ID,
							windowSize, levelThreshold, readLength, experimentIndex);
					codocAnalyser.setLeftLimit(19000);
					codocAnalyser.setRightLimit(21000);
					List<Integer> results;
					try {
						results = codocAnalyser.analyze();
						codocAnalyser.display(results, out);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		out.close();
	}

	private void setRightLimit(int i) {
		this.rightLimit = i;

	}

	private void setLeftLimit(int i) {
		this.leftLimit = i;

	}

	private void display(List<Integer> features, PrintWriter out) {
		if (features == null) {
			out.println("No features to display.");
		} else {
			for (Integer i : features) {
				out.println(i);
			}

		}
	}

//	private void display(List<FeatureRegion> features, PrintStream out) {
//
//		try {
//			for (FeatureRegion fr : features) {
//				// find position where ISS = .6:
//				double startISS = decompressorISS.query(
//						new GenomicPosition(fr.chr, fr.start))
//						.getInterpolatedCoverage();
//				double endISS = decompressorISS.query(
//						new GenomicPosition(fr.chr, fr.end))
//						.getInterpolatedCoverage();
//				int thresholdPosition = fr.start;
//				double currentISS = 0;
//
//				for (int i = fr.start; i < fr.end; i++) {
//					currentISS = decompressorISS.query(
//							new GenomicPosition(fr.chr, i))
//							.getInterpolatedCoverage();
//					if (startISS > endISS) {
//						if (currentISS < ISS_THRESHOLD) {
//							thresholdPosition = i - 1;
//							break;
//						}
//					} else {
//						if (currentISS > ISS_THRESHOLD) {
//							thresholdPosition = i - 1;
//							break;
//						}
//					}
//
//				}
//				out.println(thresholdPosition);
//				fr.thresholdPosition = thresholdPosition;
//
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

//	public static void demo_findMinMax() throws CodocException, IOException {
//		CoverageDecompressor decompressorISS = null;
//		try {
//
//			decompressorISS = CoverageDecompressor.loadFromFile(new File(
//					"/project/zuse/output/0011/result-GENOME.ISS.wig.codoc"),
//					null);
//
//			// find min and max values:
//			for (String chr : decompressorISS.getChromosomes()) {
//				float min = Float.MAX_VALUE, max = 0;
//
//				CompressedCoverageIterator it = decompressorISS
//						.getCoverageIterator();
//				while (it.hasNext()) {
//					it.next();
//
//					GenomicPosition currentPos = it.getGenomicPosition();
//					if (currentPos.getChromosome() == chr) {
//						CoverageHit hitQuery = decompressorISS
//								.query(currentPos);
//						float value = hitQuery.getInterpolatedCoverage();
//						if (min > value)
//							min = value;
//						if (max < value)
//							max = value;
//					}
//				}
//				System.out.println("Found min: " + min + ", found max: " + max);
//			}
//
//		} finally {
//			decompressorISS.close();
//		}
//	}
}
