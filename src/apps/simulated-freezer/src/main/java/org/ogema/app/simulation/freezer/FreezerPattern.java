/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
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
