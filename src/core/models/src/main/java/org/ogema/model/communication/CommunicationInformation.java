/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.model.communication;

import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Data;

/**
 * Information about a communication channel to an extern source, like a 
 * connected device or a web-server providing information. Communication
 * information can be attached to pretty much any data model, but is usually
 * not relevant for exchanging information between applications. Instead, the
 * information is usually only relevant for device drivers that wish to 
 * persistently store their information. Hence, this resource type is not
 * contained in any models, but intended to be used by drivers as a decorator
 * on their own discretion.
 */
public interface CommunicationInformation extends Data {

	/**
	 * Current status of the communication.
	 */
	CommunicationStatus communicationStatus();

	/** 
	 * Configuration for drivers polling from the device 
	 */
	PollingConfiguration pollingConfiguration();

	/**
	 * Communication address to be used for upper-level identification/user interface. Details to be defined by driver.
	 */
	StringResource communicationAddress();

	/** 
	 * Time when last message was received from device 
	 */
	TimeResource lastTimeReceive();

	/** 
	 * Time when last message was sent to device 
	 */
	TimeResource lastTimeSend();

	/** Address(es) on which device can be contacted */
	DeviceAddress comAddress();
}
