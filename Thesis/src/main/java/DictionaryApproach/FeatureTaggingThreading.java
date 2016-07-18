package DictionaryApproach;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BagOfWordsModel.ModelConfiguration;
import BagOfWordsModel.PreprocessingConfiguration;

public class FeatureTaggingThreading {
	
	HashMap<String, HashMap<Integer, List<String>>> tokenizedInput_;
	ModelConfiguration model_;
	String htmlFolder_;
	String htmlParsingElements_;
	PreprocessingConfiguration preprocessing_;
	Dictionary dictionary_;
	HashMap<String,List<String>> tokensOfAllHTML=new HashMap<String,List<String>>();
	Thread t;

	
	public FeatureTaggingThreading(HashMap<String, HashMap<Integer, List<String>>> tokenizedInput, ModelConfiguration model, 
			String htmlFolder, String htmlParsingElements, Dictionary dictionary, PreprocessingConfiguration preprocessing) {
		tokenizedInput_=tokenizedInput;
		model_=model;
		htmlFolder_=htmlFolder;
		htmlParsingElements_=htmlParsingElements;
		dictionary_=dictionary;
		preprocessing_=preprocessing;
	}
	
	public HashMap<String,List<String>> getFeatureTagging() throws IOException, InterruptedException{
		
		File folderHTML = new File(htmlFolder_);
		File[] listOfHTML = folderHTML.listFiles();
		
		
		BufferedWriter logProcessing = new BufferedWriter(new FileWriter(new File("resources/HTMLPages_dictionary_errorAnalysis_"+htmlParsingElements_+".csv")));
		System.out.println("Begin Feature Tagging");
		Thread[] threads = new Thread[listOfHTML.length];
		
		for (int i = 0; i < listOfHTML.length; i++) {
			
			t= new Thread(new FeatureTag(listOfHTML[i], logProcessing));
			t.start();
			threads[i]=t;
			
		}
		for(int t= 0; t < threads.length; t++)
			  threads[t].join();
		logProcessing.close();
		return tokensOfAllHTML;
	}
	
	public class FeatureTag implements Runnable{
		
		private File html_;
		private BufferedWriter writer_;
		FeatureTagger tag;
		FeatureTaggerResult ftResult;
		HashMap<String, ArrayList<String>> tagged;
		HashMap<String, ArrayList<String>> reversed;


		FeatureTag(File htmlFile,BufferedWriter logProcessing ) throws IOException {
			html_=htmlFile;
			writer_=logProcessing;
			tag=new FeatureTagger(tokenizedInput_.get(html_.getName()));
			ftResult = tag.setFeatureTagging(html_.getPath(),dictionary_.getDictionary(),preprocessing_, model_);
		}

		public void run(){
			try{
				List<String> allTaggedTokens= new ArrayList<String>();
		    	
				//do the tagging step
				
				tagged = ftResult.getTaggedWords();
				reversed = tag.reverseTaggedWords(tagged);
				for(Map.Entry<String, ArrayList<String>> r: reversed.entrySet()){
					for(String value:r.getValue()){
						//add it as many times as it appeared in the corpus
						for(int fr=0;fr<ftResult.getTaggedWordFrequency().get(value);fr++)
							allTaggedTokens.add(value);
					}			
				}
				
				if (reversed.size()==0) {
					System.out.println("No tagging could be done for the page:"+html_.getPath());
				}
				//in order to get the idf  based on tokenized words - but if we do the tagging based on groups of tokens we should not make it like this
				//tokensOfAllHTML.put(listOfHTML[i].getName(), getTokenizedTaggedWords(reversed));
				tokensOfAllHTML.put(html_.getName(), allTaggedTokens);
				writer_.append("Processed;"+html_.getName()+";"+allTaggedTokens);
				writer_.newLine();
				writer_.flush();
			}
			catch(Exception e){
				e.printStackTrace();
				System.exit(0);
	
			}
		
		}
	}
}
