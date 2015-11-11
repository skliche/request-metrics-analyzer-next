package de.ibm.issw.requestmetrics.gui.statistics;

import java.awt.Color;

public class ChildNodeStatisticsEntry {
	private Color chartColor;
	private String component;
	private Long numberOfExecutions;
	private Long totalTime;
	private Integer numberOfChildren;
	
	public ChildNodeStatisticsEntry() {
		this.chartColor = Color.WHITE;
	}
	
	public Color getChartColor() {
		return chartColor;
	}
	public void setChartColor(Color chartColor) {
		this.chartColor = chartColor;
	}
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
	public Integer getNumberOfChildren() {
		return numberOfChildren;
	}
	public void setNumberOfChildren(Integer numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}
}
