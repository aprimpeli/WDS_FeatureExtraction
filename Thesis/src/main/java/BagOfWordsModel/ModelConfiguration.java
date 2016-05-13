package BagOfWordsModel;

import java.util.HashMap;

public class ModelConfiguration {

	private String modelType;
	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	private String productCategory; //tv, phone, headphone
	private String catalog;
	private String htmlFolder;
	private String labelled;
	//private String nqFileMap;
	//cosine or simple(exact matching) or jaccard or simple with frequency threshold
	private String similarityType;
	//simple(average frequency) or tfidf
	private String typeOfWeighting;
	//any possible number of n-grams is possible
	private int grams;
	//applied only for simple with frequency threshold similarity - otherwise they make no sense
	private double maxFreq;
	private double minFreq;
	private HashMap<String, Double> idfWeightsCatalog;
	private HashMap<String,Double> idfWeightsPages;
	
	private HashMap<String,Integer> vectorPageFrequencies;
	private HashMap<String,Integer> vectorCatalogFrequencies;

	
	public HashMap<String, Integer> getVectorPageFrequencies() {
		return vectorPageFrequencies;
	}

	public void setVectorPageFrequencies(
			HashMap<String, Integer> vectorPageFrequencies) {
		this.vectorPageFrequencies = vectorPageFrequencies;
	}

	public HashMap<String, Integer> getVectorCatalogFrequencies() {
		return vectorCatalogFrequencies;
	}

	public void setVectorCatalogFrequencies(
			HashMap<String, Integer> vectorCatalogFrequencies) {
		this.vectorCatalogFrequencies = vectorCatalogFrequencies;
	}

	public ModelConfiguration(String modelType, String productCategory, String catalog,
			String htmlFolder, String labelled, 
			String similarityType, String typeOfWeighting, int grams,
			double maxFreq, double minFreq, boolean onTopLevenshtein,
			double levenshteinThreshold) {
		super();
		this.productCategory = productCategory;
		this.catalog = catalog;
		this.htmlFolder = htmlFolder;
		this.labelled = labelled;
		this.similarityType = similarityType;
		this.typeOfWeighting = typeOfWeighting;
		this.grams = grams;
		this.maxFreq = maxFreq;
		this.minFreq = minFreq;
		this.onTopLevenshtein = onTopLevenshtein;
		this.levenshteinThreshold = levenshteinThreshold;
	}
	//
	private boolean onTopLevenshtein;
	private double levenshteinThreshold;
	
	public ModelConfiguration(){
		
	}
	
	public String getLabelled() {
		return labelled;
	}
	public void setLabelled(String labelled) {
		this.labelled = labelled;
	}
	
	
	public String getSimilarityType() {
		return similarityType;
	}
	public void setSimilarityType(String similarityType) {
		this.similarityType = similarityType;
	}
	public String getTypeOfWeighting() {
		return typeOfWeighting;
	}
	public void setTypeOfWeighting(String typeOfWeighting) {
		this.typeOfWeighting = typeOfWeighting;
	}
	public int getGrams() {
		return grams;
	}
	public void setGrams(int grams) {
		this.grams = grams;
	}
	public double getMaxFreq() {
		return maxFreq;
	}
	public void setMaxFreq(double maxFreq) {
		this.maxFreq = maxFreq;
	}
	public double getMinFreq() {
		return minFreq;
	}
	public void setMinFreq(double minFreq) {
		this.minFreq = minFreq;
	}
	public boolean isOnTopLevenshtein() {
		return onTopLevenshtein;
	}
	public void setOnTopLevenshtein(boolean onTopLevenshtein) {
		this.onTopLevenshtein = onTopLevenshtein;
	}
	public double getLevenshteinThreshold() {
		return levenshteinThreshold;
	}
	public void setLevenshteinThreshold(double levenshteinThreshold) {
		this.levenshteinThreshold = levenshteinThreshold;
	}
	
	public String getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getHtmlFolder() {
		return htmlFolder;
	}
	public void setHtmlFolder(String htmlFolder) {
		this.htmlFolder = htmlFolder;
	}

	public HashMap<String, Double> getIdfWeightsCatalog() {
		return idfWeightsCatalog;
	}

	public void setIdfWeightsCatalog(HashMap<String, Double> idfWeightsCatalog) {
		this.idfWeightsCatalog = idfWeightsCatalog;
	}

	public HashMap<String,Double> getIdfWeightsPages() {
		return idfWeightsPages;
	}

	public void setIdfWeightsPages(HashMap<String,Double> idfWeightsPages) {
		this.idfWeightsPages = idfWeightsPages;
	}
}
