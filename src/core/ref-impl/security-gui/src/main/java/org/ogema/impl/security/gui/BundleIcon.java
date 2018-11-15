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
package org.ogema.impl.security.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

/**
 *
 * @author jlapp
 */
public class BundleIcon {

	private static final Map<Bundle, BundleIcon> icons = new HashMap<>();
	private static final BundleIcon NO_ICON = new BundleIcon(null, IconType.PNG);

	public static enum IconType {

		PNG("image/png"), JPG("image/jpg"), SVG("image/svg+xml");
		private final String contentType;

		IconType(String contentType) {
			this.contentType = contentType;
		}

	};

	private final URL url;
	private final IconType type;

	/* images that fit into buffer are stored on first read */
	static final int BUFFER_SIZE = 8192;
	private int size = -1;
	private byte[] storedBuffer;

	public BundleIcon(URL url, IconType type) {
		this.url = url;
		this.type = type;
	}

	public static BundleIcon forBundle(Bundle b, BundleIcon defaultIcon) {
		BundleIcon icon = icons.get(b);
		if (icon != null) {
			return icon == NO_ICON ? defaultIcon : icon;
		}

		URL url = b.getResource("/icon.svg");
		if (url != null) {
			icon = new BundleIcon(url, IconType.SVG);
		}
		else {
			url = b.getResource("/icon.png");
			if (url != null) {
				icon = new BundleIcon(url, IconType.PNG);
			}
			else {
				url = b.getResource("/icon.jpg");
				if (url != null) {
					icon = new BundleIcon(url, IconType.JPG);
				}
				else {
					icon = NO_ICON;
				}
			}
		}
		icons.put(b, icon);
		return icon == NO_ICON ? defaultIcon : icon;
	}

	public static void forNewBundle(String location, BundleIcon defaultIcon, HttpServletResponse resp)
			throws IOException {
		int index = location.indexOf(':');
		String jarpath;
		if (index != -1) {
			jarpath = location.substring(index + 1);
		}
		else
			jarpath = location;

		File f = new File(jarpath);
		JarFile jar = null;
		try {
			jar = new JarFile(f);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JarEntry entry = jar.getJarEntry("icon.svg");
		if (entry != null) {
			//
		}
		else {
			entry = jar.getJarEntry("icon.png");
			if (entry != null) {
				//
			}
			else {
				entry = jar.getJarEntry("icon.jpg");
				if (entry != null) {
					//
				}
				else {
					try {
						defaultIcon.writeIcon(resp);
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		InputStream inS = jar.getInputStream(entry);

		try (OutputStream out = resp.getOutputStream()) {
			out.write(IOUtils.toByteArray(inS));

		}
	}

	public void writeIcon(HttpServletResponse resp) throws IOException {
		resp.setContentType(type.contentType);

		if (size != -1 && size < BUFFER_SIZE) {
			synchronized (this) {
				if (storedBuffer == null) {
					storedBuffer = new byte[BUFFER_SIZE];
					try (InputStream is = url.openStream()) {
						int len, offset = 0;
						while ((len = is.read(storedBuffer, offset, BUFFER_SIZE - offset)) != -1) {
							offset += len;
						}
					}
				}
			}
			try (OutputStream out = resp.getOutputStream()) {
				out.write(storedBuffer, 0, size);
			}
			return;
		}

		byte[] buf = new byte[BUFFER_SIZE];
		int totalSize = 0;
		try (InputStream is = url.openStream(); OutputStream out = resp.getOutputStream()) {
			int len;
			while ((len = is.read(buf)) != -1) {
				out.write(buf, 0, len);
				totalSize += len;
			}
		}

		size = totalSize;
	}
}
