package DictionaryApproach;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;

import org.json.JSONException;

import Utils.HTMLPages;
import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.PreprocessingConfiguration;
import BagOfWordsModel.SimilarityCalculator;
import Evaluation.Evaluation;
import Evaluation.EvaluationItem;
import Evaluation.ReportGenarator;
import Evaluation.ResultItem;

public class MultipleRunsInitializerDictionary {

	//FILEPATHS
	static String modelType="DictionaryApproach";
	static String productCategory="headphone"; //tv, phone, headphone
	static String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\ProductCatalog\\HeadphoneCatalog.json";
	static String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\HTML_Pages\\headphones_test";
	static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\CorrectedLabelledEntities\\HeadphonesLabelledEntitiesProcessed.txt";
	static String allExperimentsResultPath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\ExperimentsResults\\DictionaryApproach\\TablesandListsContent\\headphones.csv";
	static String logFile="resources\\log\\logEvaluationItemsDictionary";
	static String mode="normal"; // define the mode (wrapper/normal). In the wrapper mode only the 4 plds for which a wrapper exists are considered (ebay, tesco, alibaba, overstock)

	//PREPROCESSING
	static boolean stemming=true;
	static boolean stopWordRemoval=true;
	static boolean lowerCase=true;
	static String htmlParsingElements="all_html"; //all_html, html_tables, html_lists, html_tables_lists, marked_up_data, html_tables_lists_wrapper

	//String evaluation type definition
	static String evaluationType="optimizingF1"; //average, median, optimizingF1

	public static void main (String args[]) throws Exception{
		runMultipleInitializer();		
	}
	
	private static void runMultipleInitializer() throws JSONException, IOException{
		
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
		LinkedHashMap<ModelConfiguration, ResultItem> allResults = new LinkedHashMap<ModelConfiguration,ResultItem>();
		Queue<ModelConfiguration> allmodels = defineExperiments();
		
		//create the dictionary
		Dictionary dictionary = new Dictionary();
		DictionaryCreator creator= new DictionaryCreator();
		dictionary=creator.createDictionary(catalog, productCategory,preprocessing, labelled );
		
		for(ModelConfiguration modelConfig:allmodels){
			
			
			System.out.println("---START---");
			System.out.println("The dictionary approach will be executed for the product category "+ productCategory);
			System.out.println("The chosen similarity method is "+modelConfig.getSimilarityType());
			System.out.println("The similarity measure will include Levenshtein distance on top:"+modelConfig.isOnTopLevenshtein()+" with threshold:"+modelConfig.getLevenshteinThreshold());
			
			SimilarityCalculator calculate = new SimilarityCalculator();
			File folderHTML = new File(modelConfig.getHtmlFolder());
			System.out.println(folderHTML.getPath());
			File[] listOfHTML = folderHTML.listFiles();
			ResultItem results= new ResultItem();
			List<EvaluationItem> ItemstoBeEvaluated = new ArrayList<EvaluationItem>();
			
			for (int i = 0; i < listOfHTML.length; i++) {
				//if you are in wrapper mode do not consider all pages but only the ones that could be potentially parsed by the implemented wrappers
				String pld = HTMLPages.getPLDFromHTMLPath(labelled, listOfHTML[i].getPath());
		    	if(mode.equals("wrapper") && !(pld.contains("ebay")||pld.contains("tesco")||pld.contains("alibaba")||pld.contains("overstock")) ) continue;
				
		    	ArrayList<String> rightAnswers= calculate.getRightAnswer(modelConfig.getLabelled(), listOfHTML[i].getName());
		    	if (rightAnswers.size()==0) {
		    		System.out.println("no answer defined:"+listOfHTML[i]);
		    		continue;
		    	}
		    	//get all the matches with the equivalent scores	
				HashMap<String, Double> predictedAnswersForPage = new HashMap<String,Double>();
				//do the tagging step
				FeatureTagger tag = new FeatureTagger();
				InputPreprocessor process = new InputPreprocessor();
				String htmlInput = process.textProcessing(listOfHTML[i].getPath(), null, true, preprocessing, modelConfig.getLabelled());
				
				HashMap<String, ArrayList<String>> tagged = tag.setFeatureTagging(htmlInput, dictionary.getDictionary());
				tag.printTagged(tagged);
				HashMap<String, ArrayList<String>> reversed = tag.reverseTaggedWords(tagged);
				tag.printTagged(reversed);
				FeatureSimilarityComputation sim = new FeatureSimilarityComputation( modelConfig.isOnTopLevenshtein(),modelConfig.getSimilarityType());
		    	predictedAnswersForPage = sim.getPredictedAnswersinDictionaryApproach(reversed ,dictionary,modelConfig);
		    	
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
				 "simple", "n/a", 0,0,  0,  false, 0));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple", "n/a", 0,0,  0,  true, 0.9));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "simple", "n/a", 0,0,  0,  true, 0.95));
		models.add(new ModelConfiguration
				(modelType,productCategory, catalog,htmlFolder,  labelled,  
				 "jaccard", "n/a", 0,0,  0,  false, 0));
		return models;
	}
	
}
