package de.ibm.issw.requestmetrics;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RMRecord implements Serializable {
	private String logSource;
	private String rmRecId;
	private String threadId;
	private String currentCmp;
	private String parentCmp;
	private String typeCmp;
	private String detailCmp;
	private String elapsedTime;
	private String recTime;
	private String recDate;

	public RMRecord() {
	}

	public RMRecord(String logSource, String threadId, String currentCmp, String parentCmp,
			String typeCmp, String detailCmp, String elapsedTime,
			String recTime, String recDate) {
		this.logSource = logSource;
		this.threadId = threadId;
		this.currentCmp = currentCmp;
		this.parentCmp = parentCmp;
		this.typeCmp = typeCmp;
		this.detailCmp = detailCmp;
		this.elapsedTime = elapsedTime;
		this.recTime = recTime;
		this.recDate = recDate;
		this.rmRecId = generateRmRecId(threadId, currentCmp);
	}

	public String getThreadId() {
		return this.threadId;
	}

	public String getCurrentCmp() {
		return this.currentCmp;
	}

	public String getDetailCmp() {
		return this.detailCmp;
	}

	public void setDetailCmp(String detailCmp) {
		this.detailCmp = detailCmp;
	}

	public String getElapsedTime() {
		return this.elapsedTime;
	}

	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public String getParentCmp() {
		return this.parentCmp;
	}

	public void setParentCmp(String parentCmp) {
		this.parentCmp = parentCmp;
	}

	public String getRecDate() {
		return this.recDate;
	}

	public void setRecDate(String recDate) {
		this.recDate = recDate;
	}

	public String getRecTime() {
		return this.recTime;
	}

	public void setRecTime(String recTime) {
		this.recTime = recTime;
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
		String recordRM = this.recDate + "|" + this.recTime + "|"
				+ this.elapsedTime + "|" + this.threadId + "|" + this.parentCmp
				+ "|" + this.currentCmp + "|" + this.typeCmp + "|"
				+ this.detailCmp + "|";
		return recordRM;
	}

	public String getRmRecId() {
		return this.rmRecId;
	}

	public static String generateRmRecId(String threadId, String currentCmp) {
		return threadId + "," + currentCmp;
	}

	public String determineRMRecDesc() {
		return getElapsedTime() + "ms | " + getRecDate() + " " + getRecTime()
				+ " | " + getThreadId() + " | " + getTypeCmp() + " | "
				+ getDetailCmp();
	}
}
