package de.ibm.issw.requestmetrics.engine.filter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import de.ibm.issw.requestmetrics.gui.RequestMetricsGui;
import de.ibm.issw.requestmetrics.gui.UsecaseTableModel;

public class RootCaseFilter{
	private static JTable table;
	private RowFilter<UsecaseTableModel, Object> elapsedTimeRowFilter;
	private RowFilter<UsecaseTableModel, Object> detailFilter;

	public void filterElapsedTime(final Object userInput) {
		table = RequestMetricsGui.getTable();
		elapsedTimeRowFilter = new RowFilter<UsecaseTableModel, Object>() {

			@Override
			public boolean include(javax.swing.RowFilter.Entry<? extends UsecaseTableModel, ? extends Object> entry) {
				Long elapsedTime = (Long) table.getModel().getValueAt((int) entry.getIdentifier(), 2);
				if (userInput == null || elapsedTime >= (Long) userInput)
					return true;
				else {
					return false;
				}
			}
		};
		buildCompoundFilter();
	}
	
	public void filterDetails(final String userInput) {
		detailFilter = RowFilter.regexFilter(userInput, 5);
		buildCompoundFilter();
	}
	
	public void filterType() {
		
	}
	
	private void buildCompoundFilter() {
		table = RequestMetricsGui.getTable();
		
		List<RowFilter<UsecaseTableModel, Object>> filters = new ArrayList<RowFilter<UsecaseTableModel, Object>>();
		if (elapsedTimeRowFilter != null)
			filters.add(elapsedTimeRowFilter);
		if (detailFilter != null)
			filters.add(detailFilter);
		RowFilter<UsecaseTableModel, Object> compoundFilter = RowFilter.andFilter(filters);
		
		TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>((UsecaseTableModel) table.getModel());
		table.setRowSorter(sorter);
		sorter.setRowFilter(compoundFilter);
	}
}
