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
package org.ogema.app.DRS485DEconsole;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OutputStream bout;
	ShellCommands shellCommands;

	public Servlet(ShellCommands shellCommands) {
		this.shellCommands = shellCommands;
	}

	synchronized public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String pi = request.getPathInfo();
		response.setContentType("text/script");
		bout = response.getOutputStream();

		switch (pi) {
		case "/getGraphData":
			bout.write(shellCommands.getGraphData().toString().getBytes());
			break;
		}

	}
}
