package de.ibm.issw.requestmetrics;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RMRecord {
	private String logSource;
	private Date logTimeStamp;
	private String rmRecId;
	private String threadId;
	private RMComponent currentCmp;
	private RMComponent parentCmp;
	private String typeCmp;
	private String detailCmp;
	private Long elapsedTime;
	
	public RMRecord() {
	}

	public RMRecord(String logSource, Date logTimestamp, String threadId, RMComponent currentCmp, RMComponent parentCmp,
			String typeCmp, String detailCmp, Long elapsedTime) {
		this.logSource = logSource;
		this.logTimeStamp = logTimestamp;
		this.threadId = threadId;
		this.currentCmp = currentCmp;
		this.parentCmp = parentCmp;
		this.typeCmp = typeCmp;
		this.detailCmp = detailCmp;
		this.elapsedTime = elapsedTime;
		this.rmRecId = generateRmRecId(threadId, currentCmp);
	}

	public String getThreadId() {
		return this.threadId;
	}

	public RMComponent getCurrentCmp() {
		return this.currentCmp;
	}

	public String getDetailCmp() {
		return this.detailCmp;
	}

	public void setDetailCmp(String detailCmp) {
		this.detailCmp = detailCmp;
	}

	public Long getElapsedTime() {
		return this.elapsedTime;
	}

	public void setElapsedTime(Long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public RMComponent getParentCmp() {
		return this.parentCmp;
	}

	public String getTypeCmp() {
		return this.typeCmp;
	}

	public void setTypeCmp(String typeCmp) {
		this.typeCmp = typeCmp;
	}

	public String getLogSource() {
		return logSource;
	}

	public void setLogSource(String logSource) {
		this.logSource = logSource;
	}

	public String toString() {
		String recordRM = this.logTimeStamp + "|"
				+ this.elapsedTime + "|" + this.threadId + "|" + this.parentCmp
				+ "|" + this.currentCmp + "|" + this.typeCmp + "|"
				+ this.detailCmp + "|";
		return recordRM;
	}

	public String getRmRecId() {
		return this.rmRecId;
	}

	public static String generateRmRecId(String threadId, RMComponent currentCmp) {
		return threadId + "," + currentCmp;
	}

	public Date getLogTimeStamp() {
		return logTimeStamp;
	}

	public String determineRMRecDesc() {
		SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
		
		return getElapsedTime() + "ms | " + sdf.format(logTimeStamp) 
				+ " | " + getThreadId() + " | " + getTypeCmp() + " | "
				+ getDetailCmp();
	}
}
