package wdc.Thesis;

import java.io.IOException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import BagOfWordsModel.DocPreprocessor;

public class LabellingCheckNormalizedNames {

	@Test
	public void checkNames() throws JSONException, IOException{
		
		Scanner sc = new Scanner(System.in);


		String file="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\allLabelles.txt";

		JSONArray labelled = new JSONArray(DocPreprocessor.fileToText(file));
		for(int i = 0 ; i < labelled.length() ; i++){
			JSONObject entity = labelled.getJSONObject(i);
			System.out.println(entity.getString("normalized_product_name"));
			System.out.println(entity.getString("product-name"));
			System.out.println(entity.getString("id_self"));
			if (sc.next().equals("y")) continue;
		}
		
	}
}
