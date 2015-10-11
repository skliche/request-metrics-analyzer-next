package de.ibm.issw.requestmetrics.gui.statistics;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

@SuppressWarnings("serial")
public class ChildNodeStatisticsDialog extends JDialog {
	public ChildNodeStatisticsDialog(JDialog rootWindow, String title, List<ChildNodeStatisticsEntry> entries) {
		super(rootWindow, title, true);
		
		final JTable table = new JTable();
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.setModel(new ChildNodeStatisticsTableModel(entries));
		table.getColumnModel().getColumn(0).setMinWidth(370);
		table.getColumnModel().getColumn(1).setMaxWidth(80);
		table.getColumnModel().getColumn(2).setMaxWidth(110);
		
		final JScrollPane listScrollPane = new JScrollPane(table);
		
		this.setSize(600, 350);
		this.add(listScrollPane, BorderLayout.CENTER);
	}
}
