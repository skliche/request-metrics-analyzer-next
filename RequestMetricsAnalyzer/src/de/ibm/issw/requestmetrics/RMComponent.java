package de.ibm.issw.requestmetrics;

public class RMComponent {
	private String version;
	private String ip;
	private String timestamp;
	private String pid;
	private String reqid;
	private String event;
	
	public RMComponent(String version, String ip, String timestamp, String pid, String reqid, String event) {
		this.version = version;
		this.ip = ip;
		this.timestamp = timestamp;
		this.pid = pid;
		this.reqid = reqid;
		this.event = event;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getReqid() {
		return reqid;
	}
	public void setReqid(String reqid) {
		this.reqid = reqid;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	
	@Override
	public String toString() {
		return "ver="+ version + ",ip=" + ip + ",time=" + timestamp + ",pid=" + pid + ",reqid=" + reqid + ",event=" + event;
	}
}
