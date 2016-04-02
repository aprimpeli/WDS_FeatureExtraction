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
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import Utils.ProductCatalogs;



public class DocPreprocessor {

	public static void main (String [] args){
		try{
			String filepath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\htmlPages\\43uf6400_1.html";
			DocPreprocessor process = new DocPreprocessor();
			System.out.println("CASE 1");
			process.textProcessing(filepath, null ,1,true, true, true, true);
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
	 * @param reader
	 * @return
	 * @throws IOException
	 * Checks if it is a HTML page and removes the html tags to get the text
	 * Uses Lucene library for removal of stopwords, tokenization, stemming and normalization to lower case
	 * Returns the list of the preprocessed words
	 */
	public List<String> textProcessing (String filepath, String text, int grams, boolean isHTML,  boolean stemming, boolean stopwordremoval, boolean lowercase) throws IOException{
		
		if (null==text && null!=filepath)
			text = fileToText(filepath);
	
		if (isHTML) text= Jsoup.parse(text).text();
		Reader corpus= StringToReaderConverter(text);
		
		List<String> processedWords = new ArrayList<String>();
		TokenStream result;
		if(grams==1){
			final Tokenizer source = new StandardTokenizer(Version.LUCENE_36, corpus);
			result = new StandardFilter(Version.LUCENE_36, source);
		}
		else{
			result = new StandardTokenizer(Version.LUCENE_36, corpus);
			result = new ShingleFilter(result, grams,grams);	
		}
							
		if (lowercase)
			result = new LowerCaseFilter(Version.LUCENE_36, result);
		if (stopwordremoval)
		     result = new StopFilter(Version.LUCENE_36, result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		if (stemming)
		     result = new PorterStemFilter(result);
		
		//CharTermAttribute charTermAttribute = result.addAttribute(CharTermAttribute.class);
		//result.reset();
		
		while (result.incrementToken()){
			String token=((CharTermAttribute)result.getAttribute(CharTermAttribute.class)).toString();
			if(grams>1 && !token.contains(" ")) continue;
			processedWords.add(token);	
		}
		result.close();
//		System.out.println("Size of stemmed words:"+stemmedWords.size());
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
	
	public void printList (List<String> list){
		System.out.println("SIZE of word list:"+list.size());
		for (String l:list) System.out.println(l);
	}
	
		
	
}
