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
