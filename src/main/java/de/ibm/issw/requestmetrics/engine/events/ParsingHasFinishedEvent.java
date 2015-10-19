package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;
import java.util.EventObject;

@SuppressWarnings("serial")
public class ParsingHasFinishedEvent extends EventObject {
	
	public ParsingHasFinishedEvent(Object source, File file) {
		super(source);
		this.file = file;
	}

	private File file;
	

	public File getFile() {
		return file;
	}
}
