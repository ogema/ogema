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
 * @see ZigBee Cluster Library pages 108-114.
 */
public class BasicCluster extends Cluster {

	public BasicCluster(Endpoint endpoint) {
		super((short) 0x0000, endpoint);
		setName("Basic");
		clusterId = 0x0000;
		this.endpoint = endpoint;
		clusterAttributes.put((short) 0x0000, new ClusterAttribute(this, (short) 0x0000, "ZCLVersion",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0001, new ClusterAttribute(this, (short) 0x0001, "ApplicationVersion",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0002, new ClusterAttribute(this, (short) 0x0002, "StackVersion",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0003, new ClusterAttribute(this, (short) 0x0003, "HWVersion",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0004, new ClusterAttribute(this, (short) 0x0004, "ManufacturerName",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0005, new ClusterAttribute(this, (short) 0x0005, "ModelIdentifier",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0006, new ClusterAttribute(this, (short) 0x0006, "DateCode",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0007, new ClusterAttribute(this, (short) 0x0007, "PowerSource",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0010, new ClusterAttribute(this, (short) 0x0010, "LocationDescription",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0011, new ClusterAttribute(this, (short) 0x0011, "PhysicalEnvironment",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0012, new ClusterAttribute(this, (short) 0x0012, "DeviceEnabled",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0013, new ClusterAttribute(this, (short) 0x0013, "AlarmMask",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0014, new ClusterAttribute(this, (short) 0x0014, "DisableLocalConfig",
				Constants.READ_WRITE, Constants.OPTIONAL));

		clusterCommands.put((byte) 0x00, new ClusterCommand(this, (byte) 0x00, "Reset to Factory Defaults",
				Constants.OPTIONAL));
	}
}
