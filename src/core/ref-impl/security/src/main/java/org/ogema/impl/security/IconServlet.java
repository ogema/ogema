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
package org.ogema.impl.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class IconServlet extends HttpServlet {

	private static final String DEFAULT_ICON = "ogema_nosubline.svg";
	private static final long serialVersionUID = 1l;
	private volatile SoftReference<byte[]> image;
	
	private volatile String ICON;
	private volatile String ICON_TYPE;

	IconServlet(Map<String, Object> config) {
		configUpdate(config);
	}
	
	// copied from LoginServlet
	final void configUpdate(final Map<String, Object> config) {
		final Object iconObj = config.get(ConfigurationConstants.ICON_CONFIG);
		String icon = iconObj instanceof String ? (String) iconObj : DEFAULT_ICON;
		final URL test = IconServlet.class.getResource("/web/" + icon);
		if (test == null)
			icon = DEFAULT_ICON;
		ICON = "/web/" + icon;
		final String[] components = ICON.split("\\.");
		final String type;
		if (components.length > 1) {
			final String ending = components[components.length-1];
			switch (ending.toLowerCase()) {
			case "svg":
				type = "svg+xml";
				break;
			case "jpg":
			case "jpeg":
				type = "jpeg";
				break;
			default:
				type = ending.toLowerCase();
			}
		} else
			type = "png";
		ICON_TYPE = "image/" + type;
		image = new SoftReference<byte[]>(null);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final byte[] image = load();
		resp.setContentType(ICON_TYPE);
		resp.getOutputStream().write(image);
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.flushBuffer();
	}
	
	private byte[] load() throws IOException {
		byte[] bytes = this.image.get();
		if (bytes == null) {
			synchronized (this) {
				bytes = this.image.get();
				if (bytes == null) {
					bytes = loadInternal();
					this.image = new SoftReference<byte[]>(bytes);
				}
			}
		}
		return bytes;
	}
	
	private byte[] loadInternal() throws IOException {
		final URL resource = getClass().getResource(ICON);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (final InputStream in = resource.openStream()) {
			int read;
			final byte[] buffer = new byte[4096];
			while ((read = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, read);
			}
		}
		return out.toByteArray();
	}
	
}
