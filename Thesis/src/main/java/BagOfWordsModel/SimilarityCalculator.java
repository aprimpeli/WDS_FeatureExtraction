package BagOfWordsModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simmetrics.StringMetric;
import org.simmetrics.builders.StringMetricBuilder;
import org.simmetrics.builders.StringMetricBuilder.CollectionMetricInitialSimplifierStep;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.metrics.MongeElkan;
import org.simmetrics.metrics.SmithWatermanGotoh;
import org.simmetrics.metrics.StringMetrics;
import org.simmetrics.simplifiers.Soundex;
import org.simmetrics.tokenizers.Tokenizers;

import Utils.ErrorAnalysisLog;

import org.simmetrics.*;


public class SimilarityCalculator { 
	
	static ModelConfiguration model;
	static PreprocessingConfiguration preprocessing;
	static HashMap<String,List<String>> vectorCatalogEntities;
	List<List<String>> CatalogEntitiesAsList;
	List<List<String>> PagesAsList;
	int totalCommonElements;
	static String logLine;
	ErrorAnalysisLog logger;
	String pageName;
	String productName;
	
	public SimilarityCalculator(ModelConfiguration modelConfig, PreprocessingConfiguration preprocessing, HashMap<String,List<String>> pagesTokens,HashMap<String,List<String>> catalogTokens,ErrorAnalysisLog logger ) {
		SimilarityCalculator.model = modelConfig;
		SimilarityCalculator.preprocessing=preprocessing;				
		SimilarityCalculator.vectorCatalogEntities = catalogTokens;
		Weightening weights = new Weightening();
		totalCommonElements=0;
		
		CatalogEntitiesAsList = new ArrayList<List<String>>();
		for(Map.Entry<String,List<String>> v:vectorCatalogEntities.entrySet() ) CatalogEntitiesAsList.add(v.getValue());
		
		PagesAsList = new ArrayList<List<String>>();
		for(Map.Entry<String,List<String>> v:pagesTokens.entrySet() ) PagesAsList.add(v.getValue());

		//precalculation of the tfidf weighting so that we dont have to repeat every time
		if(modelConfig.getSimilarityType().equals("cosine") && modelConfig.getTypeOfWeighting().equals("tfidf")){
			modelConfig.setIdfWeightsCatalog(weights.getIDFWeighting(CatalogEntitiesAsList));
			modelConfig.setIdfWeightsPages(weights.getIDFWeighting(PagesAsList));
		}
		this.logger=logger;
		logger.setCommonWords(new HashMap<String, String>());
//		//precalculation of words frequencies
//		if(modelConfig.getSimilarityType().equals("simple with frequency threshold")){
//			modelConfig.setVectorCatalogFrequencies(weights.getFrequencyOfWords(wordsOfVector));
//			modelConfig.setVectorPageFrequencies(weights.getFrequencyOfWords(wordsOfVector));
//		}
//		
//		//precalculation of words vectors after frequency threshold
	}
	public SimilarityCalculator(ModelConfiguration modelConfig) {
		SimilarityCalculator.model=modelConfig;
	}

	public SimilarityCalculator() {
		// TODO Auto-generated constructor stub
	}
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
		Set<String> commonElements = new HashSet<String>();
		if(SimilarityCalculator.model.isOnTopLevenshtein()){
			for(String gram1:catalogVectorSet){
				for(String gram2:pageVectorSet){
					if(commonWithLevenshteinSimilarity(gram1, gram2, model.getLevenshteinThreshold()))
						commonElements.add(gram1);
				}
			}			
		}
		else{
			commonElements = new HashSet<String>(pageVectorSet);
			commonElements.retainAll(catalogVectorSet);
		}
		
		double minsize=0.0;
		if(catalogVectorSet.size()<pageVectorSet.size()) minsize=catalogVectorSet.size();
		else minsize=pageVectorSet.size();
		if(minsize==0) return 0; //TODO
		score = ((double) commonElements.size()) / ((double) minsize);
		totalCommonElements+=commonElements.size();
		
		logger.getCommonWords().put(this.pageName+";"+this.productName,commonElements.toString());
		return score;
	}
	
	

	private double simpleWithFrequencyThreshold(List<String> vectorcatalog,
			List<String> vectorpage) {
		
		Weightening frequencies = new Weightening();
		HashMap<String,Integer> vectorPageFrequencies = frequencies.getFrequencyOfWords(vectorpage);
		HashMap<String,Integer> vectorCatalogFrequencies = frequencies.getFrequencyOfWords(vectorcatalog);
		
		if(vectorpage.size()==0) return 0;
		//update the input lists
		for (Map.Entry<String, Integer> pageGram :vectorPageFrequencies.entrySet() ){
			if(((double)pageGram.getValue()/(double)vectorpage.size()) > model.getMaxFreq() || 
					((double)pageGram.getValue()/(double)vectorpage.size()) < model.getMinFreq())
				vectorpage.removeAll((Collections.singleton(pageGram.getKey())));
		}
		for (Map.Entry<String, Integer> catalogGram :vectorCatalogFrequencies.entrySet() ){
			if(((double)catalogGram.getValue()/(double)vectorcatalog.size()) > model.getMaxFreq() || 
					((double)catalogGram.getValue()/(double)vectorcatalog.size()) < model.getMinFreq())
				vectorcatalog.removeAll((Collections.singleton(catalogGram.getKey())));
		}
		if(vectorcatalog.size()==0 || vectorpage.size()==0){
			System.out.println("The frequency thresholds given for the simple similarity with frequency threshold calculation were inappropriate and emptied the page or the catalog vectors. Please choose better thresholds");
			return 0;
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
		ArrayList<String> common = new ArrayList<String>();
		
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
				
				ArrayList<String> commonElements = new ArrayList<String>();
				if(SimilarityCalculator.model.isOnTopLevenshtein()){
					for(String gram1:tokensOfCatalogGram){
						for(String gram2:tokensOfPageGram)
							if(commonWithLevenshteinSimilarity(gram1, gram2, model.getLevenshteinThreshold()))
								commonElements.add(gram1);
					}
				} 
				else {
					commonElements = new ArrayList<String>(tokensOfPageGram);
					commonElements.retainAll(tokensOfCatalogGram);
				}
				if (commonElements.size()==tokensOfPageGram.size()) {
					common.add(gram);
					commonGrams++;
					foundCommon=true;
					break;
				}
			}
		}
		int unionSize = catalogVectorSet.size()+pageVectorSet.size() - commonGrams;
		double score=((double)commonGrams/(double) unionSize);
		totalCommonElements+=commonGrams;
		logger.getCommonWords().put(this.pageName+";"+this.productName,common.toString());
		return score;
	}
	
	public double cosineSimilarity(List<String> catalogVector, List<String> pageVector){
		
		Weightening weights = new Weightening();
		HashMap<String,Double> catalogWeights = new HashMap<String,Double>();
		HashMap<String,Double> pageWeights = new HashMap<String,Double>();
		
		Set<String> commonWords = new HashSet<String>();
		if( SimilarityCalculator.model.isOnTopLevenshtein()){
			for(String gram1:catalogVector){
				for(String gram2:pageVector)
					if(commonWithLevenshteinSimilarity(gram1, gram2, model.getLevenshteinThreshold()))
						commonWords.add(gram1);
			}
		}
		else{
			commonWords = new HashSet<String>(catalogVector);
			commonWords.retainAll(pageVector);
		}
		
		
		double score = 0.0;
		if (commonWords.size()>0){
			
			
			if(model.getTypeOfWeighting().equals("simple")){
				catalogWeights=weights.getSimpleWeighting(catalogVector);
				pageWeights=weights.getSimpleWeighting(pageVector);
			}
			else if (model.getTypeOfWeighting().equals("tfidf")){
				catalogWeights=weights.getTfIdfWeighting(catalogVector, model.getIdfWeightsCatalog());
				pageWeights=weights.getTfIdfWeighting(pageVector, model.getIdfWeightsPages());
				//pageWeights=weights.getBooleanWeightning(pageVector);
			}
			else {
				System.out.println("Type of weighting:"+model.getTypeOfWeighting()+" cannot be calculated. Available options: simple or tfidf. The program will end.");
				System.exit(0);
			}
			score = getCosineSimilarityScore(catalogWeights, pageWeights, commonWords);
			totalCommonElements+=commonWords.size();
		}
		logger.getCommonWords().put(this.pageName+";"+this.productName,commonWords.toString());
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

	    if(normA==0 || normB==0) {
	    	System.out.println("Normalization factor for Cosine equals 0. Please check.");
	    	return 0; //TODO
	    }
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
	
	public ArrayList<String> getRightAnswer(String labelledEntitiesPath,String htmlName) throws JSONException, IOException{
		
		String nodeID=htmlName.replace(".html", "");

		JSONArray labelled = new JSONArray(DocPreprocessor.fileToText(labelledEntitiesPath));
		ArrayList<String> rightAnswers = new ArrayList<String>();
		String rightAnswer = "";
		for(int i = 0 ; i < labelled.length() ; i++){
			JSONObject entity = labelled.getJSONObject(i);
			if(entity.getString("id_self").equals(nodeID)){
				rightAnswer = entity.getString("normalized_product_name");
				String [] answers = rightAnswer.split(";");
				for(int j=0;j<answers.length;j++) rightAnswers.add(answers[j]);
				break;
			}
		}
		
		return rightAnswers;
	}
	
	public HashMap<String, Double> getPredictedAnswers(List<String> vectorpage, String pageName) throws IOException{
				
		
		HashMap<String, Double> predictedAnswers = new HashMap<String,Double>();
		
		for (Map.Entry<String, List<String>> entry:vectorCatalogEntities.entrySet()){
			
			this.pageName=pageName;
			this.productName=entry.getKey();
			
			double score =0.0;
			if(model.getSimilarityType().equals("simple"))
				score=simpleContainmentSimilarity(entry.getValue(), vectorpage);
			
			else if (model.getSimilarityType().equals("cosine")){				
				score=cosineSimilarity(entry.getValue(), vectorpage);
			}
			else if (model.getSimilarityType().equals("jaccard")){
				score=jaccardSimilarity(entry.getValue(), vectorpage);
			}
			else if (model.getSimilarityType().equals("simple with frequency threshold")){
				score = simpleWithFrequencyThreshold(entry.getValue(),vectorpage);
			}
			else{
				System.out.println("The similarity type "+model.getSimilarityType()+" cannot be handled. Available options are cosine , jaccard and simple. The program will end.");
				System.exit(0);
			}
			predictedAnswers.put(entry.getKey().toLowerCase(), score);

		}
		
		
		//String htmlfrag[] = htmlPage.split("\\\\");
		//String htmlName = htmlfrag[htmlfrag.length-1];
		
		//System.out.println("The page "+htmlPage+" fitted with score "+score+" to the product name "+matchedProduct);
		
		return predictedAnswers;
	}
	
	public static boolean commonWithLevenshteinSimilarity(String a, String b, double threshold) {
		
		int distance = org.apache.commons.lang.StringUtils.getLevenshteinDistance(a, b);
        double similarity= 0;
        if(a.length()>b.length())
        	similarity=1.0-((double)distance/a.length());
        else
        	similarity=1.0-((double)distance/b.length());
        
        //System.out.println(a+"---"+b+":"+similarity);
        return (similarity>=threshold);
    }


	public static double getLevenshteinDistance(String a, String b){
		int distance = org.apache.commons.lang.StringUtils.getLevenshteinDistance(a, b);
        double similarity= 0;
        if(a.length()>b.length())
        	similarity=1.0-((double)distance/a.length());
        else
        	similarity=1.0-((double)distance/b.length());
        
        return similarity;
	}

	
	public static double getMongeElkanSimilarity(List<String> a, List<String> b, String editDistanceMeasure) throws IOException{
		MongeElkan sim;
		
		if(editDistanceMeasure.equals("default"))
			sim = new MongeElkan(StringMetrics.mongeElkan()) ;
		
		else if (editDistanceMeasure.equals("levenshtein"))
			sim = new MongeElkan(StringMetrics.levenshtein());
		
		else if (editDistanceMeasure.equals("jaroWrinkler"))
			sim = new MongeElkan(StringMetrics.jaroWinkler());
		else {
			System.out.println("No defined edit measure for MongeElkan similarity");
			return 0;
		}
		
		return sim.compare(a,b);
	}
	
	public static double getEditDistanceSimilarity(String a, String b){
		
		Levenshtein sim = new Levenshtein();
		return sim.compare(a, b);
		
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
		else if (predictedAnswer.toLowerCase().contains(rightAnswer.toLowerCase()) && !model.getProductCategory().equals("phone"))
			return "yes";
		else if (predictedAnswer.toLowerCase().equals(rightAnswer.toLowerCase()) && model.getProductCategory().equals("phone"))
			return "yes";
		else
			return "no";
		
	}
	
	
	
}
