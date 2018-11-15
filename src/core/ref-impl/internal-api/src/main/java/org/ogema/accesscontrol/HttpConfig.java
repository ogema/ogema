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
package org.ogema.accesscontrol;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Configuration type for setting HTTP headers and redirects.
 */
public interface HttpConfig {

	/**
	 * Enable CORS for certain origins. If non-null, the
	 * Access-Control-Allow-Origin header will be included in the server response.
	 * Either set to "*" or a specific address.
	 * @param req
	 * @return
	 * 		may be null
	 */
	String getAllowedOrigin(HttpServletRequest req);

	/**
	 * Set the Access-Control-Allow-Credentials header. Boolean value.
	 * If {@link #getAllowedOrigin(HttpServletRequest)} is null, then this will be ignored.
	 * @param req
	 * @return
	 */
	boolean isAllowCredentials(HttpServletRequest req);

	/**
	 * Set the Access-Control-Allow-Headers header. A comma-separated list of headers, such as 
	 * "Content-Type, X-Requested-With".
	 * If {@link #getAllowedOrigin(HttpServletRequest)} is null, then this will be ignored.
 	 * @param req
	 * @return
	 * 		may be null
	 */
	String getAllowedHeaders(HttpServletRequest req);

	/**
	 * Headers that will be sent with every HTTP response for this bundle.
	 * @param req
	 * @return
	 *  	may be null
	 */
	Map<String, String> getCustomHeaders(HttpServletRequest req);
	
	/**
	 * Request redirect configuration. Redirect requests to a static web resource that do not exactly 
	 * match a registered resource path.
	 * @return
	 *  	may be null
	 */
	Map<String, String> getRedirects();

	/**
	 * Request redirect configuration. Redirect servlet requests  that do not exactly 
	 * match a registered servlet path.
	 * @return
	 *  	may be null
	 */
	Map<String, String> getUriRedirects();
	
}
