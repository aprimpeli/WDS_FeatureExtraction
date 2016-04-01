package wdc.Thesis;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import SimpleModelsSimilarity.DocPreprocessor;
import SimpleModelsSimilarity.SimilarityCalculator;
import Utils.ProductCatalogs;

public class SimpleSimilarityTest {

	@Test
	public void runTest() throws IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\TVCatalog.json";
		String html="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\43uf6400_1.html";
		
		DocPreprocessor process = new DocPreprocessor();
		ProductCatalogs processCatalog = new ProductCatalogs();
		SimilarityCalculator calculate = new SimilarityCalculator();
		
		List<String> vectorpage = process.textProcessing(html, null, true, false, true, true);
		HashMap<String, List<String>> vectorcatalog = processCatalog.getCatalogTokens("tv", catalog);
		
		for (Map.Entry<String, List<String>> entry:vectorcatalog.entrySet()){
			double score=calculate.simpleContainmentSimilarity(entry.getValue(), vectorpage);
			System.out.println("Page and "+entry.getKey()+" score:"+score);
		}
	
	}
}
