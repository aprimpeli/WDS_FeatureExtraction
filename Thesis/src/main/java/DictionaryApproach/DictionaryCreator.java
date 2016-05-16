package DictionaryApproach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.PreprocessingConfiguration;

public class DictionaryCreator {
	

	
 	public static void main (String args[]) throws JSONException, IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\"
				+ "LabelledDataProfiling\\ProductCatalog\\PhoneCatalog.json";

		DictionaryCreator dict=new DictionaryCreator();
//		HashMap<String,Set<String>> Dictionary = dict.createDictionary(catalog, "phone").getDictionary();
//		dict.printDictionary(Dictionary);
	}

	public Dictionary createDictionary(String pathToCatalog, String productCategory, PreprocessingConfiguration preprocessing, String labelledPath) 
			throws JSONException, IOException{
		
		Dictionary CompleteDictionary = new Dictionary();
		
		HashMap<String,Set<String>> dictionary = new HashMap<String,Set<String>>();
		ArrayList<ProductEntity> allCatalogProductEntities = new ArrayList<ProductEntity>();
		
		ArrayList<String> properties = Utils.ProductCatalogs.getPropertiesFromFile(productCategory);
		
	    JSONObject catalog = new JSONObject(DocPreprocessor.fileToText(pathToCatalog));
		JSONArray catalogEntities = catalog.getJSONArray(Utils.ProductCatalogs.getHeadItem(productCategory));
		
		for(int i = 0 ; i < catalogEntities.length() ; i++){
			ProductEntity product = new ProductEntity();
			product.setName(catalogEntities.getJSONObject(i).getString("Product Name"));
			HashMap<String, ArrayList<String>> featureValues = new HashMap<String, ArrayList<String>>();
			for (String property: properties){
				featureValues.put(property, new ArrayList<String>());
				
				String value= catalogEntities.getJSONObject(i).getString(property);
				//preprocess the value
				InputPreprocessor process = new InputPreprocessor();
				if (value.equals("")) continue;
				else {
					if (null==dictionary.get(property)) dictionary.put(property, new HashSet<String>());					
					if (value.contains("|")){
						ArrayList<String> values = new ArrayList<String>(Arrays.asList(value.split("\\s*\\|\\s*")));
						Set<String> uniqueValues = new HashSet<String>(values);
						Set<String> newValues = new HashSet<String>();
						for(String v:uniqueValues){
							String processedValue = process.textProcessing(null, v, false, preprocessing, labelledPath);
							//ignore the very short tokens
							if(processedValue.length()>3)
								newValues.add(processedValue);
						}
						dictionary.get(property).addAll(newValues);
						featureValues.get(property).addAll(newValues);
					}
					else {
						String processedValue = process.textProcessing(null, value, false, preprocessing, labelledPath);
						if(processedValue.length()>3){
							dictionary.get(property).add(processedValue);
							featureValues.get(property).add(processedValue);
						}					
					}
				}
			}
			product.setFeatureValues(featureValues);
			allCatalogProductEntities.add(product);
		}
		CompleteDictionary.setDictionary(dictionary);
		CompleteDictionary.setProductEntities(allCatalogProductEntities);
		//printDictionary(dictionary);
		return CompleteDictionary;


	}
	
	public void printDictionary(HashMap<String,Set<String> >dictionary){
		for(Map.Entry<String, Set<String>> entry:dictionary.entrySet()){
			String prop=entry.getKey();
			String values="";
			for(String v:entry.getValue())
				values+=v+"--";
			System.out.println(prop+":"+values);
		}
		
	}
}
