package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import de.ibm.issw.requestmetrics.engine.RmProcessor;
import de.ibm.issw.requestmetrics.model.RMNode;

@SuppressWarnings("serial")
public class UsecasePanel extends JPanel {
	private static final Logger LOG = Logger.getLogger(UsecasePanel.class.getName());
	public UsecasePanel(RMNode useCaseRootNode, RmProcessor processor) {
		setLayout(new BorderLayout());
		
		SortableNode root = new SortableNode(useCaseRootNode);
		getRecursiveWithDeadlockPrevention(useCaseRootNode, processor, root);

		JTree tree = new JTree(root);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add("Center", scrollpane);
	}

	private void getRecursiveWithDeadlockPrevention(RMNode useCaseRootNode, RmProcessor processor, SortableNode root) {
		final List<Long> deadlockpreventionList = new ArrayList<Long>();
		getRecursive(root, useCaseRootNode, processor, deadlockpreventionList);
	}
	
	private void getRecursive(SortableNode node, RMNode rmNode, RmProcessor processor, List<Long> visited) {
		final Long rmRecId = rmNode.getData().getCurrentCmp().getReqid();
		final List<RMNode> children = processor.findByRmRecId(rmRecId);
		
		// perform deadlock check
		if(visited.contains(rmRecId)) {
			LOG.severe("Deadlock detected in rm-record with id " + rmRecId + ": " + rmNode);
			return;
		} 
		visited.add(rmRecId);
		
		for (final RMNode childRMRecNode : children) {
			final SortableNode childElement = new SortableNode(childRMRecNode);
			node.add(childElement);
			getRecursive(childElement, childRMRecNode, processor, visited);
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
