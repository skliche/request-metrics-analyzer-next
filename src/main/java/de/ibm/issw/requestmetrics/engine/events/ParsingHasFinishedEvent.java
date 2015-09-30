package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;

public class ParsingHasFinishedEvent {
	private File file;
	public ParsingHasFinishedEvent(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
}
