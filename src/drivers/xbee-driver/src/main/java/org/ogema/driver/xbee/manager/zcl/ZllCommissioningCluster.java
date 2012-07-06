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
package org.ogema.driver.xbee.manager.zcl;

import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.manager.Endpoint;

/**
 * The ZLL commissioning cluster shall have a cluster identifier of 0x1000. Those commands in the touchlink
 * commissioning command set shall be sent using the ZLL profile identifier, 0xc05e whereas those commands in the
 * commissioning utility command set shall sent using the ZHA profile identifier
 * 
 * @author puschas
 * 
 */
public class ZllCommissioningCluster extends Cluster {
	public ZllCommissioningCluster(Endpoint endpoint) {
		super((short) 0x1000, endpoint);
		setName("ZLL Commissioning");
		clusterId = 0x1000;
		this.endpoint = endpoint;

		// Touchlink
		clusterCommands.put((byte) 0x00, new ClusterCommand(this, (byte) 0x00, "Scan request", Constants.MANDATORY));
		clusterCommands.put((byte) 0x02, new ClusterCommand(this, (byte) 0x02, "Device information request",
				Constants.MANDATORY));
		clusterCommands
				.put((byte) 0x06, new ClusterCommand(this, (byte) 0x06, "Identify request", Constants.MANDATORY));
		clusterCommands.put((byte) 0x07, new ClusterCommand(this, (byte) 0x07, "Reset to factory new request",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x10, new ClusterCommand(this, (byte) 0x10, "Network start request",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x12, new ClusterCommand(this, (byte) 0x12, "Network join router request",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x14, new ClusterCommand(this, (byte) 0x14, "Network join end device request",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x16, new ClusterCommand(this, (byte) 0x16, "Network update request",
				Constants.MANDATORY));
		// Utility
		clusterCommands.put((byte) 0x41, new ClusterCommand(this, (byte) 0x41, "Get group identifiers request",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x42, new ClusterCommand(this, (byte) 0x42, "Get endpoint list request",
				Constants.MANDATORY));

	}
}
