package Evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import BagOfWordsModel.BagOfWordsConfiguration;

public class ReportGenarator {

	public void generateReport(HashMap<BagOfWordsConfiguration, ResultItem> results, String filePath){
		
		try {


			File file = new File(filePath);
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("Similarity Method;Weighting;Frequency Threshold;Levenshtein on Top;Levenshtein Threshold;Grams;Precision;Recall;F1;Step a;Threshold;Avg Common Grams");
			bw.newLine();
			for(Map.Entry<BagOfWordsConfiguration,ResultItem> result:results.entrySet()){
				String lineToAppend="";
				BagOfWordsConfiguration model= result.getKey();
				ResultItem evaluation=result.getValue();
				lineToAppend = model.getSimilarityType();
				if(model.getSimilarityType().equals("cosine")) lineToAppend+=";"+model.getTypeOfWeighting();
				else lineToAppend+=";n/a";
				if(model.getSimilarityType().equals("simple with frequency threshold")) lineToAppend+=";"+model.getMinFreq()+"-"+model.getMaxFreq();
				else lineToAppend+=";n/a";
				lineToAppend+=";"+model.isOnTopLevenshtein();
				if(model.isOnTopLevenshtein()) lineToAppend+= ";"+model.getLevenshteinThreshold();
				else lineToAppend+=";n/a";
				lineToAppend+= ";"+model.getGrams();
				lineToAppend+=";"+evaluation.getPrecision();
				lineToAppend+=";"+evaluation.getRecall();
				lineToAppend+=";"+evaluation.getF1();
				if(evaluation.getA()==-1) lineToAppend+=";n/a";
				else lineToAppend+=";"+evaluation.getA();
				lineToAppend+=";"+evaluation.getThreshold();
				lineToAppend+=";"+evaluation.getAvgCommonGrams();

				bw.append(lineToAppend);
				bw.newLine();

			}
			bw.flush();
			bw.close();
			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
