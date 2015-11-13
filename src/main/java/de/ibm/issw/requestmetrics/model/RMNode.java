package de.ibm.issw.requestmetrics.model;

import java.util.ArrayList;
import java.util.List;

public class RMNode {
	public final RMRecord rmData;
	public final List<RMNode> children = new ArrayList<RMNode>();
	private long executionTime;

	public long calculateExecutionTime () {
		executionTime = rmData.getElapsedTime();
		if (!children.isEmpty()) {
			long childExecutionTimes = 0l;
			for (RMNode childNode : children) {
				if (childNode != null) childExecutionTimes += childNode.calculateExecutionTime();
			}
			executionTime -= childExecutionTimes;
		}
		System.out.println(executionTime);
		return executionTime;
	}

	public RMNode(RMRecord rmData) {
		this.rmData = rmData;
	}

	public List<RMNode> getChildren() {
		return this.children;
	}

	public void addChild(RMNode child) {
		this.children.add(child);
	}

	public RMRecord getData() {
		return this.rmData;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
}
