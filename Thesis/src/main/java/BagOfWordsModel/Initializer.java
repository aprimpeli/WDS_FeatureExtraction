package BagOfWordsModel;

import java.io.File;
import java.util.Map.Entry;

public class Initializer {

	static String productCategory="tv"; //tv, phone, headphone
	static String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\TVCatalog.json";
	static String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\HTML";
	static String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\labelled.txt";
	static String nqFileMap="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\tvs\\FileNQMap.txt";
	//cosine or simple(exact matching) or jaccard or simple with frequency threshold
	static String similarityType="simple with frequency threshold";
	//simple(average frequency) or tfidf
	static String typeOfWeighting="n/a";
	//any possible number of n-grams is possible
	static int grams=1;
	//applied only for simple with frequency threshold similarity - otherwise they make no sense
	static double maxFreq=0.035;
	static double minFreq=0;
	
	public static void main (String args[]) throws Exception{
		
		System.out.println("---START---");
		System.out.println("The bag of words model will be executed for the product category "+ productCategory);
		System.out.println("The chosen similarity method is "+similarityType);
		System.out.println("The chosen type of weighting (not available for simple similarity method) is "+typeOfWeighting);
		System.out.println("The bag of words model will be implemented on the basis of "+grams+" grams");
		
		SimilarityCalculator calculate = new SimilarityCalculator();
		File folderHTML = new File(htmlFolder);
		File[] listOfHTML = folderHTML.listFiles();
		int correct=0;
		int wrong = 0;
		int dontKnow = 0;
		double confidenceOfCorrect = 0.0;
		double confidenceOfWrong = 0.0;
		
	    for (int i = 0; i < listOfHTML.length; i++) {
			
	    	String rightAnswer= calculate.getRightAnswer(labelled, listOfHTML[i].getPath(), nqFileMap);
	    	if (rightAnswer.equals("n/a")) {
	    		dontKnow++;
	    		continue;
	    	}
	    	Entry<String,Double> predicted = calculate.getPredictedAnswer(catalog, productCategory ,similarityType, typeOfWeighting, listOfHTML[i].getPath(), grams, maxFreq, minFreq);
			
			String answer = calculate.didIGuessRight(rightAnswer, predicted.getKey());
			if(answer.equals("yes")) {
				correct++;
				confidenceOfCorrect+=predicted.getValue();
			}
			else if(answer.equals("no")){
				wrong++;
				confidenceOfWrong+=predicted.getValue();
			}
			else {
				throw new Exception();
			}
	    }
	    
	    System.out.println("---RESULTS---");
	    System.out.println("Correct Answers: "+correct+" --->"+(((double)correct/(double)(correct+wrong)))*100+"% with confidence:"+(confidenceOfCorrect/((double)correct))*100+"%");
	    System.out.println("Wrong Answers: "+wrong+" --->"+(((double)wrong/(double)(correct+wrong)))*100+"% with confidence:"+(confidenceOfWrong/((double)wrong))*100+"%");
	    System.out.println("Not existing in the labelling set: "+dontKnow+" out of "+listOfHTML.length+"--->"+(((double)dontKnow/(double)listOfHTML.length))*100+"%");
		System.out.println("---END---");

	}
	
}
