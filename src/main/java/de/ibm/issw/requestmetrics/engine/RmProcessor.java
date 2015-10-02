package de.ibm.issw.requestmetrics.engine;

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
import java.util.Observable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ibm.issw.requestmetrics.engine.events.ParsingHasFinishedEvent;
import de.ibm.issw.requestmetrics.model.DummyRmRootCase;
import de.ibm.issw.requestmetrics.model.RMComponent;
import de.ibm.issw.requestmetrics.model.RMNode;
import de.ibm.issw.requestmetrics.model.RMRecord;
import de.ibm.issw.requestmetrics.model.RmRootCase;
import de.ibm.issw.requestmetrics.util.StringPool;

public class RmProcessor extends Observable{
	private static final Logger LOG = Logger.getLogger(RmProcessor.class.getName());
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy H:m:s:S z", Locale.US);

	private static final String REGEX = "([^:]*):\\[([^\\]]*)\\] (\\w+) PmiRmArmWrapp I\\s+PMRM0003I:\\s+parent:ver=(\\d),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+)\\s-\\scurrent:ver=([^,]+),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(.*) type=(.*) detail=(.*) elapsed=(\\w+)";	
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private final Map<Long, List<RMNode>> parentNodes = new HashMap<Long, List<RMNode>>();
	private final List<RmRootCase> rootCases = new ArrayList<RmRootCase>();
	
	private Long elapsedTimeBorder = 0l;
	private Long processedLines;
	
	public void processInputFiles(File[] files) {
		for (File file : files) {
			processInputFile (file);
		}
	}
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
				final RMRecord record = processSingleLine(line);
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
		} finally {
			//notify the observers that we are done
			setChanged();
			notifyObservers(new ParsingHasFinishedEvent(this, file));
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
		final Matcher logMatcher = PATTERN.matcher(line);
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
		final RMNode rmNode = new RMNode(rmRecord);

		// if the current record-id is the same as the parent-id, then we have a root-record
		if (rmRecord.isRootCase()) {
			// filter by time
			if (rmRecord.getElapsedTime() >= elapsedTimeBorder) {
				// we mark the record as root record and put it in the list of root-records
				final RmRootCase rootCase = new RmRootCase(rmNode);
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

	public void setElapsedTimeBorder(Long elapsedTimeBorder) {
		this.elapsedTimeBorder = elapsedTimeBorder;
	}

	public Long getProcessedLines() {
		return processedLines;
	}

	/**
	 * Find all nodes by the rm record id. The method looks in the parent node index.
	 *  
	 * @param nodeId the id of the rm record
	 * @return a list of RMNodes that have a reference to the supplied node id
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<RMNode> findByRmRecId(long nodeId) {
		if(LOG.isLoggable(Level.INFO)) {
			LOG.log(Level.INFO, "findByNodeId with record id " + nodeId);
		}
		List<RMNode> result = parentNodes.get(nodeId);
		if(result == null) {
			result = Collections.EMPTY_LIST;
			LOG.warning("findByRmRecId was called with an invalid node id");
		}
		return result;
	}
	
	/**
	 * Find all events that have no root event.
	 * @return a list of RM node that are not a root event and not referenced by a root event
	 * 
	 */
	public List<RMNode> findUnreferencedEvents() {
		LOG.info("looking for dirty events...");
		List<RMNode> result = new ArrayList<RMNode>();

		final Map<Long, Boolean> parentNodeHash = new HashMap<Long, Boolean>();
		final Map<Long, Boolean> currentNodeHash = new HashMap<Long, Boolean>();
		
		// build up a hash of all parent nodes (use the use cases for that)
		for (RmRootCase rootCase : this.rootCases) {
			parentNodeHash.put(rootCase.getRmNode().getData().getParentCmp().getReqid(), true);
		}
		
		// build up a hash of all current nodes
		for (Entry<Long, List<RMNode>> item : this.parentNodes.entrySet()) {
			List<RMNode> nodes = item.getValue();
			for (RMNode node : nodes) {
				currentNodeHash.put(node.getData().getCurrentCmp().getReqid(), true);
			}
		}
		
		// now do some set theory and by that filter
		// TODO: we are currently destroying the parentNodeKeys which is shit
		Set<Long> parentNodeKeys = parentNodes.keySet();
		parentNodeKeys.removeAll(parentNodeHash.keySet());
		parentNodeKeys.removeAll(currentNodeHash.keySet());
		
		for (Long key : parentNodeKeys) {
			rootCases.add(new DummyRmRootCase(key));
		}
		LOG.info("found " + parentNodeKeys.size() + " dirty events: " + parentNodeKeys);
		return result;
	}

	public List<RmRootCase> getRootCases() {
		return rootCases;
	}

	/**
	 * resets the processor in order to be able to process a new file
	 */
	public void reset() {
		rootCases.clear();
		parentNodes.clear();
	}
}
