package Evaluation;

import java.util.HashMap;

public class EvaluationItem {

	private HashMap<String, Double> predictedAnswers;
	private String rightAnswer;
	private String productCategory;
	public HashMap<String, Double> getPredictedAnswers() {
		return predictedAnswers;
	}
	public void setPredictedAnswers(HashMap<String, Double> predictedAnswers) {
		this.predictedAnswers = predictedAnswers;
	}
	public String getRightAnswer() {
		return rightAnswer;
	}
	public void setRightAnswer(String rightAnswer) {
		this.rightAnswer = rightAnswer;
	}
	public String getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}
}
