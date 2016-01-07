package de.ibm.issw.requestmetrics.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RMRecord {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	private static final String UNKNOWN = "UNKNOWN";
	
	private final String logSource;
	private final Date logTimeStamp;
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
		
		if(parentCmp.getReqid() == currentCmp.getReqid()) {
			sb.append(" | reqid=").append(parentCmp.getReqid())
			.append(" (root event)");
		} else {
			sb.append(" | parent=").append(parentCmp.getReqid())
			.append(" | current=").append(currentCmp.getReqid());
		}
		return sb.toString();
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer()
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
	
	public boolean isDummy() {
		return UNKNOWN.equals(typeCmp);
	}
	
	public static RMRecord createDummy(Long parentNodeId) {
		return new RMRecord(UNKNOWN, new Date(), UNKNOWN, 
				new RMComponent(0, UNKNOWN, 0, 0, parentNodeId, UNKNOWN), 
				new RMComponent(0, UNKNOWN, 0, 0, parentNodeId, UNKNOWN), 
				UNKNOWN, UNKNOWN + " / no root case", Long.MAX_VALUE);
	}
}
