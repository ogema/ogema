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
package org.ogema.tools.resourcemanipulator.model;

import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.tools.resourcemanipulator.model.schedulemgmt.ScheduleReductionAction;

/**
 * Configuration resource for the Schedule management rule.
 */
public interface ScheduleManagementModel extends ResourceManipulatorModel {

	/**
	 * Resource that is being controlled by this application is the subresource
	 * "schedule" of type org.ogema.core.model.schedule.Schedule.
	 */
	FloatResource targetResourceParent();

	/**
	 * Priority for exclusive write accesses that shall be used in case of
	 * exclusive access.
	 */
//	StringResource priority();

	/**
	 * True exactly if the write access to the {@link #targetResource() } shall
	 * be requested as exclusive.
	 */
	// TODO required?
//	BooleanResource exclusiveAccessRequired();

	/**
	 * Update interval in ms. Must be &gt; 0. If not set, a default value is used.
	 */
	TimeResource updateInterval(); 
	
	/**
	 * The actual actions to be performed.
	 * @return
	 */
	ResourceList<ScheduleReductionAction> actions();
	
}
