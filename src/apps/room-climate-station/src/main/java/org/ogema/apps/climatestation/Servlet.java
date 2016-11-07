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
