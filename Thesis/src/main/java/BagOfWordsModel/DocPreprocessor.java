package BagOfWordsModel;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import Utils.HTMLFragmentsExtractor;
import Utils.NQMapFileExtractions;
import Utils.NodeFromNQ;




public class DocPreprocessor {

	public static void main (String [] args){
		try{
			String filepath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\HTML\\43uf6400_1.html";
			String nqFileMap="";
			DocPreprocessor process = new DocPreprocessor();
			System.out.println("CASE 1");
			PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(true, true, true, "all_html");
			process.printList(process.textProcessing(filepath, null ,2,true, preprocessing, nqFileMap));
//			System.out.println("CASE 2");
//			process.printList(process.textProcessing(filepath, "",true, false, true, true));
//			System.out.println("CASE 3");
//			process.printList(process.textProcessing(filepath, "",true, false, false, true));
//			System.out.println("CASE 4");
//			process.printList(process.textProcessing(filepath,"", true, false, false, false));

		}
		catch (Exception e){
			System.out.println(e.getLocalizedMessage());
		}
	}
	/**
	 * @param string 
	 * @param reader
	 * @return
	 * @throws IOException
	 * Checks if it is a HTML page and removes the html tags to get the text
	 * Uses Lucene library for removal of stopwords, tokenization, stemming and normalization to lower case
	 * Returns the list of the preprocessed words
	 */
	public List<String> textProcessing (String filepath, String text, int grams, boolean isHTML,  PreprocessingConfiguration preprocessing, String nqFileMap) throws IOException{
		
		Reader corpus= StringToReaderConverter(getText(isHTML, text, filepath, preprocessing, nqFileMap));
		
		List<String> processedWords = new ArrayList<String>();
		TokenStream result=null;
		
		final Tokenizer source = new StandardTokenizer(Version.LUCENE_36, corpus);
		result = new StandardFilter(Version.LUCENE_36, source);
					
		if (preprocessing.isLowerCase())
			result = new LowerCaseFilter(Version.LUCENE_36, result);
		if (preprocessing.isStopWordRemoval())
		     result = new StopFilter(Version.LUCENE_36, result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		if (preprocessing.isStemming())
		     result = new PorterStemFilter(result);
		
		//CharTermAttribute charTermAttribute = result.addAttribute(CharTermAttribute.class);
		//result.reset();
		
		while(result.incrementToken()){
			String token=((CharTermAttribute)result.getAttribute(CharTermAttribute.class)).toString();		
			processedWords.add(token);	
		}	

		if(grams>1){
			StringBuilder buildText = new StringBuilder();
			for (String word:processedWords){
				buildText.append(word+" ");
			}
			//now I have the preprocessed text I can tokenize it based on n-grams
			corpus.reset();
			corpus = StringToReaderConverter(buildText.toString());
			result.reset();
			result = new StandardTokenizer(Version.LUCENE_36, corpus);
			result = new ShingleFilter(result, grams,grams);	
			while(result.incrementToken()){
				String token=((CharTermAttribute)result.getAttribute(CharTermAttribute.class)).toString();
				if(grams>1 && !token.contains(" ")) continue;
				processedWords.add(token);	
			}
		}
		result.close();
//		System.out.println("Size of stemmed words:"+stemmedWords.size());
		return processedWords;
    }
	
	public String getText(boolean isHTML, String text, String filepath,
			PreprocessingConfiguration preprocessing, String nqFileMap) throws IOException {

		if (null==text && null!=filepath)
			text = fileToText(filepath);
		
		if (isHTML) {
			if(preprocessing.getHtmlParsingType().equals("all_html"))
				text= Jsoup.parse(text).text();
			else if(preprocessing.getHtmlParsingType().equals("html_tables")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor();
				text=utils.getTableText(filepath);
			}
			else if(preprocessing.getHtmlParsingType().equals("html_lists")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor();
				text=utils.getListText(filepath);
			}
			else if(preprocessing.getHtmlParsingType().equals("html_tables_lists")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor();
				StringBuilder allContent = new StringBuilder();
				String listText=utils.getListText(filepath);
				//System.out.println("LIST:"+listText);
				String tableText=utils.getTableText(filepath);
				//System.out.println("TABLE:"+tableText);
				allContent.append(listText).append(tableText);
				text = allContent.toString();
				//System.out.println("ALL:"+text);
			}
			else if (preprocessing.getHtmlParsingType().equals("marked_up_data")){
				 NodeFromNQ node =NQMapFileExtractions.extractNodeIDFromNQMapFile(filepath, nqFileMap);
				 StringBuilder allContent = new StringBuilder();

				 allContent.append(node.getTitle());
				 allContent.append(node.getDescription());
				 text = allContent.toString();
			}
		}
		return text;
	}
	
	public Reader StringToReaderConverter(String text){
		
		StringReader wordReader = new StringReader(text);
		return wordReader;

	}
	
	
	/**
	 * @param filepath
	 * @return
	 * @throws IOException
	 * Gets the path of a file and transforms it to a string variableS
	 */
	public static String fileToText (String filepath) throws IOException{
		
		byte[] encoded = Files.readAllBytes(Paths.get(filepath));
		  return new String(encoded, StandardCharsets.UTF_8);	
	}
	
	public void printList (List<String> list){
		System.out.println("SIZE of word list:"+list.size());
		for (String l:list) System.out.println(l);
	}
	
		
	
}
