package de.ibm.issw.requestmetrics.gui.statistics.comparator;

import java.util.Comparator;

import de.ibm.issw.requestmetrics.gui.statistics.ChildNodeStatisticsEntry;

public class TotalTimeComparator implements Comparator<ChildNodeStatisticsEntry> {

	@Override
	public int compare(ChildNodeStatisticsEntry o1, ChildNodeStatisticsEntry o2) {
		Long result = o1.getTotalTime() - o2.getTotalTime();
		return result.intValue();
	}

}
