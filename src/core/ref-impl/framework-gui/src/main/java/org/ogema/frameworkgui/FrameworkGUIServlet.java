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
package org.ogema.frameworkgui;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

		if ("logout".equals(action)) {
			req.getSession().invalidate();
			resp.sendRedirect("/ogema/login");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		if ("/installedapps".equals(path)) {
			if ("listAll".equals(action)) {
				StringBuffer sb = controller.appsList2JSON();
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
