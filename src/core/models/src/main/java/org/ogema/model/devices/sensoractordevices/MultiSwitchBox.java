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
