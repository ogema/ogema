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
package org.ogema.channelmapperv2.config;

import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.model.prototypes.Configuration;

public interface ChannelMapperConfiguration extends Configuration {
	
	/**
	 * Must be a reference
	 * @return
	 */
	SingleValueResource target(); 
	
	/**
	 * The channel identifier.
	 * 
	 * @see ChannelLocator
	 * @return
	 */
	PersistentChannelLocator channelLocator();
	
	/**
	 * Admissible values:
	 * <ul>
	 * 	<li>INPUT read channel value and write it to the target resource
	 * 	<li>OUTPUT write resource value to channel
	 * 	<li>INOUT read from and write to channel
	 * </ul>
	 * 
	 * @see Direction
	 * @return
	 */
	StringResource direction();
	
	/**
	 * In ms. Only relevant for direction INPUT and INOUT.
	 * @return
	 */
	TimeResource samplingInterval();
	
	/**
	 * Scaling factor between channel value and resource value (resource value = channel value * scalingFactor + valueOffset)
	 * @return
	 */
	FloatResource scalingFactor();
	
	/**
	 * Offset between channel value and resource value (resource value = channel value * scalingFactor + valueOffset)
	 * @return
	 */
	FloatResource valueOffset();
	
	/**
	 * Optional. A human-readable description of the value.
	 * @return
	 */
	StringResource description();
	
	/**
	 * Indicates whether the channel mapping has been successful, or is still pending.
	 * @return
	 */
	BooleanResource registrationSuccessful();

}
