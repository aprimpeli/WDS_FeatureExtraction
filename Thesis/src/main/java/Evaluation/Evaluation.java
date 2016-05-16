package Evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluation {
	ResultItem currentResults;
	BufferedWriter logEvaluation;
	
	public Evaluation(String simType) throws IOException {
		currentResults=new ResultItem();
		logEvaluation= new BufferedWriter(new FileWriter("resources/logEvaluation.txt"));
		logEvaluation.append(simType);
		logEvaluation.newLine();
	}
	
	public ResultItem getResultsOptimizingF1(List<EvaluationItem>  allLinks ) throws IOException{
		ResultItem bestResult= new ResultItem();
		
		for(double a=0; a<=1; a=a+0.001){
			currentResults= new ResultItem();
			double threshold = defineThresholdWitha(allLinks,a);
			if(Double.isNaN(threshold)) {
				System.out.println("The threshold could not be defined. The program will unsuccessfully exit.");
				System.exit(0);
			}
			currentResults.setA(a);
			currentResults.setThreshold(threshold);
			for(EvaluationItem item:allLinks){
				calculateResults(threshold, item.getPredictedAnswers(), item.getRightAnswers(), item.getProductCategory());
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
	
 	public ResultItem getResultsWithMedianThreshold(List<EvaluationItem>  allLinks) throws IOException{
		
 		double threshold=defineThresholdasMedian(allLinks);
 		currentResults.setThreshold(threshold);
 		for(EvaluationItem item:allLinks){			
 			calculateResults(threshold, item.getPredictedAnswers(), item.getRightAnswers(), item.getProductCategory());
 		}	
		return currentResults;
	}

 	public ResultItem getResultsWithAverageThreshold(List<EvaluationItem>  allLinks) throws IOException{
		
 		double threshold=defineThresholdAsAverage(allLinks);
 		currentResults.setThreshold(threshold);

 		for(EvaluationItem item:allLinks){			
 			calculateResults(threshold, item.getPredictedAnswers(), item.getRightAnswers(), item.getProductCategory());
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
	
	private void calculateResults(double threshold,HashMap<String, Double> predicted, ArrayList<String> rightAnswers, String productCategory ) throws IOException{
		
		ArrayList<String> positives = new ArrayList<String>();
		ArrayList<String> negatives = new ArrayList<String>();
		
		for(Map.Entry<String, Double> entry:predicted.entrySet()){
			if(entry.getValue()>=threshold) positives.add(entry.getKey());
			else negatives.add(entry.getKey());
		}


		//implementation considering that only one answer is correct
//		boolean answerInTruePositives= false;
//				
//		for (Map.Entry<String,Double> p: positives.entrySet()){
//			
//			//logEvaluation.append("Predicted:"+p.getKey().toLowerCase()+";Right:"+arrayList.toLowerCase());
//			logEvaluation.newLine();
//			//System.out.println("Predicted:"+p.getKey()+";Right:"+answer.toLowerCase());
//			if (p.getKey().toLowerCase().equals(rightsAnswer.toLowerCase())){
//				answerInTruePositives=true;
//				break;
//			}
//			
//		}
//		if (answerInTruePositives) {
//			currentResults.setTruePositives(currentResults.getTruePositives()+1);
//			currentResults.setFalsePositives(currentResults.getFalsePositives()+positives.size()-1);
//			currentResults.setTrueNegatives(currentResults.getTrueNegatives()+negatives.size());
//			currentResults.setFalseNegatives(currentResults.getFalseNegatives()+0);
//			
//		}
//		else {
//			currentResults.setTruePositives(currentResults.getTruePositives()+0);
//			currentResults.setFalsePositives(currentResults.getFalsePositives()+positives.size());
//			currentResults.setTrueNegatives(currentResults.getTrueNegatives()+negatives.size()-1);
//			currentResults.setFalseNegatives(currentResults.getFalseNegatives()+1);
//		}
		
		
		List<String> truePositives = new ArrayList<String>(positives);
		truePositives.retainAll(rightAnswers);
		List<String> falsePositives = new ArrayList<String> (positives);
		falsePositives.removeAll(rightAnswers);
		List<String> trueNegatives = new ArrayList<String>(negatives);
		trueNegatives.removeAll(rightAnswers);
		List<String> falseNegatives = new ArrayList<String>(negatives);
		falseNegatives.retainAll(rightAnswers);
		
		
		currentResults.setTruePositives(currentResults.getTruePositives()+truePositives.size());
		currentResults.setFalsePositives(currentResults.getFalsePositives()+falsePositives.size());
		currentResults.setTrueNegatives(currentResults.getTrueNegatives()+trueNegatives.size());
		currentResults.setFalseNegatives(currentResults.getFalseNegatives()+falseNegatives.size());
		
	}
}
