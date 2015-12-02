package de.ibm.issw.requestmetrics.engine.filter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowFilter;

import de.ibm.issw.requestmetrics.gui.RequestMetricsGui;
import de.ibm.issw.requestmetrics.gui.UsecaseTableModel;

public class RootCaseFilter{
	private static JTable table = RequestMetricsGui.getTable();
	List<RowFilter<UsecaseTableModel, Object>> filters = new ArrayList<RowFilter<UsecaseTableModel, Object>>();

	public static void filterElapsedTime(final Object userInput) {
		RowFilter<UsecaseTableModel, Object> elapsedTimeRowFilter = new RowFilter<UsecaseTableModel, Object>() {

			@Override
			public boolean include(javax.swing.RowFilter.Entry<? extends UsecaseTableModel, ? extends Object> entry) {
				Long elapsedTime = (Long) table.getModel().getValueAt((int) entry.getIdentifier(), 2);
				if (elapsedTime >= (Long) userInput || userInput.equals(null))
					return true;
				else {
					return false;
				}
			}
		};
	}
	
	public static void filterDetails(final Object userInput) {
		RowFilter<UsecaseTableModel, Object> detailFilter = RowFilter.regexFilter((String) userInput, 3);
	}
	
	public void buildCompoundFilter() {
		
	}
}
