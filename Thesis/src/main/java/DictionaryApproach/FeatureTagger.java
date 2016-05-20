package DictionaryApproach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import org.json.JSONException;

import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.PreprocessingConfiguration;
import BagOfWordsModel.SimilarityCalculator;

public class FeatureTagger {
	
	HashMap<Integer, List<String>> tokenizedInput;
	public FeatureTagger(String htmlPath, boolean isHTML, PreprocessingConfiguration config, String labelledPath) throws IOException{
		tokenizedInput= new HashMap<Integer,List<String>>();
		DocPreprocessor process= new DocPreprocessor();
		for (int i=1; i<10; i++){
			List<String> grams = process.textProcessing(htmlPath, null, i, isHTML, config, labelledPath);
			tokenizedInput.put(i, grams);
		}
		
	}
	
	
	
	public static void main (String args[]) throws JSONException, IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\"
				+ "LabelledDataProfiling\\ProductCatalog\\PhoneCatalog.json";
		String text="this is iphone 4s in gold color it s a smartphone with 5 Megapixel camera";
		DictionaryCreator dict = new DictionaryCreator();
//		FeatureTagger tag = new FeatureTagger();
//		HashMap<String, ArrayList<String>> tagged = tag.setFeatureTagging( text,dict.createDictionary(catalog, "phone").getDictionary());
//		tag.printTagged(tagged);
//		HashMap<String, ArrayList<String>> reversed = tag.reverseTaggedWords(tagged);
//		tag.printTagged(reversed);
	}

	/**
	 * @param input
	 * @param dictionary
	 * @return
	 * Get a full string as input and the dictionary and creates a map out of the common grams between the dictionary and the input
	 * Every gram is then assigned at least one feature name as indicated in the dictionary
	 * example output:
	 * iphone: <phone_type,family_line>
	 * @throws IOException 
	 * 
	 */
	public HashMap<String, ArrayList<String>> setFeatureTagging(String htmlPath, HashMap<String,Set<String>> dictionary,  PreprocessingConfiguration preprocessing, DictionaryApproachModel model) throws IOException{
		
		InputPreprocessor processInput = new InputPreprocessor();
		DocPreprocessor processdoc = new DocPreprocessor();
		HashMap<String, ArrayList<String>> taggedWords= new HashMap<String, ArrayList<String>>();
		for(Map.Entry<String, Set<String>> dictEntry:dictionary.entrySet()){
			for(final String value:dictEntry.getValue()){
				if (model.getSimType().equals("exact")){
					String htmlInput = processInput.textProcessing(htmlPath, "", true, preprocessing, model.getLabelledPath());

					//if the input contains this as a token - not inside a word example: clear and cleared
					if(htmlInput.contains(" "+value+" ")) {
						if(null == taggedWords.get(value)) taggedWords.put(value, new ArrayList<String>());
						taggedWords.get(value).add(dictEntry.getKey());
					}
				}
				else {
					int gramsOfValue=processdoc.getGramsOfValue(value, preprocessing);
					if (!tokenizedInput.containsKey(gramsOfValue+model.getWindowSize())){
//						System.out.println("The input wont be tokenized for "+gramsOfValue+model.getWindowSize()+" grams. That's too much and wont make sense. Next value.");
//						System.out.println("Value:"+value);
//						System.out.println("Property:"+dictEntry.getKey());
						continue;
					}
					
					HashMap<String, Double> initialCandidates = getTopCandidates(value, gramsOfValue+model.getWindowSize(), preprocessing, htmlPath, null, true, model);
								
					HashMap<String, Double> finalCandidates = new HashMap<String, Double>();
			       //for the top candidates try to reduce the window size till you get the best scores meet the threshold
					for (Entry<String, Double> candidate : initialCandidates.entrySet())
			        {
			        	String maxCandidate="";
			        	double maxScore=0.0;
			            for(int i=gramsOfValue+model.getWindowSize(); i>=1; i--){ //reduce window size
			            	HashMap<String, Double> tempCandidates=getTopCandidates(value, i, preprocessing,  null, candidate.getKey(),  false, model);
			            	for (Entry<String,Double> c:tempCandidates.entrySet()){
			            		if (c.getValue()>maxScore) {
			            			maxScore=c.getValue();
			            			maxCandidate=c.getKey();
			           		}
			            		
			            	}
			            }
			            if (maxScore>0.0) finalCandidates.put(maxCandidate,maxScore);

			        }	
					if (finalCandidates.size()>0){
						for(Map.Entry<String, Double> c: finalCandidates.entrySet()){
							if(null == taggedWords.get(c.getKey())) taggedWords.put(c.getKey(), new ArrayList<String>());
							taggedWords.get(c.getKey()).add(dictEntry.getKey());
						}
					}
					
				}
			}
		}
		
		System.out.println("Size of tagged words:"+taggedWords.size());
		return taggedWords;
	}
	
	
	
	public HashMap<String,Double> getTopCandidates(final String valueToCompare, int gramsToTokenize, PreprocessingConfiguration preprocessing,  String htmlPath, 
			String text, boolean isHTML,DictionaryApproachModel model) throws IOException{
		
		DocPreprocessor process = new DocPreprocessor();
		List<String> gramsOfCorpus = new ArrayList<String>();
		HashMap<String,Double> topCandidates = new HashMap<String,Double>();
		
		if(isHTML) gramsOfCorpus=tokenizedInput.get(gramsToTokenize); //precalculated tokenization no need to compute it every time for the main input
		else gramsOfCorpus = process.textProcessing(htmlPath, text, gramsToTokenize, isHTML, preprocessing, model.getLabelledPath());
    	
		Set<String> uniqueGrams = new HashSet<String>(gramsOfCorpus);
    	
    	List<String> gramsOfValue = process.textProcessing(null, valueToCompare, 1, false, preprocessing, model.getLabelledPath());
    	for(final String unique:uniqueGrams){
    		if (unique.length()<model.getPruneLength()) continue;
    		List<String> gramsOfUnique = process.textProcessing(null, unique, 1, false, preprocessing, model.getLabelledPath());
			double score = SimilarityCalculator.getMongeElkanSimilarity(gramsOfUnique, gramsOfValue, model.getEditDistanceType());
			if(score>model.getSimThreshold()) topCandidates.put(unique, score);
    	}
    	return topCandidates;
	}
	public void printTagged(HashMap<String,ArrayList<String> >dictionary){
		for(Map.Entry<String, ArrayList<String>> entry:dictionary.entrySet()){
			String prop=entry.getKey();
			String values="";
			for(String v:entry.getValue())
				values+=v+"--";
			System.out.println(prop+":"+values);
		}
		
	}
	/**
	 * @return
	 * reverse the taggedWords hashMap so that it is like: 
	 * phone_type:iphone
	 * family_line:iphone
	 */
	public HashMap<String, ArrayList<String>> reverseTaggedWords(HashMap<String, ArrayList<String>> tobeReversed){
		HashMap<String, ArrayList<String>> taggedWords = new HashMap<String, ArrayList<String>>();
		for (Map.Entry<String, ArrayList<String>> e:tobeReversed.entrySet()){
			for(String feature:e.getValue()){
				if(null==taggedWords.get(feature)) taggedWords.put(feature, new ArrayList<String>());
				taggedWords.get(feature).add(e.getKey());
			}
		}
		return taggedWords;
	}

	private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> findGreatest(Map<K, V> map, int n)
	{
	    Comparator<? super Entry<K, V>> comparator = 
	        new Comparator<Entry<K, V>>()
	    {
	        public int compare(Entry<K, V> e0, Entry<K, V> e1)
	        {
	            V v0 = e0.getValue();
	            V v1 = e1.getValue();
	            return v0.compareTo(v1);
	        }
	    };
	    PriorityQueue<Entry<K, V>> highest = 
	        new PriorityQueue<Entry<K,V>>(n, comparator);
	    for (Entry<K, V> entry : map.entrySet())
	    {
	        highest.offer(entry);
	        while (highest.size() > n)
	        {
	            highest.poll();
	        }
	    }
	
	    List<Entry<K, V>> result = new ArrayList<Map.Entry<K,V>>();
	    while (highest.size() > 0)
	    {
	        result.add(highest.poll());
	    }
	    return result;
	}

}
