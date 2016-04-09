//package wdc.Thesis;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.Test;
//
//import BagOfWordsModel.SimilarityCalculator;
//import BagOfWordsModel.Weightening;
//
//public class WeightningTest {
//
//	@Test
//	public void runWeightingTest(){
//		List<String> doc1 = new ArrayList<String>(){{
//			add("apple");
//			add("apple");
//			add("cake");
//			add("cut");
//			add("slice");
//			add("recipe");
//			add("recipe");
//			
//		}};			
//		List<String> doc2 = new ArrayList<String>(){{
//			add("slice");
//			add("slice");
//			add("recipe");
//			add("recipe");
//			add("meat");
//			add("meat");
//			add("meat");
//			add("bean");
//			add("cook");
//			add("buy");
//
//		}};		
//		List<String> doc3 = new ArrayList<String>(){{
//			add("apple");
//			add("cake");
//			add("cake");
//			add("slice");
//			add("buy");
//			add("buy");
//			add("buy");
//			
//		}};	
//		List<List<String>> corpus = new ArrayList<List<String>>();
//		corpus.add(doc1);
//		corpus.add(doc2);
//		corpus.add(doc3);
//		
//		Weightening test = new Weightening();
//		HashMap<String,Double> weights1 = new HashMap<String,Double>();
//		weights1 = test.getTfIdfWeighting(doc1, corpus);
//		
//		HashMap<String,Double> weights2 = new HashMap<String,Double>();
//		weights2 = test.getTfIdfWeighting(doc2, corpus);
//		
//		HashMap<String,Double> weights3 = new HashMap<String,Double>();
//		weights3 = test.getTfIdfWeighting(doc3, corpus);
//		System.out.println("----WEIGHTS 1----");
//		for(Map.Entry<String,Double> g: weights1.entrySet()){
//			System.out.println(g.getKey()+":"+g.getValue());
//		}
//		System.out.println("----WEIGHTS 2----");
//		for(Map.Entry<String,Double> g: weights2.entrySet()){
//			System.out.println(g.getKey()+":"+g.getValue());
//		}
//
//		System.out.println("----WEIGHTS 3----");
//		for(Map.Entry<String,Double> g: weights3.entrySet()){
//			System.out.println(g.getKey()+":"+g.getValue());
//		}
//
//		List<String> query = new ArrayList<String>(){{
//			add("apple");
//			add("cake");
//			add("recipe");
//			
//		}};	
//		
//		SimilarityCalculator similarity = new SimilarityCalculator();
//		double score1 = similarity.cosineSimilarity(doc1, query, corpus);
//		double score2 = similarity.cosineSimilarity(doc2, query, corpus);
//		double score3 = similarity.cosineSimilarity(doc3, query, corpus);
//		System.out.println("Cosine Similarity Score DOC 1:"+score1);
//		System.out.println("Cosine Similarity Score DOC 2:"+score2);
//		System.out.println("Cosine Similarity Score DOC 3:"+score3);
//
//		
//	}
//}
