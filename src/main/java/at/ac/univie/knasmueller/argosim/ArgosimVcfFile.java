package at.ac.univie.knasmueller.argosim;

import java.io.File;

public class ArgosimVcfFile {
	
	File vcfFile;

	public ArgosimVcfFile(File file) {
		this.vcfFile = file;
	}

	public File getVcf() {
		return vcfFile;
	}

}
