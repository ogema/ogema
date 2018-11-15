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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.tools.SerializationManager;

/**
 *
 * @author jlapp
 */
public final class ResourceWriters {

	public interface ResourceWriter {
		void write(Resource r, Writer w) throws IOException;

		void writeSchedule(Schedule sched, long start, long end, Writer w) throws IOException;

		String contentType();
	}

	private ResourceWriters() {
	}

	public static ResourceWriter forRequest(HttpServletRequest req, SerializationManager sman) {
		return Utils.xmlOrJson(req) ? createXmlWriter(sman) : createJsonWriter(sman);
	}

	public static ResourceWriter forRequest(HttpServletRequest req, SerializationManager sman, HttpServletResponse resp)
			throws IOException {
		if (resp.isCommitted()) {
			return null;
		}
		ResourceWriter w = forRequest(req, sman);
		if (w == null) {
			resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "cannot determine/support response media type: "
					+ req.getHeader("Accept"));
		}
		return w;
	}

	public static ResourceWriter createJsonWriter(final SerializationManager sman) {
		return new ResourceWriter() {

			@Override
			public void write(Resource r, Writer w) throws IOException {
				sman.writeJson(w, r);
			}

			@Override
			public void writeSchedule(Schedule sched, long start, long end, Writer w) throws IOException {
				sman.writeJson(w, sched, start, end);
			}

			@Override
			public String contentType() {
				return "application/json; charset=utf-8";
			}
		};
	}

	public static ResourceWriter createXmlWriter(final SerializationManager sman) {
		return new ResourceWriter() {

			@Override
			public void write(Resource r, Writer w) throws IOException {
				sman.writeXml(w, r);
			}

			@Override
			public void writeSchedule(Schedule sched, long start, long end, Writer w) throws IOException {
				sman.writeXml(w, sched, start, end);
			}

			@Override
			public String contentType() {
				return "application/xml; charset=utf-8";
			}
		};
	}

}
