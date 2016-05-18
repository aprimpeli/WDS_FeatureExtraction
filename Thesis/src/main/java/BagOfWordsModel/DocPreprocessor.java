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
import Utils.LabelledFileExtractions;
import Utils.NodeFromLabelled;




public class DocPreprocessor {

	public static void main (String [] args){
		try{
			String filepath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\HTML\\43uf6400_1.html";
			String labelledPath="";
			DocPreprocessor process = new DocPreprocessor();
			System.out.println("CASE 1");
			PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(true, true, true, "marked_up_data");
			process.printList(process.textProcessing(filepath, null ,1,true, preprocessing,labelledPath));
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
	public List<String> textProcessing (String filepath, String text, int grams, boolean isHTML,  PreprocessingConfiguration preprocessing, String labelledPath) throws IOException{
		
		String extractedText=getText(isHTML, text, filepath, preprocessing, labelledPath);
		if(null==extractedText) return null;
		Reader corpus= StringToReaderConverter(extractedText);
		
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
			processedWords.clear();
			corpus.reset();
			corpus = StringToReaderConverter(buildText.toString());
			result.reset();
			result = new StandardTokenizer(Version.LUCENE_36, corpus);
			result = new ShingleFilter(result, grams,grams);	
			while(result.incrementToken()){
				String token=((CharTermAttribute)result.getAttribute(CharTermAttribute.class)).toString();
				token.trim();
				if(!token.contains(" ")) continue;
				processedWords.add(token);	
			}
		}
		result.close();
//		System.out.println("Size of stemmed words:"+stemmedWords.size());
		return processedWords;
    }
	
	public String getText(boolean isHTML, String text, String filepath,
			PreprocessingConfiguration preprocessing, String labelledpath) throws IOException {

		if (null==text && null!=filepath)
			text = fileToText(filepath);
		
		if (isHTML) {
			if(preprocessing.getHtmlParsingType().equals("all_html"))
				text= Jsoup.parse(text).text();
			else if(preprocessing.getHtmlParsingType().equals("html_tables")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor(labelledpath);
				text=utils.getTableText(filepath);
			}
			else if(preprocessing.getHtmlParsingType().equals("html_lists")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor(labelledpath);
				text=utils.getListText(filepath);
			}
			else if(preprocessing.getHtmlParsingType().equals("html_tables_lists")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor(labelledpath);
				StringBuilder allContent = new StringBuilder();
				String listText=utils.getListText(filepath);
				//System.out.println("LIST:"+listText);
				String tableText=utils.getTableText(filepath);
				//System.out.println("TABLE:"+tableText);
				allContent.append(listText).append(tableText);
				text = allContent.toString();
				//System.out.println("ALL:"+text);
			}
			else if(preprocessing.getHtmlParsingType().equals("html_tables_lists_wrapper")){
				HTMLFragmentsExtractor utils = new HTMLFragmentsExtractor(labelledpath);
				StringBuilder allContent = new StringBuilder();
				text=utils.getTableWithWrapperText(filepath);
				if (null==text) return null;
				allContent.append(text);
				text = allContent.toString();
				//System.out.println("ALL:"+text);
			}
			else if (preprocessing.getHtmlParsingType().equals("marked_up_data")){
				 NodeFromLabelled node =LabelledFileExtractions.extractNodeFromLabelledFile(filepath, labelledpath);
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
	
	public int getGramsOfValue(String value, PreprocessingConfiguration preprocessing) throws IOException {
		
		Reader corpus= StringToReaderConverter(value);
		
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
		
		
		while(result.incrementToken()){
			String token=((CharTermAttribute)result.getAttribute(CharTermAttribute.class)).toString();		
			processedWords.add(token);	
		}	
		result.close();
		return processedWords.size();
	}
	
		
	
}
