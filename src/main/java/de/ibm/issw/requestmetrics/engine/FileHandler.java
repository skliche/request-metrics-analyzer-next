package de.ibm.issw.requestmetrics.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ibm.issw.requestmetrics.engine.events.ParsingAllFilesHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.ParsingFileHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.PercentageIncreasedEvent;
import de.ibm.issw.requestmetrics.util.FileCleaner;
import de.ibm.issw.requestmetrics.util.FileDetector;

public class FileHandler extends Observable {
	public static final Logger LOG = LoggerFactory.getLogger(FileHandler.class);
	private Processor processor;
	private List<File> allFiles;
	private List<File> tmpExtractFolders;
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
	 * Counts the number of lines for a file and saves them in a hash map
	 * together with the filename. Adds the number of the lines for the current
	 * file to the total number of lines.
	 * 
	 * @param file
	 */
	public void determineNumberOfLines(File file) {
		try {
			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
			lnr.skip(Long.MAX_VALUE);
			Integer currentLinesAmount = lnr.getLineNumber();
			lnr.close();
			totalLinesAmount += currentLinesAmount;
			fileLinesMap.put(file.getAbsolutePath(), currentLinesAmount);
		} catch (Exception e) {
			LOG.error("Exception while counting number of lines for file " + file.getAbsolutePath(), e);
		}
	}

	/**
	 * Calculates the percentage of processed lines (for the currently parsing
	 * file and for all parsing files) and notifies the observers if it has
	 * increased (needed for progress bars by now)
	 * 
	 * @param processedLines
	 *            the processed lines of the currently processed file
	 * @param fileName
	 *            name of the currently processed file, needed to get its number
	 *            of lines from the fileLinesMap
	 */
	public void checkProcessedLines(Integer processedLines, String fileName) {
		// we need the factor 100 to avoid errors in calculating files with less
		// than 100 lines
		Integer percentCurrentFile = 100 * processedLines / fileLinesMap.get(fileName);
		Integer percentAllFiles = (int) (100 * totalProcessedLines / totalLinesAmount);

		// check if the percent-value changes in comparison to the last
		// processed line; if it changed, notify the observers
		if (percentCurrentFile != (100 * (processedLines - 1) / fileLinesMap.get(fileName))) {
			setChanged();
			notifyObservers(new PercentageIncreasedEvent(this, currentFile.getAbsolutePath(), percentCurrentFile,
					percentAllFiles, fileLinesMap.size(), processedFiles));
		}
	}

	public void preProcessInputFiles(List<File> files) {
		for (File element : files) {
			if (element.isFile()) {
				// case: element is a file
				if (element.getName().toLowerCase().endsWith(".zip")) {
					// case: element is a ZIP file
					try {
						LOG.debug("Pre-processing ZIP file " + element.getAbsolutePath());
						File zipFolder = preProcessZipFile(element);
						preProcessInputFiles(Arrays.asList(zipFolder));
					} catch (Exception e) {
						LOG.error("Exception while processing ZIP file " + element.getAbsolutePath(), e);
					}
				} else {
					// case: element is no ZIP file
					LOG.debug("Adding file " + element.getAbsolutePath());
					determineNumberOfLines(element);
					allFiles.add(element);
				}
			} else if (element.isDirectory()) {
				// case: element is a directory
				try {
					// get all files via FileWalker and pre-process them recursively
					LOG.debug("Pre-processing folder " + element.getAbsolutePath());
					FileDetector fileDetector = new FileDetector(element.getAbsolutePath());
					preProcessInputFiles(fileDetector.getAllFiles());
				} catch (Exception e) {
					LOG.error("Exception while pre-processing folder " + element.getAbsolutePath(), e);
				}
			}
		}
	}

	private File preProcessZipFile(File zipfile) throws ZipException, IOException {
		// get path and create folder for extracting ZIP file
		String absolutePath = zipfile.getAbsolutePath();
		String outputFolder = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
		outputFolder = outputFolder.concat(File.separator).concat(UUID.randomUUID().toString());
		File outDir = new File(outputFolder);
		outDir.mkdir();
		tmpExtractFolders.add(outDir);
		
		// read ZIP & extract entries
		ZipFile zipFile = new ZipFile(zipfile);
		try {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if(entry.isDirectory()) {
					// case: entry is directory -> create new directory
					String entryName = outputFolder.concat(File.separator).concat(entry.getName());
		            entryName = entryName.replace('/', File.separatorChar);
		            entryName = entryName.replace('\\', File.separatorChar);
		            LOG.debug("Create directory " + entryName);
		            File newFolder = new File(entryName);
	                newFolder.mkdirs();
				} else {
					// case: entry is file -> extract
					LOG.debug("Extract file " + entry.getName());
					extractEntry(entry, zipFile.getInputStream(entry), outputFolder);
				}
			}
		} catch (Exception e) {
			LOG.error("Exception while extracting ZIP file " + absolutePath, e);
		} finally {
			zipFile.close();
		}
		return outDir;
	}

	public void processInputFiles(File[] files) {
		preProcessInputFiles(Arrays.asList(files));
		
		for (File file : allFiles) {
			currentFile = file;
			processInputFile(file);
		}
		// tell the observers that processing all files has finished
		setChanged();
		notifyObservers(new ParsingAllFilesHasFinishedEvent(this, allFiles));
		
		// housekeeping
		FileCleaner fileCleaner = new FileCleaner();
		for (File folder : tmpExtractFolders) {
			try {
				fileCleaner.removeFolder(folder.getAbsolutePath());
			} catch (Exception e) {
				LOG.error("Exception while removing folder " + folder.getAbsolutePath(), e);
			}
		}
	}

	public void processInputFile(File file) {
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

				// TODO: should we do this calculation after each line?
				checkProcessedLines(processedLines, file.getAbsolutePath());
			}
			inputStream.close();

			LOG.info("Processed " + totalProcessedLines + " lines.");

			// call the after hook
			processor.afterSingleFileParsed(file);
		} catch (Exception e) {
			LOG.error("Exception while processing file " + file.getAbsolutePath(), e);
		} finally {
			processedFiles++;
			// when processed a file, notify the observers that we are done with the file
			setChanged();
			notifyObservers(new ParsingFileHasFinishedEvent(this, file.getName()));
		}
	}

	private void extractEntry(ZipEntry entry, InputStream is, String targetDir) throws IOException {
		String extractedFile = targetDir.concat(File.separator).concat(entry.getName());
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(extractedFile);
			final byte[] buf = new byte[8192];
			int length;

			while ((length = is.read(buf, 0, buf.length)) >= 0)
				fos.write(buf, 0, length);

		} catch (Exception e) {
			LOG.error("Exception while extracting entry " + entry.getName(), e);
		} finally {
			fos.close();
		}
	}

	public void reset() {
		allFiles = new ArrayList<File>();
		tmpExtractFolders = new ArrayList<File>();
		fileLinesMap = new HashMap<String, Integer>();
		totalLinesAmount = 0l;
		totalProcessedLines = 0l;
		processedFiles = 0;
	}
}
