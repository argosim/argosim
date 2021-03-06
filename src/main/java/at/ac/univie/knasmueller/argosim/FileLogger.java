package at.ac.univie.knasmueller.argosim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

public class FileLogger implements ILogger {
	private File logFile;
	boolean append;

	public FileLogger(File logFile, boolean append) {
		setLogFile(logFile);
		setAppend(append);
	}
	
	public FileLogger(File logFile) {
		this(logFile, true);
	}

	public File getLogFile() {
		return logFile;
	}

	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
	
	private String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	@Override
	public void createLogEntry(String message, String eventName) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(getLogFile(), true)));
		    out.println(getCurrentTime() + "\t" + eventName + "\t" + message);
		    out.close();
		} catch (IOException e) {
		}
	}

}
