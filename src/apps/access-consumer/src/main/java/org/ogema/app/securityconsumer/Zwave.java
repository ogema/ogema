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
package org.ogema.app.securityconsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;

public class Zwave implements Runnable, ResourceValueListener<BooleanResource> {

	private BooleanResource onOff;
	private BooleanResource isOn;
	private volatile long timestamp;
	private volatile String level = new String();

	private static final String IP_PORT = "localhost:8083";
	private static final String STATUS_REQUEST = "http://" + IP_PORT + "/ZAutomation/api/v1/devices?since=";
	private static final String COMMAND = "http://" + IP_PORT
			+ "/ZAutomation/api/v1/devices/ZWayVDev_zway_4-0-37/command/";

	public Zwave(ApplicationManager driver) {

		ResourceManagement resourceManager = driver.getResourceManagement();

		SingleSwitchBox powerMeter = resourceManager.createResource("ZWave_Switch_Box_2", SingleSwitchBox.class);
		powerMeter.activate(true);

		// The on/off switch
		onOff = (BooleanResource) powerMeter.onOffSwitch().stateControl().create();
		onOff.activate(true);
		onOff.requestAccessMode(AccessMode.SHARED, AccessPriority.PRIO_HIGHEST);

		isOn = (BooleanResource) powerMeter.onOffSwitch().stateFeedback().create();
		isOn.activate(true);
		isOn.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_HIGHEST);

		// Add listener to register on/off commands
		onOff.addValueListener(this, true);

		timestamp = 0;
		new Thread(this).start();
	}

	private String doGET(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
		return result;
	}

	private void parseJSON(String input) {
		try {
			JSONObject jobj = new JSONObject(input);
			JSONObject data = jobj.getJSONObject("data");
			timestamp = data.getLong("updateTime");
			JSONArray devices = data.getJSONArray("devices");
			if (devices.length() > 0)
				level = devices.getJSONObject(0).getJSONObject("metrics").getString("level");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String answer = null;
		while (true && Activator.bundleIsRunning) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			answer = doGET(STATUS_REQUEST + String.valueOf(timestamp));
			if (answer == null)
				continue;
			parseJSON(answer);
			if (level.equals("on"))
				isOn.setValue(true);
			else if (level.equals("off"))
				isOn.setValue(false);
		}
	}

	@Override
	public void resourceChanged(BooleanResource resource) {
		boolean newValue = resource.getValue();
		if (newValue == true)
			doGET(COMMAND + "on");
		else if (newValue == false)
			doGET(COMMAND + "off");
	}

}
