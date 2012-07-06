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
		String accept = req.getHeader("Accept");
		//System.out.println("Writer for type: " + accept);

		if (accept == null || accept.equals("*/*")) {
			String contentType = req.getHeader("Content-Type");
			if (contentType != null && contentType.startsWith("application/xml")) {
				return createXmlWriter(sman);
			}
			else {
				return createJsonWriter(sman);
			}
		}
		else if (accept.contains("application/json")) {
			return createJsonWriter(sman);
		}
		else if (accept.contains("application/xml")) {
			return createXmlWriter(sman);
		}
		else {
			//System.out.println("no writer for " + accept);
			return null;
		}
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
