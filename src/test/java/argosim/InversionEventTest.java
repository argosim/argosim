package argosim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.univie.knasmueller.argosim.CopyEvent;
import at.ac.univie.knasmueller.argosim.EventPosition;
import at.ac.univie.knasmueller.argosim.FileLogger;
import at.ac.univie.knasmueller.argosim.ILogger;
import at.ac.univie.knasmueller.argosim.InversionEvent;
import at.ac.univie.knasmueller.argosim.SpecifiedPosition;

public class InversionEventTest {
	File tempDir;
	ILogger iLogger;

	@Before
	public void setUp() throws Exception {
		this.tempDir = new File("/tmp/hulk___test/inversionTests");
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

	@Test
	public void test_invertAllChromosomes_correctResult() throws Exception {
		File seqin = new File("./testing/test_inversion.fa");
		// does not work if .cix file exists TODO
		File indexFile = new File("./testing/test_inversion.fa.cix");
		if (indexFile.exists()) {
			indexFile.delete();
		}

		InversionEvent inversionEvent = new InversionEvent(seqin, tempDir,
				iLogger);
		File result = (File) inversionEvent.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(1), "TCGA");
		assertEquals(lines.get(3), "AAAAAATTAGGTGTGTCTCTGTATAAC");
	}

	@Test
	public void test_invertOneChromosome_correctResult1() throws Exception {
		File seqin = new File("./testing/test_inversion.fa");
		// does not work if .cix file exists TODO
		File indexFile = new File("./testing/test_inversion.fa.cix");
		if (indexFile.exists()) {
			indexFile.delete();
		}

		InversionEvent inversionEvent = new InversionEvent(seqin, tempDir,
				iLogger);
		inversionEvent.restrictToRegion(1, 2, "chr1");
		File result = (File) inversionEvent.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(1), "ACGT");
		assertEquals(lines.get(3), "CAATATGTCTCTGTGTGGATTAAAAAA");
	}
	
	@Test
	public void test_invertOneChromosome_correctResult2() throws Exception {
		File seqin = new File("./testing/test_inversion.fa");
		// does not work if .cix file exists TODO
		File indexFile = new File("./testing/test_inversion.fa.cix");
		if (indexFile.exists()) {
			indexFile.delete();
		}

		InversionEvent inversionEvent = new InversionEvent(seqin, tempDir,
				iLogger);
		inversionEvent.restrictToRegion(0, 5, "chr2");
		File result = (File) inversionEvent.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(1), "AGCT");
		assertEquals(lines.get(3), "ATAACTGTCTCTGTGTGGATTAAAAAA");
	}

}
