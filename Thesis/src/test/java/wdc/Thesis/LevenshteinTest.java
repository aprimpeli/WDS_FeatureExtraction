package wdc.Thesis;

import org.junit.Test;

import BagOfWordsModel.SimilarityCalculator;

public class LevenshteinTest {

	@Test
	public void runTest(){
		SimilarityCalculator.commonWithLevenshteinSimilarity("ps", "besid" ,0.8);
		
	}
}
