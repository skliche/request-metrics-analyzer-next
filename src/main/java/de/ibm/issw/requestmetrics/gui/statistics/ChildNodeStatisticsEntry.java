package de.ibm.issw.requestmetrics.gui.statistics;

public class ChildNodeStatisticsEntry {
	private String component;
	private Long numberOfExecutions;
	private Long totalTime;
	
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
