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
import java.io.Reader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ogema.core.model.Resource;
import org.ogema.core.tools.SerializationManager;

/**
 *
 * @author jlapp
 */
public final class ResourceReaders {

	public interface ResourceReader {

		void readResource(Reader r, Resource target);

		<T extends Resource> T createResource(Reader r, Resource target);
	}

	private ResourceReaders() {
	}

	public static ResourceReader forRequest(HttpServletRequest req, SerializationManager sman) {
		String contentType = req.getHeader("Content-Type");
		if (contentType == null || contentType.startsWith("application/json")) {
			return createJsonReader(sman);
		}
		else if (contentType.startsWith("application/xml")) {
			return createXmlReader(sman);
		}
		else {
			System.out.println("no reader for type: " + contentType);
			return null;
		}
	}

	public static ResourceReader forRequest(HttpServletRequest req, SerializationManager sman, HttpServletResponse resp)
			throws IOException {
		if (resp.isCommitted()) {
			return null;
		}
		ResourceReader r = forRequest(req, sman);
		if (r == null) {
			resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "unsupported media type: "
					+ req.getHeader("Content-Type"));
		}
		return r;
	}

	public static ResourceReader createJsonReader(final SerializationManager sman) {
		return new ResourceReader() {

			@Override
			public void readResource(Reader r, Resource target) {
				sman.applyJson(r, target, true);
			}

			@Override
			public <T extends Resource> T createResource(Reader r, Resource target) {
				if (target == null) {
					return sman.createFromJson(r);
				}
				else {
					return sman.createFromJson(r, target);
				}
			}

		};
	}

	public static ResourceReader createXmlReader(final SerializationManager sman) {
		return new ResourceReader() {

			@Override
			public void readResource(Reader r, Resource target) {
				sman.applyXml(r, target, true);
			}

			@Override
			public <T extends Resource> T createResource(Reader r, Resource target) {
				if (target == null) {
					return sman.createFromXml(r);
				}
				else {
					return sman.createFromXml(r, target);
				}
			}
		};
	}

}
