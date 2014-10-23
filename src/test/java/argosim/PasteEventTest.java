package argosim;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.univie.knasmueller.argosim.CopyEvent;
import at.ac.univie.knasmueller.argosim.EventPosition;
import at.ac.univie.knasmueller.argosim.FileLogger;
import at.ac.univie.knasmueller.argosim.ILogger;
import at.ac.univie.knasmueller.argosim.PasteEvent;
import at.ac.univie.knasmueller.argosim.SpecifiedPosition;

public class PasteEventTest {

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

	@Test
	public void test_pasteFragmentOnPos0() throws IOException {
		File seqIn = new File("./testing/testGenome1.fa");
		File pasteIn = new File("./testing/polyA.fa"); /* 7 As */
		EventPosition start = new SpecifiedPosition(seqIn, "chr2", 0);
		PasteEvent pe = new PasteEvent(seqIn, pasteIn, tempDir, start, iLogger);
		File result = (File) pe.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(7).substring(0, 8), "AAAAAAAG");
	}
	
	@Test
	public void test_pasteFragmentOnPos1() throws IOException {
		File seqIn = new File("./testing/testGenome1.fa");
		File pasteIn = new File("./testing/polyA.fa"); /* 7 As */
		EventPosition start = new SpecifiedPosition(seqIn, "chr2", 1);
		PasteEvent pe = new PasteEvent(seqIn, pasteIn, tempDir, start, iLogger);
		File result = (File) pe.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		assertEquals(lines.get(7).substring(0, 8), "GAAAAAAA");
	}
	
	@Test
	public void test_pasteFragmentOnEnd() throws IOException {
		File seqIn = new File("./testing/testGenome1.fa");
		File pasteIn = new File("./testing/polyA.fa"); /* 7 As */
		EventPosition start = new SpecifiedPosition(seqIn, "chr2", 210);
		PasteEvent pe = new PasteEvent(seqIn, pasteIn, tempDir, start, iLogger);
		File result = (File) pe.processSequence();

		// test if copied fragment is correct:

		List<String> lines = ArgosimTestUtils.getStringsFromFile(result);
		for(String cur : lines) {
			System.out.println(cur);
		}
		String targetLine = lines.get(lines.size()-1);
		String last8Characters = targetLine.substring(targetLine.length() - 8, targetLine.length());
		assertEquals(last8Characters, "CAAAAAAA");
	}

}
