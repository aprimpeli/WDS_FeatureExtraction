package BagOfWordsModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import org.json.JSONException;

import Evaluation.Evaluation;
import Evaluation.EvaluationItem;
import Evaluation.ReportGenarator;
import Evaluation.ResultItem;
import Utils.HTMLPages;
import Utils.ProductCatalogs;

public class MultipleRunsInitializer {

	//FILEPATHS
	static String modelType="BagOfWordsModel";
	static String productCategory="headphone"; //tv, phone, headphone
	static String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\ProductCatalog\\HeadphoneCatalog.json";
	static String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\HTML_Pages\\heaphones";
	static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\CorrectedLabelledEntities\\HeadphonesLabelledEntitiesProcessed.txt";
	static String allExperimentsResultPath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\ExperimentsResults\\full_results\\TablesandListsContent\\test_headphones.csv";
	static String logFile="resources\\log\\logEvaluationItems";
	static String mode="wrapper"; // define the mode (wrapper/normal). In the wrapper mode only the 4 plds for which a wrapper exists are considered (ebay, tesco, alibaba, overstock)
	//PREPROCESSING
	static boolean stemming=true;
	static boolean stopWordRemoval=true;
	static boolean lowerCase=true;
	static String htmlParsingElements="html_tables_lists"; //all_html, html_tables, html_lists, html_tables_lists, marked_up_data, html_tables_lists_wrapper

	//String evaluation type definition
	static String evaluationType="optimizingF1"; //average, median, optimizingF1
	public static void main (String args[]) throws Exception{
		runMultipleInitializer();		
	}
	private static void runMultipleInitializer() throws JSONException, IOException{
		LinkedHashMap<ModelConfiguration, ResultItem> allResults = new LinkedHashMap<ModelConfiguration,ResultItem>();
		Queue<ModelConfiguration> allmodels = defineExperiments();
		
		for(ModelConfiguration modelConfig:allmodels){
		
			PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
			
			System.out.println("---START---");
			System.out.println("The bag of words model will be executed for the product category "+ productCategory);
			System.out.println("The chosen similarity method is "+modelConfig.getSimilarityType());
			System.out.println("The similarity measure will include Levenshtein distance on top:"+modelConfig.isOnTopLevenshtein()+" with threshold:"+modelConfig.getLevenshteinThreshold());
			System.out.println("The chosen type of weighting (not available for simple similarity method) is "+modelConfig.getTypeOfWeighting());
			System.out.println("The bag of words model will be implemented on the basis of "+modelConfig.getGrams()+" grams");
			
			HashMap<String,List<String>> tokensOfAllHTML = HTMLPages.getHTMLToken(modelConfig, preprocessing, mode);
			if(null==tokensOfAllHTML) {
				System.out.println("Something went wrong. Check");
				System.exit(0);
			}

			HashMap<String,List<String>> tokensOfAllCatalogEntities = ProductCatalogs.getCatalogTokens(modelConfig.getProductCategory(), modelConfig.getCatalog(), modelConfig.getGrams(), preprocessing);
			SimilarityCalculator calculate = new SimilarityCalculator(modelConfig,preprocessing, tokensOfAllHTML, tokensOfAllCatalogEntities);
			File folderHTML = new File(modelConfig.getHtmlFolder());
			File[] listOfHTML = folderHTML.listFiles();
			
			ResultItem results= new ResultItem();
			List<EvaluationItem> ItemstoBeEvaluated = new ArrayList<EvaluationItem>();
			
			for (int i = 0; i < listOfHTML.length; i++) {
				System.out.println(listOfHTML[i].getPath());
				//if you are in wrapper mode do not consider all pages but only the ones that could be potentially parsed by the implemented wrappers
				String pld = HTMLPages.getPLDFromHTMLPath(labelled, listOfHTML[i].getPath());
		    	if(mode.equals("wrapper") && !(pld.contains("ebay")||pld.contains("tesco")||pld.contains("alibaba")||pld.contains("overstock")) ) continue;
				
				HashMap<String, Double> predictedAnswersForPage = new HashMap<String,Double>();
		    	ArrayList<String> rightAnswers= calculate.getRightAnswer(modelConfig.getLabelled(), listOfHTML[i].getName());
		    	if (rightAnswers.size()==0) {
		    		System.out.println("no answer defined:"+listOfHTML[i]);
		    		continue;
		    	}
		    	//get all the matches with the equivalent scores		    	
		    	predictedAnswersForPage = calculate.getPredictedAnswers(tokensOfAllHTML.get(listOfHTML[i].getName()));
		    	
		    	EvaluationItem toBeEvaluated= new EvaluationItem();
		    	toBeEvaluated.setPredictedAnswers(predictedAnswersForPage);
		    	toBeEvaluated.setRightAnswers(rightAnswers);
		    	toBeEvaluated.setProductCategory(modelConfig.getProductCategory());
		    	ItemstoBeEvaluated.add(toBeEvaluated);

		    }
			System.out.println("Items to be Evaluated:"+ItemstoBeEvaluated.size());
			
			//write some log to check the predictions
			BufferedWriter logger = new BufferedWriter(new FileWriter(new File(logFile+modelConfig.getSimilarityType()+".txt")));
			writeLog(logger, ItemstoBeEvaluated);
			logger.close();
			
			Evaluation evaluate = new Evaluation(modelConfig.getSimilarityType());
			//average, median, optimizingF1
			if (evaluationType.equals("average")) results=evaluate.getResultsWithAverageThreshold(ItemstoBeEvaluated);
			else if (evaluationType.equals("median")) results= evaluate.getResultsWithMedianThreshold(ItemstoBeEvaluated); //*1.5
			else if (evaluationType.equals("optimizingF1")) results=evaluate.getResultsOptimizingF1(ItemstoBeEvaluated);
			else System.out.println("Wrong input for evaluation type. I can only handle average, median and optimizingF1");
			
			
			//average common words
			double avgCommonGrams=((double)calculate.totalCommonElements)/((double)htmlFolder.length()*(double)tokensOfAllCatalogEntities.size());
			results.setAvgCommonGrams(avgCommonGrams);
			
		    System.out.println("---RESULTS---");
		    System.out.println("Precision: "+results.getPrecision());
		    System.out.println("Recall: "+results.getRecall());
		    System.out.println("F1: "+results.getF1());
		    System.out.println("Average Common Grams: "+results.getAvgCommonGrams());
			System.out.println("---END---");
			
			allResults.put(modelConfig, results);
		}
		ReportGenarator report = new ReportGenarator();
		report.generateReport(allResults, allExperimentsResultPath);
	}


	private static void writeLog(BufferedWriter logger,
			List<EvaluationItem> itemstoBeEvaluated) throws IOException {
		for(EvaluationItem ev:itemstoBeEvaluated){
			logger.append("NEW ITEM");
			logger.newLine();
			for (String answer:ev.getRightAnswers()){
				logger.append("Right Answer:"+answer);
				logger.newLine();
			}
			for(Entry<String, Double> pr:ev.getPredictedAnswers().entrySet()){
				logger.append("Predicted Answer:"+pr.getKey()+"---"+pr.getValue());
				logger.newLine();

			}
		}
		
	}
	private static Queue<ModelConfiguration> defineExperiments() {
		Queue<ModelConfiguration> models = new LinkedList<ModelConfiguration>();
		
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled, 
				 "simple", "n/a", 1,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled, 
				 "simple", "n/a", 2,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple", "n/a", 3,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple", "n/a", 2,0,  0,  true, 0.9));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple", "n/a", 2,0,  0,  true, 0.95));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple with frequency threshold", "n/a", 1,0.08, 0, false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple with frequency threshold", "n/a", 1,0.06, 0, false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple with frequency threshold", "n/a", 2,0.05,0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple with frequency threshold", "n/a", 2,0.04, 0, false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "jaccard", "n/a", 2,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "jaccard", "n/a", 3,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "jaccard", "n/a", 4,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "simple", 1,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "simple", 2,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "simple", 3,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "simple", 2,0,  0,  true, 0.6));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "tfidf", 1,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "tfidf", 2,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "tfidf", 3,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "tfidf", 2,0,  0,  true, 0.6));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "cosine", "tfidf", 2,0,  0,  true, 0.8));
		
		

		return models;
	}
	
}
