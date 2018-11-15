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
