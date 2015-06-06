package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import de.ibm.issw.requestmetrics.RMNode;
import de.ibm.issw.requestmetrics.RmProcessor;

@SuppressWarnings("serial")
public class UsecasePanel extends JPanel {
	public UsecasePanel(RMNode useCaseRootNode, RmProcessor processor) {
		setLayout(new BorderLayout());
		
		SortableNode root = new SortableNode(useCaseRootNode);
		getRecursive(root, useCaseRootNode, processor);

		JTree tree = new JTree(root);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add("Center", scrollpane);
	}
	
	private void getRecursive(SortableNode node, RMNode rmNode, RmProcessor processor) {
		String rmRecId = rmNode.getData().getRmRecId();
		List<RMNode> rmRecChildren = processor.getChildrenByParentNodeId(rmRecId);

		for (RMNode childRMRecNode : rmRecChildren) {
			SortableNode childElement = new SortableNode(childRMRecNode);
			node.add(childElement);
			getRecursive(childElement, childRMRecNode, processor);
		}
	}
	class SortableNode extends DefaultMutableTreeNode implements Comparable<SortableNode> {
		public SortableNode(RMNode childRMRecNode) {
			super(childRMRecNode);
		}

//		public void insert(MutableTreeNode newChild, int childIndex) {
//			super.insert(newChild, childIndex);
//			not sure if this is required so we comment it out
//			Collections.sort(this.children);
//		}

		public int compareTo(SortableNode obj) {
			RMNode currentNode = (RMNode) this.getUserObject();
			RMNode compareNode = (RMNode) obj.getUserObject();
			
			long timestamp = currentNode.getData().getLogTimeStamp().getTime();
			long compareTimestamp = compareNode.getData().getLogTimeStamp().getTime();
			long diff = timestamp - compareTimestamp;
			return Integer.parseInt(new Long(diff).toString());
		}
		
		@Override
		public String toString() {
			return ((RMNode)getUserObject()).getData().determineRMRecDesc();
		}
	}
}
