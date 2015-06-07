package de.ibm.issw.requestmetrics;

import java.text.SimpleDateFormat;

public class RmRootCase {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	private RMRecord record;
	
	public RmRootCase(RMRecord rmRecord) {
		this.record = rmRecord;
	}
	public RMRecord getRmNode() {
		return record;
	}
	public void setRecord(RMRecord node) {
		this.record = node;
	}
	public String getDescription() {
		
		return sdf.format(record.getLogTimeStamp()) 
				+ " | " + record.getElapsedTime() 
				+ " | " + record.getTypeCmp() 
				+ " | " + record.getDetailCmp();
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
}
