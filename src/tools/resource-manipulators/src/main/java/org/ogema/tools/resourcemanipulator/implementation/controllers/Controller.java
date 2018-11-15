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
package org.ogema.tools.resourcemanipulator.implementation.controllers;

import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.model.ResourceManipulatorModel;

/**
 * Common interface for the different controllers.
 * @author Timo Fischer, Fraunhofer IWES
 */
public interface Controller {

	/**
	 * Start the controller.
	 */
	void start();

	/**
	 * Stops the controller.
	 */
	void stop();
	
	Class<? extends ManipulatorConfiguration> getType();
	
	ResourceManipulatorModel getConfigurationResource();
	
	/**
	 * Last execution time, if known. In general, this information 
	 * is not persisted. Returns null if no action has been performed yet.
	 * @return
	 */
	Long getLastExecutionTime();
	
}
