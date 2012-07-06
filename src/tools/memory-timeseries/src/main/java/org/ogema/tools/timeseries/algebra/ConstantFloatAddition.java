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
package org.ogema.tools.timeseries.algebra;

import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.tools.timeseries.api.LinearSampledValueOperator;

/**
 * Adds a constant to a SampledValue of type FloatValue
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ConstantFloatAddition implements LinearSampledValueOperator {

	private final float m_addend;

	public ConstantFloatAddition(float addend) {
		m_addend = addend;
	}

	@Override
	public final SampledValue apply(SampledValue value) {
		return new SampledValue(new FloatValue(value.getValue().getFloatValue() + m_addend), value.getTimestamp(),
				value.getQuality());
	}

}
