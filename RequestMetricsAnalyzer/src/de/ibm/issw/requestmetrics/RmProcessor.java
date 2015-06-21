package de.ibm.issw.requestmetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ibm.issw.requestmetrics.util.StringPool;

public class RmProcessor {
	private static final Logger LOG = Logger.getLogger(RmProcessor.class.getName());
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy H:m:s:S z", Locale.US);

	private static final String REGEX = "([^:]*):\\[([^\\]]*)\\] (\\w+) PmiRmArmWrapp I\\s+PMRM0003I:\\s+parent:ver=(\\d),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+)\\s-\\scurrent:ver=([^,]+),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(.*) type=(.*) detail=(.*) elapsed=(\\w+)";	
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private final Map<Long, List<RMNode>> parentNodes = new HashMap<Long, List<RMNode>>();
	private final List<RmRootCase> rootCases = new ArrayList<RmRootCase>();
	
	private Long elapsedTimeBorder = 0l;
	private Long processedLines;
	
	public void processInputFile(String inputFileName) {
		processInputFile(new File(inputFileName));
	}
		
	public void processInputFile (File file) {
		this.processedLines = 0l;
		
		try {
			final FileReader inputFileReader = new FileReader(file);
			final BufferedReader inputStream = new BufferedReader(inputFileReader);
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
			LOG.info("Number of testCase tables found: " + getRootCases().size());
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
		
		// parse the log using a REGEX, Strings are processed by a string pool
		Matcher logMatcher = PATTERN.matcher(line);
		if(logMatcher.matches()) {
			final String logFileName = StringPool.intern(logMatcher.group(1));
			final String timestamp = StringPool.intern(logMatcher.group(2));
			final String threadId = StringPool.intern(logMatcher.group(3));
			
			final Integer parentVersion = Integer.parseInt(logMatcher.group(4));
			final String parentIp = StringPool.intern(logMatcher.group(5));
			final Long parentTimestamp = Long.parseLong(logMatcher.group(6));
			final Long parentPid = Long.parseLong(logMatcher.group(7));
			final Long parentRequestId = Long.parseLong(logMatcher.group(8));
			final String parentEvent = StringPool.intern(logMatcher.group(9));
			
			final Integer currentVersion = Integer.parseInt(logMatcher.group(10));
			final String currentIp = StringPool.intern(logMatcher.group(11));
			final Long currentTimestamp = Long.parseLong(logMatcher.group(12));
			final Long currentPid = Long.parseLong(logMatcher.group(13));
			final Long currentRequestId = Long.parseLong(logMatcher.group(14));
			final String currentEvent = StringPool.intern(logMatcher.group(15));
			final String type = StringPool.intern(logMatcher.group(16));
			final String detail = StringPool.intern(logMatcher.group(17));
			final Long currentElapsed = Long.parseLong(logMatcher.group(18));
			
			Date recordDate = null; //[5/28/15 11:10:39:507 EDT]
			try {
				recordDate = sdf.parse(timestamp);
			} catch (ParseException e) {
				e.printStackTrace();
				LOG.severe("could not parse the log timestamp: " + timestamp);
				
			}
			
			final RMComponent currentCmp = new RMComponent(currentVersion, currentIp, currentTimestamp, currentPid, currentRequestId, currentEvent);
			final RMComponent parentCmp = new RMComponent(parentVersion, parentIp, parentTimestamp, parentPid, parentRequestId, parentEvent);
			
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
		if (rmRecord.isRootCase()) {
			// filter by time
			if (rmRecord.getElapsedTime() >= elapsedTimeBorder) {
				// we mark the record as root record and put it in the list of root-records
				RmRootCase rootCase = new RmRootCase(rmNode);
				rootCases.add(rootCase);
			}
		// otherwise the the current record is a child-record
		} else {
			final Long parentNodeId = rmRecord.getParentCmp().getReqid();
			List<RMNode> childNodes = parentNodes.get(parentNodeId);
			if (childNodes != null) {
				childNodes.add(rmNode);
			} else {
				childNodes = new ArrayList<RMNode>();
				childNodes.add(rmNode);
				parentNodes.put(parentNodeId, childNodes);
			}
		}
	}

	public Map<Long, List<RMNode>> getParentNodesMap() {
		return this.parentNodes;
	}

	public void setElapsedTimeBorder(Long elapsedTimeBorder) {
		this.elapsedTimeBorder = elapsedTimeBorder;
	}

	public Long getProcessedLines() {
		return this.processedLines;
	}

	@SuppressWarnings("unchecked")
	public List<RMNode> getChildrenByParentNodeId(Long parentId) {
		List<RMNode> result = this.parentNodes.get(parentId);
		if(result == null)
			result = Collections.EMPTY_LIST;
		return result;
	}

	public List<RmRootCase> getRootCases() {
		return this.rootCases;
	}

	public void reset() {
		this.rootCases.clear();
		this.parentNodes.clear();
	}
}
