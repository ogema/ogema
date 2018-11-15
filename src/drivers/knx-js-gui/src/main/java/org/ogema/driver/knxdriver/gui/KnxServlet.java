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
package org.ogema.driver.knxdriver.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.tools.SerializationManager;
import org.ogema.driver.knxdriver.ConnectionInfo;
import org.ogema.driver.knxdriver.KNXdriverI;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.TouchSensor;

public class KnxServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final KNXdriverI knxDriver;

	private final List<String> deviceType = Arrays.asList(new String[] { LightSensor.class.getSimpleName(),
			TemperatureSensor.class.getSimpleName(), MotionSensor.class.getSimpleName(),
			OccupancySensor.class.getSimpleName(), ElectricPowerSensor.class.getSimpleName(),
			OnOffSwitch.class.getSimpleName(), ElectricDimmer.class.getSimpleName(), TouchSensor.class.getSimpleName(),
			ThermalValve.class.getSimpleName()
	// FIXME: still missing these: "Setpoint", "LightSetpoint" -- BrightnessResource (Float) ansonsten allg. SimpleResource
			});

	private SerializationManager serializationManager;

	public KnxServlet(ApplicationManager appMan, KNXdriverI knxDriver) {
		this.knxDriver = knxDriver;
		serializationManager = appMan.getSerializationManager();
		Collections.sort(deviceType);
	}

	@Override
	protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final KnxDAO dao = new KnxDAO();
		dao.setAvailableInterfaces(new ArrayList<String>(knxDriver.getInterfaces().keySet()));
		dao.setAvailableTypes(deviceType);
		dao.setConnectionInfos(knxDriver.getConnectionSorted());

		String json = serializationManager.toJson(dao);
		resp.getWriter().write(json);
		resp.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("search") != null) {
			knxDriver.searchInterface();
			List<String> availableInterfaces = new ArrayList<String>(knxDriver.getInterfaces().keySet());
			resp.setContentType("application/json");
			resp.getWriter().write(serializationManager.toJson(availableInterfaces));
			resp.setStatus(200);
		}
		else if (req.getParameter("add") != null) {
			String selectedInterface = req.getParameter("selectedInterface");
			String name = req.getParameter("name");
			String groupAddress = req.getParameter("groupAddress");
			String physical = req.getParameter("physicalAddress");
			String timeInterval = req.getParameter("timeInterval");
			String device = req.getParameter("device");
			String errMsg = doAdd(selectedInterface, name, groupAddress, physical, device, timeInterval);
			resp.getWriter().write(errMsg);
		}
	}

	// copied everything from here from KNXDriverPanel (wicket-gui)...
	private String doAdd(String selectedInterface, String name, String groupAddress, String pyhsicalAddress,
			String device, String timeInterval) {
		String errormsg = "";
		if (selectedInterface.length() > 1) {
			String ipTemp = knxDriver.getInterfaces().get(selectedInterface);
			if (ipTemp == null) {
				return "Cannot find selected interface!";
			}

			String ipAdresseL = ipTemp.substring(0, ipTemp.indexOf("#"));
			String ipAdresse = ipTemp.substring(ipTemp.indexOf("#") + 1, ipTemp.indexOf(":"));
			String port = ipTemp.substring(ipTemp.indexOf(":") + 1, ipTemp.length());
			final String uriStr = device + "," + name + "," + ipAdresse + ":" + port + "," + groupAddress + ","
					+ pyhsicalAddress + "," + timeInterval + "," + ipAdresseL;

			int status = knxDriver.addConnection(uriStr);

			switch (status) {
			case 1:
				errormsg = "Error with KNX connection";
				break;
			case 2:
				errormsg = "Connection timeout, please wait and repeat";
				break;
			case 3:
				errormsg = "Group address fail with resource type";
				break;
			case 4:
				errormsg = "OGEMA error";
				break;
			case 0:
				errormsg = "";
				break;
			default:
				errormsg = "Internal error";
				break;
			}
		}

		return errormsg;
	}
}
