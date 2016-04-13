package Utils;


import java.io.IOException;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import BagOfWordsModel.DocPreprocessor;

public class HTMLFragmentsExtractor {

	public String getListText(String file) throws IOException {
		
		String contentOfFile = DocPreprocessor.fileToText(file);
		Document doc = Jsoup.parse(contentOfFile, "UTF-8");	
		StringBuilder allListContent = new StringBuilder();
	   
		
		for (Element list : doc.select("ul,ol,dl")) {
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
		    String table_text=Jsoup.parse(table.html()).text(); 
		    allTablesContent.append(table_text);
			//System.out.println(table_text);		     
		}
		//System.out.println(allTablesContent.toString());
		return allTablesContent.toString();
	}
}
