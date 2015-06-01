package de.ibm.issw.requestmetrics;

import java.util.ArrayList;
import java.util.List;

public class RMNode {
	public RMRecord rmData;
	public List<RMNode> children = new ArrayList<RMNode>();

	public RMNode() {
	}

	public RMNode(RMRecord rmData) {
		this();
		setData(rmData);
	}

	public List<RMNode> getChildren() {
		return this.children;
	}

	public void setChildren(List<RMNode> children) {
		this.children = children;
	}

	public int getNumberOfChildren() {
		return this.children.size();
	}

	public void addChild(RMNode child) {
		this.children.add(child);
	}

	public RMRecord getData() {
		return this.rmData;
	}

	public void setData(RMRecord rmData) {
		this.rmData = rmData;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{").append(getData().toString()).append(",[");
		int i = 0;
		for (RMNode e : getChildren()) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(e.getData().toString());
			i++;
		}
		sb.append("]").append("}");
		return sb.toString();
	}
}
