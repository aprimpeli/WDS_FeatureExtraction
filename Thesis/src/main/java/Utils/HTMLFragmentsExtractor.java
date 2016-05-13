package Utils;


import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import BagOfWordsModel.DocPreprocessor;

public class HTMLFragmentsExtractor {
	String labelledEntitiesPath;
	public HTMLFragmentsExtractor(String labelPath){
		labelledEntitiesPath=labelPath;
	}
				
	public static void main(String args[]) throws IOException{
//		HTMLFragmentsExtractor extr= new HTMLFragmentsExtractor();
//		System.out.println(extr.getTableWithWrapperText("C:\\Users\\Anna\\Google Drive\\Master_Thesis\\3.MatchingModels\\testInput\\phones\\HTML\\iphone 4_2.html","ebay.com"));
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
		return allListContent.toString();
	}
	
	public String getTableText(String file) throws IOException {
		
		String contentOfFile = DocPreprocessor.fileToText(file);
		Document doc = Jsoup.parse(contentOfFile, "UTF-8");	
		StringBuilder allTablesContent= new StringBuilder();
		
		for (Element table : doc.select("table")) {
			//exclude nested elements
			
		    String table_text=Jsoup.parse(table.html()).text(); 

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
	
	public String getTableWithWrapperText(String file) throws IOException{
		
		String pld= HTMLPages.getPLDFromHTMLPath(labelledEntitiesPath, file);
		
		String contentOfFile = DocPreprocessor.fileToText(file);
		Document doc = Jsoup.parse(contentOfFile, "UTF-8");	
		StringBuilder allTablesContent= new StringBuilder();
		
		if(pld.contains("ebay")){
			Element firstTable = doc.getElementsByClass("itemAttr").first();
			if(null!=firstTable){
				Elements items = firstTable.select("tr");
				for(Element item:items){
					Elements values= item.select("td");
					if(values.size()!= 4) continue;
					//get only the values not the feature names
					allTablesContent.append(values.get(1).text()+" ");
					allTablesContent.append(values.get(3).text()+" ");						
				}				
			} 					
			//detailed table
			Element secondTable = doc.getElementsByClass("prodDetailSec").first();
			if (null!=secondTable){
				Elements detaileditems = secondTable.select("tr");
				for(Element item:detaileditems){
					Elements values = item.select("td");
					if(values.size()!=2) continue;
					else {
						if (values.get(1).text().isEmpty() || values.get(1).text()==null ) continue;
						allTablesContent.append(values.get(1).text()+" ");						
					}
				}
			}						
		}// end of ebay wrapper
		else if (pld.contains("overstock")){
			Element firstTable = doc.select("table[class=table table-dotted table-extended table-header translation-table]").first();
			Element content = firstTable.select("tbody").first();
			if(null!=content){
				Elements items = content.select("tr");
				for(Element item:items){
					Elements values= item.select("td");
					if(values.size()!= 2) continue;
					allTablesContent.append(values.get(1).text()+" ");
				}	
				Element secondTable = doc.select("table[class=table table-dotted table-header]").first();
				Elements seconditems = secondTable.select("tr");
				if(null!=content){
					for(Element item:seconditems){
						Elements values= item.select("td");
						if(values.size()!= 2) continue;		
						allTablesContent.append(values.get(1).text()+" ");				
					}
					
				} 
			}
		} 	//end of overstock parser	
		else if (pld.contains("tesco")){
			Element table = doc.select("div[id=product-spec-section-content]").first();
			if(null!=table){

				Elements labelCells = table.select("div[class=product-spec-cell product-spec-label]");
				Elements labelValues = table.select("div[class=product-spec-cell product-spec-value]");
				if (labelCells.size()!= labelValues.size()) System.out.println("The labels size and the value size do not match. Please check the Tesco wrapper.");
				for(int i=0; i<labelCells.size(); i++){
					String value= labelValues.get(i).text();
					allTablesContent.append(value+" ");										
				}					
			}
		}//end of tesco wrapper
			
		else if (pld.contains("alibaba")){

			Elements labelCells = doc.select("td[class=name J-name]");
			Elements labelValues = doc.select("td[class=value J-value]");
			if (labelCells.size()!= labelValues.size()) System.out.println("The labels size and the value size do not match. Please check the Alibaba wrapper.");
			for(int i=0; i<labelCells.size(); i++){
				String value= labelValues.get(i).text();
				allTablesContent.append(value+" ");
				
			}	
			Elements secondlabelValues = doc.select("table[class=aliDataTable]").select("td");
			for(int i=0; i<secondlabelValues.size(); i=i+2){
				String value= secondlabelValues.get(i+1).text();
				allTablesContent.append(value+" ");

			}	
		}
		else {
			System.out.println("No wrapper was defined for the pld:"+pld);
			return null;
			}
		return allTablesContent.toString();
	}
}
