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
package org.ogema.tools.scheduleimporter.config;

import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Configuration;

public interface CsvFormatConfig extends Configuration {

	IntegerResource timeIndex();
	IntegerResource valueIndex();
	/**
	 * May be used alternatively to {@link #valueIndex()},
	 * to specify multiple columns to be read. 
	 * The size of the array must match the size of 
	 * {@link ScheduleImportConfig#targetParents()}.
	 * @return
	 */
	IntegerArrayResource valueIndices();
	
	TimeResource startTime();
	TimeResource interval();
	IntegerResource decimalSeparator(); //
	StringResource csvDelimiter();
	StringResource timeFormat();
	
	/**
	 * Multiply the file values by a factor. 
	 * @return
	 */
	FloatResource valueFactor();
	/**
	 * Add a value to the file values. 
	 * @return
	 */
	FloatResource valueAddend();
	
}
