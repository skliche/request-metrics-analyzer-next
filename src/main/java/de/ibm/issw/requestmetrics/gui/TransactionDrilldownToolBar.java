package de.ibm.issw.requestmetrics.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class TransactionDrilldownToolBar extends JToolBar{
	private final JButton highestExecTimeButton = new JButton("Highest Execution Time");
	private final JButton mostDirectChildrenTimeButton = new JButton("Most Direct Children");
	private final JButton calcStatisticsButton = new JButton("Open Statistics");
	private TransactionDrilldownPanel transactionDrilldownPanel;
	
	/**
	 * builds a ToolBar with three buttons of the following features:
	 * - Jump to subtransaction with highest execution time
	 * - Jump to subtransaction with most direct children
	 * - Calculate and show statistics of a subtransaction in a new dialog window
	 * 
	 * that are disabled by default
	 * 
	 */
	public TransactionDrilldownToolBar(){
		
		setFloatable(false);
		
		highestExecTimeButton.setToolTipText("Jump to the subtransaction with the highest execution time of all subtransactions.");
		highestExecTimeButton.setEnabled(false);
		highestExecTimeButton.addActionListener(selectHighestExecTimeNode());
		
		mostDirectChildrenTimeButton.setToolTipText("Jump to the subtransaction with the highest number of direct children.");
		mostDirectChildrenTimeButton.setEnabled(false);
		mostDirectChildrenTimeButton.addActionListener(selectMostDirectChildrenNode());
		
		calcStatisticsButton.setToolTipText("Calculate statistics for selected node and open the result in a new dialog window.");
		calcStatisticsButton.setEnabled(false);
		calcStatisticsButton.addActionListener(openStatistics());
		
		add(highestExecTimeButton);
		add(mostDirectChildrenTimeButton);
		add(calcStatisticsButton);
	}

	private ActionListener selectHighestExecTimeNode() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				transactionDrilldownPanel.selectTreeNode(transactionDrilldownPanel.getHighestExecTimeNode());
			}
		};
	}
	
	private ActionListener selectMostDirectChildrenNode() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				transactionDrilldownPanel.selectTreeNode(transactionDrilldownPanel.getMostDirectChildrenNode());
			}
		};
	}
	
	private ActionListener openStatistics() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (transactionDrilldownPanel != null && transactionDrilldownPanel.getSelectedTreeNode() != null)
					transactionDrilldownPanel.calculateAndOpenStatisticsDialog(transactionDrilldownPanel.getSelectedTreeNode());;
			}
		};
	}
	
	public JButton getCalcStatisticsButton() {
		return calcStatisticsButton;
	}
	
	public void enableSelectionButtons(TransactionDrilldownPanel transactionDrilldownPanel) {
		highestExecTimeButton.setEnabled(true);
		mostDirectChildrenTimeButton.setEnabled(true);
		
		//TODO: fix this - the enable method should not need a reference to the transactionDrilldownPanel
		this.transactionDrilldownPanel = transactionDrilldownPanel;
	}
	
	/**
	 * disables the buttons for automatic selection of nodes
	 */
	public void disableSelectionButtons() {
		highestExecTimeButton.setEnabled(false);
		mostDirectChildrenTimeButton.setEnabled(false);
	}
	
	/**
	 * enables the "open statistics" button
	 */
	public void enableStatisticsButton() {
		calcStatisticsButton.setEnabled(true);
	}

	/**
	 * disables the "open statistics" button
	 */
	public void disableStatisticsButton() {
		calcStatisticsButton.setEnabled(false);
	}
}
