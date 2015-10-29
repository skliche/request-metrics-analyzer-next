package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;
import java.util.EventObject;

//give feedback if Request ID occurs twice
//give feedback about which files could not be parsed when loading multiple files

@SuppressWarnings("serial")
public class UnsupportedFileEvent extends EventObject{
	private File file;

	public UnsupportedFileEvent (Object source, File file) {
		super(source);
		this.file = file;
	}

	public File getFile() {
		return file;
	}
}
