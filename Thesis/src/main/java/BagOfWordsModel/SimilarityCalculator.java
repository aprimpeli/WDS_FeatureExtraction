package BagOfWordsModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
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
	
	private double simpleWithFrequencyThreshold(List<String> vectorcatalog,
			List<String> vectorpage, double maxFrequency, double minFrequency) {
		
		Weightening frequencies = new Weightening();
		HashMap<String,Integer> vectorPageFrequencies = frequencies.getFrequencyOfWords(vectorpage);
		HashMap<String,Integer> vectorCatalogFrequencies = frequencies.getFrequencyOfWords(vectorcatalog);
		
		//update the input lists
		for (Map.Entry<String, Integer> pageGram :vectorPageFrequencies.entrySet() ){
			if(((double)pageGram.getValue()/(double)vectorPageFrequencies.size()) > maxFrequency || 
					((double)pageGram.getValue()/(double)vectorPageFrequencies.size()) < minFrequency)
				vectorpage.removeAll((Collections.singleton(pageGram.getKey())));
		}
		
		for (Map.Entry<String, Integer> catalogGram :vectorCatalogFrequencies.entrySet() ){
			if(((double)catalogGram.getValue()/(double)vectorCatalogFrequencies.size()) > maxFrequency || 
					((double)catalogGram.getValue()/(double)vectorCatalogFrequencies.size()) < minFrequency)
				vectorcatalog.removeAll((Collections.singleton(catalogGram.getKey())));
		}
		//and then just apply simple similarity containment
		double score= simpleContainmentSimilarity(vectorcatalog,vectorpage);
		return score;
	}
	public double jaccardSimilarity(List<String> catalogVector, List<String> pageVector) {
		//take unique values
		Set<String> catalogVectorSet = new HashSet<String>(catalogVector);
		Set<String> pageVectorSet = new HashSet<String>(pageVector);
		int commonGrams=0;
		
		for(String gram:pageVectorSet){
			ArrayList<String> tokensOfPageGram = new ArrayList<String>();
			String [] pageTokens = gram.split("\\s+");
			for(int i=0;i<pageTokens.length;i++) tokensOfPageGram.add(pageTokens[i]);
			boolean foundCommon=false;
			if (foundCommon) continue;
			for(String catalogGram:catalogVectorSet){
				ArrayList<String> tokensOfCatalogGram = new ArrayList<String>();
				String [] catalogTokens = catalogGram.split("\\s+");
				for (int j=0;j<catalogTokens.length; j++) tokensOfCatalogGram.add(catalogTokens[j]);
				
				List<String> commonElements = new ArrayList<String>(tokensOfPageGram);
				commonElements.retainAll(tokensOfCatalogGram);
				if (commonElements.size()==tokensOfPageGram.size()) {
					commonGrams++;
					foundCommon=true;
					break;
				}
			}
		}
		int unionSize = catalogVectorSet.size()+pageVectorSet.size() - commonGrams;
		double score=((double)commonGrams/(double) unionSize);
		return score;
	}
	
	public double cosineSimilarity(List<String> catalogVector, List<String> pageVector, List<List<String>> wholeCatalogVector,String typeOfWeighting){
		Weightening weights = new Weightening();
		HashMap<String,Double> catalogWeights = new HashMap<String,Double>();
		HashMap<String,Double> pageWeights = new HashMap<String,Double>();
		
		Set<String> commonWords = new HashSet<String>(catalogVector);
		commonWords.retainAll(pageVector);
		
		double score = 0.0;
		if(typeOfWeighting.equals("simple")){
			catalogWeights=weights.getSimpleWeighting(catalogVector);
			pageWeights=weights.getSimpleWeighting(pageVector);
		}
		else if (typeOfWeighting.equals("tfidf")){
			catalogWeights=weights.getTfIdfWeighting(catalogVector, wholeCatalogVector);
			pageWeights=weights.getSimpleWeighting(pageVector);
			//pageWeights=weights.getBooleanWeightning(pageVector);
		}
		else {
			System.out.println("Type of weighting:"+typeOfWeighting+" cannot be calculated. Available options: simple or tfidf. The program will end.");
			System.exit(0);
		}
		score = getCosineSimilarityScore(catalogWeights, pageWeights, commonWords);
		return score;
	}
	
	private double getCosineSimilarityScore(HashMap<String,Double> vector1, HashMap<String,Double> vector2, Set<String> commonWords){
		
		double [] vectorA= new double[commonWords.size()];
		double [] vectorB= new double[commonWords.size()];
		vectorA= fromMapToMatrix(vector1,commonWords );
		vectorB = fromMapToMatrix(vector2, commonWords);

		double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) dotProduct += vectorA[i] * vectorB[i];   
	    for(Map.Entry<String, Double> v1:vector1.entrySet()) normA+=Math.pow(v1.getValue(), 2);
	    for(Map.Entry<String, Double> v2:vector2.entrySet()) normB+=Math.pow(v2.getValue(), 2);

	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	private double[] fromMapToMatrix(HashMap<String,Double> map, Set<String> list){
		
		double[] matrix = new double[list.size()];
		int insertions=0;
		for(String word:list){
			if(map.containsKey(word)){
				matrix[insertions]=map.get(word);
				insertions++;
			} 
		}
		return matrix;
	}
	
	public String getRightAnswer(String labelledEntitiesPath,String htmlPath, String nqFileMapPath) throws JSONException, IOException{
		
		String nodeID=extractNodeIDFromNQMapFile(htmlPath, nqFileMapPath);

		JSONArray labelled = new JSONArray(DocPreprocessor.fileToText(labelledEntitiesPath));
		String rightAnswer = "";
		for(int i = 0 ; i < labelled.length() ; i++){
			JSONObject entity = labelled.getJSONObject(i);
			if(entity.getString("id_self").equals(nodeID)){
				rightAnswer=entity.getString("normalized_product_name");
			}
		}
		if (rightAnswer.equals("")){
			//System.out.println("The nodeID "+nodeID+" does not exist in the labelled set.");
			rightAnswer="n/a";
		}
		return rightAnswer;
	}
	
	public Entry<String, Double> getPredictedAnswer(String catalogPath, String productCategory, String similarityType, String typeOfWeighting, String htmlPage, int grams, double maxFrequency, double minFrequency) throws IOException{
		
		DocPreprocessor process = new DocPreprocessor();
		ProductCatalogs processCatalog = new ProductCatalogs();
		SimilarityCalculator calculate = new SimilarityCalculator();
		
		List<String> vectorpage = process.textProcessing(htmlPage, null, grams, true, false, true, true);
		HashMap<String, List<String>> vectorcatalog = processCatalog.getCatalogTokens(productCategory, catalogPath, grams);
		
		double maxScore = 0;
		String matchedProduct="";
		
		for (Map.Entry<String, List<String>> entry:vectorcatalog.entrySet()){
			double score =0.0;
			if(similarityType.equals("simple"))
				score=calculate.simpleContainmentSimilarity(entry.getValue(), vectorpage);
			else if (similarityType.equals("cosine")){
				List<List<String>> valuesOfMap = new ArrayList<List<String>>();
				for(Map.Entry<String,List<String>> v:vectorcatalog.entrySet() ) valuesOfMap.add(v.getValue());
				score=calculate.cosineSimilarity(entry.getValue(), vectorpage, valuesOfMap,typeOfWeighting);
			}
			else if (similarityType.equals("jaccard")){
				score=calculate.jaccardSimilarity(entry.getValue(), vectorpage);
			}
			else if (similarityType.equals("simple with frequency threshold")){
				score = calculate.simpleWithFrequencyThreshold(entry.getValue(),vectorpage, maxFrequency, minFrequency);
			}
			else{
				System.out.println("The similarity type "+similarityType+" cannot be handled. Available options are cosine , jaccard and simple. The program will end.");
				System.exit(0);
			}
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
		
		//System.out.println("The page "+htmlName+" fitted with score "+maxScore+" to the product name "+matchedProduct);
		return predictedAnswer;
	}
	
	



	

	/**
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 * Can return yes/no/N/A 
	 * */
	public String didIGuessRight(String rightAnswer, String predictedAnswer ) throws JSONException, IOException{
		
				
		//String rightAnswer = getRightAnswer(nodeID, labelledEntitiesPath);
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
