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
package org.ogema.tools.resourcemanipulator.configurations;

import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.tools.resourcemanipulator.implementation.ProgramEnforcerImpl;

/**
 * Application tool that configures a float resource for "automatically set it
 * to the value of the program that is defined for it". Note that the
 * configuration defining for which resources the programs shall be enforced is
 * stored persistently be the framework.<br>
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface ProgramEnforcer extends ManipulatorConfiguration {

	/**
	 * Configures enforcing the program. Unless the {@link InterpolationMode} of the
	 * program attached is a linear interpolation, the program enforcer will always
	 * try to update the target resource to the value configured in the program 
	 * schedule whenever the configured value changes. In case of linear interpolation,
	 * where changes usually would happen in intervals of 1 ms, the defined updateInterval
	 * is used, instead. If the schedule ceases to exist, or at times where the schedule does
	 * not contain a value with good quality, the target resource is set inactive. Similarly,
	 * an inactive resource will be set to active a soon as a sensible schedule entry can
	 * be written in it.
	 * 
	 * @param resource Float resource for which the program shall be enforced.
	 * @param updateInterval time interval in which the value shall be updated in case the interpolation mode is linear.
	 * @param priority Access priority to use for exclusive writing to the target resource. Set to null if no exclusive access is required.
	 */
	void enforceProgram(ValueResource resource, long updateInterval, AccessPriority priority);

	/**
	 * Enforces the program for the target resource, but does not request an exclusive write access.
	 * This is the same as calling {@link #enforceProgram(ValueResource, long, AccessPriority)}
	 * with an access priority of null.
	 * @param resource Float resource for which the program shall be enforced.
	 * @param updateInterval time interval in which the value shall be updated in case the interpolation mode is linear.
	 */
	void enforceProgram(ValueResource resource, long updateInterval);

	/**
	 * Gets the target resource that this program enforcer operates on.
	 * @return Reference to the resource that is written into by this. Returns null if the controller has not been configured, yet.
	 */
	ValueResource getResource();

	/**
	 * Gets the currently configured access priority.
	 * @return the priority that exclusive access was required with. Returns null if no exclusive access has been demanded.
	 */
	AccessPriority getAccessPriority();

	/**
	 * Gets the currently-configured update interval for the program enforcement. Note that the 
	 * value returned will only be effective when the interpolation mode of the attached program
	 * schedule is linear.
	 * @return time-steps between two subsequent updates of the value in ms.
	 */
	long getUpdateInterval();

	/**
	 * See {@link ProgramEnforcerImpl#setRangeFilter(float, float, int)}. Default mode: target resource
	 * is deactivated if schedule value lies outside the specified range.
	 */
	void setRangeFilter(float lowerBoundary, float upperBoundary) throws RuntimeException;

	/** 
	 * In order to activate only one of the boundaries, pass {@link Float#NaN} for the other one. Boundaries are inclusive. <br>
	 * The mode determines the behaviour in case a schedule value lies outside the specified range: <br>
	 * 0: deactivate target resource <br>
	 * 1: keep last value, do not change active status (i.e. do nothing)<br> 
	 * Deactivate the filter by passing Float.NaN for both boundary arguments. <br>
	 * This method must only be called after the {@link #commit()} has been called, otherwise a {@link RuntimeException} is thrown.
	 */
	void setRangeFilter(float lowerBoundary, float upperBoundary, int mode) throws RuntimeException;
	
	/**
	 * Set a custom resource name for the schedule subresource (default: "program")
	 * @param scheduleName
	 */
	void setTargetScheduleName(String scheduleName);
	
	/**
	 * Get the resource name of the schedule subresource (default: "program")
	 * @return
	 */
	String getTargetScheduleName();

	/**
	 * If set to true, the target resource is deactivated when no valid schedule value is available, otherwise
	 * the last activation status is retained. Default: true.
	 */
	void deactivateTargetIfProgramMissing(boolean deactivate);
}
