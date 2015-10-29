package de.ibm.issw.requestmetrics.engine.events;

import java.io.File;
import java.util.EventObject;

//Events without Root Record should be detected, and for those, recognizable DummyRootRecords should be created
//User should be informed about how many missing root records there are/ how many Dummies there are


@SuppressWarnings("serial")
public class NoRootFoundEvent extends EventObject  {
	private File file;

	public NoRootFoundEvent (Object source, File file) {
		super(source);
		this.file = file;
	}


	public File getFile() {
		return file;
	}
}
