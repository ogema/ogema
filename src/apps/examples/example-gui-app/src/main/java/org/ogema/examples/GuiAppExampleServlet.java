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
package org.ogema.examples;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.core.application.ApplicationManager;

public class GuiAppExampleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ApplicationManager appMan;

	public GuiAppExampleServlet(ApplicationManager appMan) {
		this.appMan = appMan;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = req.getReader();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}

		String response = doSomething(sb);

		resp.getWriter().write(response);
		resp.setStatus(200);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String response = "framework time: " + appMan.getFrameworkTime() + "ms";
		resp.getWriter().write(response);
		resp.setStatus(200);
	}

	private String doSomething(StringBuilder myStringBuilder) {
		return myStringBuilder.reverse().toString();
	}

}