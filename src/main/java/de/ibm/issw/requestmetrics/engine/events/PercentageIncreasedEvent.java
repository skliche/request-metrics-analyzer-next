package de.ibm.issw.requestmetrics.engine.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class PercentageIncreasedEvent extends EventObject {
	private String fileName;
	private int percentCurrentFileProcessed;
	private int percentAllFilesProcessed;
	private int totalFiles;
	private int processedFiles;

	public PercentageIncreasedEvent(Object source, String fileName, int percentCurrentFileProcessed, int processedTotalLinesPercent, int totalFiles, int processedFiles) {
		super(source);
		this.fileName = fileName;
		this.percentCurrentFileProcessed = percentCurrentFileProcessed;
		this.percentAllFilesProcessed = processedTotalLinesPercent;
		this.totalFiles = totalFiles;
		this.processedFiles = processedFiles;
	}

	public String getFileName() {
		return fileName;
	}

	public int getPercentCurrentFileProcessed() {
		return percentCurrentFileProcessed;
	}
	
	public int getPercentAllFilesProcessed() {
		return percentAllFilesProcessed;
	}
	
	public int getTotalFiles() {
		return totalFiles;
	}

	public int getFilesProcessed() {
		return processedFiles;
	}
}
