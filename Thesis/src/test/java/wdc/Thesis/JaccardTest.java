package wdc.Thesis;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import BagOfWordsModel.SimilarityCalculator;

public class JaccardTest {

	@Test
	public void runTest(){
		List<String> doc1 = new ArrayList<String>(){{
			add("apple apple");
			add("apple cake");
			add("cake cut");
			add("cut recipe");
			add("recipe cake");
			add("recipe cake");

			
		}};	
		List<String> doc2 = new ArrayList<String>(){{
			add("cake apple");
			add("apple recipe");
			add("recipe cut");
			add("recipe cake");

		}};	
	
		SimilarityCalculator test = new SimilarityCalculator();
		double score = test.jaccardSimilarity(doc1, doc2);
		System.out.println(score);
	}
}
