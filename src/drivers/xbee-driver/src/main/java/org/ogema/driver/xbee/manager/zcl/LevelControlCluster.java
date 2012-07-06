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
 * @see ZigBee Cluster Library pages 160 - 168.
 */
public final class LevelControlCluster extends Cluster {
	public LevelControlCluster(Endpoint endpoint) {
		super((short) 0x0008, endpoint);
		setName("Level Control");
		clusterId = 0x0008;
		this.endpoint = endpoint;

		clusterAttributes.put((short) 0x0000, new ClusterAttribute(this, (short) 0x0000, "CurrentLevel",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0001, new ClusterAttribute(this, (short) 0x0001, "RemainingTime",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0010, new ClusterAttribute(this, (short) 0x0010, "OnOffTransitionTime",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0011, new ClusterAttribute(this, (short) 0x0011, "OnLevel",
				Constants.READ_WRITE, Constants.OPTIONAL));

		clusterCommands.put((byte) 0x00, new ClusterCommand(this, (byte) 0x00, "Move to Level", Constants.MANDATORY));
		clusterCommands.put((byte) 0x01, new ClusterCommand(this, (byte) 0x01, "Move", Constants.MANDATORY));
		clusterCommands.put((byte) 0x02, new ClusterCommand(this, (byte) 0x02, "Step", Constants.MANDATORY));
		clusterCommands.put((byte) 0x03, new ClusterCommand(this, (byte) 0x03, "Stop", Constants.MANDATORY));
		clusterCommands.put((byte) 0x04, new ClusterCommand(this, (byte) 0x04, "Move to Level (with On/Off)",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x05, new ClusterCommand(this, (byte) 0x05, "Move (with On/Off)",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x06, new ClusterCommand(this, (byte) 0x06, "Step (with On/Off)",
				Constants.MANDATORY));
		clusterCommands.put((byte) 0x07, new ClusterCommand(this, (byte) 0x07, "Stop", Constants.MANDATORY));

	}
}
