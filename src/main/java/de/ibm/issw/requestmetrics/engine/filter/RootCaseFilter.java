package de.ibm.issw.requestmetrics.engine.filter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.TableRowSorter;

import de.ibm.issw.requestmetrics.gui.RequestMetricsGui;
import de.ibm.issw.requestmetrics.gui.UsecaseTableModel;
import de.ibm.issw.requestmetrics.gui.CheckComboBox;

public class RootCaseFilter{
	private static JTable table;
	private RowFilter<UsecaseTableModel, Object> elapsedTimeRowFilter;
	private RowFilter<UsecaseTableModel, Object> detailFilter;
	private RowFilter<UsecaseTableModel, Object> dateTimeStartFilter;
	private RowFilter<UsecaseTableModel, Object> dateTimeEndFilter;
	private RowFilter<UsecaseTableModel, Object> typeFilter;
	private List<RowFilter<UsecaseTableModel, Object>> filters = new ArrayList<RowFilter<UsecaseTableModel, Object>>();

	public void filterElapsedTime(final Object userInput) {
		table = RequestMetricsGui.getTable();
		if (elapsedTimeRowFilter != null && filters.contains(elapsedTimeRowFilter))
			filters.remove(elapsedTimeRowFilter);
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
		if (elapsedTimeRowFilter != null) {
			filters.add(elapsedTimeRowFilter);
		}
		buildCompoundFilter();
	}
	
	public void filterDetails(final String userInput) {
		if (detailFilter != null && filters.contains(detailFilter))
			filters.remove(detailFilter);
		
		try {
			detailFilter = RowFilter.regexFilter(userInput, 5);
		} catch (Exception e) {
			//TODO: define exception handling
		}

		if (detailFilter != null)
			filters.add(detailFilter);
		buildCompoundFilter();
	}
	
	public void filterDateTime(final Object startDate, final Object endDate) {
		if (dateTimeStartFilter != null && filters.contains(dateTimeStartFilter))
			filters.remove(dateTimeStartFilter);
		if (dateTimeEndFilter != null && filters.contains(dateTimeEndFilter))
			filters.remove(dateTimeEndFilter);
		
		dateTimeStartFilter = RowFilter.dateFilter(ComparisonType.AFTER, null, 1);
		dateTimeEndFilter = RowFilter.dateFilter(ComparisonType.BEFORE, null, 1);
		
		if (dateTimeStartFilter != null)
			filters.add(dateTimeStartFilter);
		if (dateTimeEndFilter != null)
			filters.add(dateTimeEndFilter);
		buildCompoundFilter();
	}
	
	public void filterType(CheckComboBox comboBox) {
		if (typeFilter != null && filters.contains(typeFilter))
			filters.remove(typeFilter);
		
		List<RowFilter<UsecaseTableModel, Object>> typeFilterList = new ArrayList<RowFilter<UsecaseTableModel, Object>>();
		if (comboBox.getSelectedItems() != null) {
			for (Object type : comboBox.getSelectedItems()) {
				RowFilter<UsecaseTableModel, Object> rowFilter = RowFilter.regexFilter(type.toString(), 3);
				typeFilterList.add(rowFilter);
			}
			typeFilter = RowFilter.orFilter(typeFilterList);
			filters.add(typeFilter);
		} else {
			filters.remove(typeFilter);
		}
		
		buildCompoundFilter();
	}
	
	public void clearFilters() {
		filters.clear();

		TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>((UsecaseTableModel) table.getModel());
		table.setRowSorter(sorter);
		sorter.setRowFilter(null);
	}
	
	private void buildCompoundFilter() {
		table = RequestMetricsGui.getTable();
		RowFilter<UsecaseTableModel, Object> compoundFilter = RowFilter.andFilter(filters);
		
		TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>((UsecaseTableModel) table.getModel());
		table.setRowSorter(sorter);
		sorter.setRowFilter(compoundFilter);
	}
}
