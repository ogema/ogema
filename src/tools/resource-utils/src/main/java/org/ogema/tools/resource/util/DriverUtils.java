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
package org.ogema.tools.resource.util;

import java.util.List;
import java.util.regex.Pattern;

import org.ogema.core.model.Resource;
import org.ogema.model.communication.CommunicationStatus;
import org.ogema.model.prototypes.PhysicalElement;

public class DriverUtils {

	private DriverUtils() {}
	
	/**
	 * Get the communication status for some driver-managed resource. It is up to the driver to provide
	 * the communication status resource, if it is not available null is returned.
	 * @param target
	 * 		Some resource that is managed by a driver that provides information on the communication status.
	 * 		Could be a {@link PhysicalElement} resource representing a device managed by a driver, or a ValueResource.
	 * @return
	 */
	public static CommunicationStatus getCommunicationStatus(final Resource target) {
		return ResourceUtils.getFirstContextResource(target, CommunicationStatus.class);
	}
	
	/**
	 * Get the device belonging to some communication status resource. 
	 * @param status
	 * @param filterCustomTypes
	 * 		if true, only devices with standard OGEMA device types are reported, custom types are ignored.
	 * @return
	 * 		the first device found that belongs to the status resource, or null if none was found.
	 */
	public static PhysicalElement getDevice(final CommunicationStatus status, final boolean filterCustomTypes) {
		return ResourceUtils.getFirstContextResource(status, PhysicalElement.class, filterCustomTypes ? Pattern.compile("^org\\.ogema\\.") : null, null);
	}
	
	/**
	 * Get the device belonging to some communication status resource. Only devices with standard OGEMA device types 
	 * are reported, custom types are ignored.
	 * @param status
	 * @param filterCustomTypes
	 * 		if true, only devices with standard OGEMA device types are reported, custom types are ignored.
	 * @return
	 * 		the list of deviecs found that belong to the status resource. 
	 */
	public static List<PhysicalElement> getDevices(final CommunicationStatus status, final boolean filterCustomTypes) {
		return ResourceUtils.getContextResources(status, PhysicalElement.class, false,  filterCustomTypes ? Pattern.compile("^org\\.ogema\\.") : null, null);
	}
	
}
