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
	(HashMap<String,ArrayList<String>> featureValuesOfPage, Dictionary dictionary, ModelConfiguration model){
		
		HashMap<String, Double> predictedAnswers = new HashMap<String, Double>();
		SimilarityCalculator calculate = new SimilarityCalculator(model);
		
		for (ProductEntity product:dictionary.getProductEntities()){
			double score=0.0;
			
			for(Map.Entry<String,ArrayList<String>> featureValue:featureValuesOfPage.entrySet()){
				List<String> valuesOfPage = featureValue.getValue();
				List<String> valuesOfcatalog= product.getFeatureValues().get(featureValue.getKey());
				if(simType.equals("simple"))
					score+=calculate.simpleContainmentSimilarity(valuesOfcatalog, valuesOfPage);				
				else if (simType.equals("jaccard")){
					score=calculate.jaccardSimilarity(valuesOfcatalog, valuesOfPage);
				}				
				else{
					System.out.println("The similarity type "+simType+" cannot be handled. Available options are jaccard and simple. The program will end.");
					System.exit(0);
				}
			}
			predictedAnswers.put(product.getName().toLowerCase(), score/(double)featureValuesOfPage.size());
		}
		
		return predictedAnswers;
	}
	
	
}
