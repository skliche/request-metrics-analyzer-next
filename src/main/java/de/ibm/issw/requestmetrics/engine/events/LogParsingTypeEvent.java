package de.ibm.issw.requestmetrics.engine.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class LogParsingTypeEvent extends EventObject {
	private String type;
	private String fileName;
	public static final String TYPE_RAW = "raw";
	public static final String TYPE_GREPPED = "grepped";
	public static final String TYPE_UNKNOWN = "unknown";	
	public static final String TYPE_PLUGIN_RAW ="plugin_raw";
	public static final String TYPE_PLUGIN_GREPPED ="plugin_grepped";
	
	public LogParsingTypeEvent (Object source, String fileName, String type) {
		super(source);
		this.fileName = fileName;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getFileName() {
		return fileName;
	}
}
