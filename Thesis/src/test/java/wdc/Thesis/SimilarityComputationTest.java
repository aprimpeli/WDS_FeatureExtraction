package wdc.Thesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import BagOfWordsModel.BagOfWordsConfiguration;
import BagOfWordsModel.PreprocessingConfiguration;
import BagOfWordsModel.SimilarityCalculator;

public class SimilarityComputationTest {

	//FILEPATHS
		static String productCategory="phone"; //tv, phone, headphone
		static String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\PhoneCatalog.json";
		static String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\phones\\HTML";
		static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\phones\\labelled.txt";
		static String nqFileMap="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\phones\\FileNQMap.txt";
		
		//PREPROCESSING
		static boolean stemming=true;
		static boolean stopWordRemoval=true;
		static boolean lowerCase=true;
		
		//SIMILARITY CONFIGURATION
		//cosine or simple(exact matching) or jaccard or simple with frequency threshold
		static String similarityType="jaccard";
		//simple(average frequency) or tfidf applied only for cosine similarity
		static String typeOfWeighting="tfidf";
		//any possible number of n-grams is possible
		static int grams=1;
		//applied only for simple with frequency threshold similarity - otherwise they make no sense
		static double maxFreq=0.03;
		static double minFreq=0;
		//can be applies on top of everyone of the previous similarity types in order to calculate common words
		static boolean onTopLevenshtein=false;
		static double levenshteinThreshold=0.8;
	@Test
	public void runSimilarityTest() throws IOException{
		List<String> doc1 = new ArrayList<String>(){{
			add("apple");
			add("apple");
			add("cake");
			add("cut");
			add("slice");
			add("recipe");
			add("recipe");
			
		}};			
		List<String> doc2 = new ArrayList<String>(){{
			add("slice");
			add("slice");
			add("recipe");
			add("recipe");
			add("meat");
			add("meat");
			add("meat");
			add("bean");
			add("cook");
			add("buy");

		}};		
		List<String> doc3 = new ArrayList<String>(){{
			add("apple");
			add("cake");
			add("cake");
			add("slice");
			add("buy");
			add("buy");
			add("buy");
			
		}};	
	
		List<String> query1 = new ArrayList<String>(){{
			add("apple");
			add("cake");
			add("recipe");
			
		}};	
		
		List<String> query2 = new ArrayList<String>(){{
			add("apple");
			add("cut");
			add("cake");
			
		}};	
	
		HashMap<String, List<String>> docCorpus = new HashMap<String,List<String>>();
		docCorpus.put("doc1",doc1);
		docCorpus.put("doc2",doc2);
		docCorpus.put("doc3",doc3);
		
		HashMap<String, List<String>> queryCorpus = new HashMap<String,List<String>>();
		queryCorpus.put("query1",query1);
		queryCorpus.put("query2",query2);
		
		BagOfWordsConfiguration modelConfig= new BagOfWordsConfiguration(productCategory, catalog,
				 htmlFolder,  labelled,  nqFileMap,
				 similarityType, typeOfWeighting, grams,
				 maxFreq,  minFreq,  onTopLevenshtein,
				 levenshteinThreshold);
		
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase);

		SimilarityCalculator calculate = new SimilarityCalculator(modelConfig, preprocessing, queryCorpus, docCorpus );

		for(Map.Entry<String,List<String>> query: queryCorpus.entrySet()){
			calculate.getPredictedAnswers(query.getValue());
		}
	}
}
