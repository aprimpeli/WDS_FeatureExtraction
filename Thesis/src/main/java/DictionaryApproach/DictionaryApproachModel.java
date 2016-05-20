package DictionaryApproach;

public class DictionaryApproachModel {

	
	public DictionaryApproachModel(String simType, int windowSize,
			String labelledPath, double simThreshold, String editDistanceType,
			int pruneLength) {
		super();
		this.simType = simType;
		this.windowSize = windowSize;
		this.labelledPath = labelledPath;
		this.simThreshold = simThreshold;
		this.editDistanceType = editDistanceType;
		this.pruneLength = pruneLength;
	}
	
	private String simType;
	private int windowSize;
	private String labelledPath;
	private double simThreshold;
	private String editDistanceType;
	private int pruneLength;
	
	public String getSimType() {
		return simType;
	}
	public void setSimType(String simType) {
		this.simType = simType;
	}
	public int getWindowSize() {
		return windowSize;
	}
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
	public String getLabelledPath() {
		return labelledPath;
	}
	public void setLabelledPath(String labelledPath) {
		this.labelledPath = labelledPath;
	}
	public double getSimThreshold() {
		return simThreshold;
	}
	public void setSimThreshold(double simThreshold) {
		this.simThreshold = simThreshold;
	}
	public String getEditDistanceType() {
		return editDistanceType;
	}
	public void setEditDistanceType(String editDistanceType) {
		this.editDistanceType = editDistanceType;
	}
	public int getPruneLength() {
		return pruneLength;
	}
	public void setPruneLength(int pruneLength) {
		this.pruneLength = pruneLength;
	}
	
	
	
}
