package BagOfWordsModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Evaluation.Evaluation;
import Evaluation.EvaluationItem;
import Evaluation.ReportGenarator;
import Evaluation.ResultItem;
import Utils.HTMLPages;
import Utils.ProductCatalogs;

public class MultipleRunsInitializer {

	//FILEPATHS
	static String productCategory="tv"; //tv, phone, headphone
	static String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\TVCatalog.json";
	static String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\HTML";
	static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\labelled.txt";
	static String nqFileMap="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\FileNQMap.txt";
	static String allExperimentsResultPath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\ExperimentsResults\\MarkedUpContent\\tvsWithF1Optimization.csv";
	
	//PREPROCESSING
	static boolean stemming=true;
	static boolean stopWordRemoval=true;
	static boolean lowerCase=true;
	static String htmlParsingElements="marked_up_data"; //all_html, html_tables, html_lists, html_tables_lists, marked_up_data


	public static void main (String args[]) throws Exception{
		
		LinkedHashMap<BagOfWordsConfiguration, ResultItem> allResults = new LinkedHashMap<BagOfWordsConfiguration,ResultItem>();
		Queue<BagOfWordsConfiguration> allmodels = defineExperiments();
		
		for(BagOfWordsConfiguration modelConfig:allmodels){
		
			PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
			
			System.out.println("---START---");
			System.out.println("The bag of words model will be executed for the product category "+ productCategory);
			System.out.println("The chosen similarity method is "+modelConfig.getSimilarityType());
			System.out.println("The similarity measure will include Levenshtein distance on top:"+modelConfig.isOnTopLevenshtein()+" with threshold:"+modelConfig.getLevenshteinThreshold());
			System.out.println("The chosen type of weighting (not available for simple similarity method) is "+modelConfig.getTypeOfWeighting());
			System.out.println("The bag of words model will be implemented on the basis of "+modelConfig.getGrams()+" grams");
			
			HashMap<String,List<String>> tokensOfAllHTML = HTMLPages.getHTMLToken(modelConfig, preprocessing);
			HashMap<String,List<String>> tokensOfAllCatalogEntities = ProductCatalogs.getCatalogTokens(modelConfig.getProductCategory(), modelConfig.getCatalog(), modelConfig.getGrams(), preprocessing);
			SimilarityCalculator calculate = new SimilarityCalculator(modelConfig,preprocessing, tokensOfAllHTML, tokensOfAllCatalogEntities);
			File folderHTML = new File(modelConfig.getHtmlFolder());
			File[] listOfHTML = folderHTML.listFiles();
			
			ResultItem results= new ResultItem();
			List<EvaluationItem> ItemstoBeEvaluated = new ArrayList<EvaluationItem>();
			
			for (int i = 0; i < listOfHTML.length; i++) {
				
				HashMap<String, Double> predictedAnswersForPage = new HashMap<String,Double>();
		    	String rightAnswer= calculate.getRightAnswer(modelConfig.getLabelled(), listOfHTML[i].getPath(), modelConfig.getNqFileMap());
		    	if (rightAnswer.equals("n/a")) {
		    		continue;
		    	}
				//System.out.println(listOfHTML[i].getName());
	
		    	//get all the matches with the equivalent scores
		    	predictedAnswersForPage = calculate.getPredictedAnswers(tokensOfAllHTML.get(listOfHTML[i].getName()));
		    	EvaluationItem toBeEvaluated= new EvaluationItem();
		    	toBeEvaluated.setPredictedAnswers(predictedAnswersForPage);
		    	toBeEvaluated.setRightAnswer(rightAnswer);
		    	toBeEvaluated.setProductCategory(modelConfig.getProductCategory());
		    	ItemstoBeEvaluated.add(toBeEvaluated);

		    }
			Evaluation evaluate = new Evaluation();
			results=evaluate.getResultsOptimizingF1(ItemstoBeEvaluated);
			
		    System.out.println("---RESULTS---");
		    System.out.println("Precision: "+results.getPrecision());
		    System.out.println("Recall: "+results.getRecall());
		    System.out.println("F1: "+results.getF1());
			System.out.println("---END---");
			
			allResults.put(modelConfig, results);
		}
		ReportGenarator report = new ReportGenarator();
		report.generateReport(allResults, allExperimentsResultPath);
	}



	private static Queue<BagOfWordsConfiguration> defineExperiments() {
		Queue<BagOfWordsConfiguration> models = new LinkedList<BagOfWordsConfiguration>();
		
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple", "n/a", 1,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple", "n/a", 2,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple", "n/a", 3,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple", "n/a", 2,0,  0,  true, 0.9));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple", "n/a", 2,0,  0,  true, 0.95));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple with frequency threshold", "n/a", 1,0.08, 0, false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple with frequency threshold", "n/a", 1,0.06, 0, false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple with frequency threshold", "n/a", 2,0.05,0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "simple with frequency threshold", "n/a", 2,0.04, 0, false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "jaccard", "n/a", 2,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "jaccard", "n/a", 3,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "jaccard", "n/a", 4,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "simple", 1,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "simple", 2,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "simple", 3,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "simple", 2,0,  0,  true, 0.6));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "tfidf", 1,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "tfidf", 2,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "tfidf", 3,0,  0,  false, 0));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "tfidf", 2,0,  0,  true, 0.6));
		models.add(new BagOfWordsConfiguration
				(productCategory, catalog,htmlFolder,  labelled,  nqFileMap,
				 "cosine", "tfidf", 2,0,  0,  true, 0.8));
		
		

		return models;
	}
	
}
