package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;

import at.ac.univie.knasmueller.argosim.BedInformation.BedMode;
import at.cibiv.ngs.tools.lds.GenomicInterval;
import at.cibiv.ngs.tools.util.FileUtils;

public class ArgosimFile {
	ArgosimProject argosimProject;
	File originalFile, tempCopyFile;
	boolean isRemoved;
	int tempFileId;

	enum MutationType {
		VcfMutation, PointMutation
	}

	public enum ConstructorType {
		ARGOSIMCOPY, ARGOSIMNOCOPY
	}

	public int getTempFileId() {
		return tempFileId;
	}

	public void setTempFileId(int tempFileId) {
		this.tempFileId = tempFileId;
	}

	private void copyFileToTemp(File from, File to) throws IOException {
		if (!from.exists()) {
			throw new IOException("Could not find source-file: " + from);
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(from);
			os = new FileOutputStream(to);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	public ArgosimFile(ArgosimProject hulkProject, File file) throws IOException {
		this(hulkProject, file, ConstructorType.ARGOSIMCOPY);
	}

	public ArgosimFile(ArgosimProject argosimProject, File file, ConstructorType ct)
			throws IOException {
		/* creates a copy of the file to the tempDir */
		/**
		 * ConstructorType specifies whether a temporary copy of the file should
		 * be created or not
		 **/
		this.argosimProject = argosimProject;
		if (ct == ConstructorType.ARGOSIMCOPY) {
			File tempCopy = FileUtils.createTempFile(
					this.argosimProject.getTempDir(), "argosimFile_");
			System.out.println("Copy file to temp: " + file + ", " + tempCopy);
			copyFileToTemp(file, tempCopy);
			this.originalFile = file;
			this.tempCopyFile = tempCopy;
			this.isRemoved = false;
			this.tempFileId = this.addTempFile(); // TODO tempFileId not
													// meaningful right now
		} else {
			/* no copying necessary, just take the file as a temp file */
			this.originalFile = file;
			this.tempCopyFile = file;
			this.isRemoved = false;
			this.tempFileId = this.addTempFile();
		}

	}

	int addTempFile() {
		System.out.println("Add temp file: " + this.getFileAbs());
		return this.argosimProject.addTempFile(this);
	}

	int addTempFile(File file) {
		System.out.println("Add temp file: " + file);
		return this.argosimProject.addTempFile(file);
	}

	public void remove() {
		/* removes this file from the tempDir */

		// check if file actually exists
		if (!this.getFileAbs().exists())
			throw new RuntimeException("Tried to delete a non-existing file: "
					+ this.getFileAbs());

		{
			boolean success = this.getFileAbs().delete();
			if (!success) {
				throw new RuntimeException("Could not delete the file "
						+ this.getFileAbs());
			}
		}

		this.argosimProject.tempFiles.remove(this.tempFileId);

	}

	public ArgosimFile insert(ArgosimFile insertFile, EventPosition pos)
			throws IOException {

		PasteEvent pe = new PasteEvent(this.getFileAbs(),
				insertFile.getFileAbs(), this.argosimProject.getTempDir(), pos,
				this.argosimProject.getILogger());
		File result = (File) pe.processSequence();
		ArgosimFile resultArgosim = new ArgosimFile(this.argosimProject, result,
				ConstructorType.ARGOSIMNOCOPY);
		/* use ARGOSIMNOCOPY because processSequence already creates a temp file */
		return resultArgosim;

	}

	public File getFileAbs() {
		/**
		 * returns absolute path of this temp dir
		 */
		return tempCopyFile;
	}
	
	/**
	 * this version of copy does not require a length input; it copies the whole fragment
	 * @param pos
	 * @return
	 * @throws IOException
	 */
	public ArgosimFile copy(EventPosition pos) throws IOException {
		CopyEvent ce = new CopyEvent(this.getFileAbs(),
				this.argosimProject.getTempDir(), pos,
				this.argosimProject.getILogger());
		File result = (File) ce.processSequence();
		ArgosimFile resultArgosim = new ArgosimFile(this.argosimProject, result,
				ConstructorType.ARGOSIMNOCOPY);
		resultArgosim.addTempFile(new File(this.getFileAbs() + ".cix"));
		/*
		 * use HulkNoCopy because processSequence() already creates a temp file
		 * in the temp dir
		 */

		return resultArgosim;
	}

	public ArgosimFile copy(EventPosition pos, long length) throws IOException {
		CopyEvent ce = new CopyEvent(this.getFileAbs(),
				this.argosimProject.getTempDir(), pos, length,
				this.argosimProject.getILogger());
		File result = (File) ce.processSequence();
		ArgosimFile resultArgosim = new ArgosimFile(this.argosimProject, result,
				ConstructorType.ARGOSIMNOCOPY);
		resultArgosim.addTempFile(new File(this.getFileAbs() + ".cix"));
		/*
		 * use HulkNoCopy because processSequence() already creates a temp file
		 * in the temp dir
		 */

		return resultArgosim;
	}

	/**
	 * copies a fragment of length length from any of the intervals
	 * 
	 * @param length
	 * @param bed
	 * @param bedMode
	 * @return
	 * @throws IOException
	 */
	public ArgosimFile copy(long length, BedInformation bed, BedMode bedMode)
			throws IOException {
		if (bedMode != BedMode.RAND) {
			throw new IllegalArgumentException(
					"Only bedMode RAND is possible for copy events");
		}
		EventPosition position;
		GenomicInterval gi = bed.getRandomInterval();
		int startPosition = (int) getRandomRegionFromInterval(gi, length);
		position = new SpecifiedPosition(this.getFileAbs(), gi.getChr(),
				startPosition);
		System.out
				.println("Successfully retrieved random position from BED file: "
						+ startPosition + " @ " + gi.getChr());
		CopyEvent ce = new CopyEvent(this.getFileAbs(),
				this.argosimProject.getTempDir(), position, length,
				this.argosimProject.getILogger());
		File result = (File) ce.processSequence();
		ArgosimFile resultArgosim = new ArgosimFile(this.argosimProject, result,
				ConstructorType.ARGOSIMNOCOPY);
		resultArgosim.addTempFile(new File(this.getFileAbs() + ".cix"));
		/*
		 * use HulkNoCopy because processSequence() already creates a temp file
		 * in the temp dir
		 */

		return resultArgosim;
	}

	public static long getRandomRegionFromInterval(GenomicInterval gi,
			long length) {
		if (gi.getWidth() < length) {
			throw new RuntimeException(
					"Could not create region from interval: parameter 'length' is larger than interval-width");
		} else if (gi.getWidth() - length < 1e-10 /*
												 * why would someone return
												 * length as a double??
												 */) {
			return gi.getLeftPosition().get0Position();
		} else {
			long minPos = gi.getLeftPosition().get0Position();
			long maxPos = minPos + (gi.getWidth().longValue() - length);
			Random randomGenerator = new Random();
			int randomInt = (int) (randomGenerator
					.nextInt((int) (maxPos - minPos)) + minPos);
			/* generates a start-position between incl. minPos and excl. maxPos */
			return randomInt;
		}
	}

	public ArgosimFile[] cut(EventPosition pos, long length) throws IOException {
		CutEvent ce = new CutEvent(this.getFileAbs(),
				this.argosimProject.getTempDir(), pos, (int) length,
				this.argosimProject.getILogger());
		@SuppressWarnings("unchecked")
		Map<String, File> cutResults = (Map<String, File>) ce.processSequence();
		File cutFragment = cutResults.get("cutFragment");
		File remainder = cutResults.get("remainder");
		ArgosimFile cutFragmentArgosim = new ArgosimFile(this.argosimProject, cutFragment,
				ConstructorType.ARGOSIMNOCOPY);
		ArgosimFile remainderArgosim = new ArgosimFile(this.argosimProject, remainder,
				ConstructorType.ARGOSIMNOCOPY);
		/* use HulkNoCopy because processSequence already creates 2 temp files */

		ArgosimFile[] hfs = { cutFragmentArgosim, remainderArgosim };

		return hfs;
	}

//	public ArgosimFile mutate(ArgosimVcfFile vcf, ArgosimBedFile regionRestriction)
//			throws Exception {
//		ArgosimFile returnFile = mutate(MutationType.VcfMutation, vcf,
//				regionRestriction, 0);
//		returnFile.addTempFile();
//		return returnFile;
//	}

	public ArgosimFile mutate(double mutationProbability,
			ArgosimBedFile regionRestriction) throws Exception {
		return mutate(MutationType.PointMutation, null, regionRestriction,
				mutationProbability);
	}

	public ArgosimFile mutate(double mutationProbability) throws Exception {
		return mutate(MutationType.PointMutation, null, null,
				mutationProbability);
	}

	public ArgosimFile mutate(ArgosimVcfFile vcf) throws Exception {
		return mutate(MutationType.VcfMutation, vcf, null, 0);
	}

	private ArgosimFile mutate(MutationType mutationType, ArgosimVcfFile vcf,
			ArgosimBedFile regionRestriction, double mutationProbability)
			throws Exception {
		MutationEvent me = null;
		if (mutationType == MutationType.PointMutation) {
			me = new PointMutationEvent(this.getFileAbs(),
					this.argosimProject.getTempDir(), mutationProbability,
					this.argosimProject.iLogger);
		} else if (mutationType == MutationType.VcfMutation) {

			me = new VCFMutationEvent(this.getFileAbs(),
					this.argosimProject.getTempDir(),
					vcf.getVcf() /* TODO Changeme */, this.argosimProject.iLogger);
		} else {
			throw new RuntimeException("Unsupported MutationType");
		}
		File resultFile = (File) me.processSequence();
		this.addTempFile(resultFile);
		return new ArgosimFile(this.argosimProject, resultFile);
	}

	public void save() throws IOException {
		/**
		 * saves this file to the result-folder using a generated name
		 */
		this.save(null);
	}

	public void save(String fileName) throws IOException {
		/**
		 * saves this file to the result-folder, naming it fileName.
		 */

		System.out.println("Attempt to save for fileName = " + fileName);

		if (fileName == null) {
			fileName = "result.fa";
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			String basename = FilenameUtils.getBaseName(fileName);
			String ext = FilenameUtils.getExtension(fileName);
			if(!ext.equals("fa") && !ext.equals("fasta")) {
				ext = "fa";
			}
			File outFile = new File(this.argosimProject.getResultDir() + "/"
					+ basename + "." + ext);
			int i = 1;
			while (outFile.exists()) {
				outFile = new File(this.argosimProject.getResultDir() + "/"
						+ basename + "_" + i + ext);
				i++;
			}
			System.out.println("outFile: " + outFile);
			is = new FileInputStream(getFileAbs());
			os = new FileOutputStream(outFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	public static void main(String[] args) {
		
	}

}
