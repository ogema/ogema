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

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.security.WebAccessManager;

public class Servlet extends HttpServlet implements Application {

	/**
	 *
	 */
	private static final long serialVersionUID = 269613440207288592L;

	private WebAccessManager wam;

	private Rooms rooms;

	@Override
	public void start(ApplicationManager appManager) {
		this.wam = appManager.getWebAccessManager();
		this.wam.registerWebResource(Constants.SERVLET_PATH, this);
		this.wam.registerWebResource("/rcs", "/web");
		this.wam.registerWebResource("/chart", "/generated");
		this.rooms = new Rooms(appManager);
	}

	@Override
	public void stop(AppStopReason reason) {
		this.wam.unregisterWebResource("/rcs");
		this.wam.unregisterWebResource(Constants.SERVLET_PATH);
	}

	synchronized public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/script");
		OutputStream bout = response.getOutputStream();

		switch (pi) {
		case "/getData":
			String result = rooms.getRoomData(request.getParameter("roomId")).toString();
			bout.write(result.getBytes());
			break;
		case "/matchType2Sensors":
			result = rooms.getMatchingSensors(request.getParameter("type")).toString();
			bout.write(result.getBytes());
			break;
		}
	}

	synchronized public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/script");
		OutputStream bout = response.getOutputStream();
		switch (pi) {
		case "/setResource4Sensor":
			String result = null;
			try {
				result = rooms.setResource4Sensor(request.getParameter("roomId"), request.getParameter("resourcePath"),
						request.getParameter("sensor"));
			} catch (Throwable t) {
				bout.write(t.getMessage().getBytes());
			}
			bout.write(result.getBytes());
			break;
		case "/resetRoomSensors":
			result = rooms.resetRoomSensors(request.getParameter("roomId"));
			bout.write(result.getBytes());
			break;
		}
	}
}
