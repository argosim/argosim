package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.*;
import at.cibiv.ngs.tools.bed.SimpleBEDFile;
import at.cibiv.ngs.tools.lds.GenomicITree;
import at.cibiv.ngs.tools.lds.GenomicInterval;
import at.cibiv.ngs.tools.sam.iterator.ParseException;
import at.cibiv.ngs.tools.vcf.SimpleVCFFile;
import at.cibiv.ngs.tools.vcf.SimpleVCFVariant;

public class ArgosimDemo {

	private static FileLogger fileLogger;

	public ArgosimDemo() {
		ArgosimDemo.fileLogger = new FileLogger(new File(
				"/project/bakk/tmp/myLog.txt"));
	}

	public static void test1() {
		File seq = new File("/project/bakk/genomes-test/ecoli-test.fa");
		File tmp = new File("/project/bakk/genomes-test/tmp");

		CutEvent ce;
		File res;
		File pasteOutput = seq;
		PasteEvent pe;
		File pasteInput = seq;
		try {
			ce = new CutEvent(seq, tmp, new RandomPosition(seq), 100,
					ArgosimDemo.fileLogger);
			res = (File) ce.processSequence();
			System.out.println("created " + res
					+ " containing the CopyEvent-Output");

			for (int i = 0; i < 10; i++) {
				// copy 100bp piece from any chromosome into temp-dir
				pe = new PasteEvent(pasteInput, res, tmp, new RandomPosition(
						seq), ArgosimDemo.fileLogger);
				pasteOutput = (File) pe.processSequence();
				pasteInput = pasteOutput;
				System.out.println("created " + pasteOutput
						+ " containing the PasteEvent-Output");
			}
			Files.move(Paths.get(pasteOutput.getAbsolutePath()),
					Paths.get("/project/bakk/genomes-test/tmp/hulk.fasta"),
					REPLACE_EXISTING);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void test_makeDuplication() throws IOException {
		/**
		 * Introduce a 5bp duplication
		 */

		int REGION_LEN = 5;

		File seq = new File("/project/bakk/genomes-test/ecoli-test.fa");
		File tmp = new File("/project/bakk/genomes-test/tmp");
		File multia = new File("/project/bakk/genomes-test/multia.fa");

		CopyEvent ce = new CopyEvent(multia, tmp, new SpecifiedPosition(seq,
				"chr1", 0), REGION_LEN, ArgosimDemo.fileLogger);
		File result = (File) ce.processSequence();
		System.out.println("Copied sequence: " + result);

		PasteEvent pasteEvent = new PasteEvent(seq, result, tmp,
				new SpecifiedPosition(seq, "chr2", 0), ArgosimDemo.fileLogger);
		File result2 = (File) pasteEvent.processSequence();

		System.out.println("Resulting file with " + REGION_LEN
				+ "bp duplication: " + result2);
	}

	public static void test_moveRegion() throws IOException {
		/**
		 * produces a file where a particular region is moved from A to B
		 */

		int FROM = 0;
		int OFFSET = +10; // move 10 bp downstream
		int LEN = 20;

		File inputSequence = new File(
				"/project/bakk/genomes-test/ecoli-test.fa");
		File tmpDir = new File("/project/bakk/genomes-test/tmp");
		SpecifiedPosition cutPosition = new SpecifiedPosition(inputSequence,
				"chr1", FROM);

		CutEvent cutEvent = new CutEvent(inputSequence, tmpDir, cutPosition,
				LEN, ArgosimDemo.fileLogger);
		@SuppressWarnings("unchecked")
		Map<String, File> cutResults = (Map<String, File>) cutEvent
				.processSequence();

		File cutFragment = cutResults.get("cutFragment");
		File remainder = cutResults.get("remainder");

		System.out.println("cutFragment: " + cutFragment + ", remainder: "
				+ remainder);

		// now re-insert the cut piece:

		PasteEvent pasteEvent = new PasteEvent(remainder, cutFragment, tmpDir,
				new SpecifiedPosition(remainder, "chr1", FROM + OFFSET),
				ArgosimDemo.fileLogger);

		File resultDuplication = (File) pasteEvent.processSequence();

		System.out.println("final result: " + resultDuplication);

	}

	public static void test_Vcf() throws IOException, ParseException {
		File vcfPath = new File("/project/bakk/vcf/experimental_tiny.vcf");
		SimpleVCFFile vcfFile = new SimpleVCFFile(vcfPath);

		vcfFile.debug(5);

		List<SimpleVCFVariant> variants = vcfFile.getVariants();
		System.out.println(variants.size() + " Variants found");

		// System.out.println(variants.get(0).toString());

	}

	public static void test_Bed() throws IOException {
		SimpleBEDFile bedFile = new SimpleBEDFile(
				new File("/project/bakk/bed/example_taurus.bed"));
		for(GenomicInterval gi : bedFile.getIntervalsList()) {
			System.out.println(gi.toString());
		}
		GenomicITree tree = bedFile.getGenomicITree();
		tree.dump();
		GenomicInterval query = new GenomicInterval("chr1", 41l, 41l, "test");
		List<? extends GenomicInterval> res = tree.queryList(query);
		System.out.println("Test überlappt mit :" + res );

	}

	public static void main(String[] args) throws IOException, ParseException {
		// test_moveRegion();

		// test_Vcf();
		test_Bed();
	}

}
