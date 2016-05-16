package DictionaryApproach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

public class FeatureTagger {
	
	public static void main (String args[]) throws JSONException, IOException{
		String catalog="C:\\Users\\Anna\\Google Drive\\Master_Thesis\\2.ProfilingOfData\\"
				+ "LabelledDataProfiling\\ProductCatalog\\PhoneCatalog.json";
		String text="this is iphone 4s in gold color it s a smartphone with 5 Megapixel camera";
		DictionaryCreator dict = new DictionaryCreator();
		FeatureTagger tag = new FeatureTagger();
//		HashMap<String, ArrayList<String>> tagged = tag.setFeatureTagging( text,dict.createDictionary(catalog, "phone").getDictionary());
//		tag.printTagged(tagged);
//		HashMap<String, ArrayList<String>> reversed = tag.reverseTaggedWords(tagged);
//		tag.printTagged(reversed);
	}

	/**
	 * @param input
	 * @param dictionary
	 * @return
	 * Get a full string as input and the dictionary and creates a map out of the common grams between the dictionary and the input
	 * Every gram is then assigned at least one feature name as indicated in the dictionary
	 * example output:
	 * iphone: <phone_type,family_line>
	 * 
	 */
	public HashMap<String, ArrayList<String>> setFeatureTagging(String input, HashMap<String,Set<String>> dictionary){
		
		HashMap<String, ArrayList<String>> taggedWords= new HashMap<String, ArrayList<String>>();
		for(Map.Entry<String, Set<String>> dictEntry:dictionary.entrySet()){
			for(String value:dictEntry.getValue()){
				//if the input contains this as a token - not inside a word example: clear and cleared
				if(input.contains(" "+value+" ")) {
					if(null == taggedWords.get(value)) taggedWords.put(value, new ArrayList<String>());
					taggedWords.get(value).add(dictEntry.getKey());
				}
			}
		}
		
		
		return taggedWords;
	}
	public void printTagged(HashMap<String,ArrayList<String> >dictionary){
		for(Map.Entry<String, ArrayList<String>> entry:dictionary.entrySet()){
			String prop=entry.getKey();
			String values="";
			for(String v:entry.getValue())
				values+=v+"--";
			System.out.println(prop+":"+values);
		}
		
	}
	/**
	 * @return
	 * reverse the taggedWords hashMap so that it is like: 
	 * phone_type:iphone
	 * family_line:iphone
	 */
	public HashMap<String, ArrayList<String>> reverseTaggedWords(HashMap<String, ArrayList<String>> tobeReversed){
		HashMap<String, ArrayList<String>> taggedWords = new HashMap<String, ArrayList<String>>();
		for (Map.Entry<String, ArrayList<String>> e:tobeReversed.entrySet()){
			for(String feature:e.getValue()){
				if(null==taggedWords.get(feature)) taggedWords.put(feature, new ArrayList<String>());
				taggedWords.get(feature).add(e.getKey());
			}
		}
		return taggedWords;
	}
}
