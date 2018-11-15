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
		case "/getGraphDataHistory":
			bout.write(shellCommands.getGraphDataHistory().toString().getBytes());
			break;
		case "/getMeterList":
			bout.write(shellCommands.getMeterList().toString().getBytes());
			break;
		}

	}
}
