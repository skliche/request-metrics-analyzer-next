package de.ibm.issw.requestmetrics.engine.events;

import java.util.EventObject;

//give feedback if Request ID occurs twice
//give feedback about which files could not be parsed when loading multiple files

@SuppressWarnings("serial")
public class UnsupportedFileEvent extends EventObject{
	private String fileName;

	public UnsupportedFileEvent (Object source, String fileName) {
		super(source);
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
