package de.ibm.issw.requestmetrics.gui.statistics.comparator;

import java.util.Comparator;

import de.ibm.issw.requestmetrics.gui.statistics.ChildNodeStatisticsEntry;

public class NumberOfExecutionsComparator implements Comparator<ChildNodeStatisticsEntry> {

	@Override
	public int compare(ChildNodeStatisticsEntry o1, ChildNodeStatisticsEntry o2) {
		Long result = o1.getNumberOfExecutions() - o2.getNumberOfExecutions();
		return result.intValue();
	}

}
