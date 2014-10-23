package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;

import at.cibiv.ngs.tools.util.FileUtils;

public abstract class ArgosimEvent {

	private String name;
	private File seqIn;
	private File tempDir;
	private ILogger iLogger;

	public ArgosimEvent(String name, File seqIn, File tempDir, ILogger iLogger) {
		this.name = name;
		this.seqIn = seqIn;
		this.tempDir = tempDir;
		this.iLogger = iLogger;
	}

	public String getName() {
		return name;
	}

	public File getSeqIn() {
		return seqIn;
	}

	public File getTempDir() {
		return tempDir;
	}
	
	public void createLogEntry(String message) {
		iLogger.createLogEntry(message, getName());
	}

	public abstract Object processSequence() throws IOException, Exception;

	/**
	 * Create a temp file.
	 * 
	 * @return
	 */
	public File createTempFile() {
		String prefix = "seq";
		String postfix = "";
		File f = null;
		int i = 0;
		do {
			f = new File(tempDir, prefix + postfix + i + ".tmp");
			i++;
		} while (f.exists());
		return f;
		
	}

	@Override
	public String toString() {
		return "[EVT: " + getName() + "]";
	}

}
