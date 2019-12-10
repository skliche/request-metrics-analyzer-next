package de.ibm.issw.requestmetrics.model;

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
		final StringBuffer sb = new StringBuffer()
			.append("ver=").append(version)
			.append(",ip=").append(ip)
			.append(",time=").append(timestamp)
			.append(",pid=").append(pid)
			.append(",reqid=").append(reqid)
			.append(",event=").append(event);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RMComponent) {
			RMComponent other = (RMComponent) o;
			return this.pid == other.pid && this.ip == other.ip && this.reqid == other.reqid;
		}
		return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		return (ip + ";" + pid + ";" + reqid).hashCode();
	}
}
