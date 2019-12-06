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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.ogema.core.timeseries.InterpolationMode;

public class ImportConfigurationBuilder {

	private CSVFormat csvFormat = ImportConfiguration.getDefaultFormat();
	private String dateTimeFormat = null;
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	private InterpolationMode mode = InterpolationMode.LINEAR;
	private char decimalSeparator = '.';
	private boolean parseEagerly = false;
	private int timeIndex = 0;
	private int valueIndex = 1;
	private List<Integer> valuesIndices = null;
	private Long startTime = null;
	private Long interval = null;
	private float factor = 1;
	private float addend = 0;
	
	private ImportConfigurationBuilder() {}
	
	public static ImportConfigurationBuilder newInstance() {
		return new ImportConfigurationBuilder();
	}

	public ImportConfiguration build() {
		return new ImportConfiguration(csvFormat, dateTimeFormat, timeUnit, mode, decimalSeparator, parseEagerly,
				timeIndex, valueIndex, startTime, interval, factor, addend, valuesIndices);
	}

	/**
	 * Default: ';'
	 * @param delimiter
	 * @return
	 */
	public ImportConfigurationBuilder setDelimiter(char delimiter) {
		csvFormat = csvFormat.withDelimiter(delimiter);
		return this;
	}
	
	/**
	 * Default: {@link CSVFormat#DEFAULT} with delimiter ';'
	 * @param csvFormat
	 * @return
	 */
	public ImportConfigurationBuilder setCsvFormat(CSVFormat csvFormat) {
		this.csvFormat = Objects.requireNonNull(csvFormat);
		return this;
	}

	/**
	 * Default: null, which means that the time is expected as long value (millis since Jan 1st 1970).
	 * @param dateTimeFormat
	 * @return
	 */
	public ImportConfigurationBuilder setDateTimeFormat(SimpleDateFormat dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat == null ? null : dateTimeFormat.toPattern();
		return this;
	}

	/**
	 * Only relevant if {@link #setDateTimeFormat(SimpleDateFormat) dateTimeFormat} is not set.
	 * Default is {@link TimeUnit#MILLISECONDS}.
	 * @param timeUnit
	 * @return
	 */
	public ImportConfigurationBuilder setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = Objects.requireNonNull(timeUnit);
		return this;
	}
	
	/**
	 * Set the interpolation mode. Default is {@link InterpolationMode#LINEAR}
	 * @param mode
	 * @return
	 */
	public ImportConfigurationBuilder setInterpolationMode(InterpolationMode mode) {
		this.mode = mode;
		return this;
	}
	
	/**
	 * Specify a decimal separator for numerical values. Default: '.'
	 * @param decimalSeparator
	 * @return
	 */
	public ImportConfigurationBuilder setDecimalSeparator(char decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
		return this;
	}

	/**
	 * Parse values eagerly on construction of the timeseries. If set to true, values 
	 * will be parsed immediately upon creation of the timeseries and will be kept in memory
	 * indefinitely, as long as a reference to the timeseries is held.   
	 * Default is false.
	 * @param parseEagerly
	 * @return
	 */
	public ImportConfigurationBuilder setParseEagerly(boolean parseEagerly) {
		this.parseEagerly = parseEagerly;
		return this;
	}
	
	/**
	 * Set the column index (0-based) of the time and value column in the CSV file.
	 * If the CSV file at hand does not contain a column for the timestamps use {@link #setTimesteps(int, long, long)}
	 * instead.
	 * @param timeIndex
	 * 		default: 0
	 * @param valueIndex
	 * 		default: 1
	 * @return
	 */
	public ImportConfigurationBuilder setTimeAndValueIndices(int timeIndex, int valueIndex) {
		if (timeIndex == valueIndex)
			throw new IllegalArgumentException("Time and value index must not be equal: " + timeIndex);
		if (timeIndex < 0 || valueIndex < 0)
			throw new IllegalArgumentException("Value index must be positive: time: " + timeIndex + ", value: " + valueIndex);
		this.timeIndex = timeIndex;
		this.valueIndex = valueIndex;
		this.valuesIndices = Collections.singletonList(valueIndex);
		this.startTime = null;
		this.interval = null;
		return this;
	}
	
	/**
	 * 
	 * @param timeIndex
	 * @param valueIndex
	 * @param startTime
	 * @return
	 */
	public ImportConfigurationBuilder setTimeAndValueIndices(int timeIndex, int valueIndex, long startTime) {
		if (timeIndex == valueIndex)
			throw new IllegalArgumentException("Time and value index must not be equal: " + timeIndex);
		if (timeIndex < 0 || valueIndex < 0)
			throw new IllegalArgumentException("Value index must be positive: time: " + timeIndex + ", value: " + valueIndex);
		this.timeIndex = timeIndex;
		this.valueIndex = valueIndex;
		this.valuesIndices = Collections.singletonList(valueIndex);
		this.startTime = startTime;
		this.interval = null;
		return this;
	}
	
	/**
	 * Use this method if the CSV file at hand does not provide a column for the timestamps and 
	 * the time interval between two data points is to be considered as constant. Otherwise use
	 * {@link #setTimeAndValueIndices(int, int)} instead.
	 * @param valueIndex
	 * 		the index of the value column
	 * @param startTime
	 * 		timestamp of the first data point (in millis since epoch, 1st Jan 1970)
	 * @param interval
	 * 		the time interval between adjacent data points, in milli seconds
	 * @return
	 */
	public ImportConfigurationBuilder setTimesteps(int valueIndex, long startTime, long interval) {
		if (valueIndex < 0)
			throw new IllegalArgumentException("Value index must be positive: " + valueIndex);
		this.valueIndex = valueIndex;
		this.valuesIndices = Collections.singletonList(valueIndex);
		this.startTime = startTime;
		this.interval = interval;
		this.timeIndex = -1;
		return this;
	}
	
	/**
	 * Like {@link #setTimesteps(int, long, long)} but allows for multiple 
	 * timeseries/columns
	 * @param valueIndices
	 * @param startTime
	 * @param interval
	 * @return
	 */
	public ImportConfigurationBuilder setTimesteps(List<Integer> valueIndices, long startTime, long interval) {
		this.valuesIndices = valueIndices == null ? null : Collections.unmodifiableList(new ArrayList<Integer>(valueIndices));
		if (valueIndices != null && valueIndices.size() == 1)
			valueIndex = valueIndices.get(0);
		this.startTime = startTime;
		this.interval = interval;
		this.timeIndex = -1;
		return this;
	}
	
	/**
	 * Parse more than one timeseries at a time, from a CSV file with multiple value columns. 
	 * @param timeIndex
	 * @param valueIndices
	 * 		may be null
	 * @return
	 */
	public ImportConfigurationBuilder setMultiValueIndices(int timeIndex, List<Integer> valueIndices) {
		this.timeIndex = timeIndex;
		this.valuesIndices = valueIndices == null ? null : Collections.unmodifiableList(new ArrayList<Integer>(valueIndices));
		if (valueIndices != null && valueIndices.size() == 1)
			valueIndex = valueIndices.get(0);
		
		return this;
	}
	
	/**
	 * Parse more than one timeseries at a time, from a CSV file with multiple value columns. 
	 * @param timeIndex
	 * @param valueIndices
	 * 		may be null
	 * @param startTime
	 * @return
	 */
	public ImportConfigurationBuilder setMultiValueIndices(int timeIndex, List<Integer> valueIndices, long startTime) {
		this.timeIndex = timeIndex;
		this.valuesIndices = valueIndices == null ? null : Collections.unmodifiableList(new ArrayList<Integer>(valueIndices));
		if (valueIndices != null && valueIndices.size() == 1)
			valueIndex = valueIndices.get(0);
		this.startTime = startTime;
		this.interval = null;		
		return this;
	}
	
	/**
	 * Multiply file values by a factor. Final value is <code>value * factor + addend</code> 
	 * @param factor
	 * @return
	 */
	public ImportConfigurationBuilder setFactor(float factor) {
		this.factor = factor;
		return this;
	}
	
	/**
	 * Add an offset to the file values. Final value is <code>value * factor + addend</code>
	 * @param addend
	 * @return
	 */
	public ImportConfigurationBuilder setAddend(float addend) {
		this.addend = addend;
		return this;
	}
	
}
