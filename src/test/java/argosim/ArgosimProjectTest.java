package argosim;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.univie.knasmueller.argosim.BedInformation;
import at.ac.univie.knasmueller.argosim.CopyEvent;
import at.ac.univie.knasmueller.argosim.ArgosimFile;
import at.ac.univie.knasmueller.argosim.ArgosimProject;
import at.ac.univie.knasmueller.argosim.ArgosimVcfFile;
import at.ac.univie.knasmueller.argosim.SpecifiedPosition;
import at.ac.univie.knasmueller.argosim.BedInformation.BedMode;

public class ArgosimProjectTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		ArgosimProject p = new ArgosimProject("/tmp/", new File(
				"/project/bakk/testRuns/test_2014-07-14-v2"));

		ArgosimFile testGenome;
		try {
			testGenome = new ArgosimFile(p, new File(
					"/project/bakk/genomes-test/ecoli2-test.fa"));
			testGenome.save("original_fragment");

			ArgosimFile copiedFragment = testGenome.copy(new SpecifiedPosition(
					testGenome.getFileAbs(), "chr2", 0), 10);
			System.out.println("Copied Fragment: "
					+ copiedFragment.getFileAbs());

			copiedFragment.save("myFragment");
			System.out.println("saved 'myFragment'");
			/* mutation test: */

			ArgosimFile mutatedGenome = testGenome.mutate(0.2);
			mutatedGenome.save("myMutatedGenomeXX1");

			System.out
					.println("saved 'myMutatedGenomeXX1' (mutation probability = 0.2)");

			ArgosimVcfFile myVcf = new ArgosimVcfFile(new File(
					"/project/bakk/vcf/experimental_tiny_with_frequencies.vcf"));
			ArgosimFile mutatedGenome2 = testGenome.mutate(myVcf);
			mutatedGenome2.save("myMutatedGenomeXX2");

			ArgosimFile testWithFragment = testGenome.insert(copiedFragment,
					new SpecifiedPosition(new File(
							"/project/bakk/genomes-test/ecoli2-test.fa"),
							"chr1", 0));

			System.out.println("Test with Fragment: "
					+ testWithFragment.getFileAbs());
			System.out.println("Try to move file to result..."
					+ p.getResultDir());
			testWithFragment.save("testWithFragment");
			p.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test_processSequence_restrictToBed() throws Exception {

		ArgosimProject p = new ArgosimProject("/tmp/", new File(
				"/project/bakk/testRuns/test_2014-07-21"));

		ArgosimFile testGenome;
		try {
			testGenome = new ArgosimFile(p, new File("./testing/testGenome1.fa"));
			testGenome.save("original_fragment");
			BedInformation myBed = new BedInformation(new File("./testing/testGenome1.bed"), new File("/tmp"));
			ArgosimFile randomCopyInInterval = testGenome.copy(5, myBed, BedMode.RAND);
			randomCopyInInterval.save("randomCopyInInterval");
			List<String> lines = ArgosimTestUtils.getStringsFromFile(randomCopyInInterval.getFileAbs());
			assertEquals(lines.get(0), "TTCAT");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
