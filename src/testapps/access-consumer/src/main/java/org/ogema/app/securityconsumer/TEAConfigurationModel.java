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
/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.app.securityconsumer;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;

/**
 * Configuration Resource Model for the ION7550 meter device
 * driver
 * 
 * @author pau
 * 
 */
public interface TEAConfigurationModel extends Resource {

	/** Interface ID for channel driver interface */
	StringResource interfaceId();

	/** Device Address for channel driver interface */
	StringResource deviceAddress();

	/** Device Parameter for channel driver interface */
	StringResource deviceParameters();

	/** Update Time in ms */
	IntegerResource timeout();

	/** Name of the ION7550DataModel to be created for this configuration */
	StringResource resourceName();
}
