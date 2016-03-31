package SimpleModelsSimilarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import Utils.ProductCatalogs;



public class DocPreprocessor {

	public static void main (String [] args){
		try{
			String filepath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\43uf6400_1.html";
			DocPreprocessor process = new DocPreprocessor();
			System.out.println("CASE 1");
			process.printList(process.textProcessing(filepath, true, true, true, true));
			System.out.println("CASE 2");
			process.printList(process.textProcessing(filepath, true, false, true, true));
			System.out.println("CASE 3");
			process.printList(process.textProcessing(filepath, true, false, false, true));
			System.out.println("CASE 4");
			process.printList(process.textProcessing(filepath, true, false, false, false));

		}
		catch (Exception e){
			System.out.println(e.getLocalizedMessage());
		}
	}
	/**
	 * @param reader
	 * @return
	 * @throws IOException
	 * Checks if it is a HTML page and removes the html tags to get the text
	 * Uses Lucene library for removal of stopwords, tokenization, stemming and normalization to lower case
	 * Returns the list of the preprocessed words
	 */
	@SuppressWarnings("resource")
	public List<String> textProcessing (String filepath, boolean isHTML,  boolean stemming, boolean stopwordremoval, boolean lowercase) throws IOException{
		
		String text = fileToText(filepath);
		
		if (isHTML) text= Jsoup.parse(text).text();
		Reader corpus= StringToReaderConverter(text);
		
		List<String> processedWords = new ArrayList<String>();
		
		final Tokenizer source = new StandardTokenizer(Version.LUCENE_36, corpus);
		TokenStream result = new StandardFilter(Version.LUCENE_36, source);
		if (lowercase)
			result = new LowerCaseFilter(Version.LUCENE_36, result);
		if (stopwordremoval)
		     result = new StopFilter(Version.LUCENE_36, result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		if (stemming)
		     result = new PorterStemFilter(result);
		
		while (result.incrementToken()){
			processedWords.add(((CharTermAttribute)result.getAttribute(CharTermAttribute.class)).toString());		
		}

//		 System.out.println("Size of stemmed words:"+stemmedWords.size());
		 return processedWords;
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
	
	public static void printList (List<String> list){
		System.out.println("SIZE of word list:"+list.size());
		for (String l:list) System.out.println(l);
	}
	
	
}
