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
package org.ogema.apps.graphgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.apps.graphgenerator.generators.GraphGenerator;
import org.ogema.apps.graphgenerator.generators.GraphwizGenerator;

/**
 * Servlet for the Graphwizz-Generator
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class GraphwizzServlet extends HttpServlet {

	public static final boolean DEBUG = false;

	private static final long serialVersionUID = 1L;
	private final GeneratorApplication m_app;

	public GraphwizzServlet(GeneratorApplication app) {
		m_app = app;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		final BufferedReader reader = req.getReader();
		final String command;
		try {
			command = reader.readLine();
		} catch (IOException exp) {
			resp.getWriter().write("No command in POST");
			resp.setStatus(400);
			return;
		} finally {
			reader.close();
		}

		if (DEBUG)
			System.out.println("Command is " + command);

		switch (command) {
		case "all": {
			GraphGenerator generator = new GraphwizGenerator();
			m_app.writeGraph(generator);
			resp.getWriter().write("wrote graph");
			resp.setStatus(200);
			return;
		}
		case "connections": {
			GraphGenerator generator = new GraphwizGenerator();
			m_app.writeConnections(generator);
			resp.getWriter().write("wrote connections");
			resp.setStatus(200);
			return;
		}
		default: {
			resp.getWriter().write("command not known: " + command);
			resp.setStatus(400);
		}
		}
	}
}
