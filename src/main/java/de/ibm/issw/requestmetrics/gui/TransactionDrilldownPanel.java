package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.ibm.issw.requestmetrics.engine.RmProcessor;
import de.ibm.issw.requestmetrics.gui.statistics.ChildNodeStatisticsDialog;
import de.ibm.issw.requestmetrics.gui.statistics.ChildNodeStatisticsEntry;
import de.ibm.issw.requestmetrics.model.RMNode;

@SuppressWarnings("serial")
public class TransactionDrilldownPanel extends JPanel {
	private static final Logger LOG = Logger.getLogger(TransactionDrilldownPanel.class.getName());
	private AnalyzerTreeNode selectedTreeNode;
	private TreePath currentTreePath;
	private JTree tree;
	private RequestMetricsGui rootWindow;
	private RMNode highestExecTimeNode;
	private RMNode mostDirectChildrenNode;
	
	public TransactionDrilldownPanel(RequestMetricsGui rootWindow, RMNode useCaseRootNode, RmProcessor processor) {
		this.rootWindow = rootWindow;
		
		highestExecTimeNode = useCaseRootNode;
		mostDirectChildrenNode = useCaseRootNode;
		initializeImportantNodes(useCaseRootNode);
		
		setLayout(new BorderLayout());
		
		AnalyzerTreeNode root = new AnalyzerTreeNode(useCaseRootNode);
		getRecursiveWithDeadlockPrevention(useCaseRootNode, processor, root);

		tree = new JTree(root);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		tree.addMouseListener(buildMouseListener());
		tree.setComponentPopupMenu(buildPopupMenu());

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add("Center", scrollpane);
	}

	private MouseListener buildMouseListener() {
	    return new MouseAdapter() {
	        @Override
	        public void mousePressed(MouseEvent event) {
	            currentTreePath = tree.getPathForLocation(event.getPoint().x, event.getPoint().y);
	            if(currentTreePath != null) {
	            	treeNodeSelected((AnalyzerTreeNode) currentTreePath.getLastPathComponent());
	            } else {
	            	rootWindow.getTransactionDrilldownToolBar().disableStatisticsButton();
	            	selectedTreeNode = null;
	            }
	            super.mousePressed(event);
	        }
	    };
	}
	
	/** 
	 * Builds the popup menu that occurs on rightclick of the tree items
	 * 
	 * @return the popup menu
	 */
	private JPopupMenu buildPopupMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem statisticsItem = new JMenuItem("Calculate statistics for children");
	    statisticsItem.addActionListener(buildNodeStatisticsListener());
	    menu.add(statisticsItem);

	    JMenuItem collapseItem = new JMenuItem("Collapse all children");
	    collapseItem.addActionListener(buildCollapseListener(false));
	    menu.add(collapseItem);
	    
	    collapseItem = new JMenuItem("Expand all children");
	    collapseItem.addActionListener(buildCollapseListener(true));
	    menu.add(collapseItem);
	    
	    return menu;
	}
	
	private ActionListener buildNodeStatisticsListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calculateAndOpenStatisticsDialog(selectedTreeNode);
			}
		};
	}

	/** 
	 * The collapse listener is able to collapse the current selected subtree
	 * @param expand true if the listener should expand, or false if it should collapse the subtree
	 * @return the collapse listener
	 */
	private ActionListener buildCollapseListener(final boolean expand) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        if (selectedTreeNode != null) {   
		        	expandAll(tree, currentTreePath, expand, true);
		        }
			}
		};
	}

	private void getRecursiveWithDeadlockPrevention(RMNode useCaseRootNode, RmProcessor processor, AnalyzerTreeNode root) {
		final List<Long> deadlockpreventionList = new ArrayList<Long>();
		getRecursive(root, useCaseRootNode, processor, deadlockpreventionList);
	}
	
	private void getRecursive(AnalyzerTreeNode node, RMNode rmNode, RmProcessor processor, List<Long> visited) {
		final Long rmRecId = rmNode.getData().getCurrentCmp().getReqid();
		
		// perform deadlock check
		if(visited.contains(rmRecId)) {
			LOG.severe("Deadlock detected in rm-record with id " + rmRecId + ": " + rmNode);
			return;
		} 
		visited.add(rmRecId);
		
		for (final RMNode childRMRecNode : rmNode.getChildren()) {
			final AnalyzerTreeNode childElement = new AnalyzerTreeNode(childRMRecNode);
			node.add(childElement);
			getRecursive(childElement, childRMRecNode, processor, visited);
		}
	}

	private void expandAll(JTree tree, TreePath path, boolean expand, boolean isFirst) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
 
        if (node.getChildCount() >= 0) {
            @SuppressWarnings("unchecked")
			Enumeration<AnalyzerTreeNode> children = node.children();
            while (children.hasMoreElements()) {
                TreeNode childNode = children.nextElement();
                TreePath childPath = path.pathByAddingChild(childNode);
 
                expandAll(tree, childPath, expand, false);
            }
        }
        
        // we do not close / open the first element 
        if(!isFirst) {
	        if (expand) {
	            tree.expandPath(path);
	        } else {
	            tree.collapsePath(path);
	        }
        }
    }
	
	private void treeNodeSelected(AnalyzerTreeNode treeNode) {
		selectedTreeNode = treeNode;
    	rootWindow.getTransactionDrilldownToolBar().enableStatisticsButton();
	}
	
	public void calculateAndOpenStatisticsDialog(AnalyzerTreeNode treeNode) {
		if(treeNode != null) {
			long totalTimeChildren = 0;
			long totalZeroTimesChildren = 0;
			final Map<String, ChildNodeStatisticsEntry> stats = new HashMap<String, ChildNodeStatisticsEntry>();
			
			@SuppressWarnings("unchecked")
			Enumeration<AnalyzerTreeNode> childNodes = treeNode.children();
			while (childNodes.hasMoreElements()) {
				final TransactionDrilldownPanel.AnalyzerTreeNode node = childNodes.nextElement();
				
				final String detail = node.getRmNode().rmData.getDetailCmp();
				final long elapsedTime = node.getRmNode().rmData.getElapsedTime();
				totalTimeChildren += elapsedTime;
				if(elapsedTime == 0) totalZeroTimesChildren+=1;
				
				if(stats.containsKey(detail)) {
					final ChildNodeStatisticsEntry entry = stats.get(detail);
					entry.setTotalTime(entry.getTotalTime() + elapsedTime);
					entry.setNumberOfExecutions(entry.getNumberOfExecutions() + 1);
				} else {
					final ChildNodeStatisticsEntry entry = new ChildNodeStatisticsEntry();
					entry.setComponent(detail);
					entry.setTotalTime(elapsedTime);
					entry.setNumberOfExecutions(1l);
					entry.setNumberOfChildren(node.getChildCount());
					stats.put(detail, entry);
				}
			}
			
			final List<ChildNodeStatisticsEntry> entries = new ArrayList<ChildNodeStatisticsEntry>(stats.values());
			final ChildNodeStatisticsDialog panel = new ChildNodeStatisticsDialog(rootWindow.getMainFrame(), treeNode.getRmNode().getData(), entries, treeNode.getChildCount(), totalTimeChildren, totalZeroTimesChildren);
			panel.setVisible(true);
		}
	}
	
	public void selectTreeNode (RMNode rmNode) {
		tree.clearSelection();
		AnalyzerTreeNode rootNode = (AnalyzerTreeNode) tree.getModel().getRoot();
		//1st case: rootNode is the most expensive node
		if (rmNode.getData().getCurrentCmp().getReqid() == rootNode.rmnode.getData().getCurrentCmp().getReqid()) {
			tree.setSelectionRow(0);
			treeNodeSelected(rootNode);
		} else {
			searchNode(rmNode, rootNode);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void searchNode (RMNode rmNode, AnalyzerTreeNode rootNode) {
		Enumeration<AnalyzerTreeNode> subtransactions = rootNode.children();
		while (subtransactions.hasMoreElements()) {
			TransactionDrilldownPanel.AnalyzerTreeNode node = subtransactions.nextElement();
			
			if (rmNode.getData().getCurrentCmp().getReqid() == node.rmnode.getData().getCurrentCmp().getReqid()) {
				currentTreePath = new TreePath(node.getPath());
				tree.addSelectionPath(currentTreePath);
				tree.scrollPathToVisible(currentTreePath);
				treeNodeSelected((AnalyzerTreeNode) currentTreePath.getLastPathComponent());
				break;
			} else {
				//if none of the children is the searched node, go search their children for the searched node
				searchNode(rmNode, node);
			}
		}
	}
	
	private void initializeImportantNodes(RMNode currentNode) {
		for (RMNode childNode : currentNode.getChildren()) {
			// check if execution time of current child node is highest
			if (childNode.getExecutionTime() > highestExecTimeNode.getExecutionTime()) 
				highestExecTimeNode = childNode;

			// check if current child node has most direct children 
			if (mostDirectChildrenNode.getChildren().size() < childNode.getChildren().size())
				mostDirectChildrenNode = childNode;
			
			initializeImportantNodes(childNode);
		}
	}
	
	public AnalyzerTreeNode getSelectedTreeNode() {
		return selectedTreeNode;
	}

	public RMNode getHighestExecTimeNode() {
		return highestExecTimeNode;
	}

	public RMNode getMostDirectChildrenNode() {
		return mostDirectChildrenNode;
	}




	/**
	 * We create our own implementation of the DefaultMutableTreeNode 
	 * since we need the ability to customize the behaviour 
	 * 
	 * @author skliche
	 *
	 */
	class AnalyzerTreeNode extends DefaultMutableTreeNode {
		private RMNode rmnode;
		
		public AnalyzerTreeNode(RMNode rmNode) {
			super(rmNode);
			this.rmnode = rmNode;
		}
		
		public RMNode getRmNode() {
			return rmnode;
		}
		
		@Override
		public String toString() {
			return rmnode.getData().determineRMRecDesc();
		}
	}
}
