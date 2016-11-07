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
package org.ogema.rest.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.rest.servlet.ResourceTypeWriters.ResourceTypeWriter;

public class RestTypesServlet extends HttpServlet  {

	private static final long serialVersionUID = 1L;

	final static String alias = "/rest/resourcelists";

	final boolean SECURITY_ENABLED;// "on".equalsIgnoreCase(System.getProperty("org.ogema.security", "off"));

	/**
	 * URL parameter defining the maximum depth of the resource tree in the response, default is 0, i.e. transfer only
	 * the requested resource and include subresources only as references.
	 */
	public static final String PARAM_RECURSIVE = "recursive";
//	private static final boolean DEFAULT_TOPLEVEL_ONLY = false;
	
	public static final String PARAM_TYPE = "type";

	private final PermissionManager permMan;
	private final RestAccess restAcc;
	protected final ApplicationManager appman;

	
	public RestTypesServlet(ApplicationManager am, PermissionManager permMan, RestAccess restAcc, boolean SECURITY_ENABLED) {
		this.restAcc = restAcc;
		this.SECURITY_ENABLED = SECURITY_ENABLED;
		this.appman = am;
		this.permMan = permMan;
	}

	// TODO get subresources of specific type
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!restAcc.setAccessContext(req, resp, SECURITY_ENABLED)) {
			return;
		}
		resp.setCharacterEncoding("UTF-8");
		ResourceTypeWriter w = ResourceTypeWriters.forRequest(req, appman);
		if (w == null) {
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return;
		}
		resp.setContentType(w.contentType());
		String clazzName = req.getParameter(PARAM_TYPE);

		Class<? extends Resource> type = null;
		if (clazzName != null) {
			try {
				type = (Class<? extends Resource>) Class.forName(clazzName);  // TODO access available resource types?
			} catch (ClassNotFoundException e) {} 
		}
		if (type == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		w.write(type, resp.getWriter());
		try {
			resp.flushBuffer();
		} catch (SecurityException se) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} finally {
			permMan.resetAccessContext();
		}
	}
	
	// TODO POST, PUT, DELETE

}
