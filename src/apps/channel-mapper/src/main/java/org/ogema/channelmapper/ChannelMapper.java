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
package org.ogema.channelmapper;

import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.model.Resource;

/**
 * A generic high-level driver for OGEMA. It is responsible for mapping OGEMA resource elements to channels of the
 * ChannelAPI.
 */
public interface ChannelMapper {

	/**
	 * App can start if ChannelMapper has finish the start Methode
	 * 
	 * @return
	 */
	boolean getAvailableFlag();

	/**
	 * Create a new resource. The newly created resource can be used to map channels to basic sub resources.
	 * 
	 * @param resourceType
	 *            example: "SmartMeter.class"
	 * @param resourceName
	 *            example: "Wago"
	 * @throws ResourceMappingException
	 */
	void addMappedResource(Class<? extends Resource> resourceType, String resourceName) throws ResourceMappingException;

	/**
	 * 
	 * Delete MappedRessource from ChannelMapper Configuration. Ressource still exist at the ResourcenManager
	 * 
	 * @param resourceName
	 * @throws ResourceMappingException
	 */
	void deleteMappedResource(String resourceName) throws ResourceMappingException;

	/**
	 * Map a channel to a resource element. Connects a measurement or control channel to a resource. The connection
	 * (mapping) is persistently stored until the unmapChannel method is called for the channel.
	 * 
	 * @param channel
	 *            the channel to be mapped
	 * @param resourceElementPath
	 *            the path to the resource element (see path syntax).
	 */
	public void mapChannelToResource(ChannelLocator channel, String resourceElementPath, Direction direction,
			long samplingPeriod, double scalingFactor, double valueOffset) throws ResourceMappingException;

	/**
	 * Unmap a channel. Remove all connections from the specified channel to OGEMA resources.
	 * 
	 * @param channel
	 *            the channel to be unmapped.
	 */
	void unmapChannel(ChannelLocator channel);

	/**
	 * Unmap a channel. Remove a single connection from the specified channel to an OGEMA resource.
	 * 
	 * @param channel
	 *            the channel to be unmapped
	 */
	void unmapChannel(ChannelLocator channel, String resourceElementPath);
}
