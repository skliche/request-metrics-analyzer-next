package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;
import java.util.EventObject;
import java.util.List;

@SuppressWarnings("serial")
public class ParsingAllFilesHasFinishedEvent extends EventObject {
	private List<File> files;
	
	public ParsingAllFilesHasFinishedEvent(Object source, List<File> files) {
		super(source);
		this.files = files;
	}

	public List<File> getFiles() {
		return files;
	}
}
