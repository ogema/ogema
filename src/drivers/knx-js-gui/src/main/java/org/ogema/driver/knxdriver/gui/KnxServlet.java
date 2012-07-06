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
package org.ogema.driver.knxdriver.gui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.TouchSensor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.ogema.model.actors.OnOffSwitch;
import org.ogema.tools.impl.FastJsonGenerator;

public class KnxServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final KNXdriverI knxDriver;

	private final Gson gson = new GsonBuilder().create();

	private final List<String> deviceType = Arrays.asList(new String[] { LightSensor.class.getSimpleName(),
			TemperatureSensor.class.getSimpleName(), MotionSensor.class.getSimpleName(),
			OccupancySensor.class.getSimpleName(), ElectricPowerSensor.class.getSimpleName(),
			OnOffSwitch.class.getSimpleName(), ElectricDimmer.class.getSimpleName(), TouchSensor.class.getSimpleName(),
			ThermalValve.class.getSimpleName()
	// FIXME: still missing these: "Setpoint", "LightSetpoint" -- BrightnessResource (Float) ansonsten allg. SimpleResource
			});

	public KnxServlet(ApplicationManager appMan, KNXdriverI knxDriver) {
		this.knxDriver = knxDriver;

		Collections.sort(deviceType);
	}

	@Override
	protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final KnxDAO dao = new KnxDAO();
		dao.setAvailableInterfaces(new ArrayList<String>(knxDriver.getInterfaces().keySet()));
		dao.setAvailableTypes(deviceType);
		dao.setConnectionInfos(knxDriver.getConnectionSorted());

		int status = HttpURLConnection.HTTP_OK;
		try {
			// FIXME: remove this when ogema-ref-impl:util implemented toJson also for non OGEMA resources
			AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
				@Override
				public Void run() throws IOException {
					resp.getWriter().write(gson.toJson(dao));
					return null;
				}
			});

		} catch (AccessControlException e) {
			e.printStackTrace();
		} catch (PrivilegedActionException e) {
			if (e.getException() instanceof IOException) {
				// let the http service care about this ...
				throw (IOException) e.getException();
			}
			else {
				// TODO logging...
				e.printStackTrace();
				status = HttpURLConnection.HTTP_INTERNAL_ERROR;
			}
		}

		resp.setStatus(status);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("search") != null) {
			knxDriver.searchInterface();
			List<String> availableInterfaces = new ArrayList<String>(knxDriver.getInterfaces().keySet());
			resp.setContentType("application/json");
			resp.getWriter().write(gson.toJson(availableInterfaces));
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
