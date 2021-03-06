package at.ac.univie.knasmueller.argosim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.ac.univie.cs.mis.lds.index.itree.ITree;
import at.cibiv.ngs.tools.bed.BedWriter;
import at.cibiv.ngs.tools.bed.SimpleBEDFile;
import at.cibiv.ngs.tools.lds.GenomicITree;
import at.cibiv.ngs.tools.lds.GenomicInterval;
import at.cibiv.ngs.tools.sam.iterator.ParseException;
import at.cibiv.ngs.tools.util.GenomicPosition;
import at.cibiv.ngs.tools.vcf.SimpleVCFFile;
import at.cibiv.ngs.tools.vcf.SimpleVCFVariant;
import at.cibiv.ngs.tools.vcf.VCFWriter;

public class GenomeExtractor {
	
	public long SELECTION_START = 30000000;
	public long  SELECTION_END = 31000000;

	public GenomeExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	public void extractBed() throws IOException {
		System.out.println("Extract some region from a Genome");
		SimpleBEDFile myBed = new SimpleBEDFile(new File("/home/bernhard/Downloads/hg19.bed"));
		GenomicITree bedTree = myBed.getGenomicITree();
		List < GenomicInterval > chr21List = myBed.getIntervalsList("chr21");
		int i = 0;
		
		String bedOutPath = "/home/bernhard/chr21_selections.bed";
		PrintStream fileWriter = new PrintStream(
			     new FileOutputStream(bedOutPath, true)); 
		
		BedWriter bedWriter = new BedWriter(fileWriter);
		
		for(GenomicInterval gi : chr21List) {
			if(gi.getLeftPosition().get0Position() > SELECTION_START &&
					gi.getRightPosition().get0Position() < SELECTION_END) {
				System.out.println(gi);
				bedWriter.add(gi);
				i++;
			}
		}
		
		bedWriter.close();
		System.out.println("Wrote " + i + " intervals to new bed file: " + bedOutPath);
	}
	
	public void extractVcf() throws IOException, ParseException {
		String pathToVcf = "/project/bakk/vcf/21-12163-TSI.vcf";
		SimpleVCFFile simpleVCFFile = new SimpleVCFFile(new File(pathToVcf));
		
		List < SimpleVCFVariant > chr21Variants = simpleVCFFile.getVariants("chr21");
		
		List < SimpleVCFVariant > selectedVariants = new ArrayList < SimpleVCFVariant >();
		
		System.out.println(chr21Variants.size());
		
		String vcfOutPath = "/home/bernhard/chr21_selections.vcf";
		PrintStream fileWriter = new PrintStream(
			     new FileOutputStream(vcfOutPath, true)); 
		
		
		VCFWriter vcfw = new VCFWriter(fileWriter);
		
		for(SimpleVCFVariant var : chr21Variants) {
			if(var.getPosition0() > SELECTION_START
					&& var.getPosition0() < SELECTION_END) {
				selectedVariants.add(var);
				vcfw.add(var);
			}
		}
		
		vcfw.close();
		
		System.out.println("Selected " + selectedVariants.size() + " variants and wrote them to " + vcfOutPath);
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		GenomeExtractor ge = new GenomeExtractor();
		ge.extractVcf();
	}

}
