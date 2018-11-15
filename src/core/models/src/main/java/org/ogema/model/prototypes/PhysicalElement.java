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
package org.ogema.model.prototypes;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.ThermalEnergyCapacityResource;
import org.ogema.model.devices.profiles.ProgramInformation;
import org.ogema.model.devices.profiles.State;
import org.ogema.model.locations.PhysicalDimensions;
import org.ogema.model.locations.Location;

/**
 * Prototype to be extended by resources representing a physical element such as sensors, actors, devices, parts of
 * devices and buildings.
 */
public interface PhysicalElement extends Resource {

	/**
	 * Physical position of device (geographic coordinates, Room in which the element is situated, mobile information.
	 * Usually only set for the top-level resource as this is usually the same for all parts of a device.
	 */
	Location location();

	/**
	 * Heat capacity of the device. Technically, this can be defined for every
	 * physical element, which is why it is defined on this level. In practice it 
	 * will only be relevant for thermal 
	 * storage devices, either dedicated storages or effective storages like rooms.
	 */
	ThermalEnergyCapacityResource heatCapacity();

	/** 
	 * Information on the physical dimensions.
	 */
	PhysicalDimensions physDim();

	/**
	 * Information on available programs, including operating cycles and typical consumption patterns.<br>
	 * Note that steady states should not be modeled as programs, but rather as states. See {@link #states()}.
	 */
	ResourceList<ProgramInformation> programs();

	/**
	 * Steady states of this device, such as 'on', 'off', 'standby'.
	 * Contains consumption/generation profiles, and the like.<br>
	 * The main difference between states and {@link PhysicalElement#programs()} is, that the latter are usually active
	 * for a specific time duration, whereas steady states can remain active for a variable duration of time.
	 * Most states are further characterized by constant measurement values, although there may be exceptions. For
	 * instance, the 'on' state of a fridge has a non-constant but periodic electric power consumption profile.    
	 */
	ResourceList<State> states(); 

	/**
	 * Human-readable name.
	 */
	StringResource name();
}
