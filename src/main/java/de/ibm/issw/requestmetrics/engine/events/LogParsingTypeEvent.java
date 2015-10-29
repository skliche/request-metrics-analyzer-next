package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;
import java.util.EventObject;

@SuppressWarnings("serial")
public class LogParsingTypeEvent extends EventObject {
	private String type;
	private File file;
	public static final String TYPE_RAW = "raw";
	public static final String TYPE_GREPPED = "grepped";
	
	
	public LogParsingTypeEvent (Object source, File file, String type) {
		super(source);
		this.file = file;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public File getFile() {
		return file;
	}
}
