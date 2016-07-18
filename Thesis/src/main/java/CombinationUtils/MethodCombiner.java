package CombinationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import BagOfWordsModel.SimilarityCalculator;
import Evaluation.Evaluation;
import Evaluation.EvaluationItem;
import Evaluation.ResultItem;

public class MethodCombiner {

	static String fileBow="resources/Rapidminer/scores_bow.txt";
	static String fileDict="resources/Rapidminer/scores_dict.txt";
	static String productCategory="phone";
	static String dataPath="C:/Users/Johannes/Google Drive/Master_Thesis/2.ProfilingOfData/LabelledDataProfiling/";
	static String labelled=dataPath+"/CorrectedLabelledEntities/UnifiedGoldStandard_extra/"+productCategory+"s.txt";
	
	static double weightBow=0.212;
	static double weightDict=0.373;
	static double intercept=-0.013;
	
	
	public static void main (String args[]) throws NumberFormatException, JSONException, IOException{
		
		MethodCombiner run = new MethodCombiner();
		
		ArrayList<HTMLScores> bowScores = run.getSimilarityScores(fileBow);
		ArrayList<HTMLScores> dictScores = run.getSimilarityScores(fileDict);
		ArrayList<EvaluationItem> itemsToBeEvaluated = run.combineTwoMethods(bowScores, dictScores);
		
		System.out.println("Items to be Evaluated:"+itemsToBeEvaluated.size());
		
				
		Evaluation evaluate = new Evaluation("combined");
		ResultItem results= new ResultItem();
		//average, median, optimizingF1
		results=evaluate.getResultsOptimizingF1(itemsToBeEvaluated);
			
	    System.out.println("---RESULTS---");
	    System.out.println("Precision: "+results.getPrecision());
	    System.out.println("Recall: "+results.getRecall());
	    System.out.println("F1: "+results.getF1());
	    System.out.println("Threshold: "+results.getThreshold());
		System.out.println("---END---");
		System.out.println("False Negatives:"+results.getFalseNegatives());
		System.out.println("False Positives:"+results.getFalsePositives());
		System.out.println("True Positives:"+results.getTruePositives());

		for (Map.Entry<String, Integer> fn:results.getFalseNegativesCounts().entrySet())
			System.out.println(fn.getKey()+"--"+fn.getValue());		
	}
	
	
	public ArrayList<HTMLScores> getSimilarityScores(String file) throws NumberFormatException, JSONException, IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));;
		ArrayList<HTMLScores> htmls = new ArrayList<HTMLScores>();
	    String line;
	    String node="";
	    String predicted="";
	    HTMLScores html=null;
	    HashMap<String,Double> scores=null;
	    while ((line = reader.readLine()) != null) {
	        if(line.startsWith("node")||line.startsWith("NEW")) {
	        	//store the previous one
	        	if(null!=html){
	        		htmls.add(html);
	        		html.setScoresTocatalog(scores);
	        	}
	        	
	        	html= new HTMLScores();
	        	node=line.trim();
	        	html.setId(node);
	        	scores = new HashMap<String,Double>();
	        	SimilarityCalculator c = new SimilarityCalculator();
	        	ArrayList<String> rightAnswers= c.getRightAnswer(labelled,html.getId());
	        	html.setRightAnswers(rightAnswers);
	        }
	        if(line.startsWith("Predicted")){
	        	predicted=line.split(":")[1].split("---")[0].trim();
	        	Double simScore=  Double.parseDouble(line.split(":")[1].split("---")[1].trim());
	        	scores.put(predicted, simScore);	        	
	        }
	   }
	   reader.close();
	   return htmls;
		
	}
	
	public ArrayList<EvaluationItem> combineTwoMethods(ArrayList<HTMLScores> bowScores, ArrayList<HTMLScores> dictScores){
		
		ArrayList<EvaluationItem> combinedScores = new ArrayList<EvaluationItem>();
		for(HTMLScores bowHTML:bowScores ){
			EvaluationItem item = new EvaluationItem();
			HashMap<String,Double> predicted = new HashMap<String,Double>();
			HTMLScores fittingDict=null;
			for(HTMLScores dictHTML:dictScores ){
				if(dictHTML.getId().equals(bowHTML.getId())) {
					fittingDict=dictHTML;
					break;
				}
			}
			for(Map.Entry<String, Double> bowPr: bowHTML.getScoresTocatalog().entrySet()){
				double score_bow= bowPr.getValue();
				double score_dict=fittingDict.getScoresTocatalog().get(bowPr.getKey());
				double newScore=score_bow*weightBow+score_dict*weightDict+intercept;
				predicted.put(bowPr.getKey(), newScore);
			}
			
			item.setPath(bowHTML.getId());
			item.setPredictedAnswers(predicted);
			item.setProductCategory(productCategory);
			item.setRightAnswers(bowHTML.getRightAnswers());
			combinedScores.add(item);
		}
		return combinedScores;
	}
}
