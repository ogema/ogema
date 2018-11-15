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
	 * rule can be (persistently) started by calling {@link ManipulatorConfiguration#commit()}.
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
