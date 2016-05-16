package DictionaryApproach;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.SimilarityCalculator;

public class FeatureSimilarityComputation {

	static boolean LevenshteinOnTop;
	static String simType;
	 public FeatureSimilarityComputation(boolean levenshtein , String simType) {
		FeatureSimilarityComputation.LevenshteinOnTop=levenshtein;
		FeatureSimilarityComputation.simType=simType;
	}
	
	public HashMap<String,Double> getPredictedAnswersinDictionaryApproach
	(HashMap<String,ArrayList<String>> featureValuesOfPage, Dictionary dictionary, ModelConfiguration model, String htmlPath){
		
		HashMap<String, Double> predictedAnswers = new HashMap<String, Double>();
		SimilarityCalculator calculate = new SimilarityCalculator(model);
		
		for (ProductEntity product:dictionary.getProductEntities()){
			double score=0.0;
			if(featureValuesOfPage.size()==0) predictedAnswers.put(product.getName().toLowerCase(), 0.0);
			
			else {
				for(Map.Entry<String,ArrayList<String>> featureValue:featureValuesOfPage.entrySet()){
					List<String> valuesOfPage = featureValue.getValue();
					List<String> valuesOfcatalog= product.getFeatureValues().get(featureValue.getKey());
					double currentScore=0.0;
					if(simType.equals("simple")){
						
						currentScore=calculate.simpleContainmentSimilarity(valuesOfcatalog, valuesOfPage);				
					}
					else if (simType.equals("jaccard")){
						currentScore=calculate.jaccardSimilarity(valuesOfcatalog, valuesOfPage);
					}				
					else{
						System.out.println("The similarity type "+simType+" cannot be handled. Available options are jaccard and simple. The program will end.");
						System.exit(0);
					}				
					if(!Double.isNaN(currentScore))	score+=currentScore;
					else {
						System.out.println("Score between "+product.getName()+" and "+htmlPath+ " could not be defined");
					}

				}
				predictedAnswers.put(product.getName().toLowerCase(), score/(double)featureValuesOfPage.size());
			}
			
		}
		
		return predictedAnswers;
	}
	
	
}
