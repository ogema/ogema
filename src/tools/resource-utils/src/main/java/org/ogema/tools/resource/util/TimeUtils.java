/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
