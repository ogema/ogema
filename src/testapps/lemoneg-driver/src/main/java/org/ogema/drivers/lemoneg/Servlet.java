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

package org.ogema.drivers.lemoneg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private OutputStream bout;
	private final LemonegDriver lemoneg_driver;

	public Servlet(LemonegDriver lemoneg_driver) {
		this.lemoneg_driver = lemoneg_driver;
	}

	/**
	 * handle getJSON request from browser an send back the current lemoneg data
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pi = request.getPathInfo();
		byte[] arr;

		response.setContentType("text");
		bout = response.getOutputStream();

		if (pi.equals("/getBusesDevicesList")) {
			bout.write(lemoneg_driver.getBusesDevicesJSONArray().toString().getBytes());
		}

	}

	/**
	 * handle post request
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		if (pi.equals("/addBus")) {
			System.out.println(request.getParameter("hardwareId"));
			System.out.println(request.getParameter("driverId"));
			System.out.println(request.getParameter("channelAddress"));

			lemoneg_driver.addBus(request.getParameter("hardwareId"), request.getParameter("driverId"), request
					.getParameter("deviceParameters"), request.getParameter("timeout"));
		}

		// remove a bus and all its devices
		if (pi.equals("/removeBus")) {

		}

		// create new device for the bus identified by hardwareId
		if (pi.equals("/addDevice")) {

			out.println("<!DOCTYPE html>\n" + "<head><title>" + "Confirmation" + "</title></head>\n" + "<body>\n"
					+ "Configuration received! Thank You!" + "</body>\n" + "</html>");

			System.out.println("/addDevice");
			System.out.println(request.getParameter("hardwareId"));
			System.out.println(request.getParameter("deviceAddress"));
			System.out.println(request.getParameter("deviceParameters"));
			System.out.println(request.getParameter("timeout"));
			System.out.println(request.getParameter("resourceName"));

			lemoneg_driver.addDevice(request.getParameter("hardwareId"), request.getParameter("channelAddress"),
					request.getParameter("deviceAddress"), request.getParameter("resourceName"));
		}

		// remove a device
		if (pi.equals("/removeDevice")) {

		}

		if (pi.equals("/getData")) {
			LemonegDataModel data_model = lemoneg_driver.getData(request.getParameter("hardwareId"), request
					.getParameter("deviceAddress"));
			bout.write(buildLemonegDataJSONArray(data_model));
		}
	}

	synchronized public byte[] buildLemonegDataJSONArray(LemonegDataModel model) {

		JSONArray lemonegJSONArray = new JSONArray();
		JSONObject voltage = new JSONObject();
		JSONObject current = new JSONObject();
		JSONObject power = new JSONObject();
		JSONObject frequency = new JSONObject();
		JSONObject time = new JSONObject();

		System.out.println("voltage:" + model.voltage().reading().getValue());
		System.out.println("current:" + model.current().reading().getValue());
		System.out.println("power:" + model.activePower().reading().getValue());
		System.out.println("frequency:" + model.phaseFrequency().getValue());
		System.out.println("time:" + model.timeStamp().getValue());

		try {
			voltage.put("voltage", model.voltage().reading().getValue());
			lemonegJSONArray.put(0, voltage);
			current.put("current", model.current().reading().getValue());
			lemonegJSONArray.put(1, current);
			power.put("power", model.activePower().reading().getValue());
			lemonegJSONArray.put(2, power);
			frequency.put("frequency", model.phaseFrequency().getValue());
			lemonegJSONArray.put(3, frequency);
			time.put("time", model.timeStamp().getValue());
			lemonegJSONArray.put(4, time);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return lemonegJSONArray.toString().getBytes();
	}

}
