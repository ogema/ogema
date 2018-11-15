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
package org.ogema.rest.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.RestAccess;
import org.ogema.core.application.ApplicationManager;
import org.ogema.rest.patternmimic.ResourcePatternWriters;
import org.ogema.rest.patternmimic.ResourcePatternWriters.ResourcePatternWriter;

//@Component
//@Service(Servlet.class)
//@Property(name = HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, value = RestPatternServlet.ALIAS)
class RestPatternServlet extends HttpServlet  {

	private static final long serialVersionUID = 1L;

	final static String ALIAS = "/rest/patterns";

	private final PermissionManager permMan;
	private final RestAccess restAcc;
	
	RestPatternServlet(PermissionManager permMan, RestAccess restAcc) {
		this.permMan = Objects.requireNonNull(permMan);
		this.restAcc = Objects.requireNonNull(restAcc);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final ApplicationManager appman = restAcc.authenticate(req, resp);
		if (appman == null) {
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
	}

}
