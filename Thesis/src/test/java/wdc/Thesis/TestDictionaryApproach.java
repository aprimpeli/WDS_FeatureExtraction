package wdc.Thesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.JSONException;
import org.junit.Test;

import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.PreprocessingConfiguration;
import BagOfWordsModel.SimilarityCalculator;
import DictionaryApproach.Dictionary;
import DictionaryApproach.DictionaryCreator;
import DictionaryApproach.FeatureSimilarityComputation;
import DictionaryApproach.FeatureTagger;
import DictionaryApproach.InputPreprocessor;

public class TestDictionaryApproach {
	static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\LabelledDataProfiling\\CorrectedLabelledEntities\\PhonesLabelledEntitiesProcessed.txt";
	static double idfThresholdForcatalog=0.0;
	static int windowSize = 2;
	//PREPROCESSING
	static boolean stemming=false;
	static boolean stopWordRemoval=false;
	static boolean lowerCase=true;
	static String htmlParsingElements="html_tables"; //all_html, html_tables, html_lists, html_tables_lists, marked_up_data, html_tables_lists_wrapper
	Map<Integer, List<String>> tokenizedInput;
	
	@Test
	public void runTest() throws JSONException, IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\"
				+ "LabelledDataProfiling\\ProductCatalog\\PhoneCatalog.json";
		String text="this is i phone4s in gold color it s a smartphone with 5Megapixel camera";
		DictionaryCreator dict = new DictionaryCreator();
		Dictionary dictionary = new Dictionary();
		PreprocessingConfiguration preprocessing = new PreprocessingConfiguration(stemming, stopWordRemoval, lowerCase, htmlParsingElements);

		dictionary=dict.createDictionary(catalog, "phone",preprocessing, labelled, idfThresholdForcatalog);		
		
		tokenizedInput = new HashMap<Integer, List<String>> ();
		DocPreprocessor process= new DocPreprocessor();
		for (int i=1; i<10; i++){
			List<String> grams = process.textProcessing(null, text, i, false, preprocessing, labelled);
			tokenizedInput.put(i, grams);
		}
		
		InputPreprocessor processInput = new InputPreprocessor();
		DocPreprocessor processdoc = new DocPreprocessor();
		HashMap<String, ArrayList<String>> taggedWords= new HashMap<String, ArrayList<String>>();
		for(Map.Entry<String, Set<String>> dictEntry:dictionary.getDictionary().entrySet()){
			for(final String value:dictEntry.getValue()){
				
				int gramsOfValue=processdoc.getGramsOfValue(value, preprocessing);
				if (!tokenizedInput.containsKey(gramsOfValue+windowSize)){
					System.out.println("The input wont be tokenized for "+gramsOfValue+windowSize+" grams. That's too much and wont make sense. Next value.");
					continue;
				}
				HashMap<String, Double> initialCandidates = getTopCandidates(value, gramsOfValue+windowSize, preprocessing, 
						0.2, 0.6, null, text, labelled, false);
				
						
				HashMap<String, Double> finalCandidates = new HashMap<String, Double>();
		       //for the top candidates try to reduce the window size till you get the best scores meet the thresold
		        for (Entry<String, Double> candidate : initialCandidates.entrySet())
		        {
		            for(int i=gramsOfValue+windowSize-1; i>=1; i--){ //reduce window size
		            	
		            	finalCandidates.putAll(getTopCandidates(value, i, preprocessing, 0.8, 0.8, null, candidate.getKey(), labelled, false));
		            }
		        }
			       
				for(Entry<String, Double> candidate : finalCandidates.entrySet())  {
					System.out.println(candidate+" is similar to the value:"+value+" and will be added the feature: "+dictEntry.getKey());
				}  
				
			}
		}
		

		
	}
	public HashMap<String,Double> getTopCandidates(final String valueToCompare, int gramsToTokenize, PreprocessingConfiguration preprocessing, double levThreshold, double simThreshold, String htmlPath, 
			String text, String labelledPath, boolean isHTML) throws IOException{
		
		DocPreprocessor process = new DocPreprocessor();
		List<String> gramsOfCorpus = new ArrayList<String>();
		
		if(isHTML) gramsOfCorpus=tokenizedInput.get(gramsToTokenize); //precalculated tokenization no need to compute it every time for the main input
		else gramsOfCorpus = process.textProcessing(htmlPath, text, gramsToTokenize, isHTML, preprocessing, labelledPath);
    	
		Set<String> uniqueGrams = new HashSet<String>(gramsOfCorpus);
    	
    	ModelConfiguration model = new ModelConfiguration();
    	model.setOnTopLevenshtein(true);
    	model.setLevenshteinThreshold(levThreshold);
    	HashMap<String,Double> topCandidates = new HashMap<String,Double>();
    	
    	
    	for(final String unique:uniqueGrams){
    		SimilarityCalculator calculate = new SimilarityCalculator(model);
			double score = calculate.simpleContainmentSimilarity(new ArrayList<String>() {{add(unique);}}, new ArrayList<String>() {{add(valueToCompare);}});
			if(score>simThreshold) topCandidates.put(unique, score);
    	}
    	return topCandidates;
	}
}
