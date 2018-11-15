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
