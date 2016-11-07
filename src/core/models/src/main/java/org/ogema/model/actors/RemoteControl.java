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

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.actors.Actor;
import org.ogema.model.devices.storage.ElectricityStorage;

/**
 * Models a remote control with an arbitrary number of buttons.  
 * It is possible to distinguish between long and short button pushes.<br>
 * If a driver does distinguish between long and short pushes, it shall 
 * use the same ordering for the resource lists {@link #shortPress()}
 * and {@link #longPress()}, i.e. the i-th element of short press shall
 * represent the same button as the i-th element of long press.
 */
public interface RemoteControl extends Actor {

	ElectricityStorage battery();

	/**
	 * When a button is pressed for a long time, the corresponding
	 * boolean subresource of this list shall be set to true for the 
	 * duration during which the button is pressed, and then reset
	 * to false.
	 */
	ResourceList<BooleanResource> longPress();

	/**
	 * When a button is pressed for a short time, the corresponding
	 * boolean subresource of this list shall be set to true, and immediately reset to false. 
	 * For applications accessing the state through a {@link ResourceValueListener}, 
	 * it is important to keep in mind that callbacks are not synchronized, i.e.   
	 * if the application reads the state of one of the boolean resources in a 
	 * {@link ResourceValueListener#resourceChanged(org.ogema.core.model.Resource)}
	 * callback, it may already be set back to false, although the callback was triggered by
	 * setting it to true. 
	 */
	ResourceList<BooleanResource> shortPress();
}
