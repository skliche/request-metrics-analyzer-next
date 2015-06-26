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
		final Long rmRecId = rmNode.getData().getCurrentCmp().getReqid();
		final List<RMNode> children = processor.findByRmRecId(rmRecId);
		
		for (final RMNode childRMRecNode : children) {
			final SortableNode childElement = new SortableNode(childRMRecNode);
			node.add(childElement);
			getRecursive(childElement, childRMRecNode, processor);
		}
	}
	class SortableNode extends DefaultMutableTreeNode {
		public SortableNode(RMNode childRMRecNode) {
			super(childRMRecNode);
		}
		
		@Override
		public String toString() {
			return ((RMNode)getUserObject()).getData().determineRMRecDesc();
		}
	}
}
