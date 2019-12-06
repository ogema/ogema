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
package org.ogema.tools.timeseriesimport.api;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.ogema.core.timeseries.InterpolationMode;

/**
 * Retrieve an instance via {@link ImportConfigurationBuilder}.
 */
public class ImportConfiguration {

	private final CSVFormat csvFormat;
	private final String dateTimeFormat;
	private final TimeUnit timeUnit;
	private final InterpolationMode mode;
	private final char decimalSeparator;
	private final boolean parseEagerly;
	private final int timeIndex;
	private final int valueIndex;
	private final float factor;
	private final float addend;
	/*
	 * Only != null if timeIndex < 0
	 */
	private final Long startTime;
	/*
	 * Only != null if timeIndex < 0
	 */
	private final Long interval;
	private final List<Integer> valueIndices;
	
	ImportConfiguration(CSVFormat csvFormat, String dateTimeFormat, TimeUnit timeUnit, InterpolationMode mode, char decimalSeparator, boolean parseEagerly,
			int timeIndex, int valueIndex, Long startTime, Long interval, float factor, float addend, List<Integer> valueIndices) {
		this.csvFormat = csvFormat;
		this.dateTimeFormat = dateTimeFormat;
		this.timeUnit = timeUnit;
		this.mode = mode;
		this.decimalSeparator = decimalSeparator;
		this.parseEagerly = parseEagerly;
		this.timeIndex = timeIndex;
		this.valueIndex = valueIndex;
		this.startTime = startTime;
		this.interval = interval;
		this.factor = factor;
		this.addend = addend;
		this.valueIndices = valueIndices;
	}
	
	public static CSVFormat getDefaultFormat() {
		return CSVFormat.DEFAULT.withDelimiter(';');
	}

	public CSVFormat getCsvFormat() {
		return csvFormat;
	}

	public SimpleDateFormat getDateTimeFormat() {
		return dateTimeFormat == null ? null : new SimpleDateFormat(dateTimeFormat, Locale.ENGLISH);
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	} 
	
	public InterpolationMode getInterpolationMode() {
		return mode;
	}
	
	public char getDecimalSeparator() {
		return decimalSeparator;
	}
	
	public boolean isParseEagerly() {
		return parseEagerly;
	}
	
	public int getTimeIndex() {
		return timeIndex;
	}
	
	public int getValueIndex() {
		return valueIndex;
	}
	
	public Long getStartTime() {
		return startTime;
	}
	
	public Long getInterval() {
		return interval;
	}
	
	public float getFactor() {
		return factor;
	}
	
	public float getAddend() {
		return addend;
	}
	
	public List<Integer> getValueIndices() {
		return valueIndices;
	}
	
}
