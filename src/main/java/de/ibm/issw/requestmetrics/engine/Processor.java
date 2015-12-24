package de.ibm.issw.requestmetrics.engine;

import java.io.File;

public interface Processor {
	/**
	 * Delegator method to transfer workload to the filehandler.
	 * The filehandler has to ensure to proper open and handle the file.
	 * 
	 * @param files the files to be processed.
	 */
	public void processInputFiles(File[] files);
	
	/** 
	 * The method is called before a single file is parsed.
	 * @param file the file that is about to be parsed.
	 */
	public void beforeSingleFileParsed(File file);
	
	/**
	 * The method is called after a file has been parsed.
	 * @param file the file that has been parsed.
	 */
	public void afterSingleFileParsed(File file);
	
	/**
	 * The method is called when a single line needs to be parsed
	 * by the processor.
	 * 
	 * @param line the line to be parsed.
	 * @param file the file that is parsed.
	 */
	public void handleLine(String line, File file);
}
