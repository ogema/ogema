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
