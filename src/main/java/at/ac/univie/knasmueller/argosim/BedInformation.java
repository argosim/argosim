package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;

import at.cibiv.ngs.tools.bed.BedWriter;
import at.cibiv.ngs.tools.bed.SimpleBEDFile;
import at.cibiv.ngs.tools.lds.GenomicInterval;
import at.cibiv.ngs.tools.util.FileUtils;

public class BedInformation {

	/**
	 * @author bernhard.knasmueller@univie.ac.at
	 * 
	 */

	private File bedFile;
	private SimpleBEDFile simpleBEDFile;
	private File tmpDir;
	
	public enum BedMode { RAND, ALL };

	public BedInformation(File bedFile, File tmpDir) throws IOException {
		this.tmpDir = tmpDir;
		this.simpleBEDFile = new SimpleBEDFile(bedFile);
	}

	public File writeBedFile() {

		return null; // TODO changeme
	}

	public void addPositionModifier(String chr, int position, int offset)
			throws IOException {

		File correctedBed = createTempFile();
		correctedBed.createNewFile();

		BedWriter bw = new BedWriter(new PrintStream(new FileOutputStream(
				correctedBed)));

		for (GenomicInterval gi : this.simpleBEDFile.getIntervalsList()) {
			gi.setAnnotation(null, null);
			if (gi.getChr().equals(chr)) {
				if (offset > 0) {
					if (gi.getLeftPosition().get0Position() >= position) {
						// whole interval gets modified
						GenomicInterval modifiedGi = new GenomicInterval(
								gi.getChr(), gi.getMin() + offset, gi.getMax()
										+ offset, gi.getUri());
						modifiedGi.setAnnotation(null, null);
						bw.add(modifiedGi);
					} else if (gi.getLeftPosition().get0Position() < position
							&& gi.getRightPosition().get0Position() >= position) {
						// insertion is IN the interval - only change right
						// boundary
						GenomicInterval modifiedGi = new GenomicInterval(
								gi.getChr(), gi.getMin(), gi.getMax() + offset,
								gi.getUri());
						modifiedGi.setAnnotation(null, null);
						bw.add(modifiedGi);
					} else {
						bw.add(gi);
					}
				} else if (offset < 0) {
					if (gi.getLeftPosition().get0Position() >= position ) {
						if (gi.getRightPosition().get0Position() < position
								+ Math.abs(offset)) {
							continue;
							// interval gets deleted
						} else {
							long newMin = gi.getMin() - Math.abs(offset);
							if(newMin < 0)
								newMin = 0;
							GenomicInterval modifiedGi = new GenomicInterval(
									gi.getChr(),
									newMin, gi.getMax()
											- Math.abs(offset), gi.getUri());
							modifiedGi.setAnnotation(null, null);
							bw.add(modifiedGi);
						}
					} else if (gi.getLeftPosition().get0Position() < position) {
						if (gi.getRightPosition().get0Position() >= position) {
							if(gi.getRightPosition().get0Position() < position + Math.abs(offset)) {
								GenomicInterval modifiedGi = new GenomicInterval(
										gi.getChr(), gi.getMin(),
										(long) position - 1, gi.getUri());
								modifiedGi.setAnnotation(null, null);
								bw.add(modifiedGi);
							} else {
								GenomicInterval modifiedGi = new GenomicInterval(
										gi.getChr(), gi.getMin(),
										gi.getMax() - Math.abs(offset), gi.getUri());
								modifiedGi.setAnnotation(null, null);
								bw.add(modifiedGi);
							}
						} else {
							bw.add(gi);
						}
					}
				}
			} else {
				bw.add(gi);
			}

		}
		bw.close();
		//System.out.println(correctedBed);
		bedFile = correctedBed;
		this.simpleBEDFile = new SimpleBEDFile(bedFile);

	}

	private File createTempFile() {
		String prefix = "tmp";
		return FileUtils.createTempFile(tmpDir, prefix);
	}

	public File getBedFile() {
		return bedFile;
	}

	public void setBedFile(File bedFile) {
		this.bedFile = bedFile;
	}

	public static void main(String[] args) {
		try {
			BedInformation bedInformation = new BedInformation(new File(
					"/project/bakk/bed/chr21_selections.bed"), new File(
					"/project/bakk/tmp/"));
			for(int i=0; i < 1000; i++) {
				int mod;
				if(i%3 == 0) {
					mod = -15;
				} else {
					mod = 7;
				}
				bedInformation.addPositionModifier("chr1", 10*i, mod);
				
			}
			
			System.out.println(bedInformation.getBedFile());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * randomly returns one of the n intervals saved in the BED file
	 * @return
	 */
	public GenomicInterval getRandomInterval() {
		List <GenomicInterval> allIntervals = this.simpleBEDFile.getIntervalsList();
		int intervalCount = allIntervals.size();
		Random randomGenerator = new Random();
	    int randomInt = randomGenerator.nextInt(intervalCount);
	    return allIntervals.get(randomInt);
	}

}
