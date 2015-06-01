package de.ibm.issw.requestmetrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RmProcessor {
	private static final Logger LOG = Logger.getLogger(RmProcessor.class.getName());

	//TODO: remove constant
	private static final String REQ_MET = "PMRM0003I";
	private static final String REGEX = "([^:]*):\\[([^\\]]*)\\] (\\w+) PmiRmArmWrapp I\\s+PMRM0003I:\\s+parent:ver=(\\d),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+)\\s-\\scurrent:ver=([^,]+),ip=([^,]+),time=([^,]+),pid=([^,]+),reqid=([^,]+),event=(\\w+) type=(\\w+) detail=(.*) elapsed=(\\w+)";	
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	private Map<String, List<RMNode>> parentNodesMap = new Hashtable<String, List<RMNode>>();
	private Map<String, RMNode> useCaseRootList = new Hashtable<String, RMNode>();
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
			////ver=1,ip=127.0.0.1,time=1432493583679,pid=436,reqid=132797,event=1
			String currentCmp ="ver="+ currentVersion + ",ip=" + currentIp + ",time=" + currentTimestamp + ",pid=" + currentPid + ",reqid=" + currentRequestId + ",event=" + currentEvent; 
			String parentCmp = "ver="+ parentVersion + ",ip=" + parentIp + ",time=" + parentTimestamp + ",pid=" + parentPid + ",reqid=" + parentRequestId + ",event=" + parentEvent;
			
			RMRecord recRM = new RMRecord(threadId, currentCmp, parentCmp, currentType, currentDetail, currentElapsed, time, date);
			
		}
		
		if (line.length() > 0) {
			if (line.charAt(0) != "[".charAt(0)) {
				int pos1 = line.indexOf(":");
				if (pos1 > 0) {
					line = line.substring(pos1 + 1);
				}
			}
			if (line.charAt(0) == "[".charAt(0)) {
				int posMsg = -1;
				if (posMsg == -1) {
					Matcher matcher = Pattern.compile(" [A-Z] ").matcher(line);
					if (matcher.find()) {
						posMsg = matcher.end();
					} else {
						posMsg = 50;
					}
				}
				String lineSysInfo = line.substring(0, posMsg);

				String lineDateTime = lineSysInfo.substring(1, lineSysInfo.indexOf("]"));

				lineSysInfo = lineSysInfo.substring(lineSysInfo.indexOf("]") + 1);
				StringTokenizer st = new StringTokenizer(lineDateTime, " ");
				String lineDate = st.nextToken();
				if (lineDate.length() != 8) {
					StringTokenizer locST = new StringTokenizer(lineDate, "/");
					String dateLine = "";
					do {
						String token = locST.nextToken();
						if (token.length() == 1)
							token = "0" + token;
						dateLine = dateLine + token + "/";
					} while (locST.hasMoreTokens());
					lineDate = dateLine.substring(0, 8);
				}

				String lineTime = st.nextToken();

				String lineZone = st.nextToken();

				st = new StringTokenizer(lineSysInfo, " ");
				String lineThreadId = st.nextToken();

				String lineComponent = st.nextToken();

				String lineMsgType = st.nextToken();

				String lineMsg = line.substring(posMsg).trim();

				st = new StringTokenizer(lineMsg, " ");
				if (st.hasMoreTokens()) {
					String lineMsgNum = st.nextToken();
					if (lineMsgNum.contains(":")) {
						lineMsgNum = lineMsgNum.substring(0, lineMsgNum.indexOf(":"));
					}

					if (lineMsgNum.equalsIgnoreCase(REQ_MET)) {
						int relPos = 0;
						this.processedLines++;
						String newRMMsg = lineMsg.substring(lineMsg.indexOf(st.nextToken()));

						relPos = newRMMsg.indexOf("current:") - 3;
						String parentCmp = newRMMsg.substring("parent:".length(), relPos);

						newRMMsg = newRMMsg.substring(relPos + 3);
						relPos = newRMMsg.indexOf("type=") - 1;
						String currentCmp = newRMMsg.substring("current:".length(), relPos);

						newRMMsg = newRMMsg.substring(relPos + 1);
						relPos = newRMMsg.indexOf("detail=") - 1;
						String typeCmp = newRMMsg.substring("type=".length(), relPos);

						newRMMsg = newRMMsg.substring(relPos + 1);
						relPos = newRMMsg.indexOf("elapsed=") - 1;
						String detailCmp = newRMMsg.substring("detail=".length(), relPos);

						newRMMsg = newRMMsg.substring(relPos + 1);
						String elapsedTime = newRMMsg.substring("elapsed=".length());

						RMRecord recRM = new RMRecord(lineThreadId, currentCmp, parentCmp, typeCmp, detailCmp, elapsedTime, lineTime, lineDate);

						RMNode rmNode = new RMNode(recRM);

						RMRecord dummyRMRec = new RMRecord(lineThreadId, parentCmp, "", "", "", "", "", "");
						String parentRMRecId = dummyRMRec.getRmRecId();

						if (!currentCmp.equals(parentCmp)) {
							List<RMNode> parentContaineesList;
							if (parentNodesMap.containsKey(parentRMRecId)) {
								parentContaineesList = parentNodesMap.get(parentRMRecId);
							} else {
								parentContaineesList = new ArrayList<RMNode>();
								parentNodesMap.put(parentRMRecId, parentContaineesList);
							}
							parentContaineesList.add(rmNode);
						} else {
							Long elapsedTimeLong = new Long(elapsedTime);
							if (elapsedTimeBorder == null || elapsedTimeLong > elapsedTimeBorder) {
								this.rmCases++;
								useCaseRootList.put(recRM.getRmRecId(), rmNode);
							}
						}

					}

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
