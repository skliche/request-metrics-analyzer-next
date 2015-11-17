package de.ibm.issw.requestmetrics.gui.comparator;

import java.util.Comparator;

import de.ibm.issw.requestmetrics.model.RmRootCase;

public class ElapsedTimeComparator implements Comparator<RmRootCase> {

	@Override
	public int compare(RmRootCase o1, RmRootCase o2) {
		Long result = o1.getRmNode().getData().getElapsedTime() - o2.getRmNode().getData().getElapsedTime();
		return result.intValue();
	}

}
