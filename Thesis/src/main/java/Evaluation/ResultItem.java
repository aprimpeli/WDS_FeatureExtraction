package Evaluation;

public class ResultItem {

	private int truePositives;
	private int trueNegatives;
	private int falsePositives;
	private int falseNegatives;
	public int getTruePositives() {
		return truePositives;
	}
	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}
	public int getTrueNegatives() {
		return trueNegatives;
	}
	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}
	public int getFalsePositives() {
		return falsePositives;
	}
	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}
	public int getFalseNegatives() {
		return falseNegatives;
	}
	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}
	
	public double getPrecision(){
		return (double) truePositives/((double) truePositives+(double)falsePositives);
	}
	public double getRecall(){
		return (double) truePositives/((double) truePositives+(double)falseNegatives);

	}
	public double getF1(){
		double p=(double) truePositives/((double) truePositives+(double)falsePositives);
		double r=(double) truePositives/((double) truePositives+(double)falseNegatives);
	    return ((2.0*p*r)/(p+r));
	}
}
