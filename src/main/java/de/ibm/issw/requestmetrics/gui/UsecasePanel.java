package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.ibm.issw.requestmetrics.engine.RmProcessor;
import de.ibm.issw.requestmetrics.model.RMNode;

@SuppressWarnings("serial")
public class UsecasePanel extends JPanel {
	private static final Logger LOG = Logger.getLogger(UsecasePanel.class.getName());
	private TreeNode selectedNode;
	private TreePath pathForLocation;
	private JTree tree;
	
	public UsecasePanel(RMNode useCaseRootNode, RmProcessor processor) {
		setLayout(new BorderLayout());
		
		SortableNode root = new SortableNode(useCaseRootNode);
		getRecursiveWithDeadlockPrevention(useCaseRootNode, processor, root);

		tree = new JTree(root);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		tree.addMouseListener(getMouseListener());
		tree.setComponentPopupMenu(buildPopupMenu());

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add("Center", scrollpane);
	}

	private MouseListener getMouseListener() {
	    return new MouseAdapter() {
	        @Override
	        public void mousePressed(MouseEvent event) {
	            pathForLocation = tree.getPathForLocation(event.getPoint().x, event.getPoint().y);
	            if(pathForLocation != null) {
	            	selectedNode = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
	            } else {
	            	selectedNode = null;
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
	    JMenuItem exportItem = new JMenuItem("Calculate time spend in children");
	    exportItem.addActionListener(getTimeCalculationListener());
	    menu.add(exportItem);

	    JMenuItem collapseItem = new JMenuItem("Collapse all children");
	    collapseItem.addActionListener(buildCollapseListener(false));
	    menu.add(collapseItem);
	    
	    collapseItem = new JMenuItem("Expand all children");
	    collapseItem.addActionListener(buildCollapseListener(true));
	    menu.add(collapseItem);
	    
	    return menu;
	}
	
	/**
	 * Calculates the time the childnodes took to perform its work and displays 
	 * the result as messagebox
	 * @return
	 */
	private ActionListener getTimeCalculationListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectedNode != null) {
					long totalTime = 0;
					long toalZeroTimes = 0;
					
					@SuppressWarnings("unchecked")
					Enumeration<SortableNode> childNodes = selectedNode.children();
					while (childNodes.hasMoreElements()) {
						UsecasePanel.SortableNode node = childNodes.nextElement();
						long elapsedTime = node.getRmNode().getData().getElapsedTime();
						totalTime += elapsedTime;
						if(elapsedTime == 0) {
							toalZeroTimes+=1;
						}
					}
					String message = "Calculated time was " + totalTime + " ms.";
					if(toalZeroTimes > 0) {
						message = message + " However, there are " + toalZeroTimes + " children with zero ms execution time.";
					}
					JOptionPane.showMessageDialog(null, message);
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
		        if (selectedNode != null) {   
		        	expandAll(tree, pathForLocation, expand, true);
		        }
			}
		};
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

	private void expandAll(JTree tree, TreePath path, boolean expand, boolean isFirst) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
 
        if (node.getChildCount() >= 0) {
            @SuppressWarnings("unchecked")
			Enumeration<SortableNode> children = node.children();
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
	
	class SortableNode extends DefaultMutableTreeNode {
		private RMNode rmnode;
		
		public SortableNode(RMNode rmNode) {
			super(rmNode);
			this.rmnode = rmNode;
		}
		
		public RMNode getRmNode() {
			return rmnode;
		}
		
		@Override
		public String toString() {
			return ((RMNode)getUserObject()).getData().determineRMRecDesc();
		}
	}
}
