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
package org.ogema.driver.hmhl;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;

public class HM_hlConfig {
	public String interfaceId;
	public String driverId = "homematic-driver";
	public String deviceAddress;
	public String deviceParameters;
	public String channelAddress;
	public String deviceId; // Actually the ID that identifies the type of Endpoint
	public long timeout;
	public String resourceName;
	public ChannelLocator chLocator;
}
