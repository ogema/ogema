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

/**
 * Base class for all manipulator configurations. A manipulator is a rule that
 * is applied to one or more resources. The rule can be configured and then 
 * added to the system to be automatically enforced.
 * 
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface ManipulatorConfiguration {

	/**
	 * Adds the manipulator to the system and starts it. If the configuration
	 * of an existing manipulator has been re-configured, this commits the changes
	 * to the manipulator. Note that changes in a configuration have no effect until
	 * this is called.
	 * @return true if the configured rule could be started, false if not (e.g. if the configuration set up did not make sense).
	 */
	boolean commit();

	/**
	 * Stops the manipulator and removes it from the system.
	 */
	void remove();

	/**
	 * Stops the manipulator, but does not remove the settings.
	 */
	void deactivate();

	/**
	 * Restarts the manipulator when it has been deactivated. Does not do anything if the manipulator is active already.
	 */
	void activate();
}
