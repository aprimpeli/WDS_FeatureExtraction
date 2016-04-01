package SimpleModelsSimilarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		return rightAnswer;
	}
}
