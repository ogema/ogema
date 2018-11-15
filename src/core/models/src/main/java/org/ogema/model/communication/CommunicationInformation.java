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
