package at.ac.univie.knasmueller.argosim;

import java.io.File;
import java.io.IOException;

public abstract class MutationEvent extends ArgosimEvent {

	public MutationEvent(String name, File seqIn, File tempDir, ILogger iLogger) throws Exception {
		super(name, seqIn, tempDir, iLogger);
	}
	

	@Override
	public abstract Object processSequence() throws IOException, Exception;


}
