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
package org.ogema.tools.scheduleimporter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.tools.scheduleimporter.config.CsvFormatConfig;
import org.ogema.tools.scheduleimporter.config.ScheduleImportConfig;
import org.ogema.tools.timeseriesimport.api.ImportConfiguration;
import org.ogema.tools.timeseriesimport.api.ImportConfigurationBuilder;
import org.slf4j.LoggerFactory;

public class ConfigPattern extends ResourcePattern<ScheduleImportConfig> {

	public ConfigPattern(Resource match) {
		super(match);
	}
	
	public final StringResource csvFile = model.csvFile();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final SingleValueResource targetParent = model.targetParent();
	
	@Existence(required=CreateMode.OPTIONAL)
	private final ResourceList<SingleValueResource> targetParents = model.targetParents();
	
	@Existence(required=CreateMode.OPTIONAL)
	private final StringResource relativePath = model.scheduleRelativePath();
	
	@Existence(required=CreateMode.OPTIONAL)
	private final StringArrayResource relativePaths = model.scheduleRelativePaths();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final BooleanResource moveStartToCurrentFrameworkTime = model.moveStartToCurrentFrameworkTime();
	
	@Existence(required=CreateMode.OPTIONAL)
	public final IntegerResource alignmentType = model.alignmentType();
	
	@Override
	public boolean accept() {
		return targetParent.exists() || targetParents.exists();
	}
	
	private CSVFormat getCsvFormat() {
		CSVFormat format = ImportConfiguration.getDefaultFormat();
		final CsvFormatConfig cfgRes = model.csvFormat();
		if (cfgRes.isActive()) {
			if (cfgRes.csvDelimiter().isActive()) {
				final String del = cfgRes.csvDelimiter().getValue().trim();
				if (del.length() > 0)
					format = format.withDelimiter(del.charAt(0));
			}
		}
		return format;
	}
	
	public void configure(final ImportConfigurationBuilder builder, final long now) {
		builder.setCsvFormat(getCsvFormat());
		if (model.csvFormat().decimalSeparator().isActive()) {
			final char sep = (char) model.csvFormat().decimalSeparator().getValue(); // FIXME?
			builder.setDecimalSeparator(sep);
		}
		if (model.csvFormat().timeFormat().isActive())
			builder.setDateTimeFormat(new SimpleDateFormat(model.csvFormat().timeFormat().getValue(), Locale.ENGLISH));
		final boolean isMultiImport = targetParents.isActive();
		final int timeIndex = model.csvFormat().timeIndex().isActive() ? model.csvFormat().timeIndex().getValue() : 0;
		final int valueIndex = model.csvFormat().valueIndex().isActive() ? model.csvFormat().valueIndex().getValue() : 1;
		final int[] valueIndices0 = !isMultiImport || !model.csvFormat().valueIndices().isActive() ? null : model.csvFormat().valueIndices().getValues();
		if (valueIndices0 != null && targetParents.size() != valueIndices0.length) {
			throw new IllegalStateException("Failed to import schedule, incompatible nr of "
					+ "resources and indices " + targetParents.size() + " != " + valueIndices0.length + "; config " + model.getPath());
		}
		final List<Integer> valueIndices;
		if (valueIndices0 == null)
			valueIndices = null;
		else {
			valueIndices = new ArrayList<>(valueIndices0.length);
			for (int i : valueIndices0)
				valueIndices.add(i);
		}
		if (moveStartToCurrentFrameworkTime.isActive() && moveStartToCurrentFrameworkTime.getValue()) {
			final long startTime;
			if (alignmentType.isActive()) {
				startTime = getAlignedIntervalStart(now, alignmentType.getValue());
			} else {
				startTime = now;
			}
			if (timeIndex >= 0) {
				if (!isMultiImport)
					builder.setTimeAndValueIndices(timeIndex, valueIndex, startTime);
				else
					builder.setMultiValueIndices(timeIndex, valueIndices, startTime);
			}
			else if (model.csvFormat().interval().isActive()) {
				if (!isMultiImport)
					builder.setTimesteps(valueIndex, startTime, model.csvFormat().interval().getValue());
				else
					builder.setTimesteps(valueIndices, startTime, model.csvFormat().interval().getValue());
			}
			else
				throw new IllegalStateException("Cannot determine time interval for config " + model);
		}
		else if (timeIndex >= 0) {
			if (!isMultiImport)
				builder.setTimeAndValueIndices(timeIndex, valueIndex);
			else
				builder.setMultiValueIndices(timeIndex, valueIndices);
		} 
		else if (model.csvFormat().startTime().isActive() && model.csvFormat().interval().isActive()) {
			if (!isMultiImport)
				builder.setTimesteps(valueIndex, model.csvFormat().startTime().getValue(), model.csvFormat().interval().getValue());
			else
				builder.setTimesteps(valueIndices, model.csvFormat().startTime().getValue(), model.csvFormat().interval().getValue());
		}
		else {
			throw new IllegalStateException("Cannot determine start time and interval for config " + model);
		}
		if (model.csvFormat().valueFactor().isActive()) 
			builder.setFactor(model.csvFormat().valueFactor().getValue());
		if (model.csvFormat().valueAddend().isActive()) 
			builder.setAddend(model.csvFormat().valueAddend().getValue());
	}
	
	public boolean isPeriodic() {
		return model.periodicSchedule().isActive() && model.importHorizon().isActive() ? model.periodicSchedule().getValue() : false;
	}
	
	public List<Resource> getSchedules() {
		final Map<SingleValueResource, String> map = getParentResources();
		final List<Resource> schedules = new ArrayList<Resource>(map.size());
		for (Map.Entry<SingleValueResource, String> entry : map.entrySet()) {
			try {
				schedules.add(entry.getKey().getLocationResource().getSubResource(entry.getValue()));
			} catch (SecurityException e) {
				LoggerFactory.getLogger(getClass()).error("Failed to import schedule for parent {}", entry.getKey().getLocation(), e);
			}
		}
		return schedules;
	}
	
	/**
	 * Map parent resource -> relative schedule path
	 * @return
	 */
	public Map<SingleValueResource, String> getParentResources() {
		if (targetParents.isActive()) {
			final Iterator<SingleValueResource> resIt = targetParents.getAllElements().iterator();
			final Iterator<String> paths;
			if (relativePaths.isActive()) {
				paths = Arrays.asList(this.relativePaths.getValues()).iterator();
			}
			else {
				final String path = relativePath.isActive() ? relativePath.getValue() : "forecast";
				paths = new Iterator<String>() {

					@Override
					public boolean hasNext() {
						return true;
					}

					@Override
					public String next() {
						return path;
					}
				};
			}  
			final Map<SingleValueResource, String> result = new LinkedHashMap<>(); // order is important
			while (resIt.hasNext() && paths.hasNext()) {
				result.put(resIt.next(), paths.next());
			}
			if (resIt.hasNext()) {
				LoggerFactory.getLogger(getClass()).warn("Inconsistent schedule importer config {}; nr of parent resources and nr "
						+ "of relative schedule paths does not match", model);
			}
			return result;
			
		}
		// note: we cannot insist on active here, because a sensible value may not exist before importing the schedule
		if (targetParent.exists()) {
			final String path = relativePath.isActive() ? relativePath.getValue() : "forecast"; 
			return Collections.singletonMap(targetParent, path);
		}
		return Collections.emptyMap();
	}
	
	static long getAlignedIntervalStart(final long t, final int calendarEnum) {
	    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
	    calendar.setTime(new Date(t));
	    // intentional fallthrough
	    switch (calendarEnum) {
	    case Calendar.YEAR:
	    	calendar.set(Calendar.MONTH, 0);
	    case Calendar.MONTH: // we
	    	calendar.set(Calendar.DAY_OF_MONTH, 1);
	    case Calendar.DAY_OF_YEAR:
	    case Calendar.DAY_OF_WEEK:
	    case Calendar.DAY_OF_MONTH:
	    case Calendar.DAY_OF_WEEK_IN_MONTH:
	    	calendar.set(Calendar.HOUR_OF_DAY, 0);
	    case Calendar.HOUR_OF_DAY:
	    case Calendar.HOUR:
	    	calendar.set(Calendar.MINUTE, 0);
	    case Calendar.MINUTE:
	    	calendar.set(Calendar.SECOND, 0);
	    case Calendar.SECOND:
	    	calendar.set(Calendar.MILLISECOND, 0);
	    	break;
	    default:
	    	throw new IllegalArgumentException("Unsupported alignment interval value " + calendarEnum);
	    }
	    return calendar.getTimeInMillis();
	}
	
}
