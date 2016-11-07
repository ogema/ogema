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
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.tools.SerializationManager;

/**
 *
 * @author jlapp
 */
public final class ResourceTypeWriters {

	public interface ResourceTypeWriter {
		void write(Class<? extends Resource> r, Writer w) throws IOException;

		String contentType();
	}

	private ResourceTypeWriters() {
	}

	public static ResourceTypeWriter forRequest(HttpServletRequest req, ApplicationManager appMan) {
		String accept = req.getHeader("Accept");
		//System.out.println("Writer for type: " + accept);
		String recursiveStr = req.getParameter(RestTypesServlet.PARAM_RECURSIVE);
		boolean recursive = true;
		if (recursiveStr != null) {
			try {
				recursive = Boolean.parseBoolean(recursiveStr);
			} catch (NumberFormatException e) {}
		}
		if (accept == null || accept.equals("*/*")) {
			String contentType = req.getHeader("Content-Type");
			if (contentType != null && contentType.startsWith("application/xml")) {
				return createXmlWriter(appMan,recursive);
			}
			else {
				return createJsonWriter(appMan,recursive);
			}
		}
		else if (accept.contains("application/json")) {
			return createJsonWriter(appMan,recursive);
		}
		else if (accept.contains("application/xml")) {
			return createXmlWriter(appMan,recursive);
		}
		else {
			//System.out.println("no writer for " + accept);
			return null;
		}
	}

	public static ResourceTypeWriter forRequest(HttpServletRequest req, ApplicationManager am, HttpServletResponse resp)
			throws IOException {
		if (resp.isCommitted()) {
			return null;
		}
		ResourceTypeWriter w = forRequest(req, am);
		if (w == null) {
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "cannot determine/support response media type: "
					+ req.getHeader("Accept"));
		}
		return w;
	}

	public static ResourceTypeWriter createJsonWriter(final ApplicationManager appMan, final boolean recursive) {
		
		
		
		return new ResourceTypeWriter() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void write(Class<? extends Resource> r, Writer w) throws IOException {
				ResourceAccess ra = appMan.getResourceAccess();
				List<? extends Resource> resources;
				if (!recursive)
					resources = ra.getToplevelResources(r);
				else
					resources = ra.getResources(r);
				SerializationManager sman = appMan.getSerializationManager();
				sman.setMaxDepth(0);
				sman.setSerializeSchedules(false);
				sman.setFollowReferences(false);
				w.write(sman.toJson((List) resources));
			}

			@Override
			public String contentType() {
				return "application/json; charset=utf-8";
			}
		};
	}

	public static ResourceTypeWriter createXmlWriter(final ApplicationManager appMan, final boolean recursive) {
		return new ResourceTypeWriter() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void write(Class<? extends Resource> r, Writer w) throws IOException {
				ResourceAccess ra = appMan.getResourceAccess();
				List<? extends Resource> resources;
				if (!recursive)
					resources = ra.getToplevelResources(r);
				else
					resources = ra.getResources(r);
				SerializationManager sman = appMan.getSerializationManager();
				sman.setMaxDepth(0); // FIXME parameters from request should be evaluated
				sman.setSerializeSchedules(false);
				sman.setFollowReferences(false);
				w.write(sman.toXml((List) resources));
			}

			@Override
			public String contentType() {
				return "application/xml; charset=utf-8";
			}
		};
	}

}
