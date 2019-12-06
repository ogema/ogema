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
package de.iwes.ogema.sim.weather;

import java.util.Calendar;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface Config {
	
	int latitudeFullDegrees();
	float latitudeArcMinutes();
	int longitudeFullDegrees();
	float longitudeArcMinutes();
	
	@AttributeDefinition(description="Location of the temperature file; either a URL or a resource location within the bundle. "
			+ "If empty (default) the temperature simulation is disabled.")
	String temperatureCsvFile() default "";
	
	@AttributeDefinition(description="Location of the temperature file; either a URL or a resource location within the bundle. "
			+ "If empty (default) the wind speed simulation is disabled.")
	String windSpeedCsvFile() default "";
	
	@AttributeDefinition(description="Location of the solar irradiation file; either a URL or a resource location within the bundle. "
			+ "If empty (default) the irradiation simulation is disabled.")
	String solarIrradiationCsvFile() default "";

	boolean activateResourceTracking() default true;
	@AttributeDefinition(description="Only relevant if activateResourceTracking is true.")
	boolean activateLogging() default true;
	
	String locationResource();
	String sensorDeviceResource();

	/*
	 * Information about the CSV file format below; to be extended if required 
	 */
	
	/**
	 * See static {@link Calendar} fields
	 * @return
	 */
	int alignmentType() default Calendar.YEAR;
	/**
	 * Negative value means there is no time column in the CSV file
	 * @return
	 */
	int timeIndex() default -1;
	int valueIndex() default 0;
	/**
	 * Only relevant if {@link #timeIndex()} is negative, i.e. there is no time column in the CSV file
	 * @return
	 */
	int intervalMinutes() default 15;
	
	long importHorizonMillis() default 7 * 24 * 60 * 60 * 1000; // 1 week

	/**
	 * Factors and offsets to convert file values to target unit
	 * @return
	 */
	float temperatureFactor() default 1;
	float temperatureOffset() default 0;
	float windSpeedFactor() default 1;
	float windSpeedOffset() default 0;
	float solarIrradiationFactor() default 1;
	float solarIrradiationOffset() default 0;
	
}
