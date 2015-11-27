package org.ogema.tools.resource.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Time and Date utils 
 */
public class TimeUtils {

	public static String getDateString(long time) {
		return getDateString(time, "yyyy-MM-dd");
	}

	public static String getDateAndTimeString(long time) {
		return getDateString(time, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 
	 * @param time
	 * @param dateFormat
	 * 		use: 'yyyy' for year, 'MM' for month of year, 'dd' for day of month, 
	 * 			'HH' for hour of day, 'mm' for minute of hour, 'ss' for second of minute
	 * @return
	 */
	public static String getDateString(long time, String dateFormat) {
		Date date = new Date(time);
		SimpleDateFormat sd = new SimpleDateFormat(dateFormat);
		return sd.format(date);
	}

}
