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
package org.ogema.apps.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.security.WebAccessManager;

public class SimulatorServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = false;

	StringBuffer sb;
	private Random rnd;

	float bfs = 0;
	int le = 0;

	private OutputStream bout;

	JSONObject json;
	JSONArray jsonPowerValues;

	SimulatorServlet(WebAccessManager wam) {
		rnd = new Random();
		sb = new StringBuffer(32);
		json = new JSONObject();
		jsonPowerValues = new JSONArray();
		wam.registerWebResource("/simu", this);
	}

	public byte[] getData() {
		// float[] solarData = solarPanel.getData();
		try {
			jsonPowerValues.put(0, rnd.nextInt(256));
			jsonPowerValues.put(1, rnd.nextInt(256));
			jsonPowerValues.put(2, rnd.nextInt(100));
			jsonPowerValues.put(3, rnd.nextInt(100));
			jsonPowerValues.put(4, rnd.nextInt(50));
			jsonPowerValues.put(5, rnd.nextInt(70));
			jsonPowerValues.put(6, rnd.nextInt(256));
			jsonPowerValues.put(7, rnd.nextInt(100));
			jsonPowerValues.put(8, rnd.nextInt(256));
			jsonPowerValues.put(9, rnd.nextInt(256));
			jsonPowerValues.put(10, rnd.nextInt(256));
			jsonPowerValues.put(11, rnd.nextInt(256));
			json.put("powerValues", jsonPowerValues);
			if (DEBUG)
				System.out.println(json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString().getBytes();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// String uri = request.getRequestURI();
		// String qs = request.getQueryString();
		// String sp = request.getServletPath();
		// String cp = request.getContextPath();
		// String pt = request.getPathTranslated();
		String pi = request.getPathInfo();

		byte[] arr;
		response.setContentType("text/script");
		bout = response.getOutputStream();
		if (pi == null) {
			arr = getData();

			bout.write(arr);
		}
		else if (pi.equals("/graph")) {
			bout.write(getCData());
		}
	}

	synchronized public byte[] getCData() {
		JSONObject json = new JSONObject();
		try {
			json.put("strType", "iSensor");
			json.put("strName", "wind");
			json.put("dblValue", rnd.nextInt(30));
			jsonPowerValues.put(0, json);

			json = new JSONObject();
			json.put("strType", "iSensor");
			json.put("strName", "solar");
			json.put("dblValue", rnd.nextInt(50));
			jsonPowerValues.put(1, json);

			json = new JSONObject();
			json.put("strType", "iSensor");
			json.put("strName", "velocity");
			json.put("dblValue", rnd.nextInt(30));
			jsonPowerValues.put(2, json);

			// json=new JSONObject();
			// json.put("strType", "iSensor");
			// json.put("strName", "Frequency");
			// json.put("dblValue",rnd.nextInt(256));
			// jsonPowerValues.put(3,json);
			//
			//
			// json=new JSONObject();
			// json.put("strType", "iSensor");
			// json.put("strName", "RealPower");
			// json.put("dblValue", rnd.nextInt(256));
			// jsonPowerValues.put(4,json);

			// if (DEBUG)
			// System.out.println(json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonPowerValues.toString().getBytes();
	}
}
