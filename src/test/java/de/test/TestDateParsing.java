package de.test;

import de.ibm.issw.requestmetrics.util.DateParser;

public class TestDateParsing {
	public static void main(String[] args) {
		System.out.println(DateParser.parseTimestamp("06/Jan/2016:12:16:52.70889"));
	}
}
