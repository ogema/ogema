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

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.application.ApplicationManager;
import org.ogema.rest.patternmimic.ResourcePatternWriters;
import org.ogema.rest.patternmimic.ResourcePatternWriters.ResourcePatternWriter;

public class RestPatternServlet extends HttpServlet  {

	private static final long serialVersionUID = 1L;

	final static String alias = "/rest/patterns";

	final boolean SECURITY_ENABLED;// "on".equalsIgnoreCase(System.getProperty("org.ogema.security", "off"));

	/**
	 * URL parameter defining the maximum depth of the resource tree in the response, default is 0, i.e. transfer only
	 * the requested resource and include subresources only as references.
	 */
	private final PermissionManager permMan;
	private final RestAccess restAcc;
	private final ApplicationManager appman;
	
	public RestPatternServlet(ApplicationManager am, PermissionManager permMan, RestAccess restAcc, boolean SECURITY_ENABLED) {
		this.restAcc = restAcc;
		this.SECURITY_ENABLED = SECURITY_ENABLED;
		this.appman = am;
		this.permMan = permMan;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
		if (!restAcc.setAccessContext(req, resp, SECURITY_ENABLED)) {
			return;
		}
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
        String request = sb.toString();  
        if (appman.getLogger().isTraceEnabled()) {
        	appman.getLogger().trace("Pattern REST request:\n" + request + "\n");
        }
		
		resp.setCharacterEncoding("UTF-8");

		ResourcePatternWriter w = ResourcePatternWriters.forRequest(req, appman);
		resp.setContentType(w.contentType());
		try {
			w.write(w.getPattern(request), resp.getWriter());
		} catch (SecurityException se) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (Exception e) {
			appman.getLogger().warn("REST pattern de-/serialization failed " + e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			try {
				resp.flushBuffer();
			} catch (IOException ee) {} 
			permMan.resetAccessContext();
		}
		} catch (Exception ee) {
			appman.getLogger().error("Error in REST pattern servlet: ",ee);
		}
	}

}
