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
