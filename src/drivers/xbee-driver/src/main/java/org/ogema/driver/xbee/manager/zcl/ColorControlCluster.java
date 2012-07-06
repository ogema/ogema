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
 * @see ZigBee Cluster Library pages 300 - 321.
 */
public final class ColorControlCluster extends Cluster {
	public ColorControlCluster(Endpoint endpoint) {
		super((short) 0x0300, endpoint);
		setName("Color Control");
		clusterId = 0x0300;
		this.endpoint = endpoint;

		clusterAttributes.put((short) 0x0000, new ClusterAttribute(this, (short) 0x0000, "CurrentHue",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0001, new ClusterAttribute(this, (short) 0x0001, "CurrentSaturation",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0002, new ClusterAttribute(this, (short) 0x0002, "RemainingTime",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0003, new ClusterAttribute(this, (short) 0x0003, "CurrentX",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0004, new ClusterAttribute(this, (short) 0x0004, "CurrentY",
				Constants.READ_ONLY, Constants.MANDATORY));
		clusterAttributes.put((short) 0x0005, new ClusterAttribute(this, (short) 0x0005, "DriftCompensation",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0006, new ClusterAttribute(this, (short) 0x0006, "CompensationText",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0007, new ClusterAttribute(this, (short) 0x0007, "ColorTemperature",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0008, new ClusterAttribute(this, (short) 0x0008, "ColorMode",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0010, new ClusterAttribute(this, (short) 0x0010, "NumberOfPrimaries",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0011, new ClusterAttribute(this, (short) 0x0011, "Primary1X",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0012, new ClusterAttribute(this, (short) 0x0012, "Primary1Y",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0013, new ClusterAttribute(this, (short) 0x0013, "Primary1Intensity",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0015, new ClusterAttribute(this, (short) 0x0015, "Primary2X",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0016, new ClusterAttribute(this, (short) 0x0016, "Primary2Y",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0017, new ClusterAttribute(this, (short) 0x0017, "Primary2Intensity",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0019, new ClusterAttribute(this, (short) 0x0019, "Primary3X",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x001a, new ClusterAttribute(this, (short) 0x001a, "Primary3Y",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x001b, new ClusterAttribute(this, (short) 0x001b, "Primary3Intensity",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0020, new ClusterAttribute(this, (short) 0x0020, "Primary4X",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0021, new ClusterAttribute(this, (short) 0x0021, "Primary4Y",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0022, new ClusterAttribute(this, (short) 0x0022, "Primary4Intensity",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0024, new ClusterAttribute(this, (short) 0x0024, "Primary5X",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0025, new ClusterAttribute(this, (short) 0x0025, "Primary5Y",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0026, new ClusterAttribute(this, (short) 0x0026, "Primary5Intensity",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0028, new ClusterAttribute(this, (short) 0x0028, "Primary6X",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0029, new ClusterAttribute(this, (short) 0x0029, "Primary6Y",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x002a, new ClusterAttribute(this, (short) 0x002a, "Primary6Intensity",
				Constants.READ_ONLY, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0030, new ClusterAttribute(this, (short) 0x0030, "WhitePointX",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0031, new ClusterAttribute(this, (short) 0x0031, "WhitePointY",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0032, new ClusterAttribute(this, (short) 0x0032, "ColorPointRX",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0033, new ClusterAttribute(this, (short) 0x0033, "ColorPointRY",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0034, new ClusterAttribute(this, (short) 0x0034, "ColorPointRIntensity",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0036, new ClusterAttribute(this, (short) 0x0036, "ColorPointGX",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0037, new ClusterAttribute(this, (short) 0x0037, "ColorPointGY",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x0038, new ClusterAttribute(this, (short) 0x0038, "ColorPointGIntensity",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x003a, new ClusterAttribute(this, (short) 0x003a, "ColorPointBX",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x003b, new ClusterAttribute(this, (short) 0x003b, "ColorPointBY",
				Constants.READ_WRITE, Constants.OPTIONAL));
		clusterAttributes.put((short) 0x003c, new ClusterAttribute(this, (short) 0x003c, "ColorPointBIntensity",
				Constants.READ_WRITE, Constants.OPTIONAL));

		clusterCommands.put((byte) 0x00, new ClusterCommand(this, (byte) 0x00, "Move to Hue", Constants.OPTIONAL));
		clusterCommands.put((byte) 0x01, new ClusterCommand(this, (byte) 0x01, "Move Hue", Constants.OPTIONAL));
		clusterCommands.put((byte) 0x02, new ClusterCommand(this, (byte) 0x02, "Step Hue", Constants.OPTIONAL));
		clusterCommands.put((byte) 0x03,
				new ClusterCommand(this, (byte) 0x03, "Move to Saturation", Constants.OPTIONAL));
		clusterCommands.put((byte) 0x04, new ClusterCommand(this, (byte) 0x04, "Move Saturation", Constants.OPTIONAL));
		clusterCommands.put((byte) 0x05, new ClusterCommand(this, (byte) 0x05, "Step Saturation", Constants.OPTIONAL));
		clusterCommands.put((byte) 0x06, new ClusterCommand(this, (byte) 0x06, "Move to Hue and Saturation",
				Constants.OPTIONAL));
		clusterCommands.put((byte) 0x07, new ClusterCommand(this, (byte) 0x07, "Move to Color", Constants.MANDATORY));
		clusterCommands.put((byte) 0x08, new ClusterCommand(this, (byte) 0x08, "Move Color", Constants.MANDATORY));
		clusterCommands.put((byte) 0x09, new ClusterCommand(this, (byte) 0x09, "Step Color", Constants.MANDATORY));
		clusterCommands.put((byte) 0x0a, new ClusterCommand(this, (byte) 0x0a, "Move to Color Temperature",
				Constants.OPTIONAL));
	}
}
