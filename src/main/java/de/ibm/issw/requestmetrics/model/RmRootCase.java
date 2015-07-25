package de.ibm.issw.requestmetrics.model;

import java.text.SimpleDateFormat;

/**
 * A root case represents the scenario where the current node 
 * and the parent node of the RM record are equal.<br> This class
 * also provides rendering logic for the root case.
 * 
 * @author skliche
 *
 */
public class RmRootCase {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	private final RMNode node;
	
	public RmRootCase(RMNode node) {
		this.node = node;
	}
	public RMNode getRmNode() {
		return node;
	}
	
	/**
	 * renders a description string. It internally uses the data of the RM Node.
	 * @return the string of the representation of the root case.
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer()
				.append(sdf.format(node.getData().getLogTimeStamp()))
				.append(" | ").append(node.getData().getElapsedTime())
				.append(" | ").append(node.getData().getTypeCmp())
				.append(" | ").append(node.getData().getDetailCmp());
		return sb.toString();
	}
}
