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

import javax.swing.JDialog;
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
public class UsecasePanel extends JPanel {
	private static final Logger LOG = Logger.getLogger(UsecasePanel.class.getName());
	private AnalyzerTreeNode selectedTreeNode;
	private TreePath currentTreePath;
	private JTree tree;
	private JDialog rootWindow;
	
	public UsecasePanel(JDialog rootWindow, RMNode useCaseRootNode, RmProcessor processor) {
		this.rootWindow = rootWindow;
		
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
	            	selectedTreeNode = (AnalyzerTreeNode) currentTreePath.getLastPathComponent();
	            } else {
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
				if(selectedTreeNode != null) {
					long totalTimeChildren = 0;
					long totalZeroTimesChildren = 0;
					final Map<String, ChildNodeStatisticsEntry> stats = new HashMap<String, ChildNodeStatisticsEntry>();
					
					@SuppressWarnings("unchecked")
					Enumeration<AnalyzerTreeNode> childNodes = selectedTreeNode.children();
					while (childNodes.hasMoreElements()) {
						final UsecasePanel.AnalyzerTreeNode node = childNodes.nextElement();
						
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
					final ChildNodeStatisticsDialog panel = new ChildNodeStatisticsDialog(rootWindow, selectedTreeNode.getRmNode().getData(), entries, selectedTreeNode.getChildCount(), totalTimeChildren, totalZeroTimesChildren);
					panel.setVisible(true);
				}
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
		final List<RMNode> children = processor.findByRmRecId(rmRecId);
		
		// perform deadlock check
		if(visited.contains(rmRecId)) {
			LOG.severe("Deadlock detected in rm-record with id " + rmRecId + ": " + rmNode);
			return;
		} 
		visited.add(rmRecId);
		
		for (final RMNode childRMRecNode : children) {
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
