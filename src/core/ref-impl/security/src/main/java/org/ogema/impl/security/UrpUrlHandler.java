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
package org.ogema.impl.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerSetter;

/**
 * URP urls contain only the user name for which the user rights proxy will be installed, e.g. "urp:rest".
 * @author jlapp
 */
public class UrpUrlHandler implements URLStreamHandlerService {

	public final static String PROTOCOL = "urp";
	private final static String URP_BUNDLE_LOCATION = "/user-rights-proxy.jar";

	static class UrpUrlConnection extends URLConnection {

		public UrpUrlConnection(URL url) {
			super(url);
		}

		@Override
		public void connect() throws IOException {
			//nothing to do here
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return getClass().getResourceAsStream(URP_BUNDLE_LOCATION);
		}

	}

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return new UrpUrlConnection(url);
	}

	@Override
	public void parseURL(URLStreamHandlerSetter realHandler, URL url, String spec, int i, int i1) {
		// store user name as url path component
		String path = spec.substring(spec.indexOf(":") + 1);
		realHandler.setURL(url, url.getProtocol(), null, -1, null, null, path, null, null);
	}

	@Override
	public String toExternalForm(URL url) {
		return url.toString();
	}

	@Override
	public boolean equals(URL url, URL url1) {
		return url.equals(url1);
	}

	@Override
	public int getDefaultPort() {
		return 0;
	}

	@Override
	public InetAddress getHostAddress(URL url) {
		return InetAddress.getLoopbackAddress();
	}

	@Override
	public int hashCode(URL url) {
		return url.hashCode();
	}

	@Override
	public boolean hostsEqual(URL url, URL url1) {
		//urp urls do not contain hosts
		return true;
	}

	@Override
	public boolean sameFile(URL url, URL url1) {
		return url.equals(url1);
	}

}
