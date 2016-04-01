package wdc.Thesis;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;

import SimpleModelsSimilarity.SimilarityCalculator;

public class GetRightAnswer {

	@Test
	public void getRightAnswer() throws JSONException, IOException{
		SimilarityCalculator test = new SimilarityCalculator();
		String labelPath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\labelled.txt";
		String nodeID= "node5d654fadc5cada7635a6525863c1829";
		System.out.println(test.getRightAnswer(nodeID, labelPath));
	}
}
