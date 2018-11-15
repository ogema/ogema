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
package org.ogema.tools.resourcemanipulator.trashcan;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.implementation.controllers.Controller;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

/**
 * Alarm supervisor for one sensor-alarm pair.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class SensorAlarmController implements Controller, ResourceValueListener<FloatResource> {

	private final OgemaLogger m_logger;
	private final SensorAlarmPattern m_device;

	public SensorAlarmController(SensorAlarmPattern device, ApplicationManager appMan) {
		m_logger = appMan.getLogger();
		m_device = device;
	}

	@Override
	public void start() {
		m_device.reading.addValueListener(this);
		resourceChanged(m_device.reading);
	}

	@Override
	public void stop() {
		m_device.reading.removeValueListener(this);
	}
	
	@Override
	public Class<? extends ManipulatorConfiguration> getType() {
		return null;
	}	

	/**
	 * Checks if limits of the sensor are violated.
	 */
	public boolean areLimitsViolated(SensorAlarmPattern sensor) {
		final float x = sensor.reading.getValue();
		if (sensor.minReading.isActive()) {
			final float min = sensor.minReading.getValue();
			if (x < min)
				return true;
		}
		if (sensor.maxReading.isActive()) {
			final float max = sensor.maxReading.getValue();
			if (x > max)
				return true;
		}
		return false;
	}

	@Override
	public void resourceChanged(FloatResource resource) {
		final boolean alarmState = areLimitsViolated(m_device);
		final boolean currentState = m_device.trigger.getValue();
		if (currentState != alarmState)
			m_device.trigger.setValue(alarmState);
	}

	@Override
	public ResourceManipulatorModel getConfigurationResource() {
		return m_device.model;
	}

	@Override
	public Long getLastExecutionTime() {
		// TODO Auto-generated method stub
		return null;
	}

}
