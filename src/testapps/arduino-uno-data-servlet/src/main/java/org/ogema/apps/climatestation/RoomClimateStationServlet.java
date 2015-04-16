/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS
 * Fraunhofer ISE
 * Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.apps.climatestation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.application.Application;
import org.ogema.core.application.Application.AppStopReason;
import org.ogema.core.application.ApplicationManager;
import org.ogema.drivers.arduino.ClimateDataImpl;
import org.ogema.drivers.arduino.data.ClimateData;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.osgi.framework.ServiceReference;

public class RoomClimateStationServlet extends HttpServlet implements Application {
	private ApplicationManager appManager;
	private OutputStream bout;
	private int counter = 0;
	private float t_outside;
	private float rh_outside;
	private float ah_outside;
	private float factor_rh2ah = 0.66f;
	private static ClimateData climateData;

	public RoomClimateStationServlet(ClimateData cD) {

		climateData = cD;
	}

	public RoomClimateStationServlet() {

	}

	//@Override
	public void start(ApplicationManager appManager) {
		System.out.println("RoomClimateStationServlet started \n");
		this.appManager = appManager;

	}

	//@Override
	public void stop(AppStopReason reason) {
		System.out.println("RoomClimateStationServlet stopped \n");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pi = request.getPathInfo();
		byte[] arr;

		response.setContentType("text/script");
		bout = response.getOutputStream();

		if (pi.equals("/getArduinoTestData")) {
			System.out.println("/getArduinoTestData");

			JSONArray ArduinoArray = new JSONArray();
			JSONObject sensor_0 = new JSONObject(); // outside
			JSONObject sensor_1 = new JSONObject();

			try {
				sensor_0.put("ID", 2);
				sensor_0.put("RTemp", 22);
				sensor_0.put("RH", 33);
				sensor_0.put("Location_ID", 2);

				ArduinoArray.put(0, sensor_0);

				sensor_1.put("ID", 3);
				sensor_1.put("RTemp", 2200);
				sensor_1.put("RH", 40);
				sensor_1.put("Location_ID", 3);
				sensor_1.put("Message_ID", "test");
				sensor_1.put("Priority", "2");

				ArduinoArray.put(1, sensor_1);

			} catch (JSONException e) {

				e.printStackTrace();
			}

			bout.write(ArduinoArray.toString().getBytes());
		}

		if (pi.equals("/getArduinoData")) {
			System.out.println("/getArduinoData");

			JSONArray ArduinoArray = new JSONArray();
			JSONObject sensor_0 = new JSONObject(); // outside
			JSONObject sensor_1 = new JSONObject();
			JSONObject sensor_2 = new JSONObject();
			JSONObject sensor_3 = new JSONObject();

			int messageID_1;
			int messageID_2;
			int messageID_3;

			Properties prop_sensors = new Properties();
			Properties prop_messages = new Properties();

			try {
				InputStream istream_sensors = this.getClass().getClassLoader().getResourceAsStream(
						"properties/sensors.properties");
				InputStream istream_messages = this.getClass().getClassLoader().getResourceAsStream(
						"properties/messages.properties");
				prop_sensors.load(istream_sensors);
				prop_messages.load(istream_messages);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (NullPointerException e1) {
				e1.printStackTrace();
			}

			for (int i = 0; i <= 2; i++) {
				if (i == 0) // outside, no need to call determineMessageID()
				{
					if (climateData.getCurrentData(2) != null) {

						try {
							sensor_0.put("ID", climateData.getCurrentData(2).get("ID"));
							sensor_0.put("RTemp", climateData.getCurrentData(2).get("RTemp"));
							sensor_0.put("RH", climateData.getCurrentData(2).get("RH"));
							sensor_0.put("Location_ID", prop_sensors.getProperty("Location_ID_0"));

							t_outside = Float.parseFloat(climateData.getCurrentData(2).getString("RTemp").toString());
							rh_outside = Float.parseFloat(climateData.getCurrentData(2).get("RH").toString());
							ah_outside = rh_outside * factor_rh2ah;

							ArduinoArray.put(0, sensor_0);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							sensor_0.put("ID", 2);
							sensor_0.put("RTemp", "not available");
							sensor_0.put("RH", "not available");
							sensor_0.put("Location_ID", "not available");

							t_outside = 25.0f;
							rh_outside = 30.0f;
							ah_outside = rh_outside * factor_rh2ah;

							ArduinoArray.put(0, sensor_0);

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}

				if (i == 1) // Küche
				{
					if (climateData.getCurrentData(3) != null) {
						try {
							sensor_1.put("ID", climateData.getCurrentData(3).get("ID"));
							sensor_1.put("RTemp", climateData.getCurrentData(3).get("RTemp"));
							sensor_1.put("RH", climateData.getCurrentData(3).get("RH"));
							sensor_1.put("Location_ID", prop_sensors.getProperty("Location_ID_1"));

							float t_1 = Float.parseFloat(climateData.getCurrentData(3).get("RTemp").toString());
							float rh_1 = Float.parseFloat(climateData.getCurrentData(3).get("RH").toString());
							float ah_1 = rh_outside * factor_rh2ah;

							messageID_1 = determineMessageID(t_1, rh_1, ah_1, t_outside, rh_outside, ah_outside);

							if (messageID_1 != 0) {
								sensor_1.put("Message_ID", messageID_1);
								sensor_1.put("Message", prop_messages.getProperty("ID_" + messageID_1));
								sensor_1.put("Priority", prop_messages.getProperty("Priority_" + messageID_1));
							}
							else {
								sensor_1.put("Message_ID", 1);
								sensor_1.put("Message", prop_messages.getProperty("ID_" + 1));
								sensor_1.put("Priority", prop_messages.getProperty("Priority_" + 1));
							}

							ArduinoArray.put(1, sensor_1);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							sensor_1.put("ID", 3);
							sensor_1.put("RTemp", "not available");
							sensor_1.put("RH", "not available");
							sensor_1.put("Location_ID", "not available");
							sensor_1.put("Message_ID", "not available");
							sensor_1.put("Priority", "not available");

							ArduinoArray.put(1, sensor_1);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				if (i == 2) //Esszimmer
				{
					if (climateData.getCurrentData(4) != null) {
						try {
							sensor_2.put("ID", climateData.getCurrentData(4).get("ID"));
							sensor_2.put("RTemp", climateData.getCurrentData(4).get("RTemp"));
							sensor_2.put("RH", climateData.getCurrentData(4).get("RH"));
							sensor_2.put("Location_ID", prop_sensors.getProperty("Location_ID_2"));

							float t_2 = Float.parseFloat(climateData.getCurrentData(4).get("RTemp").toString());
							float rh_2 = Float.parseFloat(climateData.getCurrentData(4).get("RH").toString());
							float ah_2 = rh_outside * factor_rh2ah;

							messageID_2 = determineMessageID(t_2, rh_2, ah_2, t_outside, rh_outside, ah_outside);

							if (messageID_2 != 0) {
								sensor_2.put("Message_ID", messageID_2);
								sensor_2.put("Message", prop_messages.getProperty("ID_" + messageID_2));
								sensor_2.put("Priority", prop_messages.getProperty("Priority_" + messageID_2));
							}
							else {
								sensor_2.put("Message_ID", 1);
								sensor_2.put("Message", prop_messages.getProperty("ID_" + 1));
								sensor_2.put("Priority", prop_messages.getProperty("Priority_" + 1));
							}

							ArduinoArray.put(2, sensor_2);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							sensor_2.put("ID", 4);
							sensor_2.put("RTemp", "not available");
							sensor_2.put("RH", "not available");
							sensor_2.put("Location_ID", "not available");
							sensor_2.put("Message_ID", "not available");
							sensor_2.put("Priority", "not available");

							ArduinoArray.put(2, sensor_2);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				if (i == 3) // gibts erst mal n.
				{
					if (climateData.getCurrentData(4) != null) {
						try {
							sensor_3.put("ID", climateData.getCurrentData(4).get("ID"));
							sensor_3.put("RTemp", climateData.getCurrentData(4).get("RTemp"));
							sensor_3.put("RH", climateData.getCurrentData(4).get("RH"));
							sensor_3.put("Location_ID", prop_sensors.getProperty("Location_ID_3"));

							float t_3 = Float.parseFloat(climateData.getCurrentData(4).get("RTemp").toString());
							float rh_3 = Float.parseFloat(climateData.getCurrentData(4).get("RH").toString());
							float ah_3 = rh_outside * factor_rh2ah;

							messageID_3 = determineMessageID(t_3, rh_3, ah_3, t_outside, rh_outside, ah_outside);

							if (messageID_3 != 0) {
								sensor_3.put("Message_ID", prop_messages.getProperty("ID_" + messageID_3));
								sensor_3.put("Message", messageID_3);
								sensor_3.put("Priority", prop_messages.getProperty("Priority_" + messageID_3));
							}
							else {
								sensor_3.put("Message_ID", "Alles Okay");
								sensor_3.put("Priority", 1);
							}

							ArduinoArray.put(3, sensor_3);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

			} // end for

			bout.write(ArduinoArray.toString().getBytes());
		} // end if(pi.equals("/getArduinoData"))
	}

	public int determineMessageID(float t_in, float rh_in, float ah_in, float t_out, float rh_out, float ah_out) {
		// check conditions for ID_1
		// check conditions for ID_2

		// check conditions for ID_3
		if (t_in > 26 && t_out > 26)
			return 3;

		// check conditions for ID_4
		if (t_in > 26 && t_out > 15 && t_out <= 25.9) {
			if (rh_in >= 20.1 && rh_in <= 69.9)
				return 32;
			else {
				if (rh_in >= 70 && rh_in <= 70.9 && ah_out < ah_in)
					return 33;
				else
					return 4;
			}
		}

		// check conditions for ID_5
		if (t_in > 26 && t_out <= 14.9) {
			if (rh_in >= 70 && rh_in <= 79.9 && ah_out < ah_in)
				return 34;
			else
				return 5;
		}

		// check conditions for ID_6
		if (t_in > 24 && t_out < 25) {
			return 6;
		}

		// check conditions for ID_7
		if (t_in >= 16 && t_in <= 25.9) {
			return 7;
		}

		// check conditions for ID_8
		if (t_in <= 15.9) {
			if (rh_in >= 70 && rh_in <= 79.9 && ah_out < ah_in)
				return 35;
			else
				return 8;
		}

		// check conditions for ID_9
		// check conditions for ID_10
		// check conditions for ID_11
		// check conditions for ID_12
		// check conditions for ID_13
		// check conditions for ID_14

		// check conditions for ID_15
		if (rh_in <= 20)
			return 15;

		// check conditions for ID_16
		if (rh_in <= 20.1 && rh_in <= 69.9)
			return 16;

		// check conditions for ID_17
		if (rh_in >= 70 && rh_in <= 79.9 && ah_out < ah_in)
			return 17;

		// check conditions for ID_18
		if (rh_in < 70 && ah_out >= ah_in)
			return 18;

		// check conditions for ID_19
		if (rh_in > 80)
			return 19;

		// if there is no ID for which the conditions were fulfilled return 0
		return 0;
	}

}
