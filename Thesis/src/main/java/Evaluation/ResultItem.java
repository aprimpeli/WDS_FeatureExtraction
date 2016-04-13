package Evaluation;

public class ResultItem {

	private int truePositives;
	private int trueNegatives;
	private int falsePositives;
	private int falseNegatives;
	private double threshold;
	private double a;
	
	public ResultItem(){
		this.a=-1;
	}
	
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public double getA() {
		return a;
	}
	public void setA(double a) {
		this.a = a;
	}
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
		double precision=(double) truePositives/((double) truePositives+(double)falsePositives);
		if(Double.isNaN(precision)) return 0;
		else return precision;
	}
	public double getRecall(){
		double recall=(double) truePositives/((double) truePositives+(double)falseNegatives);
		if(Double.isNaN(recall)) return 0;
		else return recall;

	}
	public double getF1(){
		
		double p=(double) truePositives/((double) truePositives+(double)falsePositives);
		double r=(double) truePositives/((double) truePositives+(double)falseNegatives);
		double f1=((2.0*p*r)/(p+r));
		if(Double.isNaN(f1)) return 0;
		else return f1;
	}
}
