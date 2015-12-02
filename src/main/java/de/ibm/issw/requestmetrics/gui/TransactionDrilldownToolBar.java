package de.ibm.issw.requestmetrics.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class TransactionDrilldownToolBar extends JToolBar{
	private final JButton highestExecTimeButton = new JButton("Highest Execution Time");
	private final JButton mostDirectChildrenTimeButton = new JButton("Most Direct Children");
	private final JButton calcStatisticsButton = new JButton("Open Statistics");
	private UsecasePanel transactionDrilldownPanel;
	
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
	
	public JButton getCalcStatisticsButton() {
		return calcStatisticsButton;
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
	
	public void setSelectionButtonsEnabled(boolean enabled) {
		if (enabled == true) {
			highestExecTimeButton.setEnabled(true);
			mostDirectChildrenTimeButton.setEnabled(true);
		} else {
			highestExecTimeButton.setEnabled(false);
			mostDirectChildrenTimeButton.setEnabled(false);
		}
	}
	
	public void setStatisticsButtonEnabled(boolean enabled) {
		if (enabled == true) calcStatisticsButton.setEnabled(true);
		else {
			calcStatisticsButton.setEnabled(false);
		}
	}
	
	public void setTransactionDrilldownPanel(UsecasePanel transactionDrilldownPanel) {
		this.transactionDrilldownPanel = transactionDrilldownPanel;
	}
	
}
