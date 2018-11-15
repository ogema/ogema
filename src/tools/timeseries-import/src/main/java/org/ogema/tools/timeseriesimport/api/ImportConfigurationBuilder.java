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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.ogema.core.timeseries.InterpolationMode;

public class ImportConfigurationBuilder {

	private CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(';');
	private String dateTimeFormat = null;
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	private InterpolationMode mode = InterpolationMode.LINEAR;
	private char decimalSeparator = '.';
	
	private ImportConfigurationBuilder() {}
	
	public static ImportConfigurationBuilder newInstance() {
		return new ImportConfigurationBuilder();
	}

	public ImportConfiguration build() {
		return new ImportConfiguration(csvFormat, dateTimeFormat, timeUnit, mode, decimalSeparator);
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
	
}
