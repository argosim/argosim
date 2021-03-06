package argosim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import at.ac.univie.knasmueller.argosim.FileLogger;
import at.ac.univie.knasmueller.argosim.ILogger;
import at.ac.univie.knasmueller.argosim.VCFMutationEvent;
import at.ac.univie.knasmueller.argosim.VCFMutationEvent.LogLevel;

public class VCFMutationEventTest {
	File tempDir;
	ILogger iLogger;

	@Before
	public void setUp() throws Exception {
		this.tempDir = new File("/tmp/hulk___test");
		if (tempDir.exists()) {
			throw new Exception("temp-dir already existing - cannot continue");
		}
		if (!tempDir.mkdirs()) {
			throw new Exception("could not create temp dir at "
					+ tempDir.getAbsolutePath());
		}

		iLogger = new FileLogger(new File("/tmp/hulk___test/log.txt"));
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(tempDir); // delete temp dir with all files
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void test_nonExistingVcfFileProvided_throwsException()
			throws Exception {
		thrown.expect(Exception.class);
		thrown.expectMessage("provided vcf file does not exist");

		File inputSequence = new File(
				"/project/bakk/genomes-test/ecoli2-test.fa");
		File vcfFile = new File("/project/bakk/vcf/thisDoesNotExist.vcf");
		VCFMutationEvent vcfMutation = new VCFMutationEvent(inputSequence,
				this.tempDir, vcfFile, this.iLogger);
		vcfMutation.setLogLevel(LogLevel.FULL);

		File resultFile = (File) vcfMutation.processSequence();
		System.out.println("ResultFile: " + resultFile);
	}

	@Test
	public void test_VcfFileWithAfFormat_notEnoughAlleleFrequenciesProvided_throwsException()
			throws Exception {
		thrown.expect(Exception.class);
		thrown.expectMessage("Fatal error: one allele frequency per alternate allele expected; check if you have one AF per alternate allele");

		File inputSequence = new File(
				"/project/bakk/genomes-test/ecoli2-test.fa");
		File vcfFile = new File("./testing/vcfWithError.vcf");
		VCFMutationEvent vcfMutation = new VCFMutationEvent(inputSequence,
				this.tempDir, vcfFile, this.iLogger);
		vcfMutation.setLogLevel(LogLevel.FULL);

		File resultFile = (File) vcfMutation.processSequence();
	}
	
	@Test
	public void test_VcfFileWithCafFormat_notEnoughAlleleFrequencies_throwsException()
			throws Exception {
		thrown.expect(Exception.class);
		thrown.expectMessage("Fatal error: one allele frequency per alternate allele expected; check if you have one AF per alternate allele");

		File inputSequence = new File(
				"/project/bakk/genomes-test/ecoli2-test.fa");
		File vcfFile = new File("./testing/vcf_caf_notEnoughEntries.vcf");
		VCFMutationEvent vcfMutation = new VCFMutationEvent(inputSequence,
				this.tempDir, vcfFile, this.iLogger);
		vcfMutation.setLogLevel(LogLevel.FULL);

		File resultFile = (File) vcfMutation.processSequence();
	}

}
