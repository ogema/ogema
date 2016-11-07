/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.tools.resource.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Time and Date utils 
 */
public class TimeUtils {
	
	// no need to construct this
	private TimeUtils() {}

	/**
	 * Returns the date in the format "yyyy-MM-dd".
	 * Use {@link #getDateString(long, String)} to specify a 
	 * custom date format instead.
	 * @param time
	 * @return
	 */
	public static String getDateString(long time) {
		return getDateString(time, "yyyy-MM-dd");
	}

	/**
	 * Returns the date in the format "yyyy-MM-dd HH:mm:ss".
	 * Use {@link #getDateString(long, String)} to specify a 
	 * custom date format instead.
	 * @param time
	 * @return
	 */
	public static String getDateAndTimeString(long time) {
		return getDateString(time, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Returns the date in the specified format String, which must conform to the 
	 * Java date format convention.
	 * @param time
	 * @param dateFormat
	 * 		use: 'yyyy' for year, 'MM' for month of year, 'dd' for day of month, 
	 * 			'HH' for hour of day, 'mm' for minute of hour, 'ss' for second of minute
	 * @return
	 * @throws IllegalArgumentException
	 * 		if the given pattern is invalid
	 */
	public static String getDateString(long time, String dateFormat) throws IllegalArgumentException {
		Date date = new Date(time);
		SimpleDateFormat sd = new SimpleDateFormat(dateFormat);
		return sd.format(date);
	}
	
	// TODO helpers for timezones

}
