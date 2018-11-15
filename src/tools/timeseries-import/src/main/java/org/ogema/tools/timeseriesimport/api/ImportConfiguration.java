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
	
	ImportConfiguration(CSVFormat csvFormat, String dateTimeFormat, TimeUnit timeUnit, InterpolationMode mode, char decimalSeparator) {
		this.csvFormat = csvFormat;
		this.dateTimeFormat = dateTimeFormat;
		this.timeUnit = timeUnit;
		this.mode = mode;
		this.decimalSeparator = decimalSeparator;
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
	
}
