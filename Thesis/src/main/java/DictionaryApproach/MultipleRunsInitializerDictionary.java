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
import java.util.Set;

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
	static String allExperimentsResultPath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\ExperimentsResults\\DictionaryApproach\\allHTMLContent\\headphones.csv";
	static String logFile="resources\\log\\logEvaluationItemsDictionary";
	static String mode="normal"; // define the mode (wrapper/normal). In the wrapper mode only the 4 plds for which a wrapper exists are considered (ebay, tesco, alibaba, overstock)
	
	//PREPROCESSING
	static boolean stemming=false;
	static boolean stopWordRemoval=false;
	static boolean lowerCase=true;
	static String htmlParsingElements="all_html"; //all_html, html_tables, html_lists, html_tables_lists, marked_up_data, html_tables_lists_wrapper
	static double idfThresholdForcatalog=0.0;
	
	//String evaluation type definition
	static String evaluationType="optimizingF1"; //average, median, optimizingF1

	public static void main (String args[]) throws Exception{
		runMultipleInitializer();		
	}
	
	private static void runMultipleInitializer() throws JSONException, IOException{
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
		LinkedHashMap<DictionaryApproachModel, ResultItem> allResults = new LinkedHashMap<DictionaryApproachModel,ResultItem>();
		Queue<DictionaryApproachModel> allmodels = defineExperiments();
		
		//create the dictionary
		Dictionary dictionary = new Dictionary();
		DictionaryCreator creator= new DictionaryCreator();
		
		for(DictionaryApproachModel modelConfig:allmodels){

			dictionary=creator.createDictionary(catalog, productCategory,preprocessing, labelled, idfThresholdForcatalog);
			//creator.printDictionary(dictionary.getDictionary());
			
			System.out.println("---START---");
			System.out.println("The dictionary approach will be executed for the product category "+ productCategory);
			System.out.println("The chosen similarity method is "+modelConfig.getSimType());
			System.out.println("The edit distance measure to be implemented for non exact matching is: "+modelConfig.getEditDistanceType());
			
			SimilarityCalculator calculate = new SimilarityCalculator();
			File folderHTML = new File(htmlFolder);
			File[] listOfHTML = folderHTML.listFiles();
			ResultItem results= new ResultItem();
			List<EvaluationItem> ItemstoBeEvaluated = new ArrayList<EvaluationItem>();
			
			for (int i = 0; i < listOfHTML.length; i++) {
				//if you are in wrapper mode do not consider all pages but only the ones that could be potentially parsed by the implemented wrappers
				String pld = HTMLPages.getPLDFromHTMLPath(labelled, listOfHTML[i].getPath());
		    	if(mode.equals("wrapper") && !(pld.contains("ebay")||pld.contains("tesco")||pld.contains("alibaba")||pld.contains("overstock")) ) continue;
				
		    	ArrayList<String> rightAnswers= calculate.getRightAnswer(modelConfig.getLabelledPath(), listOfHTML[i].getName());
		    	if (rightAnswers.size()==0) {
		    		System.out.println("no answer defined:"+listOfHTML[i]);
		    		continue;
		    	}
		    	//get all the matches with the equivalent scores	
				HashMap<String, Double> predictedAnswersForPage = new HashMap<String,Double>();
				//do the tagging step
				FeatureTagger tag = new FeatureTagger(listOfHTML[i].getPath(),true, preprocessing,modelConfig.getLabelledPath());
				
				HashMap<String, ArrayList<String>> tagged = tag.setFeatureTagging(listOfHTML[i].getPath(), dictionary.getDictionary(),preprocessing, modelConfig);
				tag.printTagged(tagged);
				HashMap<String, ArrayList<String>> reversed = tag.reverseTaggedWords(tagged);
				if (reversed.size()==0) {
					System.out.println("No tagging could be done for the page:"+listOfHTML[i].getPath());
				}
				//tag.printTagged(reversed);
				FeatureSimilarityComputation sim = new FeatureSimilarityComputation();
		    	predictedAnswersForPage = sim.getPredictedAnswersinDictionaryApproach(reversed ,dictionary,modelConfig,listOfHTML[i].getPath());
		    	
		    	EvaluationItem toBeEvaluated= new EvaluationItem();
		    	toBeEvaluated.setPredictedAnswers(predictedAnswersForPage);
		    	toBeEvaluated.setRightAnswers(rightAnswers);
		    	toBeEvaluated.setProductCategory(productCategory);
		    	ItemstoBeEvaluated.add(toBeEvaluated);

		    }
			System.out.println("Items to be Evaluated:"+ItemstoBeEvaluated.size());
			
			//write some log to check the predictions
			BufferedWriter logger = new BufferedWriter(new FileWriter(new File(logFile+modelConfig.getSimType()+".txt")));
			writeLog(logger, ItemstoBeEvaluated);
			logger.close();
			
			Evaluation evaluate = new Evaluation(modelConfig.getSimType());
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
		report.generateReportDictionaryApproach(allResults, allExperimentsResultPath);
		
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
	private static Queue<DictionaryApproachModel> defineExperiments() {
		Queue<DictionaryApproachModel> models = new LinkedList<DictionaryApproachModel>();
		
//		public DictionaryApproachModel(String simType, int windowSize,
//				String labelledPath, double simThreshold, String editDistanceType,
//				int pruneLength)
		
		models.add(new DictionaryApproachModel("exact", 0,labelled,  0, null, 0));
		models.add(new DictionaryApproachModel("non-exact", 2,labelled,  0.9, "jaroWrinkler", 3));
//		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "default", 3));
//		models.add(new DictionaryApproachModel("non-exact", 2,labelled,  0.9, "default", 3));
//		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.9, "jaroWrinkler", 3));
//		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "jaroWrinkler", 3));
//		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "jaroWrinkler", 4));
//		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.9, "levenshtein", 3));
//		models.add(new DictionaryApproachModel("non-exact", 2,labelled,  0.8, "levenshtein", 3));
//		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "levenshtein", 4));

		return models;
	}
	
}
