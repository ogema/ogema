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
