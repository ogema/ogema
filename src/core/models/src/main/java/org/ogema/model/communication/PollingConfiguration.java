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
package org.ogema.model.communication;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Data;

/**
 * Information about polling frequency settings. Non existing polling settings
 * indicate no polling of this kind or ignorance about polling of this kind.
 */
public interface PollingConfiguration extends Data {
	/**
	 * Time between two communication initiations from the OGEMA computer to the external device.
	 */
	TimeResource pollingInterval();

	/**
	 * Time between two communication initiations from the external device to the OGEMA computer.
	 * Setting by the framework that is sent to device.
	 */
	TimeResource pollingIntervalDeviceControl();

	/**
	 * Feedback channel for {@link #pollingIntervalDeviceControl()}, i.e. the actual
	 * settings the device has.
	 */
	@NonPersistent
	TimeResource pollingIntervalDeviceFeedback();

	/**
	 * Time between two keep-alive messages from the OGEMA computer to the external device.
	 */
	TimeResource keepAliveInterval();

	/**
	 * Time between two keep-alive messages from the external device to the OGEMA computer.
	 */
	TimeResource keepAliveIntervalDeviceControl();

	/**
	 * Feedback channel for {@link #keepAliveIntervalDeviceControl()}
	 */
	@NonPersistent
	TimeResource keepAliveIntervalDeviceFeedback();
}
