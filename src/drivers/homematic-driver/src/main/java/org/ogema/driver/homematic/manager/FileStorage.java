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
package org.ogema.driver.homematic.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

public class FileStorage {

	private java.io.File deviceConfigFile;
	private LocalDevice localDevice;

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	public FileStorage(LocalDevice localDevice) {
		deviceConfigFile = new File("./config", "homematic.devices");
		this.localDevice = localDevice;
		if (deviceConfigFile.exists())
			loadDeviceConfig();
	}

	public void loadDeviceConfig() {
		String json = null;
		JSONObject jdata;
		JSONArray devices;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(deviceConfigFile), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			reader.close();
			json = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (json.isEmpty() || (json == null))
			return;

		try {
			jdata = new JSONObject(json);

			if (!jdata.getString("interface").equals("USB"))
				return;

			devices = jdata.getJSONArray("devices");
			for (int i = 0; i < devices.length(); i++) {
				JSONObject dev = devices.getJSONObject(i);
				RemoteDevice temp_device = new RemoteDevice(localDevice, dev.getString("address"), dev
						.getString("type"), dev.getString("serial"));
				if ((localDevice.getDevices().get(temp_device.getAddress())) == null) {
					localDevice.getDevices().put(temp_device.getAddress(), temp_device);
					logger.debug("Device added: " + temp_device.getAddress());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void saveDeviceConfig() {
		final JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		try {
			Iterator<Entry<String, RemoteDevice>> devicesIt = localDevice.getDevices().entrySet().iterator();
			while (devicesIt.hasNext()) {
				Map.Entry<String, RemoteDevice> devicesEntry = devicesIt.next();
				RemoteDevice remoteDevice = devicesEntry.getValue();
				JSONObject dev = new JSONObject();
				dev.put("address", remoteDevice.getAddress());
				dev.put("type", remoteDevice.getDeviceType());
				dev.put("serial", remoteDevice.getSerial());
				arr.put(dev);
			}
			obj.put("interface", "USB");
			obj.put("devices", arr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		AccessController.doPrivileged(new PrivilegedAction<FileWriter>() {
			public FileWriter run() {
				FileWriter result = null;
				try {
					result = new FileWriter(deviceConfigFile);
					result.write(obj.toString());
					result.flush();
					result.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			}
		});
	}
}
