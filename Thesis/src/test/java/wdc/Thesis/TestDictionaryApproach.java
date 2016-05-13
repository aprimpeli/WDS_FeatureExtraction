package wdc.Thesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.junit.Test;

import DictionaryApproach.Dictionary;
import DictionaryApproach.DictionaryCreator;
import DictionaryApproach.FeatureSimilarityComputation;
import DictionaryApproach.FeatureTagger;

public class TestDictionaryApproach {

	@Test
	public void runTest() throws JSONException, IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\"
				+ "LabelledDataProfiling\\ProductCatalog\\PhoneCatalog.json";
		String text="this is iphone 4s in gold color it s a smartphone with 5 Megapixel camera";
		DictionaryCreator dict = new DictionaryCreator();
		Dictionary dictionary = new Dictionary();
		dictionary = dict.createDictionary(catalog, "phone",null,"");
		
		FeatureTagger tag = new FeatureTagger();
		HashMap<String, ArrayList<String>> tagged = tag.setFeatureTagging( text,dictionary.getDictionary());
		tag.printTagged(tagged);
		HashMap<String, ArrayList<String>> reversed = tag.reverseTaggedWords(tagged);
		tag.printTagged(reversed);
		FeatureSimilarityComputation sim = new FeatureSimilarityComputation(false, "simple");
		HashMap<String, Double> answers = sim.getPredictedAnswersinDictionaryApproach(reversed, dictionary,null);
		for(Map.Entry<String, Double> answer:answers.entrySet())
			System.out.println(answer.getKey()+"---"+answer.getValue());
		
	}
}
