package BagOfWordsModel;

import java.io.File;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Evaluation.Evaluation;
import Evaluation.ResultItem;
import Utils.HTMLPages;
import Utils.ProductCatalogs;

public class Initializer {

	//FILEPATHS
	static String productCategory="headphone"; //tv, phone, headphone
	static String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\HeadphoneCatalog.json";
	static String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\headphones\\HTML";
	static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\headphones\\labelled.txt";
	static String nqFileMap="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\headphones\\FileNQMap.txt";
	
	//PREPROCESSING
	static boolean stemming=true;
	static boolean stopWordRemoval=true;
	static boolean lowerCase=true;
	
	//SIMILARITY CONFIGURATION
	//cosine or simple(exact matching) or jaccard or simple with frequency threshold
	static String similarityType="cosine";
	//simple(average frequency) or tfidf applied only for cosine similarity
	static String typeOfWeighting="tfidf";
	//any possible number of n-grams is possible
	static int grams=1;
	//applied only for simple with frequency threshold similarity - otherwise they make no sense
	static double maxFreq=0.04;
	static double minFreq=0;
	//can be applies on top of everyone of the previous similarity types in order to calculate common words
	static boolean onTopLevenshtein=true;
	static double levenshteinThreshold=0.8;
	

	
	public static void main (String args[]) throws Exception{
		
		BagOfWordsConfiguration modelConfig= new BagOfWordsConfiguration(productCategory, catalog,
				 htmlFolder,  labelled,  nqFileMap,
				 similarityType, typeOfWeighting, grams,
				 maxFreq,  minFreq,  onTopLevenshtein,
				 levenshteinThreshold);
		
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase);
		
		System.out.println("---START---");
		System.out.println("The bag of words model will be executed for the product category "+ productCategory);
		System.out.println("The chosen similarity method is "+similarityType);
		System.out.println("The similarity measure will include Levenshtein distance on top:"+onTopLevenshtein+" with threshold:"+levenshteinThreshold);
		System.out.println("The chosen type of weighting (not available for simple similarity method) is "+typeOfWeighting);
		System.out.println("The bag of words model will be implemented on the basis of "+grams+" grams");
		
		HashMap<String,List<String>> tokensOfAllHTML = HTMLPages.getHTMLToken(modelConfig.getHtmlFolder(), modelConfig.getGrams(), preprocessing);
		HashMap<String,List<String>> tokensOfAllCatalogEntities = ProductCatalogs.getCatalogTokens(modelConfig.getProductCategory(), modelConfig.getCatalog(), modelConfig.getGrams(), preprocessing);
		SimilarityCalculator calculate = new SimilarityCalculator(modelConfig,preprocessing, tokensOfAllHTML, tokensOfAllCatalogEntities);
		File folderHTML = new File(modelConfig.getHtmlFolder());
		File[] listOfHTML = folderHTML.listFiles();
		
		HashMap<String, Double> predictedAnswersForPage = new HashMap<String,Double>();
		ResultItem results= new ResultItem();

		for (int i = 0; i < listOfHTML.length; i++) {
	    	String rightAnswer= calculate.getRightAnswer(modelConfig.getLabelled(), listOfHTML[i].getPath(), modelConfig.getNqFileMap());
	    	if (rightAnswer.equals("n/a")) {
	    		continue;
	    	}
			//System.out.println(listOfHTML[i].getName());

	    	//get all the matches with the equivalent scores
	    	predictedAnswersForPage = calculate.getPredictedAnswers(tokensOfAllHTML.get(listOfHTML[i].getName()));
	    	
			Evaluation evaluate = new Evaluation(results);
			results=evaluate.getResults(predictedAnswersForPage, rightAnswer, modelConfig.getProductCategory());
	    }
	    
	    System.out.println("---RESULTS---");
	    System.out.println("Precision: "+results.getPrecision());
	    System.out.println("Recall: "+results.getRecall());
	    System.out.println("F1: "+results.getF1());
		System.out.println("---END---");

	}
	
}
