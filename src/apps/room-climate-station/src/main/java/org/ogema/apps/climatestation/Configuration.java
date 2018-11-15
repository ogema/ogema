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
