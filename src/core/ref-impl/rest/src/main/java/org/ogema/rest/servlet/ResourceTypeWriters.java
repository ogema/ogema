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
		//System.out.println("Writer for type: " + accept);
		String recursiveStr = req.getParameter(RestTypesServlet.PARAM_RECURSIVE);
		boolean recursive = true;
		if (recursiveStr != null) {
			try {
				recursive = Boolean.parseBoolean(recursiveStr);
			} catch (NumberFormatException e) {}
		}
		return Utils.xmlOrJson(req) ? createXmlWriter(appMan, recursive) : createJsonWriter(appMan, recursive);
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
