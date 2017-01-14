package com.ab.selenium.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ab.selenium.test.Logger;

/**
 * Class for time manipulations.
 */
public final class TimeUtils {
	/** How many milliseconds are in one second? */
	private static final Long MILLISECONDS_IN_SECOND = Long.valueOf(1000);

	/**
	 * Private constructor. Currently not used, since all methods are static.
	 */
	private TimeUtils() {

	}

	/**
	 * Convert milliseconds to seconds.
	 * 
	 * @param milliseconds
	 *            time in milliseconds
	 * @return time in seconds
	 */
	@SuppressWarnings("boxing")
	public static Long getTimeInSeconds(final Long milliseconds) {
		return milliseconds / MILLISECONDS_IN_SECOND;
	}

	/**
	 * This function expects String in format yyyy年MM月dd日 and returns date in yyyyMMdd
	 * 
	 * @param date
	 *            is string which should be in format yyyy年MM月dd日
	 * @return Date in format yyyyMMdd
	 */
	public static Date readJapaneseDate(String date) {
		Date itemDate = null;
		DateFormat inputDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
		DateFormat outputDateFormat = new SimpleDateFormat("yyyyMMdd");
		try {
			itemDate = outputDateFormat.parse(outputDateFormat.format(inputDateFormat.parse(date)));
			Logger.logStepResult("Item date is " + itemDate, false);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return itemDate;
	}
}
