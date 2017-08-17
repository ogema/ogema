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
package org.ogema.channelmapperv2.impl;

import org.ogema.channelmapperv2.config.PersistentChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class ChannelMapperUtil {
	
	private static final String getErrorMsg(String cl) {
		return "Passed argument is not a serialized channel locator: " + cl + ". Requires "
				+ "format \"driverName:interfaceName:deviceAddress:parameters:channelAddress";
	}
	
	static ChannelLocator getChannelLocator(String channelLocatorString) throws IllegalArgumentException {
		String[] components = channelLocatorString.split(":");
		if (components.length < 5)
			throw new IllegalArgumentException(getErrorMsg(channelLocatorString));
		StringBuilder sb  =new StringBuilder();
		for (int i=3;i<components.length-1;i++) 
			sb.append(components[i]);
		DeviceLocator dloc = new DeviceLocator(components[0], components[1], components[2], sb.toString());
		ChannelLocator cloc = new ChannelLocator(components[components.length-1], dloc);
		return cloc;
	}
	
	static ChannelLocator getChannelLocator(PersistentChannelLocator pcl) throws IllegalArgumentException {
		DeviceLocator dloc = new DeviceLocator(pcl.driverId().getValue(), pcl.interfaceId().getValue(), pcl.deviceAddress().getValue(), pcl.parameters().getValue());
		ChannelLocator cloc = new ChannelLocator(pcl.channelAddress().getValue(), dloc);
		return cloc;
	}

}
