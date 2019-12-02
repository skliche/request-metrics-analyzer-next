package de.ibm.issw.requestmetrics.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.ibm.issw.requestmetrics.model.RMNode;
import de.ibm.issw.requestmetrics.model.RMRecord;
import de.ibm.issw.requestmetrics.model.RmRootCase;

@SuppressWarnings("serial")
public class RootCaseTableModel extends AbstractTableModel {
	public static final int FILE_COLUMN_INDEX = 0;
	public static final int TIMESTAMP_COLUMN_INDEX = 1;
	public static final int ELAPSEDTIME_COLUMN_INDEX = 2;
	public static final int TYPE_COLUMN_INDEX = 3;
	public static final int IPADDRESS_COLUMN_INDEX = 4;
	public static final int PID_COLUMN_INDEX = 5;
	public static final int REQUESTID_COLUMN_INDEX = 6;
	public static final int DETAILS_COLUMN_INDEX = 7;
	
	private static final String[] columnNames = {"File", "Timestamp", "Elapsed Time", "Type", "IP Address", "PID", "Request ID", "Details"};

	private List<RmRootCase> useCases;
		

	public RootCaseTableModel(List<RmRootCase> useCases) {
		this.useCases = useCases;
	}

	public List<RmRootCase> getUseCases() {
		return useCases;
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
		if(useCases != null && !useCases.isEmpty()) {
			RMNode node = useCases.get(0).getRmNode();
			RMRecord record = node.getData();
			switch (columnIndex) {
				case FILE_COLUMN_INDEX: return String.class;
				case TIMESTAMP_COLUMN_INDEX: return record.getLogTimeStamp().getClass();
				case ELAPSEDTIME_COLUMN_INDEX:	return Long.class;
				case TYPE_COLUMN_INDEX: return record.getTypeCmp().getClass();
				case IPADDRESS_COLUMN_INDEX: return String.class;
				case PID_COLUMN_INDEX: return Long.class;
				case REQUESTID_COLUMN_INDEX: return Long.class;
				case DETAILS_COLUMN_INDEX: return record.getDetailCmp().getClass();
				default: return String.class;
			}
		}
		return String.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(useCases != null && !useCases.isEmpty()) {
			RMNode node = useCases.get(rowIndex).getRmNode();
			RMRecord record = node.getData();
			
			switch (columnIndex) {
				case FILE_COLUMN_INDEX: return record.getLogSource();
				case TIMESTAMP_COLUMN_INDEX: return record.getLogTimeStamp();
				case ELAPSEDTIME_COLUMN_INDEX:	return record.getElapsedTime();
				case TYPE_COLUMN_INDEX: return record.getTypeCmp();
				case IPADDRESS_COLUMN_INDEX: return record.getCurrentCmp().getIp();
				case PID_COLUMN_INDEX: return record.getCurrentCmp().getPid();
				case REQUESTID_COLUMN_INDEX: return record.getCurrentCmp().getReqid();
				case DETAILS_COLUMN_INDEX: return record.getDetailCmp();
				default: return "Invalid column";
			}
		}
		return "No RootCases";
	}
}
