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
package org.ogema.driver.hmhl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

public class DeviceDescriptor {

	private JSONObject jdata;
	private String json;
	private Iterator<String> Itr;
	private Map<String, JSONObject> types = new HashMap<String, JSONObject>();
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("hm_hl");

	public DeviceDescriptor() {
		InputStream is = getClass().getClassLoader().getResourceAsStream("deviceTypes.json");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {

		}
		try {
			jdata = new JSONObject(json);
			@SuppressWarnings("unchecked")
			Iterator<String> keys = jdata.keys();
			Itr = keys;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (Itr.hasNext()) {
			String s = Itr.next();
			try {
				types.put(s, jdata.getJSONObject(s));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * JSONObject: name st cyc rxt lst chn
	 */

	public String getName(String type) {
		String name = null;
		try {
			name = types.get(type).getString("name");
		} catch (JSONException e) {
			logger.error("Homematic device Type " + type + " unknown");
		}
		return name;
	}

	public String getSubType(String type) {
		String classType = null;
		try {
			classType = types.get(type).getString("st");
		} catch (JSONException e) {
			logger.error("Homematic device Type " + type + " unknown");
		}
		return classType;
	}

	public String[] getChannels(String type) {
		String chnstr = null;
		String[] channels = null;
		try {
			chnstr = types.get(type).getString("chn");
			channels = chnstr.split(",");
		} catch (JSONException e) {
			logger.error("Homematic device Type " + type + " unknown");
		}
		return channels;
	}

	public String[] getLists(String type) {
		String lststr = null;
		String[] lists = null;
		try {
			lststr = types.get(type).getString("lst");
			lists = lststr.split(",");
		} catch (JSONException e) {
			logger.error("Homematic device Type " + type + " unknown");
		}
		return lists;
	}

	// TODO: Implement all features

}
