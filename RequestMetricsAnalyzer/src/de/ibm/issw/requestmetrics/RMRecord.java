package de.ibm.issw.requestmetrics;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RMRecord {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	
	private final String logSource;
	private final Date logTimeStamp;
	private final String rmRecId;
	private final String threadId;
	private final RMComponent currentCmp;
	private final RMComponent parentCmp;
	private final String typeCmp;
	private final String detailCmp;
	private final long elapsedTime;
	
	public RMRecord(String logSource, Date logTimestamp, String threadId, RMComponent currentCmp, RMComponent parentCmp,
			String typeCmp, String detailCmp, long elapsedTime) {
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

	public long getElapsedTime() {
		return this.elapsedTime;
	}

	public RMComponent getParentCmp() {
		return this.parentCmp;
	}

	public String getTypeCmp() {
		return this.typeCmp;
	}

	public String getLogSource() {
		return logSource;
	}

	public String getRmRecId() {
		return this.rmRecId;
	}

	private String generateRmRecId(String threadId, RMComponent currentCmp) {
		StringBuffer sb = new StringBuffer()
			.append(threadId).append(",").append(currentCmp);
		return  sb.toString();
	}

	public Date getLogTimeStamp() {
		return logTimeStamp;
	}

	public String determineRMRecDesc() {
		StringBuffer sb = new StringBuffer()
			.append(getElapsedTime()).append("ms | ")
			.append(sdf.format(logTimeStamp)).append(" | ")
			.append(getThreadId()).append(" | ")
			.append(getTypeCmp()).append(" | ")
			.append(getDetailCmp());
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer()
			.append(this.logTimeStamp).append("|")
			.append(this.elapsedTime).append("|") 
			.append(this.threadId).append("|")
			.append(this.parentCmp).append("|") 
			.append(this.currentCmp).append("|") 
			.append(this.typeCmp).append("|")
			.append(this.detailCmp).append("|");
		return sb.toString();
	}
	public boolean isRootCase() {
		return currentCmp.getReqid() == parentCmp.getReqid();
	}
}
