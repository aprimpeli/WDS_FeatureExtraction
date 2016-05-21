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
import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.PreprocessingConfiguration;
import BagOfWordsModel.SimilarityCalculator;
import Evaluation.Evaluation;
import Evaluation.EvaluationItem;
import Evaluation.ReportGenarator;
import Evaluation.ResultItem;

public class MultipleRunsInitializerDictionary {

	//FILEPATHS
	static String mainPath="C:\\Users\\Johannes\\Documents\\UserAnna";
	static String modelType="DictionaryApproach";
	static String productCategory="headphone"; //tv, phone, headphone
	static String catalog=mainPath+"\\LabelledDataProfiling\\ProductCatalog\\HeadphoneCatalog.json";
	static String htmlFolder=mainPath+"\\LabelledDataProfiling\\HTML_Pages\\headphones";
	static String labelled=mainPath+"\\LabelledDataProfiling\\CorrectedLabelledEntities\\HeadphonesLabelledEntitiesProcessed.txt";
	static String allExperimentsResultPath=mainPath+"\\headphones.csv";
	static String logFile="resources\\log\\logEvaluationItemsDictionary";
	static String mode="normal"; // define the mode (wrapper/normal). In the wrapper mode only the 4 plds for which a wrapper exists are considered (ebay, tesco, alibaba, overstock)
	
	//PREPROCESSING
	static boolean stemming=false;
	static boolean stopWordRemoval=false;
	static boolean lowerCase=true;
	static String htmlParsingElements="all_html"; //all_html, html_tables, html_lists, html_tables_lists, marked_up_data, html_tables_lists_wrapper
	static double idfThresholdForcatalog=0.5;
	static boolean idfFiltering =true;
	
	//String evaluation type definition
	static String evaluationType="optimizingF1"; //average, median, optimizingF1

	HashMap<String, HashMap<Integer, List<String>>> tokenizedInput;
	HashMap<String, ArrayList<String>> rightAnswersIndex;

	public static void main (String args[]) throws Exception{
		MultipleRunsInitializerDictionary run =new MultipleRunsInitializerDictionary();
		System.out.println("Get Right Answers");
		run.getRightAnswers();
		run.runMultipleInitializer();
	}
	
	private void getRightAnswers() throws JSONException, IOException {
		File folderHTML = new File(htmlFolder);
		File[] listOfHTML = folderHTML.listFiles();
		rightAnswersIndex = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < listOfHTML.length; i++) {
			SimilarityCalculator calculate = new SimilarityCalculator();
	    	ArrayList<String> answers= calculate.getRightAnswer(labelled, listOfHTML[i].getName());
	    	if (answers.size()==0) {
	    		System.out.println("no answer defined:"+listOfHTML[i]);
	    		continue;
	    	}
	    	rightAnswersIndex.put(listOfHTML[i].getName(),answers);
		}
		
	}

	private void initializeTokenizer(String htmlName) throws IOException {
		File folderHTML = new File(htmlFolder);
		File[] listOfHTML = folderHTML.listFiles();
		tokenizedInput= new HashMap<String, HashMap<Integer, List<String>>>();
		for (int i = 0; i < listOfHTML.length; i++) {
			while (!listOfHTML[i].getName().equals(htmlName)) continue;
			PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
			HashMap<Integer,List<String>> pagetokenizedInput= new HashMap<Integer,List<String>>();
			DocPreprocessor process= new DocPreprocessor();
			for (int j=1; j<=10; j++){
				List<String> grams = process.textProcessing(listOfHTML[i].getPath(), null, j, true, preprocessing,labelled);
				pagetokenizedInput.put(j, grams);
			}
			tokenizedInput.put(listOfHTML[i].getName(), pagetokenizedInput);
		}
	}

	private void runMultipleInitializer() throws JSONException, IOException{
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);
		LinkedHashMap<DictionaryApproachModel, ResultItem> allResults = new LinkedHashMap<DictionaryApproachModel,ResultItem>();
		Queue<DictionaryApproachModel> allmodels = defineExperiments();
		
		//create the dictionary
		Dictionary dictionary = new Dictionary();
		DictionaryCreator creator= new DictionaryCreator();
		System.out.println("Get Dictionary");

		dictionary=creator.createDictionary(catalog, productCategory,preprocessing, labelled, idfThresholdForcatalog, idfFiltering);

		System.out.println("Start Running the Models");

		for(DictionaryApproachModel modelConfig:allmodels){

			//creator.printDictionary(dictionary.getDictionary());
			
			System.out.println("---START---");
			System.out.println("The dictionary approach will be executed for the product category "+ productCategory);
			System.out.println("The chosen similarity method is "+modelConfig.getSimType());
			System.out.println("The edit distance measure to be implemented for non exact matching is: "+modelConfig.getEditDistanceType());
			
			File folderHTML = new File(htmlFolder);
			File[] listOfHTML = folderHTML.listFiles();
			ResultItem results= new ResultItem();
			List<EvaluationItem> ItemstoBeEvaluated = new ArrayList<EvaluationItem>();
			
			for (int i = 0; i < listOfHTML.length; i++) {
				
				if(null==tokenizedInput.get(listOfHTML[i].getName())) {
					tokenizedInput=new HashMap<String, HashMap<Integer, List<String>>>();
					initializeTokenizer(listOfHTML[i].getName());
				}
				//if you are in wrapper mode do not consider all pages but only the ones that could be potentially parsed by the implemented wrappers
				String pld = HTMLPages.getPLDFromHTMLPath(labelled, listOfHTML[i].getPath());
		    	if(mode.equals("wrapper") && !(pld.contains("ebay")||pld.contains("tesco")||pld.contains("alibaba")||pld.contains("overstock")) ) continue;
				
		    	
		    	//get all the matches with the equivalent scores	
				HashMap<String, Double> predictedAnswersForPage = new HashMap<String,Double>();
				//do the tagging step
				FeatureTagger tag = new FeatureTagger(tokenizedInput.get(listOfHTML[i].getName()));
				
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
		    	toBeEvaluated.setRightAnswers(rightAnswersIndex.get(listOfHTML[i].getName()));
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
		models.add(new DictionaryApproachModel("non-exact", 2,labelled,  0.8, "default", 3));
		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "default", 3));
		models.add(new DictionaryApproachModel("non-exact", 2,labelled,  0.9, "default", 3));
		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.9, "jaroWrinkler", 3));
		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "jaroWrinkler", 3));
		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "jaroWrinkler", 4));
		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.9, "levenshtein", 3));
		models.add(new DictionaryApproachModel("non-exact", 2,labelled,  0.8, "levenshtein", 3));
		models.add(new DictionaryApproachModel("non-exact", 3,labelled,  0.8, "levenshtein", 4));

		return models;
	}
	
}
