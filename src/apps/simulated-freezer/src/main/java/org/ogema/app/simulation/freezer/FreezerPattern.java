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
package org.ogema.app.simulation.freezer;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import static org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType.FIXED_INTERVAL;
import static org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType.ON_VALUE_CHANGED;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.ranges.PowerRange;
import org.ogema.model.ranges.TemperatureRange;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.targetranges.TemperatureTargetRange;
import org.ogema.model.devices.whitegoods.CoolingDevice;

/**
 * @author Timo Fischer, Fraunhofer IWES
 */
public class FreezerPattern extends ResourcePattern<CoolingDevice> {

	/*
	 The fields declared as private serve two purposes:
	 1) they provide shortcuts for the declaration of the public fields
	 2) Explicitly listing them here ensures that the activation of the pattern also activates them (only nodes explicitly listed in the pattern are activated)
	 */
	private final TemperatureSensor temperatureSensor = model.temperatureSensor();
	private final ElectricityConnection electricityConnection = model.electricityConnection();
	private final ElectricPowerSensor powerSensor = electricityConnection.powerSensor();
	private final PowerRange powerLimits = powerSensor.ratedValues();
	private final TemperatureTargetRange temperatureSettings = temperatureSensor.settings();
	private final TemperatureRange temperatureLimits = temperatureSettings.controlLimits();
	private final OnOffSwitch onOffSwitch = model.onOffSwitch();

	/*
	 Fields to be accessed from the simulation
	 */
	public final BooleanResource actorControllable = onOffSwitch.controllable();

	public final FloatResource minTemp = temperatureLimits.lowerLimit();
	public final FloatResource maxTemp = temperatureLimits.upperLimit();
	public final TemperatureResource currentTemp = temperatureSensor.reading();

	public final BooleanResource stateControl = onOffSwitch.stateControl();
	public final BooleanResource feedback = onOffSwitch.stateFeedback();

	public final PowerResource currentPower = powerSensor.reading();
	public final PowerResource maxPower = powerLimits.upperLimit();
	public final PowerResource minPower = powerLimits.lowerLimit();

	public FreezerPattern(Resource demandHit) {
		super(demandHit);
	}

	/**
	 * Finalization of the initialization of a newly-created freezer resource
	 * pattern.
	 *
	 * @return
	 */
	public boolean init() {
		stateControl.setValue(false);
		feedback.setValue(false);
		actorControllable.setValue(true);
		minTemp.setValue(273.15f - 20.0f);
		maxTemp.setValue(273.15f - 16.0f);
		currentTemp.setValue(273.15f - 18.f);
		maxPower.setValue(50.f);
		minPower.setValue(0.f);
		currentPower.setValue(0.f);
		configureLogging();
		return true;
	}

	/**
	 * Configures relevant data points in the simulated freezer for automated
	 * logging by the OGEMA framework.
	 */
	public void configureLogging() {
		// configure temperature for logging once per minute
		final RecordedDataConfiguration tempConfig = new RecordedDataConfiguration();
		tempConfig.setStorageType(FIXED_INTERVAL);
		tempConfig.setFixedInterval(60 * 1000l);
		currentTemp.getHistoricalData().setConfiguration(tempConfig);

		// configure state-feedback for logging
		final RecordedDataConfiguration stateConfig = new RecordedDataConfiguration();
		stateConfig.setStorageType(ON_VALUE_CHANGED);
		feedback.getHistoricalData().setConfiguration(stateConfig);
	}
}
