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
package org.ogema.impl.security.headers;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ogema.accesscontrol.HttpConfig;


/**
 * Configuration type for setting HTTP headers and redirects.<br> 
 * Configurations can be provided globally via system properties (e.g. {@link #CORS_ALLOWED_ORIGIN_PROP},
 * or {@link #STATIC_REDIRECTS_PROP}), and per bundle 
 * via Configuration Admin (config type {@link HttpBundleConfiguration}).
 */
public class HttpConfigImpl implements HttpConfig {

	/**
	 * Enable CORS for certain origins. Will lead to the Access-Control-Allow-Origin header being
	 * set in the server response.
	 * Either set to "*" or a specific address. 
	 */
	private static final String CORS_ALLOWED_ORIGIN_PROP = "org.ogema.webresourcemanager.allowedOrigin";
	/**
	 * Set the Access-Control-Allow-Credentials header. Boolean value.
	 */
	private static final String CORS_ALLOW_CREDENTIALS_PROP = "org.ogema.webresourcemanager.allowCredentials";
	/**
	 * Set the Access-Control-Allow-Headers header. A comma-separated list of headers, such as 
	 * "Content-Type, X-Requested-With".
	 */
	private static final String CORS_ALLOW_HEADERS_PROP = "org.ogema.webresourcemanager.allowHeaders";
	
	private static final String STATIC_REDIRECTS_PROP = "org.ogema.webresourcemanager.staticredirects";
	private static final String STATIC_URI_REDIRECTS_PROP =	"org.ogema.webresourcemanager.staticuriredirects";
	

	/**
	 * Default: nothing allowed.
	 */
	static final HttpConfigImpl DEFAULT_CONFIG;
	
	static {
		final boolean secure = System.getSecurityManager() != null;
		final String allowedOrigin = getProperty(secure, CORS_ALLOWED_ORIGIN_PROP);
		final boolean allowCredentials = allowedOrigin != null && "true".equalsIgnoreCase(getProperty(secure, CORS_ALLOW_CREDENTIALS_PROP));
		final String allowedHeaders = allowedOrigin == null ? null : getProperty(secure, CORS_ALLOW_HEADERS_PROP);
		final String staticRedirects = getProperty(secure, STATIC_REDIRECTS_PROP);
		final String staticUriRedirects = getProperty(secure, STATIC_URI_REDIRECTS_PROP);
		DEFAULT_CONFIG = new HttpConfigImpl(allowedOrigin, allowCredentials, allowedHeaders, staticRedirects, staticUriRedirects, null);
	}
	
	private static String getProperty(final boolean secure, final String property) {
		return secure ? System.getProperty(property) : AccessController.doPrivileged(new PrivilegedAction<String>() {

			@Override
			public String run() {
				return System.getProperty(property);
			}
		});
	}
	
	
	private final String allowedOrigin;
	private final boolean allowCredentials;
	private final String allowedHeaders;
	private final Map<String,String> staticRedirects;
	private final Map<String,String> staticUriRedirects;
	private final Map<String, String> customHeaders;
	
	HttpConfigImpl(String allowedOrigin, boolean allowCredentials, String allowedHeaders, String staticRedirects, String staticUriRedirects, Header[] headers) {
		this.allowedOrigin = allowedOrigin == null || allowedOrigin.isEmpty() ? null : allowedOrigin;
		this.allowCredentials = allowedOrigin == null ? false : allowCredentials;
		this.allowedHeaders = allowedOrigin == null || allowedHeaders == null || allowedHeaders.isEmpty() ? null : allowedHeaders;
		this.staticRedirects = getRedirects(staticRedirects);
		this.staticUriRedirects = getRedirects(staticUriRedirects);
		if (headers == null || headers.length == 0)
			this.customHeaders = null;
		else {
			final Map<String, String> ch = new HashMap<>(headers.length);
			for (Header h: headers) {
				final String key = h.key();
				final String val = h.value();
				if (key == null || val == null) // e.g. if the config properties index starts with 1 instead of 0
					continue;
				ch.put(key, val);
			}
			this.customHeaders = Collections.unmodifiableMap(ch);
		}
	}

	@Override
	public String getAllowedOrigin(HttpServletRequest req) {
		return allowedOrigin;
	}

	@Override
	public boolean isAllowCredentials(HttpServletRequest req) {
		return allowCredentials;
	}

	@Override
	public String getAllowedHeaders(HttpServletRequest req) {
		return allowedHeaders;
	}
	
	/**
	 * @return
	 *  	may be null
	 */
	@Override
	public Map<String, String> getRedirects() {
		return staticRedirects;
	}

	/**
	 * @return
	 *  	may be null
	 */
	@Override
	public Map<String, String> getUriRedirects() {
		return staticUriRedirects;
	}

	/**
	 * @return
	 *  	may be null
	 */
	@Override
	public Map<String, String> getCustomHeaders(HttpServletRequest req) {
		return customHeaders;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("HeaderConfig[");
		if (allowedOrigin == null) 
			sb.append("CORS disabled");
		else {
			sb.append("allowedOrigin: ").append(allowedOrigin).append(',').append(' ')
				.append("allowCredentials: ").append(allowCredentials).append(',').append(' ')
				.append("allowHeaders: ").append(allowedHeaders);
		}
		if (staticRedirects != null)
			sb.append(", static redirects: ").append(staticRedirects);
		if (staticUriRedirects != null)
			sb.append(", static URI redirects: ").append(staticUriRedirects);
		if (customHeaders != null)
			sb.append(", custom headers: ").append(customHeaders);
		sb.append(']');
		return sb.toString();
	}
	
	private static Map<String,String> getRedirects(final String redirects) {
		if (redirects == null || redirects.isEmpty())
			return null;
		String[] rd = redirects.split(",");
		if (rd.length % 2 == 0) {
			Map<String,String> rds = new HashMap<>((int) (rd.length/ 2 * 1.25));
			for (int i=0;i<rd.length/2;i++) {
				String key = rd[2*i].trim();
				if (key.isEmpty())
					continue;
				if (!key.endsWith("/"))
					key = key + "/";
				if (key.charAt(0) != '/')
					key = "/" + key;
					
				String target = rd[2*i+1].trim();
				rds.put(key, target);
			}
			return Collections.unmodifiableMap(rds);
		}
		else
			return null;
 	}
	
}
