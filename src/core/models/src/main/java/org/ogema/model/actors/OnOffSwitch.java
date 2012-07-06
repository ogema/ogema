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
package org.ogema.model.actors;

import org.ogema.core.model.ModelModifiers.NonPersistent;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.ranges.BinaryRange;
import org.ogema.model.targetranges.BinaryTargetRange;

/**
 * Generic binary (on/off) switch.
 */
public interface OnOffSwitch extends Actor {

	@Override
	BooleanResource stateControl();

	@Override
	@NonPersistent
	BooleanResource stateFeedback();

	@Override
	BinaryRange ratedValues();

	@Override
	BinaryTargetRange settings();

	/**
	 * Time before the switch may be switched on after it was switched off. Note
	 * that this is NOT enforced by the framework and that the time includes the
	 * time to power down the device.
	 */
	public TimeResource timeBeforeSwitchOn();

	/**
	 * Time before the switch may be switched off after it was switched on. Note
	 * that this is NOT enforced by the framework and that the time includes the
	 * time to power up the device.
	 */
	public TimeResource timeBeforeSwitchOff();

}
