package wdc.Thesis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.SimilarityCalculator;
import Utils.ProductCatalogs;

public class SimilarityTest {

	//@Test
	public void runTest() throws IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\TVCatalog.json";
		String html="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\43uf6400_1.html";
		int grams =1;
		DocPreprocessor process = new DocPreprocessor();
		ProductCatalogs processCatalog = new ProductCatalogs();
		SimilarityCalculator calculate = new SimilarityCalculator();
		
		List<String> vectorpage = process.textProcessing(html, null, grams, true, false, true, true);
		HashMap<String, List<String>> vectorcatalog = processCatalog.getCatalogTokens("tv", catalog, grams);
		
		for (Map.Entry<String, List<String>> entry:vectorcatalog.entrySet()){
			double score=calculate.simpleContainmentSimilarity(entry.getValue(), vectorpage);
			System.out.println("Page and "+entry.getKey()+" score:"+score);
		}
	
	}
	
	@Test
	public void runTest2() throws Exception{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\HeadphonesCatalog.json";
		String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\headphones\\HTML";
		String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\headphones\\labelled.txt";
		String nqFileMap="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\headphones\\FileNQMap.txt";
		//cosine or simple(exact matching)
		String similarityType="cosine";
		//simple(average frequency) or tfidf
		String typeOfWeighting="tfidf";
		//any possible number of n-grams is possible
		int grams=1;
		double maxFreq=0;
		double minFreq=0;
		
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
	    	Entry<String,Double> predicted = calculate.getPredictedAnswer(catalog, "tv",similarityType, typeOfWeighting, listOfHTML[i].getPath(), grams, maxFreq, minFreq);
			
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
	    
	    System.out.println("----FINAL RESULT----");
	    System.out.println("Correct Answers: "+correct+" --->"+(((double)correct/(double)(correct+wrong)))*100+"% with confidence:"+(confidenceOfCorrect/((double)correct))*100+"%");
	    System.out.println("Wrong Answers: "+wrong+" --->"+(((double)wrong/(double)(correct+wrong)))*100+"% with confidence:"+(confidenceOfWrong/((double)wrong))*100+"%");
	    System.out.println("Not existing in the labelling set: "+dontKnow+" out of "+listOfHTML.length+"--->"+(((double)dontKnow/(double)listOfHTML.length))*100+"%");

	}
}
