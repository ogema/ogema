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
package org.ogema.driver.xbee.manager.zcl;

import org.ogema.driver.xbee.Constants;
import org.ogema.driver.xbee.manager.Endpoint;

/**
 * 
 * @author puschas
 * @see ZigBee Cluser Library pages 141-155.
 */
public class ScenesCluster extends Cluster {
	public ScenesCluster(Endpoint endpoint) {
		super((short) 0x0005, endpoint);
		setName("Scenes");
		clusterId = 0x0005;
		this.endpoint = endpoint;

		clusterAttributes.put((short) 0x0000, new ClusterAttribute(this, (short) 0x0000, "SceneCount",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0001, new ClusterAttribute(this, (short) 0x0001, "CurrentScene",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0002, new ClusterAttribute(this, (short) 0x0002, "CurrentGroup",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0003, new ClusterAttribute(this, (short) 0x0003, "SceneValid",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0004, new ClusterAttribute(this, (short) 0x0004, "NameSupport",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0005, new ClusterAttribute(this, (short) 0x0005, "LastConfiguredBy",
				Constants.READ_ONLY, Constants.OPTIONAL));

		clusterCommands.put((byte) 0x00, new ClusterCommand(this, (byte) 0x00, "Add scene", Constants.MANDATORY));
		clusterCommands.put((byte) 0x01, new ClusterCommand(this, (byte) 0x01, "View scene", Constants.MANDATORY));
		clusterCommands.put((byte) 0x02, new ClusterCommand(this, (byte) 0x02, "Remove scene", Constants.MANDATORY));
		clusterCommands.put((byte) 0x03,
				new ClusterCommand(this, (byte) 0x03, "Remove all scenes", Constants.MANDATORY));
		clusterCommands.put((byte) 0x04, new ClusterCommand(this, (byte) 0x04, "Store scene", Constants.MANDATORY));
		clusterCommands.put((byte) 0x05, new ClusterCommand(this, (byte) 0x05, "Recall scene", Constants.MANDATORY));
		clusterCommands.put((byte) 0x06, new ClusterCommand(this, (byte) 0x06, "Get scene membership",
				Constants.MANDATORY));
		// TODO: Responses?
	}
}
