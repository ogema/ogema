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
package org.ogema.model.targetranges;

import org.ogema.core.model.simple.FloatResource;
import org.ogema.model.ranges.GenericFloatRange;

/**
 * Target range for generic float ranges. Should only be used if no more
 * specialized target range is available. The meaning and units of the values
 * must be defined somewhere in the parent resources, in this case.
 */
public interface GenericFloatTargetRange extends TargetRange {

	@Override
	FloatResource setpoint();

	@Override
	GenericFloatRange controlLimits();

	@Override
	GenericFloatRange targetRange();

	@Override
	GenericFloatRange alarmLimits();
}
