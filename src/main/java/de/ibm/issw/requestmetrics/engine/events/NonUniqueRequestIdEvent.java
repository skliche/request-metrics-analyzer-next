package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;
import java.util.EventObject;

@SuppressWarnings("serial")
public class NonUniqueRequestIdEvent extends EventObject {
	private File file;
	private Long id;
	
	public NonUniqueRequestIdEvent (Object source, File file, Long id) {
		super(source);
		this.file = file;
		this.id = id;
	}

	public File getFile() {
		return file;
	}

	public Long getId() {
		return id;
	}
	
	
}