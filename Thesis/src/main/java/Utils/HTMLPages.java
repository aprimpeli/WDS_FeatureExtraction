package Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.PreprocessingConfiguration;

public class HTMLPages {

	public static HashMap<String, List<String>> getHTMLToken(ModelConfiguration model, PreprocessingConfiguration preprocessing, String mode){
		  try{
			HashMap<String,List<String>> tokensOfPages = new HashMap<String,List<String>>();
			File folderHTML = new File(model.getHtmlFolder());
		    File[] listOfHTML = folderHTML.listFiles();
			DocPreprocessor processText = new DocPreprocessor();
		    for (int i = 0; i < listOfHTML.length; i++) {		  
		    	String pld= getPLDFromHTMLPath(model.getLabelled(), listOfHTML[i].getPath());
		    	if(mode.equals("wrapper") && !(pld.contains("ebay")|| pld.contains("overstock")||pld.contains("alibaba")||pld.contains("tesco"))) continue;
				List<String> tokenizedValue = processText.textProcessing(listOfHTML[i].getPath(), null, model.getGrams(), true, preprocessing,model.getLabelled());
				if (null==tokenizedValue) {
					System.out.println("The page "+listOfHTML[i].getName()+" had no content");
					continue;
				}
				tokensOfPages.put(listOfHTML[i].getName(), tokenizedValue);
		    }
			return tokensOfPages;      
		  }catch(Exception e){
		     e.printStackTrace();
		     return null;
		  }

	}

	public static String getPLDFromHTMLPath(String labelledEntitiesPath,String htmlName) throws JSONException, IOException{
		
		String concatName;
		if(htmlName.contains("\\"))
			 concatName = htmlName.substring(htmlName.lastIndexOf("\\")+1);
		else
			 concatName = htmlName.substring(htmlName.lastIndexOf("/")+1);

		String nodeID=concatName.replace(".html", "");

		JSONArray labelled = new JSONArray(DocPreprocessor.fileToText(labelledEntitiesPath));
		String url = "";
		for(int i = 0 ; i < labelled.length() ; i++){
			JSONObject entity = labelled.getJSONObject(i);
			if(entity.getString("id_self").equals(nodeID)){
				url = entity.getString("url");
				//String domain = de.wbsg.loddesc.util.DomainUtils.getDomain(url);
				break;
			}
		}
		
		return url.split("\\.")[1].split("/")[0];
	}
	
}
