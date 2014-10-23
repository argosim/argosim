package argosim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.univie.knasmueller.argosim.CopyEvent;
import at.ac.univie.knasmueller.argosim.EventPosition;
import at.ac.univie.knasmueller.argosim.FileLogger;
import at.ac.univie.knasmueller.argosim.ILogger;
import at.ac.univie.knasmueller.argosim.SpecifiedPosition;

public class CopyEventTest {
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

	@Test
	public void testProcessSequence_copyFromPosZeroOnChr1() throws Exception {
		File seqin = new File("./testing/testGenome1.fa");

		EventPosition start = new SpecifiedPosition(seqin, "chr1", 0);
		CopyEvent ce = new CopyEvent(seqin, tempDir, start, 3, iLogger);
		File result = (File) ce.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(0), "AGC");
	}
	
	@Test
	public void testProcessSequence_copyFromPosFiveOnChr2() throws Exception {
		File seqin = new File("./testing/testGenome1.fa");

		EventPosition start = new SpecifiedPosition(seqin, "chr2", 16);
		CopyEvent ce = new CopyEvent(seqin, tempDir, start, 3, iLogger);
		File result = (File) ce.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(0), "TGC");
	}
	
	@Test
	public void testProcessSequence_copyFragmentLongerThanChromosome_getFragmentUntilEnd() throws Exception {
		File seqin = new File("./testing/testGenome1.fa");

		EventPosition start = new SpecifiedPosition(seqin, "chr1", 206);
		CopyEvent ce = new CopyEvent(seqin, tempDir, start, 50, iLogger);
		File result = (File) ce.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(0), "CACC");
	}
	
	@Test
	public void testProcessSequence_copyEmptyFragment() throws Exception {
		File seqin = new File("./testing/testGenome1.fa");

		EventPosition start = new SpecifiedPosition(seqin, "chr1", 206);
		CopyEvent ce = new CopyEvent(seqin, tempDir, start, 0, iLogger);
		File result = (File) ce.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.size(), 0);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(tempDir); // delete temp dir with all files
	}

}
