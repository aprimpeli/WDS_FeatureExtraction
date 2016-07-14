package BagOfWordsModel;

public class PreprocessingConfiguration {

	private String htmlParsingType;
	private boolean stemming;
	private boolean stopWordRemoval;
	private boolean lowerCase;
	private boolean unitConversion;
	private boolean tablesListsFiltering;
	
	public boolean isStemming() {
		return stemming;
	}
	public void setStemming(boolean stemming) {
		this.stemming = stemming;
	}
	public boolean isStopWordRemoval() {
		return stopWordRemoval;
	}
	public void setStopWordRemoval(boolean stopWordRemoval) {
		this.stopWordRemoval = stopWordRemoval;
	}
	public boolean isLowerCase() {
		return lowerCase;
	}
	public void setLowerCase(boolean lowerCase) {
		this.lowerCase = lowerCase;
	}
	public PreprocessingConfiguration(boolean stemming,
			boolean stopWordRemoval, boolean lowerCase, String htmlParsingElements, boolean unitConversion,boolean tablesFiltering) {
		super();
		this.stemming = stemming;
		this.stopWordRemoval = stopWordRemoval;
		this.lowerCase = lowerCase;
		this.htmlParsingType=htmlParsingElements;
		this.unitConversion=unitConversion;
		this.tablesListsFiltering=tablesFiltering;
	}

	
	public String getHtmlParsingType() {
		return htmlParsingType;
	}
	public void setHtmlParsingType(String htmlParsingType) {
		this.htmlParsingType = htmlParsingType;
	}
	public boolean isUnitConversion() {
		return unitConversion;
	}
	public void setUnitConversion(boolean unitConversion) {
		this.unitConversion = unitConversion;
	}
	public boolean isTablesFiltering() {
		return tablesListsFiltering;
	}
	public void setTablesFiltering(boolean tablesFiltering) {
		this.tablesListsFiltering = tablesFiltering;
	}
	
	
}
