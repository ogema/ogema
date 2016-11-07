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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.SmokeDetector;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.WaterDetector;

public class Rooms {

	private static final Object OUTSIDE_ROOM_NAME = "OUTSIDE";

	private ResourceAccess resAcc;
	private OgemaLogger logger;

	private ResourceManagement resMan;
	ResourceList<Room> roomsList;
	HashMap<String, HomeRoom> roomsMap;
	HashMap<String, List<String>> typeSensors;

	static TemperatureResource outsideTemp;

	static FloatResource outsideHumidity;

	static Room outside;

	@SuppressWarnings("unchecked")
	public Rooms(ApplicationManager appman) {
		this.resMan = appman.getResourceManagement();
		this.resAcc = appman.getResourceAccess();
		this.logger = appman.getLogger();
		roomsMap = new HashMap<String, HomeRoom>();
		roomsList = resAcc.getResource("MyHome");
		if (roomsList == null) {
			roomsList = resMan.createResource("MyHome", ResourceList.class);
			roomsList.setElementType(Room.class);
			roomsList.activate(false);
		}
		else {
			if (roomsList.getElementType() == null)
				roomsList.setElementType(Room.class);
			List<Room> allRooms = roomsList.getAllElements();
			for (Room room : allRooms) {
				String name = room.name().getValue();
				HomeRoom hr = new HomeRoom(resAcc, logger, room);
				hr.theRoom = room;
				hr.roomId = name;
				roomsMap.put(name, hr);
				if (name.equals(OUTSIDE_ROOM_NAME)) {
					outside = room;
				}
				hr.createDimmerAction();
			}
		}
		if (outside != null) {
			outsideTemp = outside.temperatureSensor().reading();
			outsideHumidity = outside.humiditySensor().reading();
		}
		initTypeSensorsMap();
	}

	static final String SENSOR_NAME_TEMPERATURE = "Temperature";
	static final String SENSOR_NAME_HUMIDITY = "Humidity";
	static final String SENSOR_NAME_LIGHT_SWICHT = "Light Switch";
	static final String SENSOR_NAME_LIGHT_DIMMER = "Light Dimmer";
	static final String SENSOR_NAME_LIGHT = "Light";
	static final String SENSOR_NAME_SWBOX1 = "Switch Box 1";
	static final String SENSOR_NAME_SWBOX2 = "Switch Box 2";
	static final String SENSOR_NAME_SWBOX_SWITCH1 = "Supply Switch 1";
	static final String SENSOR_NAME_SWBOX_SWITCH2 = "Supply Switch 2";
	static final String SENSOR_NAME_MOTION = "Motion Sensor";
	static final String SENSOR_NAME_WATER = "Water Sensor";
	static final String SENSOR_NAME_SMOKE = "Smoke Sensor";

	private void initTypeSensorsMap() {
		String type;
		Vector<String> sensors = new Vector<String>();
		typeSensors = new HashMap<String, List<String>>();

		type = TemperatureSensor.class.getName();
		sensors.add(SENSOR_NAME_TEMPERATURE);
		typeSensors.put(type, sensors);

		type = HumiditySensor.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_HUMIDITY);
		typeSensors.put(type, sensors);

		type = BooleanResource.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_LIGHT_SWICHT);
		sensors.add(SENSOR_NAME_LIGHT_DIMMER);
		sensors.add(SENSOR_NAME_SWBOX_SWITCH2);
		sensors.add(SENSOR_NAME_SWBOX_SWITCH1);
		typeSensors.put(type, sensors);

		type = ElectricDimmer.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_LIGHT);
		typeSensors.put(type, sensors);

		type = SingleSwitchBox.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_SWBOX1);
		sensors.add(SENSOR_NAME_SWBOX2);
		typeSensors.put(type, sensors);

		type = MotionSensor.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_MOTION);
		typeSensors.put(type, sensors);

		type = WaterDetector.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_WATER);
		typeSensors.put(type, sensors);

		type = SmokeDetector.class.getName();
		sensors = new Vector<String>();
		sensors.add(SENSOR_NAME_SMOKE);
		typeSensors.put(type, sensors);
	}

	static final JSONObject empty = new JSONObject();

	public JSONObject getRoomData(String roomId) {
		HomeRoom room = roomsMap.get(roomId);
		if (room == null)
			return empty;
		else
			return room.getRoomData();
	}

	public String setResource4Sensor(String roomId, String resourcePath, String sensor) {
		HomeRoom hr = getRoom(roomId);
		Room room;
		int type = 1;
		if (hr == null) {
			if (roomsList.getElementType() == null)
				roomsList.setElementType(Room.class);
			room = roomsList.add();
			hr = new HomeRoom(resAcc, logger, room);
			room.activate(false);
			StringResource name = room.name();
			name.create().activate(false);
			name.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_LOWEST);
			room.name().setValue(roomId);

			IntegerResource typeRes = room.type();
			typeRes.create().activate(false);
			name.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_LOWEST);
			room.type().setValue(type);
			hr.theRoom = room;
			hr.roomId = roomId;
			roomsMap.put(roomId, hr);
		}
		else
			room = hr.theRoom;
		String result = hr.setResource4Sensor(resourcePath, sensor);
		if (roomId.equals(OUTSIDE_ROOM_NAME)) {
			outside = room;
			type = 0;
			outsideTemp = room.temperatureSensor().reading();
			outsideHumidity = room.humiditySensor().reading();
		}
		return result;
	}

	private HomeRoom getRoom(String roomId) {
		return roomsMap.get(roomId);
	}

	static int determinePriority(int messageID) {
		switch (messageID) {
		case 1:
		case 7:
		case 16:
			return 1;
		case 3:
		case 5:
		case 8:
		case 11:
		case 15:
		case 18:
			return 2;
		case 0:
		case 2:
		case 4:
		case 6:
		case 9:
		case 13:
		case 17:
		case 36:
			return 3;
		case 10:
		case 12:
		case 14:
		case 19:
			return 4;
		}
		return 0;
	}

	static String determineMessage(int messageID) {
		try {
			return Constants.messages[messageID];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public static int determineMessageID(float t_in, float rh_in, float ah_in, float t_out, float rh_out, float ah_out) {
		// check conditions for ID_1
		// check conditions for ID_2
		// check conditions for ID_3
		if ((Float.isNaN(t_in) || Float.isNaN(rh_in) || Float.isNaN(t_out) || Float.isNaN(rh_out)))
			return 0;
		if (t_in > 26 && t_out > 26) {
			return 3;
		}

		// check conditions for ID_4
		if (t_in > 26 && t_out > 15 && t_out <= 26) {
			if (rh_in > 20 && rh_in <= 70) {
				return 32;
			}
			else {
				if (rh_in >= 70 && rh_in <= 80 && ah_out < ah_in) {
					return 33;
				}
				else {
					return 4;
				}
			}
		}

		// check conditions for ID_5
		if (t_in > 26 && t_out <= 15) {
			if (rh_in >= 70 && rh_in <= 80 && ah_out < ah_in) {
				return 34;
			}
			else {
				return 5;
			}
		}

		// check conditions for ID_6
		if (t_in > 24 && t_out > 25) {
			return 6;
		}

		// check conditions for ID_7
		if (t_in >= 16 && t_in <= 26) {
			return 7;
		}

		// check conditions for ID_8
		if (t_in <= 16) {
			if (rh_in >= 70 && rh_in <= 80 && ah_out < ah_in) {
				return 35;
			}
			else {
				return 8;
			}
		}

		// check conditions for ID_9
		// check conditions for ID_10
		// check conditions for ID_11
		// check conditions for ID_12
		// check conditions for ID_13
		// check conditions for ID_14
		// check conditions for ID_15
		if (rh_in <= 20) {
			return 15;
		}

		// check conditions for ID_16
		if (rh_in <= 20 && rh_in <= 70) {
			return 16;
		}

		// check conditions for ID_17
		if (rh_in >= 70 && rh_in <= 80 && ah_out < ah_in) {
			return 17;
		}

		// check conditions for ID_18
		if (rh_in < 70 && ah_out >= ah_in) {
			return 18;
		}

		// check conditions for ID_19
		if (rh_in > 80) {
			return 19;
		}

		// if there is no ID for which the conditions were fulfilled return 0
		return Constants.DEFAULT_MESSAGEID;
	}

	public String resetRoomSensors(String roomId) {
		String result = "Alle Sensoren des Raumes " + roomId + " wurden freigegeben!";
		Room room = getRoom(roomId).theRoom;
		if (room != null) {
			roomsList.remove(room);
			room.delete();
		}
		else
			result = "Der Raum " + roomId + " wurde noch nicht angelegt!";
		return result;
	}

	public JSONArray getMatchingSensors(String type) {
		JSONArray sensors = new JSONArray();
		List<String> list = typeSensors.get(type);
		if (list != null)
			for (String str : list) {
				sensors.put(str);
			}
		return sensors;
	}
}
