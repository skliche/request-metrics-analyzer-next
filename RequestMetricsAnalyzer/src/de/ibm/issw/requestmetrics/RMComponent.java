package de.ibm.issw.requestmetrics;

public class RMComponent {
	private final int version;
	private final String ip;
	private final long timestamp;
	private final long pid;
	private final long reqid;
	private final String event;
	
	public RMComponent(final int currentVersion, final String ip, final long currentTimestamp, final long pid, final long reqid, final String event) {
		this.version = currentVersion;
		this.ip = ip;
		this.timestamp = currentTimestamp;
		this.pid = pid;
		this.reqid = reqid;
		this.event = event;
	}
	
	public int getVersion() {
		return version;
	}
	public String getIp() {
		return ip;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public long getPid() {
		return pid;
	}
	public long getReqid() {
		return reqid;
	}
	public String getEvent() {
		return event;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer()
			.append("ver=").append(version)
			.append(",ip=").append(ip)
			.append(",time=").append(timestamp)
			.append(",pid=").append(pid)
			.append(",reqid=").append(reqid)
			.append(",event=").append(event);
		return sb.toString();
	}
}
