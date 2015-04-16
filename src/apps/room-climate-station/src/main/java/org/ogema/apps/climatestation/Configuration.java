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
package org.ogema.apps.climatestation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {

	private static final String ENTRY_NAME_ROOMID = "roomId";
	private JSONObject obj;
	private JSONArray roomsArray;
	private java.io.File roomConfigFile;

	public Configuration() {

		roomConfigFile = new File("./config/rcs.config");

		if (!roomConfigFile.exists()) {
			try {
				roomConfigFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		loadConfig();
	}

	public void loadConfig() {
		String json = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(roomConfigFile), 8);
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

		if (json.isEmpty() || (json == null)) {
			obj = new JSONObject();
			roomsArray = new JSONArray();
			try {
				obj.put("rooms", roomsArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else {

			try {
				obj = new JSONObject(json);
				roomsArray = obj.getJSONArray("rooms");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void addRoom(String roomId) {

		boolean roomIsExisting = false;

		for (int i = 0; i < roomsArray.length(); i++) {
			JSONObject room;
			try {
				room = roomsArray.getJSONObject(i);
				if (room.get(ENTRY_NAME_ROOMID).equals(roomId))
					roomIsExisting = true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (roomIsExisting == false) {

			JSONObject newRoom = new JSONObject();
			try {
				newRoom.put(ENTRY_NAME_ROOMID, roomId);
				roomsArray.put(newRoom);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void addSensor(String roomId, String resourcePath, String sensor) {

		for (int i = 0; i < roomsArray.length(); i++) {
			JSONObject room;
			try {
				room = roomsArray.getJSONObject(i);
				if (room.get(ENTRY_NAME_ROOMID).equals(roomId)) {
					switch (sensor) {
					case "temperatureInside":
						room.put("resourcePathTemperatureInside", resourcePath);
						break;
					case "humidityInside":
						room.put("resourcePathHumidityInside", resourcePath);
						break;
					case "temperatureOutside":
						room.put("resourcePathTemperatureOutside", resourcePath);
						break;
					case "humidityOutside":
						room.put("resourcePathHumidityOutside", resourcePath);
						break;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public JSONObject getRoom(String roomId) {

		JSONObject returnRoom = null;

		for (int i = 0; i < roomsArray.length(); i++) {
			JSONObject room;
			try {
				room = roomsArray.getJSONObject(i);
				if (room.get(ENTRY_NAME_ROOMID).equals(roomId)) {
					returnRoom = room;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return returnRoom;
	}

	public void saveConfig() {

		FileWriter file;
		try {
			file = new FileWriter(roomConfigFile);
			file.write(obj.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
