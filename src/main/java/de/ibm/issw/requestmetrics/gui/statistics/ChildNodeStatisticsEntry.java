package de.ibm.issw.requestmetrics.gui.statistics;

public class ChildNodeStatisticsEntry {
	String component;
	Long numberOfExecutions;
	Long totalTime;
	
	public String getComponent() {
		return component;
	}
	public void setComponent(String component) {
		this.component = component;
	}
	public Long getNumberOfExecutions() {
		return numberOfExecutions;
	}
	public void setNumberOfExecutions(Long numberOfExecutions) {
		this.numberOfExecutions = numberOfExecutions;
	}
	public Long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(Long totalTime) {
		this.totalTime = totalTime;
	}
}
