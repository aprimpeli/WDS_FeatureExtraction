package Utils;


import java.io.IOException;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import BagOfWordsModel.DocPreprocessor;

public class HTMLFragmentsExtractor {
	
	public static void main(String args[]) throws IOException{
		HTMLFragmentsExtractor extr= new HTMLFragmentsExtractor();
		System.out.println(extr.getTableText("C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\phones\\HTML\\htc one m9_1.html"));
	}

	public String getListText(String file) throws IOException {
		
		String contentOfFile = DocPreprocessor.fileToText(file);
		Document doc = Jsoup.parse(contentOfFile, "UTF-8");	
		StringBuilder allListContent = new StringBuilder();
	   
		
		for (Element list : doc.select("ul,ol,dl")) {
			//exclude nested elements
			if(isNestedElement(list, false,true)) continue;
			
		    String list_text=Jsoup.parse(list.html()).text(); 
		    allListContent.append(list_text);
		}
		//System.out.println(allListContent.toString());
		return allListContent.toString();
	}
	
	public String getTableText(String file) throws IOException {
		
		String contentOfFile = DocPreprocessor.fileToText(file);
		Document doc = Jsoup.parse(contentOfFile, "UTF-8");	
		StringBuilder allTablesContent= new StringBuilder();
		
		for (Element table : doc.select("table")) {
			//exclude nested elements
			
		    String table_text=Jsoup.parse(table.html()).text(); 
		    //System.out.println(table.html());	

		    allTablesContent.append(table_text);
			//System.out.println(table_text);		     
		}
		//System.out.println(allTablesContent.toString());
		return allTablesContent.toString();
	}

	public boolean isNestedElement(Element el, boolean isTable, boolean isList){
		if((isTable && el.children().select("table").size()>1) || (isList && el.children().select("table").size()>0)) return true;
		if((isList && el.children().select("ul,ol,dl").size()>1) || (isTable && el.children().select("ul,ol,dl").size()>0)) return true;
//		if((isTable && el.parents().select("table").size()>1) || (isList && el.parents().select("table").size()>0)) return true;
//		if((isList && el.parents().select("ul,ol,dl").size()>1) || (isTable && el.parents().select("ul,ol,dl").size()>0)) return true;
		return false;

	}
	
}
