package BagOfWordsModel;

public class PreprocessingConfiguration {

	private boolean stemming;
	private boolean stopWordRemoval;
	private boolean lowerCase;
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
			boolean stopWordRemoval, boolean lowerCase) {
		super();
		this.stemming = stemming;
		this.stopWordRemoval = stopWordRemoval;
		this.lowerCase = lowerCase;
	}
	
	
}
