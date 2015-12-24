package de.ibm.issw.requestmetrics.gui.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;

import de.ibm.issw.requestmetrics.gui.comparator.TotalTimeComparator;
import de.ibm.issw.requestmetrics.model.RMRecord;
import de.ibm.issw.requestmetrics.util.ColorHelper;

@SuppressWarnings("serial")
public class ChildNodeStatisticsDialog extends JDialog {
	private static final int DIALOG_WIDTH = 1200;
	private static final int DIALOG_HEIGHT = 900;
	
	private final JFreeChart totalTimeChart;
	private final JFreeChart numberOfExecutionsChart;
	private final DefaultPieDataset totalTimeDataset = new DefaultPieDataset();
	private final DefaultPieDataset numberOfExecutionsDataset = new DefaultPieDataset();
	
	private String lastSelection;

	public ChildNodeStatisticsDialog(JFrame rootWindow, RMRecord root, List<ChildNodeStatisticsEntry> entries, long numberOfChildren,
			long totalTimeChildren, long totalZeroTimesChildren) {
		super(rootWindow, null, true);
		final String dialogTitle = String.format("Statistics for record '%s' - '%s'", root.getCurrentCmp().getReqid(), root.getDetailCmp());

		final JPanel infoPanel = createInfoPanel(root, numberOfChildren, totalTimeChildren, totalZeroTimesChildren);

		initChartDatasets(entries);
		totalTimeChart = createPieChart("Total Time", totalTimeDataset, entries);
		numberOfExecutionsChart = createPieChart("Number Of Executions", numberOfExecutionsDataset, entries);
		final JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
		chartsPanel.add(createChartPanel(totalTimeChart));
		chartsPanel.add(createChartPanel(numberOfExecutionsChart));

		final ChildNodeStatisticsTableModel model = new ChildNodeStatisticsTableModel(entries);
		final JTable table = new JTable(model);
		table.setDefaultRenderer(Object.class, new ChildNodeStatisticsTableCellRenderer());
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.getColumnModel().getColumn(1).setMinWidth(370);
		table.getColumnModel().getColumn(2).setMaxWidth(80);
		table.getColumnModel().getColumn(3).setMaxWidth(80);
		table.getColumnModel().getColumn(4).setMaxWidth(110);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				// check if we are in an event sequence and only process the last one
				if(!event.getValueIsAdjusting() && !table.getSelectionModel().isSelectionEmpty()) {
					int row = table.getSelectedRow();
					if(row != -1) { //if no row is selected row = -1 (and we do nothing)
						String key = (String)model.getValueAt(table.convertRowIndexToModel(row), 1);
						explodeChartSection(key);
					}
				}
			} 
		});
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
		JLabel rootIdLabel = new JLabel("Request ID: ");
		rootIdLabel.setLabelFor(rootIdField);
		
		// root name
		JTextField rootNameField = createTextField(60, root.getDetailCmp());
		JLabel rootNameLabel = new JLabel("Component Name: ");
		rootNameLabel.setLabelFor(rootIdField);
		
		// elapsed time
		JTextField elapsedTimeField = createTextField(10, Long.toString(root.getElapsedTime()) + " ms");
		JLabel elapsedTimeLabel = new JLabel("Elapsed Time: ");
		elapsedTimeLabel.setLabelFor(elapsedTimeField);

		// effective time
		JTextField effectiveTimeField = createTextField(10, Long.toString(root.getElapsedTime() - totalTimeChildren) + " ms");
		JLabel effectiveTimeLabel = new JLabel("Effective Time: ");
		effectiveTimeLabel.setLabelFor(effectiveTimeField);

		// number of children
		JTextField numberOfChildrenField = createTextField(10, Long.toString(numberOfChildren));
		JLabel numberOfChildrenLabel = new JLabel("Total Children: ");
		numberOfChildrenLabel.setLabelFor(numberOfChildrenField);
		
		// number of children with zero execution time
		JTextField numberOfChildrenZeroTimeField = createTextField(10, Long.toString(totalZeroTimesChildren));
		JLabel numberOfChildrenZeroTimeLabel = new JLabel("Children with zero time: ");
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
	
	/**
	 * Method to initialize and sort the pie datasets
	 * 
	 * @param entries	list of entries to fill the pie datasets from
	 */
	private void initChartDatasets(List<ChildNodeStatisticsEntry> entries) {
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
	}
	
	private JFreeChart createPieChart(String title, DefaultPieDataset pieDataset, List<ChildNodeStatisticsEntry> entries) {
		JFreeChart pieChart = ChartFactory.createPieChart(title, pieDataset, false, true, false);

		PiePlot piePlot = (PiePlot) pieChart.getPlot();
		// hide labels
		piePlot.setLabelGenerator(null);
		// set colors
		DrawingSupplier drawSupplier = piePlot.getDrawingSupplier();
		for (ChildNodeStatisticsEntry childNodeStatisticsEntry : entries) {
			if (childNodeStatisticsEntry.getChartColor().equals(Color.WHITE)) {
				// case: default color -> get default chart color & convert to pastel color
				Paint actPaint = drawSupplier.getNextPaint();
				Color actColor = ColorHelper.getPastelColor((Color) actPaint, Color.WHITE);
				childNodeStatisticsEntry.setChartColor(actColor);
			}
			piePlot.setSectionPaint(childNodeStatisticsEntry.getComponent(), childNodeStatisticsEntry.getChartColor());
		}
		return pieChart;
	}

	private ChartPanel createChartPanel(JFreeChart pieChart) {
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
	
	/**
	 * This method explodes the section for the given parameter key in both pie charts and inserts the section selected before
	 * 
	 * @param key	the key from the pie dataset section to explode
	 */
	private void explodeChartSection(String key) {
		PiePlot piePlot = (PiePlot) totalTimeChart.getPlot();
		if(lastSelection != null) piePlot.setExplodePercent(lastSelection, 0.0D);
		piePlot.setExplodePercent(key, 0.2D);
		
		piePlot = (PiePlot) numberOfExecutionsChart.getPlot();
		if(lastSelection != null) piePlot.setExplodePercent(lastSelection, 0.0D);
		piePlot.setExplodePercent(key, 0.2D);
		
		lastSelection = key;
	}
}
