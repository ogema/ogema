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
package org.ogema.drivers.lemoneg;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;

/**
 * Configuration Resource Model for the LEMONEG Power Measurement System device driver
 * 
 * @author pau
 * 
 */
public interface LemonegConfigurationModel extends Resource {

	/** Interface ID for channel driver interface */
	StringResource interfaceId();

	/** Driver ID for channel driver interface */
	StringResource driverId();

	/** Device Address for channel driver interface */
	StringResource deviceAddress();

	/** Device Parameter for channel driver interface */
	StringResource deviceParameters();

	/** Channel Address for channel driver interface */
	StringResource channelAddress();

	/** Update Time in ms */
	IntegerResource timeout();

	/** Name of the LemonegDataModel to be created for this configuration */
	StringResource resourceName();
}
