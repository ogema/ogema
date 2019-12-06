/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.tools.timeseries.v2.tools;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;

import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;

/**
 * Tools for printing time series
 */
public class TimeSeriesDebugging {

	private final static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.appendPattern("yyyy-MM-dd")
			.optionalStart()
				.appendPattern("'T'HH")
				.optionalStart()
					.appendPattern(":mm")
					.optionalStart()
						.appendPattern(":ss")
					.optionalEnd()
				.optionalEnd()
			.optionalEnd()
			.toFormatter(Locale.ENGLISH);
	private final static ZoneId zone = ZoneId.of("Z");
	
	private TimeSeriesDebugging() {}

	public static String printValues(final ReadOnlyTimeSeries ts, final long startTime) {
		return printValues(ts, startTime, Long.MAX_VALUE, 10, false, true, 0);
	}
	
	public static String printTimestamps(
			ReadOnlyTimeSeries ts,
			long startTime,
			long endTime,
			int limit,
			boolean lineBreak,
			boolean format) {
		return TimeSeriesUtils.getValuesAsStream(ts, startTime, endTime)
			.map(sv -> printTimestamp(sv, format))
			.limit(limit)
			.collect(Collectors.joining(lineBreak ? "\n" : ", "));
	}
	
	public static String printValues(
			ReadOnlyTimeSeries ts,
			long startTime,
			long endTime,
			int limit,
			boolean lineBreak,
			boolean format,
			int indent) {
		return printValues(ts, startTime, endTime, limit, lineBreak, format, indent, 1, 0);
	}
	
	public static String printValues(
			ReadOnlyTimeSeries ts,
			long startTime,
			long endTime,
			int limit,
			boolean lineBreak,
			boolean format,
			int indent,
			float factor,
			float offset) {
		final StringBuilder i = new StringBuilder();
		for (int j=0;j<indent; j++)
			i.append(' ');
		final String ind = i.toString();
		final String delimiter = lineBreak ? "\n" + ind : ", ";
		final Iterator<SampledValue> it = ts.iterator(startTime, endTime);
		int cnt = 0;
		final StringBuilder sb = new StringBuilder();
		while (it.hasNext() && cnt++ < limit) {
			if (cnt > 1)
				sb.append(delimiter);
			final SampledValue sv = it.next();
			sb.append(toString(sv, format, factor, offset));
		}
		if (it.hasNext()) {
			final SampledValue last = ts.getPreviousValue(endTime);
			if (cnt > 0)
				sb.append(delimiter).append("...").append(delimiter);
			sb.append(toString(last, format, factor, offset));
		}
		return ind + sb.toString();
	}
	
	private static String toString(final SampledValue sv, final boolean format, final float factor, final float offset) {
		final String time = format ? formatter.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(sv.getTimestamp()), zone).toLocalDateTime()) 
				: String.valueOf(sv.getTimestamp());
		return "[" + time  + ", " + (sv.getValue().getFloatValue() * factor + offset) + ", " + sv.getQuality() + "]";
	}
	
	private static String printTimestamp(final SampledValue sv, final boolean format) {
		return format ? formatter.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(sv.getTimestamp()), zone).toLocalDateTime()) 
				: String.valueOf(sv.getTimestamp());
	} 
	
}
