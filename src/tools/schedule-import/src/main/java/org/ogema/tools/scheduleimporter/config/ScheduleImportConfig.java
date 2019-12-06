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

import java.net.URL;
import java.util.Calendar;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Configuration;

public interface ScheduleImportConfig extends Configuration {

	/**
	 * The {@link URL} of a CSV file.
	 * Examples:
	 * <ul>
	 * 	<li>"file:imports/schedule.csv" for a file in the "imports"-folder below the rundir.
	 * </ul>
	 * @return
	 */
	StringResource csvFile();
	
	/**
	 * A reference
	 * @return
	 */
	SingleValueResource targetParent();
	
	/**
	 * Use alternatively to {@link #targetParent()} if the CSV file 
	 * contains multiple time series which shall be read into multiple
	 * schedules.
	 * @return
	 */
	ResourceList<SingleValueResource> targetParents();
	
	/**
	 * Use alternatively to {@link #scheduleRelativePath()} if the CSV file 
	 * contains multiple time series which shall be read into multiple
	 * schedules, and the schedule relative path is different for those.
	 * @return
	 */
	StringArrayResource scheduleRelativePaths();
	
	/**
	 * Path relative to {@link #targetParent()} for the
	 * actual schedule.
	 * @return
	 */
	StringResource scheduleRelativePath();

	/**
	 * If true, the start of the imported timeseries is moved to the
	 * current framework time, or if {@link #alignmentType()} is set, 
	 * to the start of the current aligned time interval.
	 * @return
	 */
	BooleanResource moveStartToCurrentFrameworkTime();

	/**
	 * Only evaluated of {@link #moveStartToCurrentFrameworkTime()}
	 * is active and true. 
	 * Allowed values: static {@link Calendar} types, such as {@link Calendar#YEAR},
	 * {@link Calendar#DAY_OF_YEAR} or {@link Calendar#HOUR_OF_DAY}.
	 * @return
	 */
	IntegerResource alignmentType();
	
	/**
	 * If active and true, then the CSV timeseries will be imported repeatedly, to create a periodic schedule
	 * Note: if this is set, then also importHorizon is required. 
	 * @return
	 */
	BooleanResource periodicSchedule();
	
	/**
	 * Determines when to update a schedule that receives data periodically
	 * @return
	 */
	TimeResource importHorizon();
	
	CsvFormatConfig csvFormat();
	
}
