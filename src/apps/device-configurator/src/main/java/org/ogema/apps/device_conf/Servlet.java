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
package org.ogema.apps.device_conf;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OutputStream bout;
	DeviceConfigurator device_configurator;

	public Servlet(DeviceConfigurator device_configurator) {
		this.device_configurator = device_configurator;
	}

	synchronized public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/script");
		bout = response.getOutputStream();

		switch (pi) {
		case "/showCC":
			bout.write(device_configurator.showCC(request.getParameter("hlDriverId"),
					request.getParameter("deviceAddress")).toString().getBytes());
			break;

		case "/readC":
			bout.write(device_configurator.readC(request.getParameter("hlDriverId"),
					request.getParameter("config_data_json")).toString().getBytes());
			break;

		case "/scan":
			bout.write(device_configurator.scan(request.getParameter("llDriverId")).toString().getBytes());
			break;

		case "/showCD":
			bout.write(device_configurator.showCD(request.getParameter("llDriverId"),
					request.getParameter("interfaceId"), request.getParameter("device"),
					request.getParameter("endpoint"), request.getParameter("clusterId")).toString().getBytes());
			break;

		case "/showDD":
			bout.write(device_configurator.showDD(request.getParameter("llDriverId"),
					request.getParameter("interfaceId"), request.getParameter("deviceAddress")).toString().getBytes());
			break;

		case "/showACC":
			bout.write(device_configurator.showACC(request.getParameter("llDriverId")).toString().getBytes());
			break;

		case "/showH":
			bout.write(device_configurator.showH(request.getParameter("llDriverId")).toString().getBytes());
			break;

		case "/showN":
			bout.write(device_configurator.showN(request.getParameter("llDriverId")).toString().getBytes());
			break;

		case "/showAllHLDrivers":
			bout.write((device_configurator.showAllHLDrivers()).toString().getBytes());
			break;

		case "/showAllLLDrivers":
			bout.write((device_configurator.showAllLLDrivers()).toString().getBytes());
			break;

		case "/cache":
			bout.write(device_configurator.cache(request.getParameter("llDriverId")).toString().getBytes());
			break;
		}

	}

	synchronized public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/script");

		switch (pi) {
		case "/createC":
			device_configurator.createC(request.getParameter("hlDriverId"), request.getParameter("config_data_json"));
			break;

		case "/writeC":
			device_configurator.writeC(request.getParameter("hlDriverId"), request.getParameter("config_data_json"));
			break;

		case "/deleteC":
			device_configurator.deleteC(request.getParameter("hlDriverId"), request.getParameter("config_data_json"));
			break;

		case "/addC":
			device_configurator.addC(request.getParameter("llDriverId"), request.getParameter("hardwareIdentifier"));
			break;

		case "/addCVP":
			device_configurator.addCVP(request.getParameter("llDriverId"), request.getParameter("portName"));
			break;
		}

	}
}
