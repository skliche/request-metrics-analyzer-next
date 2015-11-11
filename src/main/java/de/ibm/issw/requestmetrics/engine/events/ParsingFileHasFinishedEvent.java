package de.ibm.issw.requestmetrics.engine.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ParsingFileHasFinishedEvent extends EventObject {
	private String fileName;
	
	public ParsingFileHasFinishedEvent(Object source, String fileName) {
		super(source);
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
