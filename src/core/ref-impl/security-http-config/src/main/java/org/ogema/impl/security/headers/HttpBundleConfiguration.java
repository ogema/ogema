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

/**
 * Configuration interface used by ConfigurationAdmin. 
 * Source for {@link HttpConfigImpl}s.
 */
// FIXME can we prevent these properties from being propagated to service properties?
public @interface HttpBundleConfiguration {
	
	/**
	 * The configuration will be applicable to a bundle with matching symbolic name. 
	 * @return
	 */
	String bundleSymbolicName();
	/**
	 * Enable CORS for certain origins. Will lead to the Access-Control-Allow-Origin header being
	 * set in the server response. Either set to "*" or a specific address. Default is empty, which 
	 * disables CORS. 
	 * @return
	 */
	String corsAllowedOrigin() default "";
	
	/**
	 * Set the Access-Control-Allow-Credentials header. Boolean value.
	 */
	boolean corsAllowCredentials() default false;
	
	/**
	 * Set the Access-Control-Allow-Headers header. A comma-separated list of headers, such as 
	 * "Content-Type, X-Requested-With".
	 */
	String corsAllowHeaders() default "";
	
	// TODO explain
	String staticRedirects() default "";
	
	String staticUriRedirects() default "";
	
	/**
	 * Can be used to include custom headers in the HTTP responses from the bundle.
	 * Note: configure via properties
	 * <ul>
	 *   <li>header.0.key
	 *   <li>header.0.value
	 *   <li>header.0.key
	 *   <li>header.1.key
	 *   <li>...
	 * </ul>
	 * 
	 * @return
	 */
	Header[] header() default {};
	
}
