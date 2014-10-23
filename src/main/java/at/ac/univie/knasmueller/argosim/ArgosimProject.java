package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import at.cibiv.ngs.tools.util.GenomicPosition;

public class ArgosimProject {
	File resultDir;
	File tempDir;
	ILogger iLogger;
	int tempFileCounter;
	Map<Integer, File> tempFiles;

	public File getTempDir() {
		System.out.println("tempDir: " + this.tempDir);
		return this.tempDir;
	}

	public int addTempFile(ArgosimFile file) {
		this.tempFiles.put(this.incrementTempFileCounter(), file.getFileAbs());
		return 0;
	}

	public int addTempFile(File file) {
		this.tempFiles.put(this.incrementTempFileCounter(), file);
		return 0;
	}

	private int incrementTempFileCounter() {
		this.tempFileCounter += 1;
		return this.tempFileCounter - 1;
	}

	public ArgosimProject(String tempDir, File resultDir) {
		this.tempFileCounter = 0;
		this.tempFiles = new HashMap<Integer, File>();

		boolean tempDirCreationSuccess = (new File(tempDir + "/tmp")).mkdirs();
		if (!tempDirCreationSuccess
				&& !Files.exists(new File(tempDir + "/tmp").toPath())) {
			throw new RuntimeException("could not create tmp directory");
		}
		this.tempDir = new File(tempDir + "/tmp/"); // in tempDir: a folder
													// "temp" is created
		this.resultDir = resultDir;
		if (!this.resultDir.exists()) {
			System.out.println("Creating resultDir at " + this.resultDir);
			boolean resultDirCreationSuccess = (this.resultDir).mkdirs();
			if (!resultDirCreationSuccess) {
				throw new RuntimeException("could not create result directory");
			}
		}

		ILogger iLogger = new FileLogger(new File(this.resultDir + "/log.txt"));
		this.iLogger = iLogger;
	}

	public ILogger getILogger() {
		return this.iLogger;
	}

	public void close() {
		System.out.println("Call to close() - delete all temp files");

		// removes all created temp-files including the created "tmp" folder
		for (Map.Entry<Integer, File> entry : this.tempFiles.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = "
					+ entry.getValue());
			entry.getValue().delete();
		}
		try {
			boolean success = this.tempDir.delete();
			if (!success) {
				System.out.println("Could not delete tmp-Folder at "
						+ this.tempDir + " - maybe it's not empty?");
			}
		} catch (Exception e) {
			System.out
					.println("Could not delete tmp-Folder; " + e.getMessage());
		}

	}

	public static void main(String[] args) throws Exception {
		ArgosimProject p = new ArgosimProject("/tmp/", new File(
				"/project/bakk/testRuns/test_2014-05-18"));

		ArgosimFile testGenome = new ArgosimFile(p, new File(
				"/project/bakk/genomes-test/ecoli2-test.fa"));
		ArgosimFile copiedFragment = testGenome.copy(new SpecifiedPosition(
				testGenome.getFileAbs(), "chr2", 0), 10);

		ArgosimFile targetWithFragment = testGenome.insert(copiedFragment,
				new SpecifiedPosition(testGenome.getFileAbs(), "chr1", 20));
		
		ArgosimFile[] targetCut = targetWithFragment.cut(new RandomPosition(targetWithFragment.getFileAbs()), 18);
		ArgosimFile cutFragment = targetCut[0];
		ArgosimFile targetRemainder = targetCut[1];
		cutFragment.save("cut_fragment");
		targetWithFragment.save("original_with_fragment");
		

		 /* mutation test: */
		
		 // HulkFile mutatedGenome = testGenome.mutate(0);
		 // mutatedGenome.save("myMutatedGenomeXX1");
		
		 ArgosimVcfFile myVcf = new ArgosimVcfFile(new File(
		 "/project/bakk/vcf/experimental_tiny_with_frequencies.vcf"));
		 ArgosimFile mutatedGenome2 = testGenome.mutate(myVcf);
		 mutatedGenome2.save("myMutatedGenomeXX2");
		 //
		 // HulkFile testWithFragment = testGenome
		 // .insert(copiedFragment,
		 // new SpecifiedPosition(new File(
		 // "/project/bakk/genomes-test/ecoli2-test.fa"),
		 // "chr1", 0));
		 //
		 // System.out.println("Test with Fragment: "
		 // + testWithFragment.getFileAbs());
		 // System.out.println("Try to move file to result..." +
		 // p.getResultDir());
		 // testWithFragment.save("testWithFragment");
		 p.close();
		 
		 

	}

	public File getResultDir() {
		return this.resultDir;
	}
}
