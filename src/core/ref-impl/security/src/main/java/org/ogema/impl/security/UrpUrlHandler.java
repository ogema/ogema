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
		return "urp:" + url.getPath();
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
