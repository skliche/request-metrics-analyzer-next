package de.ibm.issw.requestmetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RmProcessor {
	private static final Logger LOG = Logger.getLogger(RmProcessor.class.getName());
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy H:m:s:S z", Locale.US);

	private static final String REGEX = "([^:]*):\\[([^\\]]*)\\] (\\w+) PmiRmArmWrapp I\\s+PMRM0003I:\\s+parent:ver=(\\d),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+)\\s-\\scurrent:ver=([^,]+),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(.*) type=(.*) detail=(.*) elapsed=(\\w+)";	
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private Map<String, List<RMNode>> parentNodesMap = new HashMap<String, List<RMNode>>();
	private Map<String, RMNode> useCaseRootList = new HashMap<String, RMNode>();
	private List<RmRootCase> rootCases = new ArrayList<RmRootCase>();
	
	private Long elapsedTimeBorder = 0l;
	private Long processedLines;
	
	public void processInputFile(String inputFileName) {
		processInputFile(new File(inputFileName));
	}
		
	public void processInputFile (File file) {
		this.processedLines = 0l;
		
		try {
			FileReader inputFileReader = new FileReader(file);
			BufferedReader inputStream = new BufferedReader(inputFileReader);
			String line = null;
			while ((line = inputStream.readLine()) != null) {
				RMRecord record = processSingleLine(line);
				if(record != null) {
					// process the record 
					addRmRecordToDataset(record);
				}
				processedLines++;
			}

			inputStream.close();
			LOG.info("Processed " + this.processedLines + " lines of PMRM0003I type.");
			LOG.info("Number of testCase tables found: " + getUseCases().size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * processes a single line and creates the java record representation out of it
	 * @param line the line
	 * @return RMRecord the record that has been generated or null if the line is not a valid log statement
	 * 
	 */
	private RMRecord processSingleLine(String line) {
		RMRecord record = null;
		
		// parse the log using a REGEX
		Matcher logMatcher = PATTERN.matcher(line);
		if(logMatcher.matches()) {
			String logFileName = logMatcher.group(1);
			String timestamp = logMatcher.group(2);
			String threadId = logMatcher.group(3);
			
			String parentVersion = logMatcher.group(4);
			String parentIp = logMatcher.group(5);
			String parentTimestamp = logMatcher.group(6);
			String parentPid = logMatcher.group(7);
			String parentRequestId = logMatcher.group(8);
			String parentEvent = logMatcher.group(9);
			
			String currentVersion = logMatcher.group(10);
			String currentIp = logMatcher.group(11);
			String currentTimestamp = logMatcher.group(12);
			String currentPid = logMatcher.group(13);
			String currentRequestId = logMatcher.group(14);
			String currentEvent = logMatcher.group(15);
			String type = logMatcher.group(16);
			String detail = logMatcher.group(17);
			Long currentElapsed = Long.parseLong(logMatcher.group(18));
			
			Date recordDate = null; //[5/28/15 11:10:39:507 EDT]
			try {
				recordDate = sdf.parse(timestamp);
			} catch (ParseException e) {
				e.printStackTrace();
				LOG.severe("could not parse the log timestamp: " + timestamp);
				
			}
			
			RMComponent currentCmp = new RMComponent(currentVersion, currentIp, currentTimestamp, currentPid, currentRequestId, currentEvent);
			RMComponent parentCmp = new RMComponent(parentVersion, parentIp, parentTimestamp, parentPid, parentRequestId, parentEvent);
			
			// create the record from the log line
			record = new RMRecord(logFileName, recordDate, threadId, 
									currentCmp, parentCmp, 
									type, detail, 
									currentElapsed);
		}
		return record;
	}

	private void addRmRecordToDataset(RMRecord rmRecord) {
		// add the record to a node - we always create a node
		RMNode rmNode = new RMNode(rmRecord);

		// if the current record-id is the same as the parent-id, then we have a root-record
		if (rmRecord.getCurrentCmp().getReqid().equals(rmRecord.getParentCmp().getReqid())) {
			// filter by time
			if (rmRecord.getElapsedTime() >= elapsedTimeBorder) {
				// we mark the record as root record and put it in the list of root-records
				useCaseRootList.put(rmRecord.getRmRecId(), rmNode);
				
				RmRootCase rootCase = new RmRootCase(rmRecord);
				rootCases.add(rootCase);
			}
		// otherwise the the current record is a child-record
		} else {
			List<RMNode> parentContaineesList = parentNodesMap.get(RMRecord.generateRmRecId(rmRecord.getThreadId(), rmRecord.getParentCmp()));
			if (parentContaineesList != null) {
				parentContaineesList.add(rmNode);
			} else {
				parentContaineesList = new ArrayList<RMNode>();
				parentNodesMap.put(RMRecord.generateRmRecId(rmRecord.getThreadId(), rmRecord.getParentCmp()), parentContaineesList);
				parentContaineesList.add(rmNode);
			}
		}
	}

	public Map<String, List<RMNode>> getParentNodesMap() {
		return this.parentNodesMap;
	}

	public void setElapsedTimeBorder(Long elapsedTimeBorder) {
		this.elapsedTimeBorder = elapsedTimeBorder;
	}

	public Long getProcessedLines() {
		return processedLines;
	}

	public Map<String, RMNode> getUseCaseRootList() {
		return useCaseRootList;
	}

	public List<RMNode> getChildrenByParentNodeId(String rmRecId) {
		List<RMNode> result = parentNodesMap.get(rmRecId);
		if(result == null) {
			result = new ArrayList<RMNode>();
		}
		return result;
	}

	public List<RmRootCase> getUseCases() {
		return this.rootCases;
	}

	public void reset() {
		this.useCaseRootList.clear();
		this.rootCases.clear();
		this.parentNodesMap.clear();
	}
}
