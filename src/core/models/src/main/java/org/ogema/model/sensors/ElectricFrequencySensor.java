/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.model.sensors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.units.FrequencyResource;
import org.ogema.model.ranges.FrequencyRange;
import org.ogema.model.targetranges.FrequencyTargetRange;

/** GenericFloatSensor for the frequency of alternating current */
public interface ElectricFrequencySensor extends Sensor {

	@NonPersistent
	@Override
	FrequencyResource reading();

	@Override
	FrequencyRange ratedValues();

	@Override
	FrequencyTargetRange settings();

	@Override
	FrequencyTargetRange deviceSettings();

	@Override
	FrequencyTargetRange deviceFeedback();
}
