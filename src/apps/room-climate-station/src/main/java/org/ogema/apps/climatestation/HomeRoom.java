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

import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.HumiditySensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.SmokeDetector;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.WaterDetector;

public class HomeRoom {

	private static final String LIGHT_DECORATOR_NAME = "lightControl";
	private static final String WATER_DECORATOR_NAME = "waterDetector";
	private static final String SMOKE_DECORATOR_NAME = "smokeDetector";
	private static final String SWITCH_BOX_1_DECORATOR_NAME = "switchBox1";
	private static final String SWITCH_BOX_2_DECORATOR_NAME = "switchBox2";
	private static final String LIGHT_DIMMER_SOURCE_NAME = "dimmerSourceSwitch";

	private ResourceAccess resAcc;
	private OgemaLogger logger;

	Room theRoom;

	String roomId;

	public HomeRoom(ResourceAccess ra, OgemaLogger ol, Room room) {
		this.resAcc = ra;
		this.logger = ol;
		this.theRoom = room;
		initResources();
	}

	private void initResources() {
		this.temp = theRoom.temperatureSensor();
		if (temp != null)
			this.tempRes = temp.reading();
		this.humidity = theRoom.humiditySensor();
		if (humidity != null)
			this.humidityRes = humidity.reading();
		this.motion = theRoom.motionSensor();
		this.motionRes = motion.reading();
		this.swBox2 = theRoom.getSubResource(SWITCH_BOX_2_DECORATOR_NAME);
		if (swBox2 != null)
			this.swBox2Feedback = swBox2.onOffSwitch().stateFeedback();
		this.dimmer = theRoom.getSubResource(LIGHT_DECORATOR_NAME);
		if (dimmer != null)
			this.dimmerFeedback = dimmer.onOffSwitch().stateFeedback();
		this.smoke = theRoom.getSubResource(SMOKE_DECORATOR_NAME);
		if (smoke != null)
			this.smokeAlert = smoke.reading();
		this.swBox1 = theRoom.getSubResource(SWITCH_BOX_1_DECORATOR_NAME);
		if (swBox1 != null)
			this.swBox1Control = swBox1.onOffSwitch().stateControl();
		this.water = theRoom.getSubResource(WATER_DECORATOR_NAME);
		if (water != null)
			this.highWaterRes = water.reading();
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

	private TemperatureSensor temp;
	TemperatureResource tempRes;
	private HumiditySensor humidity;
	FloatResource humidityRes;
	private MotionSensor motion;
	BooleanResource motionRes;
	private SingleSwitchBox swBox2;
	BooleanResource swBox2Feedback;
	private ElectricDimmer dimmer;
	BooleanResource dimmerFeedback;
	private SmokeDetector smoke;
	BooleanResource smokeAlert;
	private SingleSwitchBox swBox1;
	BooleanResource swBox1Control;
	private WaterDetector water;
	StringResource highWaterRes;

	void createDimmerAction() {
		// Check if a DimmerAction is to be created
		StringResource dimmerSourcePath = theRoom.getSubResource(LIGHT_DIMMER_SOURCE_NAME);
		if (dimmerSourcePath != null) {
			BooleanResource res = resAcc.getResource(dimmerSourcePath.getValue());
			if (res == null || !(res instanceof BooleanResource)) {
				logger.error("Dimmer switch resource could not be found!");
			}
			BooleanResource dimmer = res;
			// Check if the room is already decorated with an ElectricLight
			ElectricDimmer el = theRoom.getSubResource(LIGHT_DECORATOR_NAME);
			if (el != null) {
				DimmerAction da = new DimmerAction();
				da.setSource(dimmer);
				da.setTarget(el);
			}
		}
	}

	public JSONObject getRoomData() {
		JSONObject roomData = new JSONObject();
		int messageID;
		Room room = theRoom;
		float tempInside = Float.NaN, rhInside = Float.NaN, ahInside = Float.NaN;
		float tempOutside = Float.NaN, rhOutside = Float.NaN, ah_outside = Float.NaN;
		if (room != null) {
			/*
			 * temperature inside
			 */
			try {
				// temp = room.temperatureSensor();
				if (temp != null && temp.isActive()) {
					tempInside = tempRes.getValue();
					if (tempInside == 0) {
						tempInside = Float.NaN;
						roomData.put(Constants.JSON_TEMPIN_NAME, "NaN");
					}
					else {
						tempInside = tempInside - 273.15f;
						roomData.put(Constants.JSON_TEMPIN_NAME, tempInside);
					}
				}

			} catch (JSONException | NullPointerException e) {
			}
			/*
			 * humiditiy inside
			 */
			try {
				// humidity = room.humiditySensor();
				if (humidity != null && humidity.isActive()) {
					rhInside = humidityRes.getValue();
					if (rhInside == 0) {
						rhInside = Float.NaN;
						roomData.put(Constants.JSON_RHIN_NAME, "NaN");
					}
					else {
						roomData.put(Constants.JSON_RHIN_NAME, rhInside);
					}
					ahInside = rhInside * Constants.RH2AH_FACTOR;
				}

			} catch (JSONException e) {
			}
			/*
			 * Motion sensor inside
			 */
			try {
				// motion = room.motionSensor();
				if (motion != null && motion.isActive()) {
					boolean motionInside = motionRes.getValue();
					roomData.put(Constants.JSON_MOTIONIN_NAME, motionInside);
				}
			} catch (JSONException e) {
			}

			/*
			 * Mains power outlet (Switchbox 2)
			 */
			try {
				// swBox2 = room.getSubResource(SWITCH_BOX_2_DECORATOR_NAME);
				if (swBox2 != null) {
					boolean lightSwitchState = swBox2Feedback.getValue();
					roomData.put(Constants.JSON_LIGHT2_NAME, lightSwitchState);
				}
			} catch (JSONException e) {
			}

			/*
			 * Light
			 */
			try {
				// dimmer = room.getSubResource(LIGHT_DECORATOR_NAME);
				if (dimmer != null) {
					boolean lightSwitchState = dimmerFeedback.getValue();
					roomData.put(Constants.JSON_LIGHT_NAME, lightSwitchState);
				}
			} catch (JSONException e) {
			}
			/*
			 * Smoke detector
			 */
			try {
				// smoke = room.getSubResource(SMOKE_DECORATOR_NAME);
				if (smoke != null) {
					boolean alert = smokeAlert.getValue();
					roomData.put(Constants.JSON_SMOKE_NAME, alert);
				}
			} catch (JSONException e) {
			}

			/*
			 * air condition (Switch box 1)
			 */
			try {
				// swBox1 = room.getSubResource(SWITCH_BOX_1_DECORATOR_NAME);
				if (swBox1 != null) {
					boolean airSwitch = swBox1Control.getValue();
					roomData.put(Constants.JSON_AIR_NAME, airSwitch);
				}
			} catch (JSONException e) {
			}

			/*
			 * Water detector
			 */
			try {
				// water = room.getSubResource(WATER_DECORATOR_NAME);
				if (water != null) {
					String highWater = highWaterRes.getValue();
					if (highWater.equals("wet"))
						roomData.put(Constants.JSON_WATER_NAME, true);
					else
						roomData.put(Constants.JSON_WATER_NAME, false);
				}
			} catch (JSONException e) {
			}

			/*
			 * temperature outside
			 */
			try {
				TemperatureResource tsout = Rooms.outsideTemp;
				if (tsout != null && tsout.isActive()) {
					tempOutside = tsout.getValue();
					if (tempOutside == 0) {
						tempOutside = Float.NaN;
						roomData.put(Constants.JSON_TEMPOUT_NAME, "NaN");
					}
					else {
						tempOutside = tempOutside - 273.15f;
						roomData.put(Constants.JSON_TEMPOUT_NAME, tempOutside);
					}
				}
			} catch (JSONException | NullPointerException e) {
			}

			/*
			 * humidity outside
			 */
			try {
				FloatResource rhsout = Rooms.outsideHumidity;
				if (rhsout != null && rhsout.isActive()) {
					rhOutside = rhsout.getValue();
					if (rhOutside == 0) {
						rhOutside = Float.NaN;
						roomData.put(Constants.JSON_RHOUT_NAME, "NaN");
					}
					else {
						roomData.put(Constants.JSON_RHOUT_NAME, rhOutside);
					}
					ah_outside = rhOutside * Constants.RH2AH_FACTOR;
				}
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
		messageID = Rooms.determineMessageID(tempInside, rhInside, ahInside, tempOutside, rhOutside, ah_outside);

		/*
		 * action message and priority
		 */
		try {
			if (messageID != -1) {
				String message = Rooms.determineMessage(messageID);
				int prio = Rooms.determinePriority(messageID);
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

	public String setResource4Sensor(String resourcePath, String sensor) {
		String result = null;
		Room room = theRoom;
		Resource res = null;

		int errorCode = 0;
		switch (sensor) {
		// if the resource is already configured, delete it before
		case SENSOR_NAME_TEMPERATURE:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof TemperatureSensor)) {
				errorCode = 1;
				break;
			}
			TemperatureSensor tsens = (TemperatureSensor) res;
			if (room.temperatureSensor() != null) {
				room.temperatureSensor().delete();
			}
			temp = room.temperatureSensor().setAsReference(tsens);
			tempRes = temp.reading();
			break;
		case SENSOR_NAME_HUMIDITY:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof HumiditySensor)) {
				errorCode = 1;
				break;
			}
			HumiditySensor rhsens = (HumiditySensor) res;
			if (room.humiditySensor() != null) {
				room.humiditySensor().delete();
			}
			humidity = room.humiditySensor().setAsReference(rhsens);
			humidityRes = humidity.reading();
			break;
		case SENSOR_NAME_LIGHT_SWICHT:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				errorCode = 1;
				break;
			}
			BooleanResource swtch = (BooleanResource) res;
			// Check if the room is already decorated with an ElectricLight
			ElectricDimmer el = room.getSubResource(LIGHT_DECORATOR_NAME);
			if (el != null) {
				el.onOffSwitch().stateControl().delete();
				el.onOffSwitch().stateControl().setAsReference(swtch);
				dimmer = el;
				dimmerFeedback = el.onOffSwitch().stateFeedback();
			}
			else
				throw new IllegalStateException("Room not yet decorated with ElectricDimmer.");
			break;
		case SENSOR_NAME_LIGHT_DIMMER:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				errorCode = 1;
				break;
			}
			BooleanResource dimmerValue = (BooleanResource) res;
			// Check if the room is already decorated with an ElectricLight
			el = room.getSubResource(LIGHT_DECORATOR_NAME);
			if (el != null) {
				DimmerAction da = new DimmerAction();
				da.setSource(dimmerValue);
				da.setTarget(el);

				// store the path persistently to remade the connection over DimmerAction after restart
				StringResource dimmerSourcePath = room.addDecorator(LIGHT_DIMMER_SOURCE_NAME, StringResource.class);
				dimmerSourcePath.setValue(resourcePath);
				dimmer = el;
				dimmerFeedback = el.onOffSwitch().stateFeedback();
			}
			else
				throw new IllegalStateException("Room not yet decorated with ElectricDimmer.");
			break;
		case SENSOR_NAME_LIGHT:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof ElectricDimmer)) {
				errorCode = 1;
				break;
			}
			ElectricDimmer dimmerRes = (ElectricDimmer) res;
			// Check if the room is already decorated with an ElectricLight
			el = room.getSubResource(LIGHT_DECORATOR_NAME);
			if (el == null) {
				el = room.addDecorator(LIGHT_DECORATOR_NAME, dimmerRes);
			}
			dimmer = el;
			dimmerFeedback = el.onOffSwitch().stateFeedback();
			break;
		case SENSOR_NAME_SWBOX1:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof SingleSwitchBox)) {
				errorCode = 1;
				break;
			}
			SingleSwitchBox swBoxRes = (SingleSwitchBox) res;
			// Check if the room is already decorated with the switch box 1
			SingleSwitchBox swBox = room.getSubResource(SWITCH_BOX_1_DECORATOR_NAME);
			if (swBox == null) {
				room.addDecorator(SWITCH_BOX_1_DECORATOR_NAME, swBoxRes);
				swBox1 = swBoxRes;
				swBox1Control = swBoxRes.onOffSwitch().stateControl();
			}
			break;
		case SENSOR_NAME_SWBOX_SWITCH1:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				errorCode = 1;
				break;
			}
			swtch = (BooleanResource) res;
			// Check if the room is already decorated with the switch box 1
			swBox = room.getSubResource(SWITCH_BOX_1_DECORATOR_NAME);
			if (swBox != null) {
				swBox.onOffSwitch().stateControl().delete();
				swBox.onOffSwitch().stateControl().setAsReference(swtch);
			}
			else
				throw new IllegalStateException("Room not yet decorated with SingleSwitchBox.");
			break;
		case SENSOR_NAME_SWBOX2:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof SingleSwitchBox)) {
				errorCode = 1;
				break;
			}
			swBoxRes = (SingleSwitchBox) res;
			// Check if the room is already decorated with the switch box 2
			swBox = room.getSubResource(SWITCH_BOX_2_DECORATOR_NAME);
			if (swBox == null) {
				room.addDecorator(SWITCH_BOX_2_DECORATOR_NAME, swBoxRes);
				swBox2 = swBoxRes;
				swBox2Feedback = swBoxRes.onOffSwitch().stateFeedback();
			}
			break;
		case SENSOR_NAME_SWBOX_SWITCH2:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof BooleanResource)) {
				errorCode = 1;
				break;
			}
			swtch = (BooleanResource) res;
			// Check if the room is already decorated with the switch box 2
			swBox = room.getSubResource(SWITCH_BOX_2_DECORATOR_NAME);
			if (swBox != null) {
				swBox.onOffSwitch().stateControl().delete();
				swBox.onOffSwitch().stateControl().setAsReference(swtch);
			}
			else
				throw new IllegalStateException("Room not yet decorated with SingleSwitchBox.");
			break;
		case SENSOR_NAME_MOTION:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof MotionSensor)) {
				errorCode = 1;
				break;
			}
			MotionSensor motionSens = (MotionSensor) res;
			if (motion != null) {
				motion.delete();
			}
			room.motionSensor().setAsReference(motionSens);
			motion = motionSens;
			motionRes = motionSens.reading();
			break;
		case SENSOR_NAME_WATER:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof WaterDetector)) {
				errorCode = 1;
				break;
			}
			WaterDetector waterRes = (WaterDetector) res;
			// Check if the room is already decorated with a waterDetector
			WaterDetector waterDed = room.getSubResource(WATER_DECORATOR_NAME);
			if (waterDed == null) {
				room.addDecorator(WATER_DECORATOR_NAME, waterRes);
				water = waterRes;
				highWaterRes = waterRes.reading();
			}
			break;
		case SENSOR_NAME_SMOKE:
			res = resAcc.getResource(resourcePath);
			if (res == null || !(res instanceof SmokeDetector)) {
				errorCode = 1;
				break;
			}
			SmokeDetector smokeRes = (SmokeDetector) res;
			// Check if the room is already decorated with a waterDetector
			SmokeDetector smokeDed = room.getSubResource(SMOKE_DECORATOR_NAME);
			if (smokeDed == null) {
				room.addDecorator(SMOKE_DECORATOR_NAME, smokeRes);
				smoke = smokeRes;
				smokeAlert = smokeRes.reading();
			}
			break;
		default:
			errorCode = 2;
			break;
		}

		switch (errorCode) {
		case 0:
			result = roomId + " wurde erfolgreich mit der Ressource ausgestattet: " + resourcePath;
			break;
		case 1:
			result = "Die Ressource " + resourcePath + " kann nicht als Raumsensor konfiguriert werden!";
			break;
		case 2:
			result = sensor + " ist nicht für die Konfiguration vorgesehen!";
			break;
		default:
			break;
		}

		return result;
	}
}
