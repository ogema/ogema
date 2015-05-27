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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.core.administration.AdministrationManager;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.tools.SerializationManager;
import org.ogema.rest.RootResource;
import org.ogema.rest.servlet.ResourceReaders.ResourceReader;
import org.ogema.rest.servlet.ResourceWriters.ResourceWriter;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * 
 * @author jlapp
 */
@Component(specVersion = "1.2")
@Service(Application.class)
public class RestServlet extends HttpServlet implements Application {

	private static final long serialVersionUID = 1L;

	final static String alias = "/rest/resources";

	final boolean SECURITY_ENABLED = "on".equalsIgnoreCase(System.getProperty("org.ogema.security", "off"));

	/**
	 * URL parameter defining the maximum depth of the resource tree in the response, default is 0, i.e. transfer only
	 * the requested resource and include subresources only as references.
	 */
	public static final String PARAM_DEPTH = "depth";
	static final int DEFAULT_DEPTH = 0;
	/**
	 * URL parameter defining whether schedule data should be included in the response, default is false, which means
	 * that schedule data will not be included.
	 */
	public static final String PARAM_SCHEDULES = "schedules";
	static final boolean DEFAULT_SCHEDULES = false;
	/**
	 * URL parameter defining whether references in the result resource tree will be included as resources, default is
	 * false, which means that references will only be listed as reference elements.
	 */
	public static final String PARAM_REFERENCES = "references";
	static final boolean DEFAULT_REFERENCES = false;

	@Reference
	HttpService http;
	@Reference
	private PermissionManager permMan;
	@Reference
	private AdministrationManager adminMan;

	private RestAccess restAcc;

	protected ApplicationManager appman;

	protected void activate(Map<String, ?> config) {
		restAcc = new RestAccess(permMan, adminMan);
	}

	protected void deactivate(Map<String, ?> config) {

	}

	@Override
	public void start(ApplicationManager appManager) {
		appman = appManager;
		if (SECURITY_ENABLED && System.getSecurityManager() == null) {
			throw new Error("org.ogema.security=on, but security manager is null!");
		}
		try {
			http.registerServlet(alias, this, null, null);
			appman.getLogger().info("REST servlet registered, security enabled: {}", SECURITY_ENABLED);
		} catch (ServletException | NamespaceException ex) {
			appman.getLogger().error("could not register servlet");
		}
		
		appman.getWebAccessManager().registerWebResourcePath("/rest-gui", "rest/gui");
	}

	@Override
	public void stop(AppStopReason reason) {
		http.unregister(alias);
		appman.getWebAccessManager().unregisterWebResourcePath("/rest-gui");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!setAccessContext(req, resp)) {
			return;
		}
		resp.setCharacterEncoding("UTF-8");
		final SerializationManager sman = getSerializationManager(req, resp);
		if (sman == null) {
			return;
		}
		ResourceWriter w = ResourceWriters.forRequest(req, sman);
		if (w == null) {
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return;
		}
		resp.setContentType(w.contentType());

		String path = req.getPathInfo();
		if (path == null || path.equals("/")) {
			ResourceWriters.forRequest(req, sman).write(new RootResource(appman), resp.getWriter());
			return;
		}

		ResourceRequestInfo r;
		try {
			r = selectResource(req.getPathInfo());
			if (r == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				if (r.isSchedule()) {
					w.writeSchedule((Schedule) r.getResource(), r.getStart(), r.getEnd(), resp.getWriter());
				}
				else {
					w.write(r.getResource(), resp.getWriter());
				}
			}
			resp.flushBuffer();
		} catch (SecurityException se) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} finally {
			permMan.resetAccessContext();
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!setAccessContext(req, resp)) {
			return;
		}
		resp.setCharacterEncoding("UTF-8");
		String path = req.getPathInfo();
		if (path == null || path.equals("/")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		ResourceRequestInfo r = selectResource(req.getPathInfo());
		if (r == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		SerializationManager sman = getSerializationManager(req, resp);
		ResourceReader reader = ResourceReaders.forRequest(req, sman, resp);
		ResourceWriter w = ResourceWriters.forRequest(req, sman, resp);
		if (resp.isCommitted()) {
			return;
		}
		reader.readResource(req.getReader(), r.getResource());
		resp.setContentType(w.contentType());

		if (r.isSchedule()) {
			w.writeSchedule((Schedule) r.getResource(), r.getStart(), r.getEnd(), resp.getWriter());
		}
		else {
			w.write(r.getResource(), resp.getWriter());
		}
		resp.flushBuffer();
		permMan.resetAccessContext();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		SerializationManager sman = getSerializationManager(req, resp);
		ResourceReader reader = ResourceReaders.forRequest(req, sman, resp);
		ResourceWriter w = ResourceWriters.forRequest(req, sman, resp);
		if (resp.isCommitted()) {
			return;
		}
		resp.setCharacterEncoding("UTF-8");
		if (!setAccessContext(req, resp)) {
			return;
		}
		resp.setContentType(w.contentType());
		String path = req.getPathInfo();
		if (path == null || path.equals("/")) {
			Resource resource = reader.createResource(req.getReader(), null);
			w.write(resource, resp.getWriter());
		}
		else {
			Resource r = selectResource(req.getPathInfo()).getResource();
			if (r == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				Resource resource = reader.createResource(req.getReader(), r);
				w.write(resource, resp.getWriter());
			}
		}
		permMan.resetAccessContext();
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!setAccessContext(req, resp)) {
			return;
		}
		resp.setCharacterEncoding("UTF-8");
		String path = req.getPathInfo();
		if (path == null || path.equals("/")) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		ResourceRequestInfo r = selectResource(req.getPathInfo());
		if (r == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		else {
			r.resource.delete();
			resp.sendError(HttpServletResponse.SC_OK);
		}
		permMan.resetAccessContext();
	}

	protected ResourceRequestInfo selectResource(String pathInfo) {
		if (pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		String[] path = pathInfo.split("/");
		if (path.length == 0) {
			return null;
		}
		Resource r = appman.getResourceAccess().getResource(path[0]);
		for (int i = 1; i < path.length && r != null; i++) {
			if (r instanceof Schedule) {
				String next = path[i];
				try {
					long start = Long.parseLong(next);
					if (i + 1 < path.length) {
						try {
							long end = Long.parseLong(path[i + 1]);
							// schedule [start, end)
							return new ResourceRequestInfo(r, true, start, end);
						} catch (NumberFormatException nfe) {
							// illegal path
							return null;
						}
					}
					else {
						// schedule [start, infinity)
						return new ResourceRequestInfo(r, true, start, -1);
					}
				} catch (NumberFormatException nfe) {
					// nevermind, treat as subresource access
				}
			}
			r = r.getSubResource(path[i]);
		}
		if (r == null || !r.exists()) {
			return null;
		}
		if (Schedule.class.isAssignableFrom(r.getResourceType())) {
			return new ResourceRequestInfo(r, true, -1, -1);
		}
		return new ResourceRequestInfo(r, false, 0, 0);
	}

	/*
	 * create a SerializationManager according to the request parameters, may send an error and close the response if
	 * any of the parameters are bad.
	 */
	protected SerializationManager getSerializationManager(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		SerializationManager sman = appman.getSerializationManager();
		String depth = req.getParameter(PARAM_DEPTH);
		String schedules = req.getParameter(PARAM_SCHEDULES);
		String references = req.getParameter(PARAM_REFERENCES);
		if (depth != null) {
			try {
				sman.setMaxDepth(Integer.parseInt(depth));
			} catch (NumberFormatException nfe) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, String.format("illegal value for '%s': %s",
						PARAM_DEPTH, depth));
				return null;
			}
		}
		else {
			sman.setMaxDepth(DEFAULT_DEPTH);
		}
		if (schedules != null) {
			sman.setSerializeSchedules(Boolean.parseBoolean(schedules));
		}
		else {
			sman.setSerializeSchedules(DEFAULT_SCHEDULES);
		}
		if (references != null) {
			sman.setFollowReferences(Boolean.parseBoolean(references));
		}
		else {
			sman.setFollowReferences(DEFAULT_REFERENCES);
		}
		return sman;
	}

	protected boolean setAccessContext(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		if (!SECURITY_ENABLED) {
			return true;
		}
		if (!restAcc.checkAccess(req)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}
		else {
			return true;
		}
	}

}
