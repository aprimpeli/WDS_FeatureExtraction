package BagOfWordsModel;

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
	
	public HashMap<String, Double> getIDFWeighting(List<List<String>> vectorsOfCorpus){
		HashMap<String,Double> IDFWeights = new HashMap<String,Double>();
		Set<String> uniqueGrams = new HashSet<String>();
		for(List<String> vector: vectorsOfCorpus){
			for(String gram:vector)
				uniqueGrams.add(gram);
		}
		for (String uniqueGram:uniqueGrams){
			
			int appearanceOfTokeninCorpus=0;
			for(List<String> vector: vectorsOfCorpus)
				if(vector.contains(uniqueGram)) appearanceOfTokeninCorpus++;
			IDFWeights.put(uniqueGram, Math.log10((double)vectorsOfCorpus.size()/(double)appearanceOfTokeninCorpus));
		}
		
		return IDFWeights;
	}
	
	public HashMap<String, Double> getTfIdfWeighting (List<String> wordsOfVector, HashMap<String,Double> IDFWeights){
		
		HashMap<String, Double> tfidfWeights = new HashMap<String,Double>();
		HashMap<String, Integer> frequencyOfWords = getFrequencyOfWords(wordsOfVector);
		double tfidf=0;
		for(Map.Entry<String,Integer> gramFrequency: frequencyOfWords.entrySet()){	
			if(null==IDFWeights.get(gramFrequency.getKey())){
				System.out.println("The idf weight for:"+gramFrequency.getKey()+"could not be retrieved. If you are in inexact matching this is ok.");
			}
			else  tfidf = (double) gramFrequency.getValue() * IDFWeights.get(gramFrequency.getKey());
			tfidfWeights.put(gramFrequency.getKey(), tfidf);
			//System.out.println(gramFrequency.getKey()+"--tf:"+gramFrequency.getValue()+"--idf:"+IDFWeights.get(gramFrequency.getKey())+"--tfidf:"+tfidf);
		}
		return tfidfWeights;
	}
	
	public HashMap<String,Double> getTfIdfWeighting_(List<String> wordsOfVector, List<List<String>> vectorsOfCorpus){
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

	public HashMap<String,Double> getBooleanWeightning(List<String> wordsOfVector){
		HashMap<String,Double> weights = new HashMap<String,Double>();
		for (String gram: wordsOfVector)
			weights.put(gram, 1.0);
		return weights;
	}
	public HashMap<String, Integer> getFrequencyOfWords(List<String> wordsOfVector){
	
		HashMap<String, Integer> frequencyOfWords = new HashMap<String,Integer>();
		Set<String> unique = new HashSet<String>(wordsOfVector);
		for (String key : unique) {
			frequencyOfWords.put(key, Collections.frequency(wordsOfVector, key));
		}
		return frequencyOfWords;
	}
}
