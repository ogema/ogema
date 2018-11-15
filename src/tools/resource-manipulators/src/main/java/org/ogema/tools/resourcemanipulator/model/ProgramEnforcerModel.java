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
	 * Update interval. Set to &lt;=0 for "on schedule update".
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
	
	/**
	 * Default value: "program"
	 * @return
	 */
	StringResource scheduleResourceName();
	
}
