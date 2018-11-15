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
package org.ogema.model.sensors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.ranges.BinaryRange;
import org.ogema.model.targetranges.BinaryTargetRange;

/**
 * motion detector
 */
public interface MotionSensor extends GenericBinarySensor {

	/**
	 * motion status (non-persistent)<br>
	 * true: motion detected. false: no motion detected
	 */
	@NonPersistent
	@Override
	BooleanResource reading();

	/**
	 * Possible values for motion status (non-persistent)<br>
	 * true: motion detected. false: no motion detected
	 */
	@Override
	BinaryRange ratedValues();

	/**
	 * Settings for motion status for EM-system.<br>
	 * true: motion detected. false: no motion detected
	 */
	@Override
	BinaryTargetRange settings();

	/**
	 * Settings for motion status to be sent to the device.<br>
	 * true: motion detected. false: no motion detected
	 */
	@Override
	BinaryTargetRange deviceSettings();

	/**
	 * Device settings feedback for motion status.<br>
	 * true: motion detected. false: no motion detected
	 */
	@Override
	BinaryTargetRange deviceFeedback();
}
