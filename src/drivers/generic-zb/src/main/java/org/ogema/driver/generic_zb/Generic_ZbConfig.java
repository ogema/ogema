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
package org.ogema.driver.generic_zb;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;

public class Generic_ZbConfig {
	public String interfaceId;
	public String driverId = "xbee-driver";
	public String deviceAddress;
	public String deviceParameters;
	public String channelAddress;
	public short deviceId; // Actually the ID that identifies the type of Endpoint
	public long timeout;
	public String resourceName;
	public ChannelLocator chLocator;
}
