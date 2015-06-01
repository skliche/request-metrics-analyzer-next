package de.ibm.issw.requestmetrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RmProcessor {
	private static final Logger LOG = Logger.getLogger(RmProcessor.class.getName());

	private static final String REGEX = "([^:]*):\\[([^\\]]*)\\] (\\w+) PmiRmArmWrapp I\\s+PMRM0003I:\\s+parent:ver=(\\d),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+)\\s-\\scurrent:ver=([^,]+),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+) type=(\\w+) detail=(.*) elapsed=(\\w+)";	
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private Map<String, List<RMNode>> parentNodesMap = new HashMap<String, List<RMNode>>();
	private Map<String, RMNode> useCaseRootList = new HashMap<String, RMNode>();

	private Long elapsedTimeBorder;
	private Long processedLines;
	private Long rmCases;
	
	public Map<String, RMNode> processInputFile(String inputFileName) {
		this.processedLines = 0l;
		this.rmCases = 0l;
		
		try {
			FileReader inputFileReader = new FileReader(inputFileName);
			BufferedReader inputStream = new BufferedReader(inputFileReader);
			String line = null;
			while ((line = inputStream.readLine()) != null) {
				processSingleLine(line);
				processedLines++;
			}

			inputStream.close();
			LOG.info("Processed " + this.processedLines + " lines of PMRM0003I type.");
			LOG.info("Number of testCase tables found: " + this.rmCases);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return useCaseRootList;
	}

	private void processSingleLine(String line) {
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
			String currentType = logMatcher.group(16);
			String currentDetail = logMatcher.group(17);
			String currentElapsed = logMatcher.group(18);
			
			//TODO: further optimize this
			String time = timestamp.split(" ")[1];
			String date = timestamp.split(" ")[0];
			//ver=1,ip=127.0.0.1,time=1432493583679,pid=436,reqid=132797,event=1
			String currentCmp ="ver="+ currentVersion + ",ip=" + currentIp + ",time=" + currentTimestamp + ",pid=" + currentPid + ",reqid=" + currentRequestId + ",event=" + currentEvent; 
			String parentCmp = "ver="+ parentVersion + ",ip=" + parentIp + ",time=" + parentTimestamp + ",pid=" + parentPid + ",reqid=" + parentRequestId + ",event=" + parentEvent;
			
			// create the record from the log line
			RMRecord recRM = new RMRecord(logFileName, threadId, currentCmp, parentCmp, currentType, currentDetail, currentElapsed, time, date);
			// add the record to a node
			RMNode rmNode = new RMNode(recRM);

			if (currentCmp.equals(parentCmp)) {
				Long elapsedTimeLong = new Long(currentElapsed);
				if (elapsedTimeBorder == null || elapsedTimeLong > elapsedTimeBorder) {
					this.rmCases++;
					useCaseRootList.put(recRM.getRmRecId(), rmNode);
				}
			} else {
				RMRecord dummyRMRec = new RMRecord(logFileName, threadId, parentCmp, "", "", "", "", "", "");
				if (parentNodesMap.containsKey(dummyRMRec.getRmRecId())) {
					List<RMNode> parentContaineesList = parentNodesMap.get(dummyRMRec.getRmRecId());
					parentContaineesList.add(rmNode);
				} else {
					List<RMNode> parentContaineesList = new ArrayList<RMNode>();
					parentNodesMap.put(dummyRMRec.getRmRecId(), parentContaineesList);
					parentContaineesList.add(rmNode);
				}
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

	public Long getRmCases() {
		return rmCases;
	}

	public Map<String, RMNode> getUseCaseRootList() {
		return useCaseRootList;
	}
}
