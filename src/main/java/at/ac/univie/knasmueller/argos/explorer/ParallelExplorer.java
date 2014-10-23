package at.ac.univie.knasmueller.argos.explorer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelExplorer extends Explorer {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Please call with exactly 1 argument");
			return;
		}
		experimentEcoli3(args);
	}

		public static void experimentEcoli1(String[] args) {
		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {

			// test with following mutation frequencies:
			double[] mutationFrequencies = { 0 };
			int[] fragmentLengths = { 1000 };
			double[] percentageReadLengths = { 0.05 };
			int[] readLengths = { 50 };
			for (final double mutFreq : mutationFrequencies) {
				for (final double p : percentageReadLengths) {
					for (final int readLength : readLengths) {
						for (final int i : fragmentLengths) {
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										testDetectabilityEcoli1(readLength,
												(int) (readLength * p), i,
												mutFreq, ID + "_" + i + "_" + p
														+ "_" + readLength
														+ "_" + mutFreq);
									} catch (Exception e1) {
										e1.printStackTrace();
									}
									try {
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new FileWriter(
																"output/"
																		+ ID
																		+ "_"
																		+ i
																		+ "_"
																		+ p
																		+ "_"
																		+ readLength
																		+ "_"
																		+ mutFreq
																		+ "/log.txt",
																true)));
										out.println("Ran argos with:\tREAD_LENGTH\t"
												+ readLength
												+ "\tSTEP_SIZE\t"
												+ (int) (readLength * p)
												+ "\tFRAGMENT_SIZE\t"
												+ i
												+ "\tPercReadLen\t" + p + "\n");
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}
	}

	public static void experimentEcoli2(String[] args) {
		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {

			// test with following mutation frequencies:
			double[] mutationFrequencies = { 0 };
			int[] fragmentLengths = { 1000 };
			double[] percentageReadLengths = { 0.05 };
			int[] readLengths = { 50 };
			for (final double mutFreq : mutationFrequencies) {
				for (final double p : percentageReadLengths) {
					for (final int readLength : readLengths) {
						for (final int i : fragmentLengths) {
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										testDetectabilityEcoli2(readLength,
												(int) (readLength * p), i,
												mutFreq, ID + "_" + i + "_" + p
														+ "_" + readLength
														+ "_" + mutFreq);
									} catch (Exception e1) {
										e1.printStackTrace();
									}
									try {
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new FileWriter(
																"output/"
																		+ ID
																		+ "_"
																		+ i
																		+ "_"
																		+ p
																		+ "_"
																		+ readLength
																		+ "_"
																		+ mutFreq
																		+ "/log.txt",
																true)));
										out.println("Ran argos with:\tREAD_LENGTH\t"
												+ readLength
												+ "\tSTEP_SIZE\t"
												+ (int) (readLength * p)
												+ "\tFRAGMENT_SIZE\t"
												+ i
												+ "\tPercReadLen\t" + p + "\n");
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}
	}
	
	public static void experimentEcoli3(String[] args) {
		// insertion of sequence with partly similarity
		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {

			// test with following mutation frequencies:
			double[] mutationFrequencies = { 0 };
			int[] fragmentLengths = { 1000 };
			double[] percentageReadLengths = { 0.05 };
			int[] readLengths = { 50 };
			for (final double mutFreq : mutationFrequencies) {
				for (final double p : percentageReadLengths) {
					for (final int readLength : readLengths) {
						for (final int i : fragmentLengths) {
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										testDetectabilityEcoli3(readLength,
												(int) (readLength * p), i,
												mutFreq, ID + "_" + i + "_" + p
														+ "_" + readLength
														+ "_" + mutFreq);
									} catch (Exception e1) {
										e1.printStackTrace();
									}
									try {
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new FileWriter(
																"output/"
																		+ ID
																		+ "_"
																		+ i
																		+ "_"
																		+ p
																		+ "_"
																		+ readLength
																		+ "_"
																		+ mutFreq
																		+ "/log.txt",
																true)));
										out.println("Ran argos with:\tREAD_LENGTH\t"
												+ readLength
												+ "\tSTEP_SIZE\t"
												+ (int) (readLength * p)
												+ "\tFRAGMENT_SIZE\t"
												+ i
												+ "\tPercReadLen\t" + p + "\n");
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}
	}

	public static void pointMutation(String[] args) {

		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {

			// test with following mutation frequencies:
			double[] mutationFrequencies = { 0, 0.02, 0.04, 0.06, 0.08, 0.10,
					0.12, 0.14, 0.16, 0.18, 0.2, 0.25 };
			int[] fragmentLengths = { 400, 150 };
			double[] percentageReadLengths = { 0.05 };
			int[] readLengths = { 30, 40, 50, 60, 70, 80, 90, 100, 200, 400 };
			for (final double mutFreq : mutationFrequencies) {
				for (final double p : percentageReadLengths) {
					for (final int readLength : readLengths) {
						for (final int i : fragmentLengths) {
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										testDetectabilityPointMutation(
												readLength,
												(int) (readLength * p), i,
												mutFreq, ID + "_" + i + "_" + p
														+ "_" + readLength
														+ "_" + mutFreq);
									} catch (Exception e1) {
										e1.printStackTrace();
									}
									try {
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new FileWriter(
																"output/"
																		+ ID
																		+ "_"
																		+ i
																		+ "_"
																		+ p
																		+ "_"
																		+ readLength
																		+ "_"
																		+ mutFreq
																		+ "/log.txt",
																true)));
										out.println("Ran argos with:\tREAD_LENGTH\t"
												+ readLength
												+ "\tSTEP_SIZE\t"
												+ (int) (readLength * p)
												+ "\tFRAGMENT_SIZE\t"
												+ i
												+ "\tPercReadLen\t" + p + "\n");
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}

	}

	public static void experimentRepetitive1(String[] args) {

		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {

			// test with following mutation frequencies:
			double[] mutationFrequencies = { 0 };
			int[] fragmentLengths = { 1000 };
			double[] percentageReadLengths = { 0.05 };
			int[] readLengths = { 50 };
			for (final double mutFreq : mutationFrequencies) {
				for (final double p : percentageReadLengths) {
					for (final int readLength : readLengths) {
						for (final int i : fragmentLengths) {
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										testDetectabilityRepetitive1(
												readLength,
												(int) (readLength * p), i,
												mutFreq, ID + "_" + i + "_" + p
														+ "_" + readLength
														+ "_" + mutFreq);
									} catch (Exception e1) {
										e1.printStackTrace();
									}
									try {
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new FileWriter(
																"output/"
																		+ ID
																		+ "_"
																		+ i
																		+ "_"
																		+ p
																		+ "_"
																		+ readLength
																		+ "_"
																		+ mutFreq
																		+ "/log.txt",
																true)));
										out.println("Ran argos with:\tREAD_LENGTH\t"
												+ readLength
												+ "\tSTEP_SIZE\t"
												+ (int) (readLength * p)
												+ "\tFRAGMENT_SIZE\t"
												+ i
												+ "\tPercReadLen\t" + p + "\n");
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}

	}

	public static void experimentRepetitive2(String[] args) {

		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {

			// test with following mutation frequencies:
			double[] mutationFrequencies = { 0 };
			int[] fragmentLengths = { 1000 };
			double[] percentageReadLengths = { 0.05 };
			int[] readLengths = { 50 };
			for (final double mutFreq : mutationFrequencies) {
				for (final double p : percentageReadLengths) {
					for (final int readLength : readLengths) {
						for (final int i : fragmentLengths) {
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										testDetectabilityRepetitive2(
												readLength,
												(int) (readLength * p), i,
												mutFreq, ID + "_" + i + "_" + p
														+ "_" + readLength
														+ "_" + mutFreq);
									} catch (Exception e1) {
										e1.printStackTrace();
									}
									try {
										PrintWriter out = new PrintWriter(
												new BufferedWriter(
														new FileWriter(
																"output/"
																		+ ID
																		+ "_"
																		+ i
																		+ "_"
																		+ p
																		+ "_"
																		+ readLength
																		+ "_"
																		+ mutFreq
																		+ "/log.txt",
																true)));
										out.println("Ran argos with:\tREAD_LENGTH\t"
												+ readLength
												+ "\tSTEP_SIZE\t"
												+ (int) (readLength * p)
												+ "\tFRAGMENT_SIZE\t"
												+ i
												+ "\tPercReadLen\t" + p + "\n");
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							});
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}

	}

	public static void minimalDuplicationLength(String[] args) {

		if (args.length != 1) {
			System.out.println("Please call with exactly 1 argument");
			return;
		}
		final String ID = args[0];
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		System.out.println("Start with "
				+ Runtime.getRuntime().availableProcessors() + " threads");
		try {
			int[] fragmentLengths = { 150, 120, 80, 70, 60, 50, 40, 30, 20, 15,
					10, 8, 6, 4 };
			double[] percentageReadLengths = { 0.05, 0.1, 0.2 };
			int[] readLengths = { 30, 40, 50, 60, 70, 80, 90, 100, 200, 400 };
			for (final double p : percentageReadLengths) {
				for (final int readLength : readLengths) {
					for (final int i : fragmentLengths) {
						exec.submit(new Runnable() {
							@Override
							public void run() {
								try {
									testDetectability(readLength,
											(int) (readLength * p), i, ID + "_"
													+ i + "_" + p + "_"
													+ readLength);
								} catch (Exception e1) {
									e1.printStackTrace();
								}
								try {
									PrintWriter out = new PrintWriter(
											new BufferedWriter(new FileWriter(
													"output/" + ID + "_" + i
															+ "_" + p + "_"
															+ readLength
															+ "/log.txt", true)));
									out.println("Ran argos with:\tREAD_LENGTH\t"
											+ readLength
											+ "\tSTEP_SIZE\t"
											+ (int) (readLength * p)
											+ "\tFRAGMENT_SIZE\t"
											+ i
											+ "\tPercReadLen\t" + p + "\n");
									out.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}

	}

}
