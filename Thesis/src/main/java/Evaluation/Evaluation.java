package Evaluation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Evaluation {
	
	ResultItem currentResults;
	
	public Evaluation(ResultItem results) {
		currentResults=results;
	}

	public ResultItem getResults(HashMap<String, Double> predicted, String answer, String productCategory ){
		HashMap<String, Double> positives = new HashMap<String, Double>();
		HashMap<String, Double> negatives = new HashMap<String, Double>();
		double threshold=defineThresholdasMedian(predicted);
		
		for(Map.Entry<String, Double> entry:predicted.entrySet()){
			if(entry.getValue()>=threshold) positives.put(entry.getKey(), entry.getValue());
			else negatives.put(entry.getKey(), entry.getValue());
		}
		boolean answerInTruePositives= false;
		for (Map.Entry<String,Double> p: positives.entrySet()){
			if (p.getKey().toLowerCase().contains(answer.toLowerCase()) && !productCategory.equals("phone")){
				answerInTruePositives=true;
				break;
			}
			if (p.getKey().toLowerCase().equals(answer.toLowerCase()) && productCategory.equals("phone")){
				answerInTruePositives=true;
				break;
			}
		}
		ResultItem results= new ResultItem();
		if (answerInTruePositives) {
			results.setTruePositives(currentResults.getTruePositives()+1);
			results.setFalsePositives(currentResults.getFalsePositives()+positives.size()-1);
			results.setTrueNegatives(currentResults.getTrueNegatives()+negatives.size());
			results.setFalseNegatives(currentResults.getFalseNegatives()+0);
			
		}
		else {
			results.setTruePositives(currentResults.getTruePositives()+0);
			results.setFalsePositives(currentResults.getFalsePositives()+positives.size());
			results.setTrueNegatives(currentResults.getTrueNegatives()+negatives.size()-1);
			results.setFalseNegatives(currentResults.getFalseNegatives()+1);
		}
		
		return results;
	}

	private double defineThresholdasMedian(HashMap<String, Double> predicted) {
		
		double [] similarityScores = new double[predicted.size()];
		int i=0;
		for(Map.Entry<String, Double> entry:predicted.entrySet()){
			similarityScores[i]=entry.getValue();
			i++;
		}
		
		Arrays.sort(similarityScores);
		double median;
		if (similarityScores.length % 2 == 0)
		    median = ((double)similarityScores[similarityScores.length/2] + (double)similarityScores[similarityScores.length/2 - 1])/2;
		else
		    median = (double) similarityScores[similarityScores.length/2];
		return 1.5*median;

	}

}
