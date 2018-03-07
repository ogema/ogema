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
