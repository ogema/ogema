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
