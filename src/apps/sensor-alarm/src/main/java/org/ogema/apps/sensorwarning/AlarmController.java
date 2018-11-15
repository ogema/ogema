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
