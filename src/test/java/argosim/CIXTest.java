package argosim;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.cibiv.ngs.tools.fasta.FastaSequence;
import at.cibiv.ngs.tools.fasta.MultiFastaSequence;

public class CIXTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		File cix = new File("/project/bakk/genomes-test/singlefasta.fa.cix");
		if(cix.exists())
			cix.delete();
		
		// it will work the 1st time but won't work the second time when the cix file is created
		for (int i = 0; i < 2; i++) {
			try {
				MultiFastaSequence fs = new MultiFastaSequence(new File(
						"/project/bakk/genomes-test/singlefasta.fa"), true);
				for (String chr : fs.getChromosomes()) {
					FastaSequence fa = fs.getSequence(chr);
					fa.validate(null);
					Long regionLength = fa.getLength();
					System.out.println(regionLength);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// fail("Not yet implemented");
	}

}
