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
package org.ogema.apps.climatestation;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.TemperatureSensor;

public class Rooms {

	private static final String LIGHT_DECORATOR_NAME = "lightControl";
	private static final String SWITCH_BOX_DECORATOR_NAME = "switchBox";
	private static final Object OUTSIDE_ROOM_NAME = "OUTSIDE";
	private static final String LIGHT_DIMMER_SOURCE_NAME = "dimmerSourceSwitch";
	private ResourceAccess resAcc;
	private OgemaLogger logger;

	private ResourceManagement resMan;
	ResourceList<Room> roomsList;
	HashMap<String, Room> roomsMap;

	private Room outside;

	@SuppressWarnings("unchecked")
	public Rooms(ApplicationManager appman) {
		this.resMan = appman.getResourceManagement();
		this.resAcc = appman.getResourceAccess();
		this.logger = appman.getLogger();
		roomsMap = new HashMap<String, Room>();
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
				roomsMap.put(name, room);
				if (name.equals(OUTSIDE_ROOM_NAME)) {
					outside = room;
				}
				// Check if a DimmerAction is to be created
				StringResource dimmerSourcePath = room.getSubResource(LIGHT_DIMMER_SOURCE_NAME);
				if (dimmerSourcePath != null) {
					BooleanResource res = resAcc.getResource(dimmerSourcePath.getValue());
					if (res == null || !(res instanceof BooleanResource)) {
						logger.error("Dimmer switch resource could not be found!");
					}
					BooleanResource dimmer = (BooleanResource) res;
					// Check if the room is already decorated with an ElectricLight
					ElectricDimmer el = room.getSubResource(LIGHT_DECORATOR_NAME);
					if (el != null) {
						DimmerAction da = new DimmerAction();
						da.setSource(dimmer);
						da.setTarget(el);
					}
				}
			}
		}
	}

	public JSONObject getRoomData(String roomId) {
		JSONObject roomData = new JSONObject();
		int messageID;
		Room room = roomsMap.get(roomId);
		float tempInside = Float.NaN, rhInside = Float.NaN, ahInside = Float.NaN;
		if (room != null) {
			try {
				TemperatureSensor tsin = room.temperatureSensor();
				if (tsin != null) {
					tempInside = tsin.reading().getValue();
					tempInside = tempInside == 0 ? Float.NaN : tempInside - 273.15f;
					roomData.put(Constants.JSON_TEMPIN_NAME, tempInside);
				}
				else
					tempInside = Float.NaN;
			} catch (JSONException | NullPointerException e) {
			}
			try {
				HumiditySensor rhsin = room.humiditySensor();
				if (rhsin != null) {
					rhInside = rhsin.reading().getValue();
					roomData.put(Constants.JSON_RHIN_NAME, rhInside);
					ahInside = rhInside * Constants.RH2AH_FACTOR;
				}
				else
					rhInside = Float.NaN;
			} catch (JSONException | NullPointerException e) {
			}
		}
		else {
			logger.debug("request for room " + roomId + " which is not available");
			try {
				roomData.put(Constants.JSON_MESSAGEID_NAME, Constants.DEFAULT_MESSAGEID);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		float tempOutside = Float.NaN, rhOutside = Float.NaN, ah_outside = Float.NaN;
		if (outside != null) {
			try {
				TemperatureSensor tsout = outside.temperatureSensor();
				if (tsout != null) {
					tempOutside = tsout.reading().getValue();
					tempOutside = tempOutside == 0 ? Float.NaN : tempOutside - 273.15f;
					roomData.put(Constants.JSON_TEMPOUT_NAME, tempOutside);
				}
				else
					tempOutside = Float.NaN;
			} catch (JSONException | NullPointerException e) {
			}
			try {
				HumiditySensor rhsout = outside.humiditySensor();
				if (rhsout != null) {
					rhOutside = rhsout.reading().getValue();
					roomData.put(Constants.JSON_RHOUT_NAME, rhOutside);
					ah_outside = rhOutside * Constants.RH2AH_FACTOR;
				}
				else
					rhOutside = Float.NaN;
			} catch (JSONException | NullPointerException e) {
			}
		}
		else {
			logger.debug("request for room " + roomId + " which is not available");
			try {
				roomData.put(Constants.JSON_MESSAGEID_NAME, Constants.DEFAULT_MESSAGEID);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		messageID = determineMessageID(tempInside, rhInside, ahInside, tempOutside, rhOutside, ah_outside);
		try {
			if (messageID != -1) {
				String message = determineMessage(messageID);
				int prio = determinePriority(messageID);
				roomData.put(Constants.JSON_MESSAGEID_NAME, messageID);
				roomData.put(Constants.JSON_MESSAGE_NAME, message);
				roomData.put(Constants.JSON_PRIORITY_NAME, prio);
			}
			else {
				roomData.put(Constants.JSON_MESSAGEID_NAME, Constants.DEFAULT_MESSAGEID);
				roomData.put(Constants.JSON_MESSAGE_NAME, Constants.messages[Constants.DEFAULT_MESSAGEID]);
				roomData.put(Constants.JSON_PRIORITY_NAME, Constants.Priority_1);
			}
		} catch (JSONException | NullPointerException e) {
		}
		return roomData;
	}

	private int determinePriority(int messageID) {
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

	private String determineMessage(int messageID) {
		try {
			return Constants.messages[messageID];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public void setResource4Sensor(String roomId, String resourcePath, String sensor) {
		Room room = getRoom(roomId);
		Resource res = null;
		int type = 1;
		if (room == null) {
			if (roomsList.getElementType() == null)
				roomsList.setElementType(Room.class);
			room = roomsList.add();
			room.activate(false);
			if (roomId.equals(OUTSIDE_ROOM_NAME)) {
				outside = room;
				type = 0;
			}
			StringResource name = room.name();
			name.create().activate(false);
			name.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_LOWEST);
			room.name().setValue(roomId);

			IntegerResource typeRes = room.type();
			typeRes.create().activate(false);
			name.requestAccessMode(AccessMode.EXCLUSIVE, AccessPriority.PRIO_LOWEST);
			room.type().setValue(type);
			roomsMap.put(roomId, room);
		}

		switch (sensor) {
		// if the resource is already configured, delete it before
		case "Temperature":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof TemperatureSensor)) {
				throw new RuntimeException();
			}
			TemperatureSensor tsens = (TemperatureSensor) res;
			if (room.temperatureSensor() != null) {
				room.temperatureSensor().delete();
			}
			room.temperatureSensor().setAsReference(tsens);
			break;
		case "Humidity":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof HumiditySensor)) {
				throw new RuntimeException();
			}
			HumiditySensor rhsens = (HumiditySensor) res;
			if (room.humiditySensor() != null) {
				room.humiditySensor().delete();
			}
			room.humiditySensor().setAsReference(rhsens);
			break;
		case "Light Switch":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				throw new RuntimeException();
			}
			BooleanResource swtch = (BooleanResource) res;
			// Check if the room is already decorated with an ElectricLight
			ElectricDimmer el = room.getSubResource(LIGHT_DECORATOR_NAME);
			if (el != null) {
				el.onOffSwitch().stateControl().delete();
				el.onOffSwitch().stateControl().setAsReference(swtch);
			}
			else
				throw new IllegalStateException("Room not yet decorated with ElectricDimmer.");
			break;
		case "Light Dimmer":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				throw new RuntimeException();
			}
			BooleanResource dimmer = (BooleanResource) res;
			// Check if the room is already decorated with an ElectricLight
			el = room.getSubResource(LIGHT_DECORATOR_NAME);
			if (el != null) {
				DimmerAction da = new DimmerAction();
				da.setSource(dimmer);
				da.setTarget(el);

				// store the path persistently to remade the connection over DimmerAction after restart
				StringResource dimmerSourcePath = room.addDecorator(LIGHT_DIMMER_SOURCE_NAME, StringResource.class);
				dimmerSourcePath.setValue(resourcePath);
			}
			else
				throw new IllegalStateException("Room not yet decorated with ElectricDimmer.");
			break;
		case "Light":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof ElectricDimmer)) {
				throw new RuntimeException();
			}
			ElectricDimmer dimmerRes = (ElectricDimmer) res;
			// Check if the room is already decorated with an ElectricLight
			el = room.getSubResource(LIGHT_DECORATOR_NAME);
			if (el == null) {
				room.addDecorator(LIGHT_DECORATOR_NAME, dimmerRes);
			}
			break;
		case "Switch Box":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof SingleSwitchBox)) {
				throw new RuntimeException();
			}
			SingleSwitchBox swBoxRes = (SingleSwitchBox) res;
			// Check if the room is already decorated with an ElectricLight
			SingleSwitchBox swBox = room.getSubResource(SWITCH_BOX_DECORATOR_NAME);
			if (swBox == null) {
				room.addDecorator(SWITCH_BOX_DECORATOR_NAME, swBoxRes);
			}
			break;
		case "Supply Switch":
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				throw new RuntimeException();
			}
			swtch = (BooleanResource) res;
			// Check if the room is already decorated with an ElectricLight
			swBox = room.getSubResource(SWITCH_BOX_DECORATOR_NAME);
			if (swBox != null) {
				swBox.onOffSwitch().stateControl().delete();
				swBox.onOffSwitch().stateControl().setAsReference(swtch);
			}
			else
				throw new IllegalStateException("Room not yet decorated with SingleSwitchBox.");
			break;
		}
	}

	private Room getRoom(String roomId) {
		return roomsMap.get(roomId);
	}

	public int determineMessageID(float t_in, float rh_in, float ah_in, float t_out, float rh_out, float ah_out) {
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
		if (t_in > 24 && t_out < 25) {
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

}
