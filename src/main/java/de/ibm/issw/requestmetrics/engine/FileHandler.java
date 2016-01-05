package de.ibm.issw.requestmetrics.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ibm.issw.requestmetrics.engine.events.ParsingAllFilesHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.ParsingFileHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.PercentageIncreasedEvent;
import de.ibm.issw.requestmetrics.util.FileWalker;

public class FileHandler extends Observable {
	public static final Logger LOG = LoggerFactory.getLogger(FileHandler.class);
	private Processor processor;
	private File currentFile;
	
	// statistic variables
	private Map<String, Integer> fileLinesMap;
	private Long totalLinesAmount;
	private Long totalProcessedLines = 0l;
	private Integer processedFiles = 0;
	
	public FileHandler(Processor processor) {
		this.processor = processor;
	}

	/**
	 * Counts the number of lines for each file and saves them in a hash map together with the filename.
	 * Adds the number of the lines for the current file to the total number of lines.
	 * @param files Array of all files that were selected to parse
	 */
	public void preProcessInputFiles(List<File> files) {
		try{
			fileLinesMap = new HashMap<String, Integer>();
			totalLinesAmount = 0l;
			for (File file: files) {
				LineNumberReader lnr = new LineNumberReader(new FileReader(file));
				lnr.skip(Long.MAX_VALUE);
				Integer currentLinesAmount = lnr.getLineNumber();
				lnr.close();
				totalLinesAmount += currentLinesAmount;
				fileLinesMap.put(file.getName(), currentLinesAmount);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates the percentage of processed lines (for the currently parsing file and for all parsing files)
	 * and notifies the observers if it has increased (needed for progress bars by now)
	 * @param processedLines the processed lines of the currently processed file
	 * @param fileName name of the currently processed file, needed to get its number of lines from the fileLinesMap
	 */
	public void checkProcessedLines(Integer processedLines, String fileName) {
		//we need the factor 100 to avoid errors in calculating files with less than 100 lines
		Integer percentCurrentFile = 100 * processedLines / fileLinesMap.get(fileName);
		Integer percentAllFiles = (int) (100 * totalProcessedLines / totalLinesAmount);

		//check if the percent-value changes in comparison to the last processed line; if it changed, notify the observers
		if (percentCurrentFile != (100*(processedLines-1) / fileLinesMap.get(fileName))) {
			setChanged();
			notifyObservers(new PercentageIncreasedEvent(this, currentFile.getName(), percentCurrentFile, percentAllFiles, fileLinesMap.size(), processedFiles));
		}
	}
	
	public void processInputFiles(File[] files) {
		List<File> allFiles = new ArrayList<File>();
		for (File element : files) {
			if(element.isFile()) {
				// case: element is a file -> add it to the list of all files
				allFiles.add(element);
			} else if(element.isDirectory()) {
				// case: element is a directory -> get all files via FileWalker and add them to the list of all files
				try {
					FileWalker walker = new FileWalker(element.getAbsolutePath());
					allFiles.addAll(walker.getAllFiles());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		preProcessInputFiles(allFiles);
		for (File file : allFiles) {
			currentFile = file;
			processInputFile (file);
		}
		//tell the observers that processing all files has finished
		setChanged();
		notifyObservers(new ParsingAllFilesHasFinishedEvent(this, currentFile));
	}
	
	public void processInputFile (File file) {		
		try {
			final BufferedReader inputStream = new BufferedReader(new FileReader(file));
			// call the before hook 
			processor.beforeSingleFileParsed(file);
			
			Integer processedLines = 0;
			String line = null;
			while ((line = inputStream.readLine()) != null) {
				// call the processor callback to process the line
				processor.handleLine(line, file);

				processedLines++;
				totalProcessedLines++;
				
				//TODO: should we do this calculation after each line?
				checkProcessedLines(processedLines, file.getName());
			}
			inputStream.close();
			
			LOG.info("Processed " + totalProcessedLines + " lines.");
			
			// call the after hook
			processor.afterSingleFileParsed(file);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			processedFiles++;
			//when processed a file, notify the observers that we are done with the file
			setChanged();
			notifyObservers(new ParsingFileHasFinishedEvent(this, file.getName()));
		}
	}
	
	public void reset() {
		fileLinesMap = new HashMap<String, Integer>();
		totalLinesAmount = 0l;
		totalProcessedLines = 0l;
		processedFiles = 0;
	}
}
