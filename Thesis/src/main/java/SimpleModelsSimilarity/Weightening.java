package SimpleModelsSimilarity;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Weightening {
	
	public HashMap<String,Double> getSimpleWeighting(List<String> wordsOfVector){
		HashMap<String, Double> gramWeights = new HashMap<String,Double>();
		int totalGrams= wordsOfVector.size();
		HashMap<String, Integer> frequencyOfWords = getFrequencyOfWords(wordsOfVector);
		for(Map.Entry<String,Integer> gramFrequency: frequencyOfWords.entrySet()){
			gramWeights.put(gramFrequency.getKey(), ((double)gramFrequency.getValue()/(double) totalGrams));
		}
		
		return gramWeights;
	}
	
	public HashMap<String,Double> getTfIdfWeighting(List<String> wordsOfVector, List<List<String>> vectorsOfCorpus){
		//get tf
		HashMap<String, Integer> frequencyOfWords = getFrequencyOfWords(wordsOfVector);
		int totalDocuments = vectorsOfCorpus.size();
		HashMap<String,Double> gramWeights= new HashMap<String,Double>();
		for(Map.Entry<String,Integer> gramFrequency: frequencyOfWords.entrySet()){
			int appearanceOfTokeninCorpus=0;
			String token = gramFrequency.getKey();
			//search for the token in the other documents
			for(List<String> gramsOfDoc : vectorsOfCorpus){
				if(gramsOfDoc.contains(token)) appearanceOfTokeninCorpus++;
			}
			double idf = Math.log10((double)totalDocuments /(double) appearanceOfTokeninCorpus);
			gramWeights.put(token, (double)gramFrequency.getValue()*idf);
		}
		return gramWeights;
		
	}

	private HashMap<String, Integer> getFrequencyOfWords(List<String> wordsOfVector){
	
		HashMap<String, Integer> frequencyOfWords = new HashMap<String,Integer>();
		Set<String> unique = new HashSet<String>(wordsOfVector);
		for (String key : unique) {
			frequencyOfWords.put(key, Collections.frequency(wordsOfVector, key));
		    //System.out.println(key + ": " + Collections.frequency(wordsOfVector, key));
		}
		
		return frequencyOfWords;
	}
}
