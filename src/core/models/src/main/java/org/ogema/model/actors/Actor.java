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
package org.ogema.model.actors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.SimpleResource;
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
	SimpleResource stateControl();

	/**
	 * Device's feedback for the setting (if applicable). Interpretation of the value is the same
	 * as in {@link #stateControl() }
	 */
	@NonPersistent
	SimpleResource stateFeedback();

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
