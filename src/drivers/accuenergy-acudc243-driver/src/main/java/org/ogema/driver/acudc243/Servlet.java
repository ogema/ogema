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
package org.ogema.driver.acudc243;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.recordeddata.ReductionMode;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OutputStream bout;
	private AcuDC243Device device;

	Servlet(AcuDC243Device dev) {
		this.device = dev;
	}

	synchronized public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/script");
		bout = response.getOutputStream();

		switch (pi) {
		case "/getGraphData":
			bout.write(getGraphData().toString().getBytes());
			break;
		case "/getGraphDataHistory":
			bout.write(getGraphDataHistory().toString().getBytes());
			break;
		case "/getMeterList":
			// bout.write(getMeterList().toString().getBytes());
			break;
		}
	}

	// public JSONArray getMeterList() {
	//
	// JSONArray jsonArr = new JSONArray();
	// JSONObject json;
	//
	// List<AcuDC243Device> devices = driver.getDevices();
	//
	// for (int index = 0; index < devices.size(); index++) {
	// AcuDC243Device device = devices.get(index);
	// if (device != null) {
	// json = new JSONObject();
	// try {
	// json.put("name", device.getName());
	// jsonArr.put(json);
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// return jsonArr;
	// }
	//
	public JSONObject getGraphData() {

		JSONObject json = new JSONObject();

		// float amps = device.current.getValue();
		// float volts = device.voltage.getValue();
		float power = device.power.getValue();
		try {

			json.put("current_drs485de", ((float) Math.round(power * 10000)) / 10);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	public JSONArray getGraphDataHistory() {
		JSONArray json = new JSONArray();
		AcuDC243Device dev = device;// driver.getDevices().get(0);
		// float current = dev.dataResource.current.getValue();

		DateTime now = DateTime.now();
		List<SampledValue> list = dev.dataResource.current.getHistoricalData().getValues(now.minusDays(1).getMillis(),
				now.getMillis(), 60000, ReductionMode.AVERAGE);
		System.out.println("historical list :" + list);

		try {

			for (int i = 0; i < list.size() - 1; i++) {
				json.put(list.get(i).getValue().getDoubleValue() / 3600);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

}
