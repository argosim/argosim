package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import at.cibiv.ngs.tools.util.GenomicPosition;

public abstract class EventPosition {

	public static Random rand = new Random();
	
	private File seqIn;

	public EventPosition(File seqIn) throws FileNotFoundException {
		this.seqIn = seqIn;
		if (!seqIn.exists())
			throw new FileNotFoundException();
	}

	public File getSeqIn() {
		return seqIn;
	}

	public abstract GenomicPosition getGenomicPosition() throws IOException;
}
