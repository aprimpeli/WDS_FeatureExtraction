package wdc.Thesis;

import java.io.IOException;

import org.junit.Test;

import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.PreprocessingConfiguration;
import Utils.HTMLFragmentsExtractor;

public class ExtractHTMLFragmentsTest {
	String file="C://Users//Anna//Google Drive//Master_Thesis//3.MatchingModels//testInput//tvs//HTML//43uf6400_2.html";
	//PREPROCESSING
	static boolean stemming=true;
	static boolean stopWordRemoval=true;
	static boolean lowerCase=true;
	static String htmlParsingElements="html_tables_lists";
	
	//@Test
	public void test() throws IOException{
		HTMLFragmentsExtractor extract = new HTMLFragmentsExtractor();
		String tablecontent = extract.getTableText(file);
//		System.out.println(tablecontent);
		
//		String listcontent = extract.getListText(file);
	}
	
	@Test
	public void test2() throws IOException{
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);

		DocPreprocessor pre = new DocPreprocessor();
		pre.getText(true, null, file, preprocessing,null);
		
	}
}
