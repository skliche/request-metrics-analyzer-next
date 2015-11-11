package de.ibm.issw.requestmetrics.gui.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;

import de.ibm.issw.requestmetrics.gui.statistics.comparator.NumberOfExecutionsComparator;
import de.ibm.issw.requestmetrics.gui.statistics.comparator.TotalTimeComparator;
import de.ibm.issw.requestmetrics.model.RMRecord;
import de.ibm.issw.requestmetrics.util.ColorHelper;

@SuppressWarnings("serial")
public class ChildNodeStatisticsDialog extends JDialog {
	private static final int DIALOG_WIDTH = 1200;
	private static final int DIALOG_HEIGHT = 900;

	public ChildNodeStatisticsDialog(JDialog rootWindow, RMRecord root, List<ChildNodeStatisticsEntry> entries, long numberOfChildren,
			long totalTimeChildren, long totalZeroTimesChildren) {
		super(rootWindow, null, true);
		final String dialogTitle = String.format("Statistics for record '%s' - '%s'", root.getCurrentCmp().getReqid(), root.getDetailCmp());

		final JPanel infoPanel = createInfoPanel(root, numberOfChildren, totalTimeChildren, totalZeroTimesChildren);

		List<ChartPanel> charts = createStatisticCharts(entries);
		final JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
		chartsPanel.add(charts.get(0));
		chartsPanel.add(charts.get(1));

		final JTable table = new JTable(new ChildNodeStatisticsTableModel(entries));
		table.setDefaultRenderer(Object.class, new ChildNodeStatisticsTableCellRenderer());
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.getColumnModel().getColumn(1).setMinWidth(370);
		table.getColumnModel().getColumn(2).setMaxWidth(80);
		table.getColumnModel().getColumn(3).setMaxWidth(80);
		table.getColumnModel().getColumn(4).setMaxWidth(110);
		final JScrollPane listScrollPane = new JScrollPane(table);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(DIALOG_HEIGHT / 2);
		splitPane.setLeftComponent(chartsPanel);
		splitPane.setRightComponent(listScrollPane);

		this.setTitle(dialogTitle);
		this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		this.add(infoPanel, BorderLayout.NORTH);
		this.add(splitPane, BorderLayout.CENTER);
	}

	private JPanel createInfoPanel(RMRecord root, long numberOfChildren, long totalTimeChildren,
			long totalZeroTimesChildren) {
		final JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Root Node Information"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		// root id
		JTextField rootIdField = createTextField(10, Long.toString(root.getCurrentCmp().getReqid()));
		JLabel rootIdLabel = new JLabel("ID: ");
		rootIdLabel.setLabelFor(rootIdField);
		
		// root name
		JTextField rootNameField = createTextField(60, root.getDetailCmp());
		JLabel rootNameLabel = new JLabel("Name: ");
		rootNameLabel.setLabelFor(rootIdField);
		
		// elapsed time
		JTextField elapsedTimeField = createTextField(10, Long.toString(root.getElapsedTime()));
		JLabel elapsedTimeLabel = new JLabel("Elapsed Time: ");
		elapsedTimeLabel.setLabelFor(elapsedTimeField);

		// effective time
		JTextField effectiveTimeField = createTextField(10, Long.toString(root.getElapsedTime() - totalTimeChildren));
		JLabel effectiveTimeLabel = new JLabel("Effective Time: ");
		effectiveTimeLabel.setLabelFor(effectiveTimeField);

		// number of children
		JTextField numberOfChildrenField = createTextField(10, Long.toString(numberOfChildren));
		JLabel numberOfChildrenLabel = new JLabel("# Children: ");
		numberOfChildrenLabel.setLabelFor(numberOfChildrenField);
		
		// number of children with zero execution time
		JTextField numberOfChildrenZeroTimeField = createTextField(10, Long.toString(totalZeroTimesChildren));
		JLabel numberOfChildrenZeroTimeLabel = new JLabel("# Children zero time: ");
		numberOfChildrenZeroTimeLabel.setLabelFor(numberOfChildrenZeroTimeField);
		
		// build the layout with four box layouts added to a flow layout
		final JPanel firstInfoColumn = new JPanel();
		firstInfoColumn.setLayout(new BoxLayout(firstInfoColumn, BoxLayout.PAGE_AXIS));
		firstInfoColumn.add(rootIdLabel);
		firstInfoColumn.add(elapsedTimeLabel);
		firstInfoColumn.add(numberOfChildrenLabel);
		final JPanel secondInfoColumn = new JPanel();
		secondInfoColumn.setLayout(new BoxLayout(secondInfoColumn, BoxLayout.PAGE_AXIS));
		secondInfoColumn.add(rootIdField);
		secondInfoColumn.add(elapsedTimeField);
		secondInfoColumn.add(numberOfChildrenField);
		final JPanel thirdInfoColumn = new JPanel();
		thirdInfoColumn.setLayout(new BoxLayout(thirdInfoColumn, BoxLayout.PAGE_AXIS));
		thirdInfoColumn.add(rootNameLabel);
		thirdInfoColumn.add(effectiveTimeLabel);
		thirdInfoColumn.add(numberOfChildrenZeroTimeLabel);
		final JPanel fourthInfoColumn = new JPanel();
		fourthInfoColumn.setLayout(new BoxLayout(fourthInfoColumn, BoxLayout.PAGE_AXIS));
		fourthInfoColumn.add(rootNameField);
		fourthInfoColumn.add(effectiveTimeField);
		fourthInfoColumn.add(numberOfChildrenZeroTimeField);
		
		infoPanel.add(firstInfoColumn);
		infoPanel.add(secondInfoColumn);
		infoPanel.add(thirdInfoColumn);
		infoPanel.add(fourthInfoColumn);
		
		return infoPanel;
	}

	private JTextField createTextField(int size, String value) {
		JTextField textField = new JTextField(size);
		textField.setEditable(false);
		textField.setBorder(BorderFactory.createLineBorder(getBackground(), 0));
		textField.setText(value);
		return textField;
	}
	
	private List<ChartPanel> createStatisticCharts(List<ChildNodeStatisticsEntry> entries) {
		List<ChartPanel> charts = new ArrayList<ChartPanel>();
		DefaultPieDataset totalTimeDataset = new DefaultPieDataset();
		DefaultPieDataset numberOfExecutionsDataset = new DefaultPieDataset();

		// sort the entries by total time descending to ensure deterministic chart colors
		Collections.sort(entries, new TotalTimeComparator());
		Collections.reverse(entries);
		
		// fill dataset for pie charts
		for (ChildNodeStatisticsEntry childNodeStatisticsEntry : entries) {
			totalTimeDataset.setValue(childNodeStatisticsEntry.getComponent(), childNodeStatisticsEntry.getTotalTime());
			numberOfExecutionsDataset.setValue(childNodeStatisticsEntry.getComponent(),	childNodeStatisticsEntry.getNumberOfExecutions());
		}
		totalTimeDataset.sortByValues(SortOrder.ASCENDING);
		numberOfExecutionsDataset.sortByValues(SortOrder.ASCENDING);

		// create charts & add them to the list
		charts.add(createPieChart("Total Time", totalTimeDataset, entries));
		charts.add(createPieChart("Number Of Executions", numberOfExecutionsDataset, entries));

		return charts;
	}

	private ChartPanel createPieChart(String title, DefaultPieDataset pieDataset,
			List<ChildNodeStatisticsEntry> entries) {
		JFreeChart pieChart = ChartFactory.createPieChart(title, pieDataset, false, true, false);

		PiePlot piePlot = (PiePlot) pieChart.getPlot();
		// hide labels
		piePlot.setLabelGenerator(null);
		// set colors
		DrawingSupplier drawSupplier = piePlot.getDrawingSupplier();
		for (ChildNodeStatisticsEntry childNodeStatisticsEntry : entries) {
			if (childNodeStatisticsEntry.getChartColor().equals(Color.WHITE)) {
				// case: default color -> get default chart color & convert to
				// pastel color
				Paint actPaint = drawSupplier.getNextPaint();
				Color actColor = ColorHelper.getPastelColor((Color) actPaint, Color.WHITE);
				childNodeStatisticsEntry.setChartColor(actColor);
			}
			piePlot.setSectionPaint(childNodeStatisticsEntry.getComponent(), childNodeStatisticsEntry.getChartColor());
		}
		// set size
		ChartPanel chartPanel = new ChartPanel(pieChart) {
			@Override
			public Dimension getPreferredSize() {
				int numberOfCharts = 2;
				int chartWidth = (DIALOG_WIDTH / numberOfCharts) - (numberOfCharts * 10);
				int chartHeight = DIALOG_HEIGHT / 3;
				return new Dimension(chartWidth, chartHeight);
			}
		};
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setVisible(true);
		return chartPanel;
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
