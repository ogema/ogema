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
