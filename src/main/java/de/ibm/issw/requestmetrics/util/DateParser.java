package de.ibm.issw.requestmetrics.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateParser {
	private static final Logger LOG = LoggerFactory.getLogger(DateParser.class);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy H:m:s:S z", Locale.US);
	private static final SimpleDateFormat pluginSdf = new SimpleDateFormat("dd/MMM/yy:HH:mm:ss.S", Locale.US);
	
	public static Date parseTimestamp(final String timestamp) {
		Date recordDate = null; //[5/28/15 11:10:39:507 EDT]
		try {
			recordDate = sdf.parse(timestamp);
		} catch (ParseException e) {
			try {
				recordDate = pluginSdf.parse(timestamp);
			} catch (ParseException e1) {
				LOG.error("could not parse the log timestamp: " + timestamp);
			}
		}
		return recordDate;
	}
}
