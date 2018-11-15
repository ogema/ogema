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
package org.ogema.model.actors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.ValueResource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.ranges.Range;
import org.ogema.model.targetranges.TargetRange;

/**
 * Prototype to be extended by resources representing an actor.
 */
public interface Actor extends PhysicalElement {

	/**
	 * Setting of the actor as set by the energy management software. Must be overwritten with
	 * a suitable type in actual actors.
	 */
	ValueResource stateControl();

	/**
	 * Device's feedback for the setting (if applicable). Interpretation of the value is the same
	 * as in {@link #stateControl() }
	 */
	@NonPersistent
	ValueResource stateFeedback();

	/**
	 * Range of the possible values that the actor can assume. Interpretation of the values is
	 * identical to {@link #stateControl() }.
	 */
	Range ratedValues();

	/**
	 * Setting setpoints and target ranges that shall be achieved by energy management.
	 */
	TargetRange settings();

	/**
	 * Flag that indicates if the actor can be controlled by the gateway at all.
	 * true: Device accepts control settings sent by the framework. false: gateway
	 * should not send control commands.
	 */
	BooleanResource controllable();
}
