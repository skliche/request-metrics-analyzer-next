package de.ibm.issw.requestmetrics.gui.comparator;

import java.util.Comparator;

import de.ibm.issw.requestmetrics.model.RmRootCase;

public class LogTimeStampComparator implements Comparator<RmRootCase> {

	@Override
	public int compare(RmRootCase o1, RmRootCase o2) {
		return o1.getRmNode().getData().getLogTimeStamp().compareTo(o2.getRmNode().getData().getLogTimeStamp());
	}

}
