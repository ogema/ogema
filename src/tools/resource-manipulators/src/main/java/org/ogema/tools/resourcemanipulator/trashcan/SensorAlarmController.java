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
