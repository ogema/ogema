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
