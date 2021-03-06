package argosim;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import at.cibiv.ngs.tools.util.FileUtils;

public class ArgosimTestUtils {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	public File prepareTestFile() throws IOException {
		File createdFile = folder.newFile("myfile.txt");

		PrintWriter out = new PrintWriter(createdFile);

		out.println(">1");
		out.println("AGCTTTTCATTCTGACTGCAACGGGCAATATGTCTCTGTGTGGATTAAAAAAAGAGTGTCTGATAGCAGC");
		out.println("TTCTGAACTGGTTACCTGCCGTGAGTAAATTAAAATTTTATTGACTTAGGTCACTAAATACTTTAACCAA");
		out.println("TATAGGCATAGCGCACAGACAGATAAAAATTACAGAGTACACAACATCCATGAAACGCATTAGCACCACC");
		out.println("");
		out.println(">2");
		out.println("GGGGGGGGGGGGGGGCTGCAACGGGCAATATGTCTCTGTGTGGATTAAAAAAAGAGTGTCTGATAGCAGC");
		out.println("TTCTGAACTGGTTACCTGCCGTGAGTAAATTAAAATTTTATTGACTTAGGTCACTAAATACTTTAACCAA");
		out.println("TATAGGCATAGCGCACAGACAGATAAAAATTACAGAGTACACAACATCCATGAAACGCATTAGCACCACC");
		out.println("");
		out.close();

		return createdFile;
	}

	public static List<String> getStringsFromFile(File file) throws IOException {
		Charset encoding = StandardCharsets.UTF_8;
		List<String> lines = Files.readAllLines(
				Paths.get(file.getAbsolutePath()), encoding);
		return lines;
	}

}
