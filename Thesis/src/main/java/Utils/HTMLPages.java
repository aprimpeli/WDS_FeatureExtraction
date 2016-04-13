package Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import BagOfWordsModel.BagOfWordsConfiguration;
import BagOfWordsModel.DocPreprocessor;
import BagOfWordsModel.PreprocessingConfiguration;

public class HTMLPages {

	public static HashMap<String, List<String>> getHTMLToken(BagOfWordsConfiguration model, PreprocessingConfiguration preprocessing){
		  try{
			HashMap<String,List<String>> tokensOfPages = new HashMap<String,List<String>>();
			File folderHTML = new File(model.getHtmlFolder());
		    File[] listOfHTML = folderHTML.listFiles();
			DocPreprocessor processText = new DocPreprocessor();

		    for (int i = 0; i < listOfHTML.length; i++) {		    	
				List<String> tokenizedValue = processText.textProcessing(listOfHTML[i].getPath(), null, model.getGrams(), true, preprocessing, model.getNqFileMap());
				tokensOfPages.put(listOfHTML[i].getName(), tokenizedValue);
		    }
			return tokensOfPages;      
		  }catch(Exception e){
		     e.printStackTrace();
		     return null;
		  }

	}
}
