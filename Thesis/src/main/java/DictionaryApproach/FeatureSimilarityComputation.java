package DictionaryApproach;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.SimilarityCalculator;

public class FeatureSimilarityComputation {


	 public FeatureSimilarityComputation() {
		
	}
	
	public HashMap<String,Double> getPredictedAnswersinDictionaryApproach
	(HashMap<String,ArrayList<String>> featureValuesOfPage, Dictionary dictionary, DictionaryApproachModel model, String htmlPath) throws IOException{
		
		HashMap<String, Double> predictedAnswers = new HashMap<String, Double>();
		SimilarityCalculator calculate = new SimilarityCalculator();
		
		for (ProductEntity product:dictionary.getProductEntities()){
			double score=0.0;
			if(featureValuesOfPage.size()==0) predictedAnswers.put(product.getName().toLowerCase(), 0.0);
			
			else {
				for(Map.Entry<String,ArrayList<String>> featureValue:featureValuesOfPage.entrySet()){
					List<String> valuesOfPage = featureValue.getValue();
					List<String> valuesOfcatalog= product.getFeatureValues().get(featureValue.getKey());
					double currentScore=0.0;
					if(model.getSimType().equals("simple")){
						
						currentScore=calculate.simpleContainmentSimilarity(valuesOfcatalog, valuesOfPage);				
					}
					else {
						
						currentScore=calculate.getMongeElkanSimilarity(valuesOfcatalog, valuesOfPage, model.getEditDistanceType());
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
