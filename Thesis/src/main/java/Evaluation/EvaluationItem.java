package Evaluation;

import java.util.ArrayList;
import java.util.HashMap;

public class EvaluationItem {

	private HashMap<String, Double> predictedAnswers;
	private ArrayList<String> rightAnswers;
	private String productCategory;
	public HashMap<String, Double> getPredictedAnswers() {
		return predictedAnswers;
	}
	public void setPredictedAnswers(HashMap<String, Double> predictedAnswers) {
		this.predictedAnswers = predictedAnswers;
	}
	
	
	public String getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}
	public ArrayList<String> getRightAnswers() {
		return rightAnswers;
	}
	public void setRightAnswers(ArrayList<String> rightAnswers) {
		this.rightAnswers = rightAnswers;
	}
}
