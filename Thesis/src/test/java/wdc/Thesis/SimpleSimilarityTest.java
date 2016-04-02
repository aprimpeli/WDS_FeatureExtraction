package wdc.Thesis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import SimpleModelsSimilarity.DocPreprocessor;
import SimpleModelsSimilarity.SimilarityCalculator;
import Utils.ProductCatalogs;

public class SimpleSimilarityTest {

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
	public void runTest2() throws IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\TVCatalog.json";
		String htmlFolder="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\htmlPages";
		String labelled="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\labelled.txt";
		String nqFileMap="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\nqFileMap.txt";
		int grams=1;
		
		SimilarityCalculator calculate = new SimilarityCalculator();
		File folderHTML = new File(htmlFolder);
		File[] listOfHTML = folderHTML.listFiles();
		int correct=0;
		int wrong = 0;
		int dontKnow = 0;

	    for (int i = 0; i < listOfHTML.length; i++) {
			String predicted = calculate.getPredictedAnswer(catalog, "tv", listOfHTML[i].getPath(), grams).getKey();
			
			String answer = calculate.didIGuessRight(predicted, listOfHTML[i].getPath(), labelled, nqFileMap);
			if(answer.equals("yes")) correct++;
			else if (answer.equals("no")) wrong++;
			else dontKnow++;
			System.out.println("The prediction was: "+answer);
	    }
	    
	    System.out.println("----FINAL RESULT----");
	    System.out.println("Correct Answers: "+correct+" --->"+(((double)correct/(double)(correct+wrong)))+"%");
	    System.out.println("Wrong Answers: "+wrong+" --->"+(((double)wrong/(double)(correct+wrong)))+"%");
	    System.out.println("Not existing in the labelling set: "+dontKnow+" out of "+listOfHTML.length+"--->"+(((double)dontKnow/(double)listOfHTML.length))+"%");

	}
}
