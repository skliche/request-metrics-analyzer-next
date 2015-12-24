package de.ibm.issw.requestmetrics.engine.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.TableRowSorter;

import de.ibm.issw.requestmetrics.gui.CheckComboBox;
import de.ibm.issw.requestmetrics.gui.RequestMetricsGui;
import de.ibm.issw.requestmetrics.gui.RootCaseTableModel;

public class RootCaseFilter{

	private RowFilter<RootCaseTableModel, Object> elapsedTimeRowFilter;
	private RowFilter<RootCaseTableModel, Object> detailFilter;
	private RowFilter<RootCaseTableModel, Object> dateTimeStartFilter;
	private RowFilter<RootCaseTableModel, Object> dateTimeEndFilter;
	private RowFilter<RootCaseTableModel, Object> typeFilter;
	private List<RowFilter<RootCaseTableModel, Object>> filters = new ArrayList<RowFilter<RootCaseTableModel, Object>>();
	private JTable rootCaseTable;
	private RequestMetricsGui gui;
	
	private final int TIMESTAMP_COLUMN = 1;
	private final int ELAPSED_TIME_COLUMN = 2;
	private final int TYPE_COLUMN = 3;
	private final int DETAIL_COLUMN = 5;
	
	public RootCaseFilter(JTable rootCaseTable, RequestMetricsGui gui) {
		this.rootCaseTable = rootCaseTable;
		this.gui = gui;
	}
	
	/**
	 * uses a numberfilter in the elapsed time column. whenever the input 
	 * changes, the old filter is removed to ensure that filter is only
	 * applied for the current input
	 * @param userInput
	 */
	public void filterElapsedTime(final Object userInput) {
		//
		if (elapsedTimeRowFilter != null && filters.contains(elapsedTimeRowFilter))
			filters.remove(elapsedTimeRowFilter);
		elapsedTimeRowFilter = new RowFilter<RootCaseTableModel, Object>() {

			//
			@Override
			public boolean include(javax.swing.RowFilter.Entry<? extends RootCaseTableModel, ? extends Object> entry) {
				if (rootCaseTable != null){
					Long elapsedTime = (Long) rootCaseTable.getModel().getValueAt((Integer) entry.getIdentifier(), ELAPSED_TIME_COLUMN);
					if (userInput == null || elapsedTime >= (Long) userInput)
						return true;
					else 
						return false;
				} 
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
	
	/**
	 * uses a regex filter in the detail column. whenever the input changes,
	 * the old filter is removed to ensure that filter is only applied for
	 * the current input
	 * @param userInput the input we want to filter for
	 */
	public void filterDetails(final String userInput) {
		if (detailFilter != null && filters.contains(detailFilter))
			filters.remove(detailFilter);
		
		try {
			// perform case insensitive filtering
			detailFilter = RowFilter.regexFilter("(?i)" + userInput, DETAIL_COLUMN);
		} catch (Exception e) {
			//TODO: define exception handling
		}

		if (detailFilter != null)
			filters.add(detailFilter);
		buildCompoundFilter();
	}
	
	/**
	 * 
	 * @param startDate
	 */
	public void filterStartDate(final Date startDate) {
		if (dateTimeStartFilter != null && filters.contains(dateTimeStartFilter))
			filters.remove(dateTimeStartFilter);
		
		dateTimeStartFilter = RowFilter.dateFilter(ComparisonType.AFTER, startDate, TIMESTAMP_COLUMN);
		
		if (dateTimeStartFilter != null)
			filters.add(dateTimeStartFilter);
		buildCompoundFilter();
	}
	
	/**
	 * uses a date filter in the timestamp column. whenever the input changes,
	 * the old filter is removed to ensure that filter is only applied for
	 * the current input
	 * @param endDate
	 */
	public void filterEndDate(final Date endDate) {
		if (dateTimeEndFilter != null && filters.contains(dateTimeEndFilter))
			filters.remove(dateTimeEndFilter);
		
		dateTimeEndFilter = RowFilter.dateFilter(ComparisonType.BEFORE, endDate, TIMESTAMP_COLUMN);
		
		if (dateTimeEndFilter != null)
			filters.add(dateTimeEndFilter);
		buildCompoundFilter();
	}
	
	/**
	 * uses a date filter in the timestamp column. whenever the input changes,
	 * the old filter is removed to ensure that filter is only applied for
	 * the current input
	 * @param comboBox
	 */
	public void filterType(CheckComboBox comboBox) {
		if (typeFilter != null && filters.contains(typeFilter))
			filters.remove(typeFilter);
		
		List<RowFilter<RootCaseTableModel, Object>> typeFilterList = new ArrayList<RowFilter<RootCaseTableModel, Object>>();
		if (comboBox.getSelectedItems() != null) {
			for (Object type : comboBox.getSelectedItems()) {
				RowFilter<RootCaseTableModel, Object> rowFilter = RowFilter.regexFilter(type.toString(), TYPE_COLUMN);
				typeFilterList.add(rowFilter);
			}
			typeFilter = RowFilter.orFilter(typeFilterList);
			filters.add(typeFilter);
		} else {
			filters.remove(typeFilter);
		}
		
		buildCompoundFilter();
	}
	
	/**
	 * clears the all filters and thereby ensures that the table is unfiltered
	 * (i.e. all table entries are displayed again)
	 */
	public void clearFilters() {
		filters.clear();

		if (rootCaseTable != null) {
			TableRowSorter<RootCaseTableModel> sorter = new TableRowSorter<RootCaseTableModel>((RootCaseTableModel) rootCaseTable.getModel());
			rootCaseTable.setRowSorter(sorter);
			sorter.setRowFilter(null);
		}
	}
	
	/**
	 * applies all the different filters on the table by using an "andFilter"
	 * thereby ensures that different filters can be applied at the same time
	 */
	private void buildCompoundFilter() {
		RowFilter<RootCaseTableModel, Object> compoundFilter = RowFilter.andFilter(filters);
		
		TableRowSorter<RootCaseTableModel> sorter = new TableRowSorter<RootCaseTableModel>((RootCaseTableModel) rootCaseTable.getModel());
		rootCaseTable.setRowSorter(sorter);
		sorter.setRowFilter(compoundFilter);
		
		// update the number of root cases in the frame title
		gui.setTitleRootCaseFrame(rootCaseTable.getRowCount() + " Transactions");
	}
}
