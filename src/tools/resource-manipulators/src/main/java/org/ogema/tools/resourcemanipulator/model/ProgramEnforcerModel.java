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

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;

/**
 * Configuration resource for the Program enforcer rule.
 */
public interface ProgramEnforcerModel extends ResourceManipulatorModel {

	final static String TARGET_NAME = "targetResource";

	/**
	 * Resource that is being controlled by this application.
	 */
	Resource targetResource();

	/**
	 * Priority for exclusive write accesses that shall be used in case of
	 * exclusive access.
	 */
	StringResource priority();

	/**
	 * True exactly if the write access to the {@link #targetResource() } shall
	 * be requested as exclusive.
	 */
	BooleanResource exclusiveAccessRequired();

	/**
	 * Update interval. Set to <=0 for "on schedule update".
	 */
	TimeResource updateInterval();

	/**
	 * Optional element. If set, only schedule values within the specified target range shall be written to 
	 * the target resource. The subresource {@link RangeFilter#mode()} specifies what to do in case of values 
	 * that violate the target range.
	 */
	RangeFilter range();

	/**
	 * If true, target resource will be deactivated when no valid schedule value is available, otherwise last
	 * deactivation status is retained.
	 */
	BooleanResource deactivateIfValueMissing();
}
