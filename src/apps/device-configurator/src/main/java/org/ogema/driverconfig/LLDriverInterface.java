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
package org.ogema.driverconfig;

import org.json.JSONArray;
import org.json.JSONObject;

public interface LLDriverInterface {
	public void addConnection(String hardwareIdentifier);

	public void addConnectionViaPort(String portName);

	public JSONObject showClusterDetails(String interfaceId, String device, String endpoint, String clusterId);

	public JSONArray showDeviceDetails(String interfaceId, String deviceAddress);

	public JSONArray showAllCreatedChannels();

	public JSONObject showHardware();

	public JSONObject showNetwork(String option);

	public JSONObject scanForDevices();

	public String whichTech();

	public String whichID();

	public JSONObject cacheDevices();
}
