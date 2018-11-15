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
import org.ogema.core.model.units.AngleResource;
import org.ogema.model.ranges.AngleRange;
import org.ogema.model.targetranges.AngleTargetRange;

/**
 * GenericFloatSensor determining any geographic direction. In case a movement
 * is measured, usually the direction from which the flow comes is used (but
 * details should be provided with the parent resource)
 */
public interface GeographicDirectionSensor extends GenericFloatSensor {

	/**
	 * Direction <br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@NonPersistent
	@Override
	AngleResource reading();

	/**
	 * Technical constraints for the angle.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleRange ratedValues();

	/**
	 * Direction settings. Interpreation of the value is identical to
	 * {@link #reading()}.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleTargetRange settings();

	/**
	 * Direction settings to be sent to device. Interpreation of the value is identical to
	 * {@link #reading()}.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleTargetRange deviceSettings();

	/**
	 * Device direction settings feedback. Interpreation of the value is identical to
	 * {@link #reading()}.<br>
	 * unit: ° (angle degree; zero=north; 90°=east, 180°=south, 270°=west)
	 */
	@Override
	AngleTargetRange deviceFeedback();

}
