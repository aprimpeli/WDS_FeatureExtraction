//package wdc.Thesis;
//
//import java.io.IOException;
//
//import org.junit.Test;
//
//import BagOfWordsModel.DocPreprocessor;
//import BagOfWordsModel.PreprocessingConfiguration;
//import Utils.HTMLFragmentsExtractor;
//
//public class ExtractHTMLFragmentsTest {
//	String file="";
//	//PREPROCESSING
//	static boolean stemming=true;
//	static boolean stopWordRemoval=true;
//	static boolean lowerCase=true;
//	static String htmlParsingElements="html_tables_lists";
//	
//	
//	
//	@Test
//	public void test2() throws IOException{
//		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
//
//		DocPreprocessor pre = new DocPreprocessor();
//		pre.getText(true, null, file, preprocessing,null);
//		
//	}
//}
