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
package org.ogema.tools.resourcemanipulator;

import java.util.List;
import org.ogema.core.application.ApplicationManager;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;

/**
 * Basic interface for all resource manipulators. To create a common usage practice
 * among the manipulators, all the resource manipulators defined in this package
 * should implement this interface.
 */
public interface ResourceManipulator {

	/**
	 * Starts the tool. Should be called once, usually at application startup.
	 */
	void start();

	/**
	 * Starts the respective manipulator type. Should be called once at the
	 * start of the application using the manipulator.
	 * @deprecated Current implementation passes application manager upon creation.
	 */
	@Deprecated
	void start(ApplicationManager applicationManager);

	/**
	 * Stops the manipulators. Should be called when the application stops to 
	 * ensure that all timers and resource demands are properly cleaned up. Note
	 * that stopping this will not remove the configuration rules set. They will
	 * be re-enforced when the same applications re-starts this or another 
	 * ResourceManipulator. To remove all configured rules, call {@link #deleteAllConfigurations() }.
	 */
	void stop();

	/**
	 * Creates a new instance of a configuration for a rule. Application of the
	 * rule can be (persistently) started by calling {@link ManipulatorConfiguration#start()}.
	 * @param <T>  configuration interface of the rule that shall be created.
	 * @param type configuration interface of the rule that shall be created.
	 * @return instance of a new rule that can be configured and started.
	 */
	<T extends ManipulatorConfiguration> T createConfiguration(Class<T> type);

	/**
	 * Gets a list of all currently active rules that fit a certain configuration class.
	 * Application of the rules can be stopped by calling the {@link ManipulatorConfiguration#deactivate()}
	 * or {@link ManipulatorConfiguration#remove()} method. Rules that are not applied are removed from the system and will 
	 * not show up in subsequent calls of this methods. Note that applications are
	 * only given access to rules that they created themselves.
	 * @param <T> type of the rule to look for.
	 * @param type type of the rule to look for.
	 * @return List of currently active rules created by the application that created the manipulator.
	 */
	<T extends ManipulatorConfiguration> List<T> getConfigurations(Class<T> type);

	/**
	 * Deletes all configuration rules set by the application.
	 */
	void deleteAllConfigurations();
}
