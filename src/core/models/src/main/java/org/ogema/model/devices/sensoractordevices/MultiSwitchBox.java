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
package org.ogema.model.devices.sensoractordevices;

import org.ogema.core.model.ResourceList;
import org.ogema.model.prototypes.PhysicalElement;

/**
 * Device to be provided by hardware driver. Represents a switching/measuring plug adapter that can be plugged/installed
 * between a device and its grid connection for switching/measuring purposes and that contains more than one unit for
 * switching/measurement
 */
public interface MultiSwitchBox extends PhysicalElement {
	
	/** 
	 * Set of the individual switch box units. 
	 */
	public ResourceList<SingleSwitchBox> switchboxes();
}
