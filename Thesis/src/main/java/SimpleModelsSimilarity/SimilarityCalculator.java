package SimpleModelsSimilarity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Utils.ProductCatalogs;

public class SimilarityCalculator {

	/**
	 * @param catalogVector
	 * @param pageVector
	 * @return
	 * Gets the vector of words of the html page and the catalog entry and ca;culates the simple containment
	 * of the one list to the other. Duplicates are removed
	 */
	public double simpleContainmentSimilarity(List<String> catalogVector, List<String> pageVector){
		
		Set<String> catalogVectorSet = new HashSet<String>(catalogVector);
		Set<String> pageVectorSet = new HashSet<String>(pageVector);
		
		double score = 0;
		List<String> commonElements = new ArrayList<String>(pageVectorSet);
		commonElements.retainAll(catalogVectorSet);
		score = ((double) commonElements.size()) / ((double) pageVectorSet.size());
		
		return score;
	}
	
	public String getRightAnswer(String nodeID, String labelledEntitiesPath) throws JSONException, IOException{
		
		JSONArray labelled = new JSONArray(DocPreprocessor.fileToText(labelledEntitiesPath));
		String rightAnswer = "";
		for(int i = 0 ; i < labelled.length() ; i++){
			JSONObject entity = labelled.getJSONObject(i);
			if(entity.getString("id_self").equals(nodeID)){
				rightAnswer=entity.getString("normalized_product_name");
			}
		}
		if (rightAnswer.equals("")){
			System.out.println("The nodeID "+nodeID+" does not exist in the labelled set.");
			rightAnswer="n/a";
		}
		return rightAnswer;
	}
	
	public Entry<String, Double> getPredictedAnswer(String catalogPath, String productCategory, String htmlPage, int grams) throws IOException{
		
		DocPreprocessor process = new DocPreprocessor();
		ProductCatalogs processCatalog = new ProductCatalogs();
		SimilarityCalculator calculate = new SimilarityCalculator();
		
		List<String> vectorpage = process.textProcessing(htmlPage, null, grams, true, false, true, true);
		HashMap<String, List<String>> vectorcatalog = processCatalog.getCatalogTokens(productCategory, catalogPath, grams);
		
		double maxScore = 0;
		String matchedProduct="";
		
		for (Map.Entry<String, List<String>> entry:vectorcatalog.entrySet()){
			double score=calculate.simpleContainmentSimilarity(entry.getValue(), vectorpage);
			if(score>maxScore){
				maxScore=score;
				matchedProduct=entry.getKey();
			}
			//System.out.println("Page and "+entry.getKey()+" score:"+score);
		}
		Map.Entry<String,Double> predictedAnswer =
			    new AbstractMap.SimpleEntry<String, Double>(matchedProduct, maxScore);
		
		String htmlfrag[] = htmlPage.split("\\\\");
		String htmlName = htmlfrag[htmlfrag.length-1];
		
		System.out.println("The page "+htmlName+" fitted with score "+maxScore+" to the product name "+matchedProduct);
		return predictedAnswer;
	}
	
	/**
	 * @param predictedAnswer
	 * @param htmlPath
	 * @param labelledEntitiesPath
	 * @param nqFileMapPath
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * Can return yes/no/N/A 
	 * */
	public String didIGuessRight(String predictedAnswer, String htmlPath, String labelledEntitiesPath, String nqFileMapPath) throws JSONException, IOException{
		
		String nodeID=extractNodeIDFromNQMapFile(htmlPath, nqFileMapPath);
				
		String rightAnswer = getRightAnswer(nodeID, labelledEntitiesPath);
		if (rightAnswer.equals("n/a"))
			return ("n/a");
		else if (predictedAnswer.toLowerCase().contains(rightAnswer.toLowerCase()))
			return "yes";
		else
			return "no";
		
	}
	
	public static String extractNodeIDFromNQMapFile (String htmlPath, String nqFileMap) throws IOException{
		String htmlfrag[] = htmlPath.split("\\\\");
		String htmlName = htmlfrag[htmlfrag.length-1];
		String nodeID="";
		
		FileInputStream in = new FileInputStream(nqFileMap);
		 BufferedReader br = new BufferedReader(new InputStreamReader(in));
		 String strLine;
	 
		  while((strLine = br.readLine())!= null)
		  {
			  String htmlPageName = strLine.split("\\|\\|\\|\\|")[0];
			  if(htmlPageName.equals(htmlName)){
				  nodeID = strLine.split("\\|\\|\\|\\|")[1].split("\\|\\|")[0];
				  break;
			  }
		  }
		  br.close();
		  if (nodeID.equals("")) {
			  System.out.println("Something went wrong. The node ID could not be retrieved from the NQFileMap file: "+nqFileMap);
		  }
		  return nodeID;
	}
	
}
