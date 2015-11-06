package de.ibm.issw.requestmetrics.gui.statistics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import de.ibm.issw.requestmetrics.gui.statistics.comparator.NumberOfExecutionsComparator;
import de.ibm.issw.requestmetrics.gui.statistics.comparator.TotalTimeComparator;

@SuppressWarnings("serial")
public class ChildNodeStatisticsDialog extends JDialog {
	private static final int DIALOG_WIDTH = 1200;
	private static final int DIALOG_HEIGHT = 900;
	
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
		
		final JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
		final ChartPanel totalTimeChartPanel = createTotalTimePieChart(entries, 5);
		final ChartPanel numbOfExecChartPanel = createNumberOfExecutionsPieChart(entries, 5);
		chartsPanel.add(totalTimeChartPanel);
		chartsPanel.add(numbOfExecChartPanel);
		
		this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		this.add(chartsPanel, BorderLayout.NORTH);
		this.add(listScrollPane, BorderLayout.CENTER);
	}
	
	private ChartPanel createTotalTimePieChart(List<ChildNodeStatisticsEntry> entries, int numberToShow) {
		String title = String.format("TOP %s - total time", numberToShow);
		DefaultPieDataset pieDataset = new DefaultPieDataset();

		// sort the entries by total time & fill the dataset for the pie chart
		Collections.sort(entries, new TotalTimeComparator());
		Collections.reverse(entries);
		for (int i = 0; i < entries.size() && i < numberToShow; i++) {
			pieDataset.setValue(entries.get(i).getComponent(), entries.get(i).getTotalTime());
		}
		
		return createPieChart(title, pieDataset);
	}

	private ChartPanel createNumberOfExecutionsPieChart(List<ChildNodeStatisticsEntry> entries, int numberToShow) {
		String title = String.format("TOP %s - number of executions", numberToShow);
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		
		// sort the entries by number of executions & fill the dataset for the pie chart
		Collections.sort(entries, new NumberOfExecutionsComparator());
		Collections.reverse(entries);
		for (int i = 0; i < entries.size() && i < numberToShow; i++) {
			pieDataset.setValue(entries.get(i).getComponent(), entries.get(i).getNumberOfExecutions());
		}
		
		return createPieChart(title, pieDataset);
	}
	
	private ChartPanel createPieChart(String title, DefaultPieDataset pieDataset) {
		JFreeChart pieChart = ChartFactory.createPieChart(title, pieDataset, false, true, false);
		ChartPanel chartPanel = new ChartPanel(pieChart){
		    @Override
		    public Dimension getPreferredSize() {
		    	int numberOfCharts = 2;
		    	int chartWidth = (DIALOG_WIDTH / numberOfCharts) - (numberOfCharts * 10);
		    	int chartHeight = DIALOG_HEIGHT / 3;
		        return new Dimension(chartWidth,chartHeight);
		    }
		};
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setVisible(true);
		return chartPanel;
	}
}
