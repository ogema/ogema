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
