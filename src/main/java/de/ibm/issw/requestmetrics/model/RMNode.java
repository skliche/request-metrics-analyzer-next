package de.ibm.issw.requestmetrics.model;

import java.util.ArrayList;
import java.util.List;

public class RMNode {
	public final RMRecord rmData;
	public final List<RMNode> children = new ArrayList<RMNode>();

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
}
