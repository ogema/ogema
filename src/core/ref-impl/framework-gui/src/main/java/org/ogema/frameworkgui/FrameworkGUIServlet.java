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
package org.ogema.frameworkgui;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.accesscontrol.SessionAuth;
import org.ogema.frameworkgui.utils.BundleIcon;

/**
 *
 * @author tgries
 */
public class FrameworkGUIServlet extends HttpServlet {

	private static final long serialVersionUID = -6186673043383854564L;
	private final FrameworkGUIController controller;

	private final BundleIcon defaultIcon = new BundleIcon(getClass().getResource(
			"/org/ogema/frameworkgui/gui/img/svg/appdefaultlogo.svg"), BundleIcon.IconType.SVG);

	public FrameworkGUIServlet(FrameworkGUIController controller) {
		this.controller = controller;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setContentType("application/json");

		String path = req.getPathInfo();
		String action = req.getParameter("action");

		// System.out.println(req.getUserPrincipal().getName());

		if ("logout".equals(action)) {
			req.getSession().invalidate();
			//resp.sendRedirect("/ogema/login");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		SessionAuth sesAuth = (SessionAuth) req.getSession().getAttribute("ogemaAuth");
		String user = null;
		if (sesAuth != null) // if security is disabled sesAuth is null
			user = sesAuth.getName();

		if ("/installedapps".equals(path)) {
			if ("listAll".equals(action)) {
				StringBuffer sb = controller.appsList2JSON(user);
				String data = sb.toString();
				printResponse(resp, data);
			}
			else if ("getIcon".equals(action)) {
				int id = Integer.valueOf(req.getParameter("id"));
				BundleIcon.forBundle(controller.getBundleContext().getBundle(id), defaultIcon).writeIcon(resp);
			}
		}
	}

	private void printResponse(HttpServletResponse resp, String data) throws IOException {
		PrintWriter pw = resp.getWriter();
		pw.print(data);
		resp.setStatus(200);
	}

}
