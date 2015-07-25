package de.ibm.issw.requestmetrics.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.ibm.issw.requestmetrics.model.RMNode;
import de.ibm.issw.requestmetrics.model.RMRecord;
import de.ibm.issw.requestmetrics.model.RmRootCase;

@SuppressWarnings("serial")
public class UsecaseTableModel extends AbstractTableModel {
	private static final String[] columnNames = {"Timestamp", "Elapsed Time", "Type", "Request ID", "Details"};

	private List<RmRootCase> useCases;
		
	public UsecaseTableModel(List<RmRootCase> useCases) {
		this.useCases = useCases;
	}

	@Override
	public int getRowCount() {
		return useCases.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		RMNode node = useCases.get(0).getRmNode();
		RMRecord record = node.getData();
		switch (columnIndex) {
			case 0: return record.getLogTimeStamp().getClass();
			case 1:	return Long.class;
			case 2: return record.getTypeCmp().getClass();
			case 3: return Long.class;
			default: return record.getDetailCmp().getClass();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RMNode node = useCases.get(rowIndex).getRmNode();
		RMRecord record = node.getData();
		
		switch (columnIndex) {
			case 0: return record.getLogTimeStamp();
			case 1:	return record.getElapsedTime();
			case 2: return record.getTypeCmp();
			case 3: return record.getCurrentCmp().getReqid();
			default: return record.getDetailCmp();
		}
	}
}
