package at.ac.univie.knasmueller.argos.explorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import at.ac.univie.knasmueller.argosim.ArgosimFile;
import at.ac.univie.knasmueller.argosim.ArgosimProject;
import at.ac.univie.knasmueller.argosim.RandomPosition;
import at.ac.univie.knasmueller.argosim.SpecifiedPosition;
import at.cibiv.ngs.tools.fasta.FastaTools;

public class Explorer {

	public static void createRandomGenome(int length, int chromosomes, File out)
			throws IOException {
		if (!out.exists()) {
			out.getParentFile().mkdirs();
			out.createNewFile();
		} else {
			throw new IOException("Target file already exists.");
		}
		PrintStream outStream = new PrintStream(new FileOutputStream(out));
		FastaTools.createRandomFasta(length, chromosomes, outStream);
	}

	public static void runArgos(String inputGenome, String outputFolder,
			String tempFolder, int readLength, int stepSize) {
		runArgos(inputGenome, outputFolder, tempFolder, readLength, stepSize,
				false);
	}

	public static void runArgos(String inputGenome, String outputFolder,
			String tempFolder, int readLength, int stepSize, boolean useGlobal) {
		try {
			ProcessBuilder builder;
			String pathToScript = "/home/CIBIV/bernhard_/argos-eval/run_16.sh";
			if (useGlobal) {
				builder = new ProcessBuilder("bash", pathToScript, "-g",
						inputGenome, "-o ", outputFolder, "-useGlobal", "-t",
						tempFolder, "-rl", ((Integer) readLength).toString(),
						"-step", ((Integer) stepSize).toString() /*
																 * , ">",
																 * "/dev/null",
																 * "2>&1"
																 */);
			} else {
				builder = new ProcessBuilder("bash", pathToScript, "-g",
						inputGenome, "-o ", outputFolder, "-t", tempFolder,
						"-rl", ((Integer) readLength).toString(), "-step",
						((Integer) stepSize).toString() /*
														 * , ">", "/dev/null",
														 * "2>&1"
														 */);
			}

			builder.directory(new File("/home/CIBIV/bernhard_/argos-eval/"));

			Map<String, String> env = builder.environment();
			String path = env.get("PATH");
			String libPath = "/usr/local/bin/python:/software/ngm/bin-linux/:/software/sge-2011.11/bin/linux-x64:/usr/lib64/mpi/gcc/openmpi/bin:/home/CIBIV/bernhard_/bin:/usr/local/bin:/usr/bin:/bin:/usr/bin/X11:/usr/X11R6/bin:/usr/games:/opt/kde3/bin:/usr/lib/mit/bin:/usr/lib/mit/sbin";
			path = path + File.pathSeparator + libPath;
			env.put("PATH", path);

			Process p = builder.start();
			List<String> ls = builder.command();

			// for (String s : ls) {
			// System.out.println(s);
			// }

			String output = loadStream(p.getInputStream());
			String error = loadStream(p.getErrorStream());
			int rc = p.waitFor();
			System.out.println("Process ended with rc=" + rc);
			System.out.println("\nStandard Output:\n");
			System.out.println(output);
			System.out.println("\nStandard Error:\n");
			System.out.println(error);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Process is done; output was written to "
					+ outputFolder);
		}
	}

	private static String introduceMutations(File genome, String ID) {
		ArgosimProject p = new ArgosimProject("hulkTemp", new File(
				"hulkResult/" + ID + "/"));
		try {
			ArgosimFile hf = new ArgosimFile(p, genome);
			ArgosimFile fragment = hf.copy(new SpecifiedPosition(genome,
					"chr1", 20000), 10000);
			ArgosimFile result = hf.insert(fragment,
					new SpecifiedPosition(hf.getFileAbs(), "chr1", 60000));
			String resultName = "result.fa";
			result.save(resultName);
			return resultName;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String noMutations(File genome, String ID) {
		ArgosimProject p = new ArgosimProject("hulkTemp", new File(
				"hulkResult/" + ID + "/"));
		try {
			ArgosimFile hf = new ArgosimFile(p, genome);
			String resultName = "result.fa";
			hf.save(resultName);
			return resultName;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String measureInsertPosition(File genome, String ID) {
		ArgosimProject p = new ArgosimProject("hulkTemp", new File(
				"hulkResult/" + ID + "/"));
		try {
			ArgosimFile hf = new ArgosimFile(p, genome);
			RandomPosition rpos = new RandomPosition(genome);
			rpos.setFixedChrom("chr1");
			ArgosimFile fragment = hf.copy(rpos, 10000); // copy 10kbp fragment
															// to
															// a random position
															// on
															// same chromosome
			// DEBUG:
			fragment.save("tempCOPYFRAGMENT.fa");
			ArgosimFile result = hf.insert(fragment, rpos); // rpos generates a
															// new
															// random position
															// on every call of
															// getGenomicPosition()
			String resultName = "result.fa";
			result.save(resultName);
			return resultName;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String loadStream(InputStream s) throws Exception {
		/*
		 * from
		 * http://stackoverflow.com/questions/4225663/problem-processbuilder
		 * -running-script-sh
		 */
		BufferedReader br = new BufferedReader(new InputStreamReader(s));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null)
			sb.append(line).append("\n");
		return sb.toString();
	}

	public static void testDetectability(int readLength, int stepSize,
			int duplicationFragmentSize, String ID) throws Exception {
		File targetGenome = null;
		File randomGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/random150k.fa");
		if (!randomGenome.exists()) {
			try {
				Explorer.createRandomGenome(150000, 3, randomGenome); /*
																	 * only work
																	 * with the
																	 * first 2
																	 * chromosomes
																	 * ; last
																	 * one
																	 * always
																	 * makes
																	 * issues
																	 * with
																	 * bigWig
																	 * Files
																	 */
				System.out.println("Created random genome at: " + randomGenome);
				targetGenome = randomGenome;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		} else {
			targetGenome = randomGenome;
		}

		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length
			ArgosimFile fragment = hf.copy(new SpecifiedPosition(targetGenome,
					"chr1", 20000), duplicationFragmentSize);
			ArgosimFile result = hf.insert(fragment,
					new SpecifiedPosition(hf.getFileAbs(), "chr1", 60000));
			resultGenomeName = "result.fa";
			result.save(resultGenomeName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void testDetectabilityPointMutation(int readLength,
			int stepSize, int fragmentLength, double mutFreq, String ID)
			throws Exception {
		File targetGenome = null;
		File randomGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/random150k.fa");
		if (!randomGenome.exists()) {
			try {
				Explorer.createRandomGenome(150000, 3, randomGenome); /*
																	 * only work
																	 * with the
																	 * first 2
																	 * chromosomes
																	 * ; last
																	 * one
																	 * always
																	 * makes
																	 * issues
																	 * with
																	 * bigWig
																	 * Files
																	 */
				System.out.println("Created random genome at: " + randomGenome);
				targetGenome = randomGenome;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		} else {
			targetGenome = randomGenome;
		}

		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length
			ArgosimFile fragment = hf.copy(new SpecifiedPosition(targetGenome,
					"chr1", 20000), fragmentLength);
			ArgosimFile mutatedFragment = fragment.mutate(mutFreq);
			ArgosimFile result = hf.insert(mutatedFragment,
					new SpecifiedPosition(hf.getFileAbs(), "chr1", 60000));
			resultGenomeName = "result.fa";
			result.save(resultGenomeName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void testDetectabilityRepetitive1(int readLength,
			int stepSize, int fragmentLength, double mutFreq, String ID)
			throws Exception {
		File targetGenome = null;
		File randomGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/random150k.fa");
		if (!randomGenome.exists()) {
			try {
				Explorer.createRandomGenome(150000, 3, randomGenome); /*
																	 * only work
																	 * with the
																	 * first 2
																	 * chromosomes
																	 * ; last
																	 * one
																	 * always
																	 * makes
																	 * issues
																	 * with
																	 * bigWig
																	 * Files
																	 */
				System.out.println("Created random genome at: " + randomGenome);
				targetGenome = randomGenome;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		} else {
			targetGenome = randomGenome;
		}

		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length
			// ArgosimFile fragment = hf.copy(new
			// SpecifiedPosition(targetGenome,
			// "chr1", 20000), fragmentLength);
			ArgosimFile fragment = hf.copy(new SpecifiedPosition(targetGenome,
					"chr1", 20000), 10);
			System.out.println("Created 'fragment' at " + fragment.getFileAbs()
					+ ".");

			ArgosimFile temp = hf;
			System.out.println("'temp' now points to 'hf' (at "
					+ temp.getFileAbs() + ").");
			for (int i = 0; i < 100; i++) {
				temp = temp
						.insert(fragment,
								new SpecifiedPosition(temp.getFileAbs(),
										"chr1", 60000));
				System.out.println("Temp now points to " + temp.getFileAbs());
			}
			// ArgosimFile result = hf.insert(fragment,
			// new SpecifiedPosition(hf.getFileAbs(), "chr1", 60000));
			ArgosimFile result = temp;
			resultGenomeName = "result.fa";
			result.save(resultGenomeName);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// p.close();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void testDetectabilityRepetitive2(int readLength,
			int stepSize, int fragmentLength, double mutFreq, String ID)
			throws Exception {
		File targetGenome = null;
		File randomGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/random150k.fa");
		if (!randomGenome.exists()) {
			try {
				Explorer.createRandomGenome(150000, 3, randomGenome); /*
																	 * only work
																	 * with the
																	 * first 2
																	 * chromosomes
																	 * ; last
																	 * one
																	 * always
																	 * makes
																	 * issues
																	 * with
																	 * bigWig
																	 * Files
																	 */
				System.out.println("Created random genome at: " + randomGenome);
				targetGenome = randomGenome;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		} else {
			targetGenome = randomGenome;
		}

		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length
			// ArgosimFile fragment = hf.copy(new
			// SpecifiedPosition(targetGenome,
			// "chr1", 20000), fragmentLength);
			ArgosimFile fragment = hf.copy(new SpecifiedPosition(targetGenome,
					"chr1", 20000), 10);
			System.out.println("Created 'fragment' at " + fragment.getFileAbs()
					+ ".");

			ArgosimFile temp = hf;
			System.out.println("'temp' now points to 'hf' (at "
					+ temp.getFileAbs() + ").");
			for (int i = 0; i < 100; i++) {
				temp = temp
						.insert(fragment,
								new SpecifiedPosition(temp.getFileAbs(),
										"chr1", 60000));
				System.out.println("Temp now points to " + temp.getFileAbs());
			}

			// now copy a fragment from position 10,000 into the created
			// highly-repetitive region
			ArgosimFile fragmentFrom10000 = hf.copy(new SpecifiedPosition(
					targetGenome, "chr1", 10000), 200);
			temp = temp.insert(fragmentFrom10000,
					new SpecifiedPosition(temp.getFileAbs(), "chr1", 60500));
			ArgosimFile result = temp;
			resultGenomeName = "result.fa";
			result.save(resultGenomeName);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// p.close();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void testDetectabilityEcoli1(int readLength, int stepSize,
			int fragmentLength, double mutFreq, String ID) throws Exception {
		File targetGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/eco_k12_mg1655/ecoliK12MG1655.fa");
		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			boolean noMutation = true;
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length
			if (!noMutation) {
				ArgosimFile fragment = hf.copy(new SpecifiedPosition(
						targetGenome, "chr1", 20000), fragmentLength);
				ArgosimFile mutatedFragment = fragment.mutate(mutFreq);
				ArgosimFile result = hf.insert(mutatedFragment,
						new SpecifiedPosition(hf.getFileAbs(), "chr1", 60000));
				resultGenomeName = "result.fa";
				result.save(resultGenomeName);
			} else {
				hf.save(resultGenomeName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void testDetectabilityEcoli2(int readLength, int stepSize,
			int fragmentLength, double mutFreq, String ID) throws Exception {
		// insert a random sequence into e coli
		File targetGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/eco_k12_mg1655/ecoliK12MG1655.fa");
		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			boolean noMutation = true;
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length

			// create random sequence of length 1000
			File randSeq1000 = new File(
					"/home/CIBIV/bernhard_/argos-eval/genomes/rand1k.fa");
			if (!randSeq1000.exists()) {
				try {
					Explorer.createRandomGenome(1000, 2, randSeq1000); /*
																		 * only
																		 * work
																		 * with
																		 * the
																		 * first
																		 * chromosome
																		 * ;
																		 * last
																		 * one
																		 * always
																		 * makes
																		 * issues
																		 * with
																		 * bigWig
																		 * Files
																		 */
					System.out.println("Created random genome at: "
							+ randSeq1000);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

				}
			}

			ArgosimFile argoRandSeq1000 = new ArgosimFile(p, randSeq1000);
			// copy first chromosome
			ArgosimFile argoRandSeq1000Chr1 = argoRandSeq1000.copy(
					new SpecifiedPosition(randSeq1000, "chr1", 0), 1000);

			ArgosimFile result = hf.insert(argoRandSeq1000Chr1,
					new SpecifiedPosition(hf.getFileAbs(), "chr1", 224000));
			resultGenomeName = "result.fa";
			result.save(resultGenomeName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}
	
	public static void testDetectabilityEcoli3(int readLength, int stepSize,
			int fragmentLength, double mutFreq, String ID) throws Exception {
		// insert a sequence with partly similarity
		File targetGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/eco_k12_mg1655/ecoliK12MG1655.fa");
		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length

			// create random sequence of length 1000
			File adenylateKinase1Human = new File(
					"/home/CIBIV/bernhard_/argos-eval/genomes/enolase_subtilis.fa");
			

			ArgosimFile argosAK1 = new ArgosimFile(p, adenylateKinase1Human);
			// copy first chromosome
			ArgosimFile argoRandSeq1000Chr1 = argosAK1.copy(
					new SpecifiedPosition(adenylateKinase1Human, "chr1", 0));

			ArgosimFile result = hf.insert(argoRandSeq1000Chr1,
					new SpecifiedPosition(hf.getFileAbs(), "chr1", 2907000));
			resultGenomeName = "result.fa";
			result.save(resultGenomeName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void testGlobalEffectsEcoli1(int readLength, int stepSize,
			int fragmentLength, double mutFreq, String ID) throws Exception {
		File targetGenome = new File(
				"/home/CIBIV/bernhard_/argos-eval/genomes/eco_k12_mg1655/ecoliK12MG1655.fa");
		ArgosimProject p = new ArgosimProject("hulkTemp/" + ID + "/", new File(
				"hulkResult/" + ID + "/"));
		String resultGenomeName = "result.fa";
		try {
			boolean noMutation = true;
			ArgosimFile hf = new ArgosimFile(p, targetGenome);
			// copy fragment of the specified length
			if (!noMutation) {
				ArgosimFile fragment = hf.copy(new SpecifiedPosition(
						targetGenome, "chr1", 20000), fragmentLength);
				ArgosimFile mutatedFragment = fragment.mutate(mutFreq);
				ArgosimFile result = hf.insert(mutatedFragment,
						new SpecifiedPosition(hf.getFileAbs(), "chr1", 60000));
				resultGenomeName = "result.fa";
				result.save(resultGenomeName);
			} else {
				hf.save(resultGenomeName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		Explorer.runArgos(
				homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
						+ "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
				readLength, stepSize, false);

		/* "default" values: read length = 100, step size = 5 */

		System.out.println("Done.");
	}

	public static void main(String[] args) {
		System.out
				.println("Please run ParallelExplorer. Program will exit now.");
		// if (args.length != 1) {
		// System.out.println("Please call with exactly 1 argument");
		// return;
		// }
		// String ID = args[0];
		// // int READ_LENGTH = 50; // 100
		// // int STEP_SIZE = 5; // 5
		// try {
		// int[] fragmentLengths = { 300, 150, 120, 80, 70, 60, 50, 40, 20,
		// 15, 14, 13, 12, 11, 10, 9, 8 };
		// double[] percentageReadLengths = { 0.05, 0.1, 0.2, 0.5 };
		// int[] readLengths = { 30, 40, 50, 60, 70, 80, 90, 100, 120, 140,
		// 160, 180, 200, 250, 300, 350, 400 };
		// for (double p : percentageReadLengths) {
		// for (int readLength : readLengths) {
		// for (int i : fragmentLengths) {
		// testDetectability(readLength, (int) (readLength * p),
		// i, ID + "_" + i + "_" + p);
		// try {
		// PrintWriter out = new PrintWriter(
		// new BufferedWriter(new FileWriter("output/"
		// + ID + "_" + i + "/log.txt", true)));
		// out.println("Ran argos with:\tREAD_LENGTH\t"
		// + readLength + "\tSTEP_SIZE\t"
		// + (int) (readLength * p)
		// + "\tFRAGMENT_SIZE\t" + i
		// + "\tPercReadLen\t" + p + "\n");
		// out.close();
		// } catch (IOException e) {
		// }
		//
		// }
		//
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// // File randomGenome = new File(
		// // "/home/CIBIV/bernhard_/argos-eval/genomes/random150k.fa");
		//
		// boolean useRandomGenome = false;
		// File targetGenome = null;
		// if (useRandomGenome) {
		// File randomGenome = new File(
		// "/home/CIBIV/bernhard_/argos-eval/genomes/random1000.fa");
		// if (!randomGenome.exists()) {
		// try {
		// Explorer.createRandomGenome(1000, 3, randomGenome); /*
		// * only
		// * work
		// * with
		// * the
		// * first
		// * 2
		// * chromosomes
		// * ;
		// * last
		// * one
		// * always
		// * makes
		// * issues
		// * with
		// * bigWig
		// * Files
		// */
		// System.out.println("Created random genome at: "
		// + randomGenome);
		// targetGenome = randomGenome;
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		//
		// }
		// }
		// } else {
		// targetGenome = new File(
		// "/home/CIBIV/bernhard_/argos-eval/genomes/eco_k12_mg1655/eco_k12_mg1655.fa");
		// }
		//
		// // introduce mutations
		// // String resultGenomeName = introduceMutations(targetGenome, ID);
		// // String resultGenomeName = measureInsertPosition(targetGenome, ID);
		// String resultGenomeName = noMutations(targetGenome, ID); // do not
		// add
		// // SVs
		//
		// String homeDir = "/home/CIBIV/bernhard_/argos-eval/";
		// Explorer.runArgos(
		// homeDir + "hulkResult/" + ID + "/" + resultGenomeName, homeDir
		// + "output/" + ID + "/", homeDir + "tmp2/" + ID + "/",
		// READ_LENGTH, STEP_SIZE);
		//
		// /* "default" values: read length = 100, step size = 5 */
		//
		// System.out.println("Done.");

	}

}
