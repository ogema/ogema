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
