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
import org.ogema.model.prototypes.Configuration;

/**
 * Collection node for all configuration rules in the system.
 */
public interface CommonConfigurationNode extends Configuration {

	/**
	 * List of all thresholds configured for the system.
	 */
	ResourceList<ThresholdModel> thresholds();

	/**
	 * All program enforcer configurations.
	 */
	ResourceList<ProgramEnforcerModel> programEnforcers();

	/**
	 * All ScheduleSum configurations.
	 */
	ResourceList<ScheduleSumModel> scheduleSums();

	/**
	 * All Sum configurations.
	 */
	ResourceList<SumModel> sums();
	
	/**
	 * All Schedule management configurations 
	 */
	ResourceList<ScheduleManagementModel> scheduleManagements();
}
