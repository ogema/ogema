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
 * 
 * @author puschas
 * @see ZigBee Cluster Library pages 155 - 157.
 */
public final class OnOffCluster extends Cluster {
	public OnOffCluster(Endpoint endpoint) {
		super((short) 0x0006, endpoint);
		setName("On/Off");
		clusterId = 0x0006;
		this.endpoint = endpoint;

		clusterAttributes.put((short) 0x0000, new ClusterAttribute(this, (short) 0x0000, "OnOff", Constants.READ_ONLY,
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x00, new ClusterCommand(this, (byte) 0x00, "Off", Constants.MANDATORY));
		clusterCommands.put((byte) 0x01, new ClusterCommand(this, (byte) 0x01, "On", Constants.MANDATORY));
		clusterCommands.put((byte) 0x02, new ClusterCommand(this, (byte) 0x02, "Toggle", Constants.MANDATORY));
	}
}
