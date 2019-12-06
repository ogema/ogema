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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.model.units.EnergyPerAreaResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.model.units.VelocityResource;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.devices.sensoractordevices.WindSensor;
import org.ogema.model.locations.GeographicLocation;
import org.ogema.model.sensors.SolarIrradiationSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.LoggingUtils;
import org.ogema.tools.resource.util.ResourceUtils;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.scheduleimporter.config.CsvFormatConfig;
import org.ogema.tools.scheduleimporter.config.ScheduleImportConfig;
import org.osgi.framework.Bundle;
import org.slf4j.LoggerFactory;

class Device extends ResourcePattern<SensorDevice> {

	private final Config config;
	private final boolean temperatureEnabled;
	private final boolean windEnabled;
	private final boolean solarEnabled;
	
	protected Device(Resource match, Config config, GeographicLocation location, ApplicationManager appMan) {
		super(match);
		this.config = config;
		this.temperatureEnabled = !config.temperatureCsvFile().isEmpty();
		this.windEnabled = !config.windSpeedCsvFile().isEmpty();
		this.solarEnabled = !config.solarIrradiationCsvFile().isEmpty();
		this.location.setAsReference(location);
		if (temperatureEnabled)
			temperature.forecast().<AbsoluteSchedule> create().setInterpolationMode(InterpolationMode.LINEAR);
		if (solarEnabled)
			irradiation.forecast().<AbsoluteSchedule> create().setInterpolationMode(InterpolationMode.LINEAR);
		if (windEnabled) {
			windSpeed.forecast().<AbsoluteSchedule> create().setInterpolationMode(InterpolationMode.LINEAR);
//			windDirection.create();
		}
		ResourceUtils.activateComplexResources(model, true, appMan.getResourceAccess());
	}
	
	public final GeographicLocation location = model.location().geographicLocation();
	
	public final TemperatureSensor tempSens = model.getSubResource("temperatureSensor", TemperatureSensor.class);
	public final TemperatureResource temperature = tempSens.reading();
	
	public final SolarIrradiationSensor irradSens = model.getSubResource("solarIrradiationSensor", SolarIrradiationSensor.class);
	public final EnergyPerAreaResource irradiation = irradSens.reading();
	
	public final WindSensor windSens = model.getSubResource("windSensor", WindSensor.class);
	public final VelocityResource windSpeed = windSens.speed().reading();
	// TODO
//	public final AngleResource windDirection = windSens.direction().reading();
	
	// TODO activate resource manipulator: program/forecast tracking... 
	public void activateSimulation(final ResourceList<ScheduleImportConfig> configs, final Bundle bundle) {
		if (windEnabled)
			addConfig(windSpeed, configs, config.windSpeedCsvFile(), bundle, config.windSpeedFactor(), config.windSpeedOffset());
		if (solarEnabled)
			addConfig(irradiation, configs, config.solarIrradiationCsvFile(), bundle, config.solarIrradiationFactor(), config.solarIrradiationOffset());
		if (temperatureEnabled)
			addConfig(temperature, configs, config.temperatureCsvFile(), bundle, config.temperatureFactor(), config.temperatureOffset());
	}
	
	public void activateResourceTracking(final ResourceManipulator tool) {
		final List<ProgramEnforcer> enforcers = tool.getConfigurations(ProgramEnforcer.class);
		if (windEnabled) 
			activateResourceTracking(enforcers, windSpeed, tool);
		if (temperatureEnabled) 
			activateResourceTracking(enforcers, temperature, tool);
		if (solarEnabled) 
			activateResourceTracking(enforcers, irradiation, tool);
	}
	
	public void activateLogging() {
		if (windEnabled) 
			LoggingUtils.activateLogging(windSpeed, -2);
		if (temperatureEnabled) 
			LoggingUtils.activateLogging(temperature, -2);
		if (solarEnabled) 
			LoggingUtils.activateLogging(irradiation, -2);
	}
	
	private static void activateResourceTracking(final List<ProgramEnforcer> enforcers, final FloatResource target, final ResourceManipulator tool) {
		/*
		if (enforcers.stream()
			.filter(enforcer -> target.equalsLocation(enforcer.getResource()))
			.findAny()
			.isPresent())
			return;
			*/
		for (ProgramEnforcer enforcer : enforcers) {
			if (target.equalsLocation(enforcer.getResource()))
				return;
		}
		final ProgramEnforcer enforcer = tool.createConfiguration(ProgramEnforcer.class);
		enforcer.setTargetScheduleName("forecast");
		enforcer.enforceProgram(target, 0);
		enforcer.commit();
	}
	
	
	private static ScheduleImportConfig getExistingConfig(final FloatResource parent, final ResourceList<ScheduleImportConfig> configs) {
		/*
		return configs.getAllElements().stream()
			.filter(config -> parent.equalsLocation(config.targetParent()))
			.findAny()
			.orElse(null);
			*/
		for (ScheduleImportConfig config : configs.getAllElements()) {
			if (parent.equalsLocation(config.targetParent()))
				return config;
		}
		return null;
	}
	
	private ScheduleImportConfig addConfig(
			final FloatResource parent, 
			final ResourceList<ScheduleImportConfig> configs,
			final String filePath,
			final Bundle bundle,
			final float factor,
			final float offset) {
		final ScheduleImportConfig existing = getExistingConfig(parent, configs);
		if (existing != null)
			return existing;
		final URL url = filePathToUrl(filePath, bundle);
		if (url == null) {
			LoggerFactory.getLogger(SimulatedWeatherApp.class).error("Could not find resource {}, cannot activate simulation for {}", filePath, parent);
			return null;
		}
		final ScheduleImportConfig cfg = configs.add();
		cfg.targetParent().setAsReference(parent);
		cfg.scheduleRelativePath().<StringResource> create().setValue("forecast");
		cfg.csvFile().<StringResource> create().setValue(url.toString());
		cfg.moveStartToCurrentFrameworkTime().<BooleanResource> create().setValue(true);
		cfg.alignmentType().<IntegerResource> create().setValue(config.alignmentType());
		cfg.periodicSchedule().<BooleanResource> create().setValue(true);
		cfg.importHorizon().<TimeResource> create().setValue(config.importHorizonMillis());
		final CsvFormatConfig csvFormat = cfg.csvFormat();
		csvFormat.timeIndex().<IntegerResource> create().setValue(config.timeIndex());
		csvFormat.valueIndex().<IntegerResource> create().setValue(config.valueIndex());
		csvFormat.interval().<TimeResource> create().setValue(Duration.ofMinutes(config.intervalMinutes()).toMillis());
		if (factor != 1)
			csvFormat.valueFactor().<FloatResource> create().setValue(factor);
		if (offset != 0)
			csvFormat.valueAddend().<FloatResource> create().setValue(offset);
		cfg.activate(true);
		return cfg;
	}
	
	private static URL filePathToUrl(final String filePath, final Bundle bundle) {
		try {
			return new URL(filePath);
		} catch (MalformedURLException expected) {}
		return bundle.getResource(filePath);
	}
	

}
