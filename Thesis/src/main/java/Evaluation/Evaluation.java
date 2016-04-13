package Evaluation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluation {
	ResultItem currentResults;
	
	public Evaluation() {
		currentResults=new ResultItem();
	}
	
	public ResultItem getResultsOptimizingF1(List<EvaluationItem>  allLinks ){
		
		ResultItem bestResult= new ResultItem();
		
		for(double a=0; a<=1; a=a+0.001){
			currentResults= new ResultItem();
			double threshold = defineThresholdWitha(allLinks,a);
			currentResults.setA(a);
			currentResults.setThreshold(threshold);
			for(EvaluationItem item:allLinks){
				calculateResults(threshold, item.getPredictedAnswers(), item.getRightAnswer(), item.getProductCategory());
			}
			if (currentResults.getF1()>bestResult.getF1()){
				bestResult=currentResults;
			}
			//if f1 is the same take the result with the best precision
			else if (currentResults.getF1()==bestResult.getF1()){
				if(bestResult.getPrecision()<currentResults.getPrecision()){
					bestResult=currentResults;
				}
			}
		}
		
		return bestResult;
	}

 	private double defineThresholdWitha(List<EvaluationItem> allLinks,
			double a) {
 		//calculate the mean(average)
 		double sum=0;
 		int numberOfAllLinks=0;
 		for(EvaluationItem item:allLinks){
 			for(Map.Entry<String, Double> entry:item.getPredictedAnswers().entrySet()){
 				sum+=entry.getValue();
 				numberOfAllLinks++;
 			}
 		}
		
		double mean= sum/(double)numberOfAllLinks;
		//calculate the standard deviation
		double sdev = 0;
		for(EvaluationItem item:allLinks){
			for(Map.Entry<String, Double> entry:item.getPredictedAnswers().entrySet()){
				sdev+= Math.pow(entry.getValue() - mean, 2);
			}
		}
		
		sdev=sdev/(double)numberOfAllLinks;
		//define threshold
		double threshold= mean+a*sdev;
		
		return threshold;
	}
	
 	public ResultItem getResultsWithMedianThreshold(List<EvaluationItem>  allLinks){
		
 		double threshold=defineThresholdasMedian(allLinks);
 		currentResults.setThreshold(threshold);
 		for(EvaluationItem item:allLinks){			
 			calculateResults(threshold, item.getPredictedAnswers(), item.getRightAnswer(), item.getProductCategory());
 		}	
		return currentResults;
	}

 	public ResultItem getResultsWithAverageThreshold(List<EvaluationItem>  allLinks){
		
 		double threshold=defineThresholdAsAverage(allLinks);
 		currentResults.setThreshold(threshold);

 		for(EvaluationItem item:allLinks){			
 			calculateResults(threshold, item.getPredictedAnswers(), item.getRightAnswer(), item.getProductCategory());
 		}	
		return currentResults;
	}

	private double defineThresholdasMedian(List<EvaluationItem> allLinks) {
				
		double [] similarityScores = new double[allLinks.size()*allLinks.get(0).getPredictedAnswers().size()];
		int i=0;
		for (EvaluationItem item:allLinks){
			for(Map.Entry<String, Double> entry:item.getPredictedAnswers().entrySet()){
				similarityScores[i]=entry.getValue();
				i++;
			}
		}
		
		
		Arrays.sort(similarityScores);
		double median;
		if (similarityScores.length % 2 == 0)
		    median = ((double)similarityScores[similarityScores.length/2] + (double)similarityScores[similarityScores.length/2 - 1])/2;
		else
		    median = (double) similarityScores[similarityScores.length/2];
		
		return 1.5*median;

	}

	private double defineThresholdAsAverage(List<EvaluationItem> allLinks){
		
		double sum=0;
 		int numberOfAllLinks=0;
 		for(EvaluationItem item:allLinks){
 			for(Map.Entry<String, Double> entry:item.getPredictedAnswers().entrySet()){
 				sum+=entry.getValue();
 				numberOfAllLinks++;
 			}
 		}
		
		double mean= sum/(double)numberOfAllLinks;
		
		return mean;
	}
	
	private void calculateResults(double threshold,HashMap<String, Double> predicted, String answer, String productCategory ){
		
		HashMap<String, Double> positives = new HashMap<String, Double>();
		HashMap<String, Double> negatives = new HashMap<String, Double>();
		
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
		if (answerInTruePositives) {
			currentResults.setTruePositives(currentResults.getTruePositives()+1);
			currentResults.setFalsePositives(currentResults.getFalsePositives()+positives.size()-1);
			currentResults.setTrueNegatives(currentResults.getTrueNegatives()+negatives.size());
			currentResults.setFalseNegatives(currentResults.getFalseNegatives()+0);
			
		}
		else {
			currentResults.setTruePositives(currentResults.getTruePositives()+0);
			currentResults.setFalsePositives(currentResults.getFalsePositives()+positives.size());
			currentResults.setTrueNegatives(currentResults.getTrueNegatives()+negatives.size()-1);
			currentResults.setFalseNegatives(currentResults.getFalseNegatives()+1);
		}
		
	}
}
