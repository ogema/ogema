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
package org.ogema.apps.sensorwarning;

import org.ogema.apps.sensorwarning.pattern.ConfiguredSensorPattern;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Alarm supervisor for one sensor-alarm pair.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class AlarmController implements ResourceValueListener<FloatResource> {

	private final OgemaLogger m_logger;
	private final ConfiguredSensorPattern m_device;

	public AlarmController(ConfiguredSensorPattern device, ApplicationManager appMan) {
		m_logger = appMan.getLogger();
		m_device = device;
	}

	public void start() {
		// Add a resource listener to the temperature reading, which is invoked whenever the temperature of the device changes.
		m_device.reading.addValueListener(this);
		resourceChanged(m_device.reading);
	}

	void stop() {
		m_device.reading.removeValueListener(this);
	}

	@Override
	public void resourceChanged(FloatResource resource) {
		final boolean alarmState = m_device.areLimitsViolated();
		final boolean currentState = m_device.trigger.getValue();
		if (currentState != alarmState)
			m_device.trigger.setValue(alarmState);
	}
}
