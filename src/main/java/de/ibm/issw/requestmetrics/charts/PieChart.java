package de.ibm.issw.requestmetrics.charts;

import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class PieChart {

	public static void openChart(String title, Map<Comparable<String>,Double> data, boolean legend, boolean tooltips) {
		DefaultPieDataset pieDataset = new DefaultPieDataset();
		
		for (Comparable<String> key : data.keySet()) {
			pieDataset.setValue(key, data.get(key));
		}

		JFreeChart chart = ChartFactory.createPieChart(title, pieDataset, legend, tooltips, false);
		
		ChartFrame frame = new ChartFrame("Request Metrics Analyzer - Reporting", chart);
		frame.pack();
		frame.setVisible(true);
	}
}
