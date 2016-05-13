package Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.PreprocessingConfiguration;

public class ProductCatalogs {
	
	public static void main (String args[]){
		try{
			DocPreprocessor process = new DocPreprocessor();
			ProductCatalogs test = new ProductCatalogs();
			String filepath="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\catalog\\TVCatalog.json";
			PreprocessingConfiguration preprocess = new PreprocessingConfiguration(false, true, true,"");
			HashMap<String, List<String>> tokens = test.getCatalogTokens("tv", filepath, 1, preprocess);
			for (Map.Entry<String,List<String> > entry: tokens.entrySet()){
				System.out.println(entry.getKey());
				process.printList(entry.getValue());
			}
		}
		catch (Exception e){
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	public static HashMap<String, List<String>> getCatalogTokens(String productCategory, String filePath, int grams, PreprocessingConfiguration preprocessing) throws JSONException, IOException{

		DocPreprocessor processText = new DocPreprocessor();
		HashMap<String, List<String>> catalogProducts = new HashMap<String,List<String>>();
		String headItem=getHeadItem(productCategory);
		
		ArrayList<String> properties = new ArrayList<String>();
		
		properties = getPropertiesFromFile(productCategory);

	    JSONObject catalog = new JSONObject(DocPreprocessor.fileToText(filePath));

		JSONArray array = catalog.getJSONArray(headItem);
		for(int i = 0 ; i < array.length() ; i++){
			ArrayList<String> entityValues = new ArrayList<String>();
			for (String property: properties){
				String value= array.getJSONObject(i).getString(property);
				if (value.equals("")) continue;
				List<String> tokenizedValue = processText.textProcessing(null, value, grams, false, preprocessing,null);
				entityValues.addAll(tokenizedValue);

			}
			catalogProducts.put(array.getJSONObject(i).getString("Product Name"), entityValues);
		}
		
		return catalogProducts;
	}

	public static ArrayList<String> getPropertiesFromFile(String productCategory){
		String filename="";
		ArrayList<String> properties = new ArrayList<String>();
		if(productCategory.equals("phone")){
			filename="resources/Properties/phone.txt";
		}
		else if (productCategory.equals("headphone")){
			filename="resources/Properties/headphone.txt";
		}
		else if (productCategory.equals("tv"))		{
			filename="resources//Properties//tv.txt";
		}			
		else {
			System.out.println("Cannot retrieve properties for "+productCategory+". The catalogs stored can handle the following product categories: phones, headphone and tv ");			
			System.exit(0);
		}
		 try
		 {
			 FileInputStream in = new FileInputStream(filename);
			 BufferedReader br = new BufferedReader(new InputStreamReader(in));
			 String strLine;
		 
			  while((strLine = br.readLine())!= null)
			  {
				  properties.add(strLine);
			  }
			  br.close();
			  return properties;
		 }
		 catch(Exception e){
			 System.out.println(e);
			 return null;
		 }
	}
	
	public static String getHeadItem(String productCategory){
		if(productCategory.equals("phone")){
			return "phones";
		}
		else if (productCategory.equals("headphone")){
			 return "headphones";
		}
		else if (productCategory.equals("tv"))		{
			return "tvs";
		}			
		else {
			System.out.println("Cannot retrieve properties for "+productCategory+". The catalogs stored can handle the following product categories: phone, headphone and tv ");			
			System.exit(0);
			return null;
		}
	}
}

