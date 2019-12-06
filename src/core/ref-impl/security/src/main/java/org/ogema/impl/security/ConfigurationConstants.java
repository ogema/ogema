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

public class ConfigurationConstants {

	public static final String CONFIGURATION_PID = "org.ogema.impl.security.AccessManagerImpl";
	
	/*
	 * AccessManager properties
	 */
	/**
	 * Optional configuration property for authenticator services.
	 * May be used to restrict the set of admissible Authenticators. A string with 
	 * different authenticators separated by {@link #AUTH_SEP}.
	 */
	public static final String OGEMA_AUTHENTICATORS_CONFIG = "ogmAuthenticators";
	public static final String AUTH_SEP = "__AUTH__";
	
	/**
	 * Enable http (by default, only https is allowed). Use for testing only. 
	 */
	public static final String HTTP_ENABLE_PROP = "org.ogema.non-secure.http.enable";
	
	/*
	 * Login page properties below
	 */
	
	/**
	 * Optional configuration property for the icon shown on the login page.
	 * Login icon file name within src/main/resources/web folder. Default is "ogema.svg", which
	 * is delivered with this bundle. Other icons can be made available via fragment bundles. 
	 */
	public static final String LOGIN_ICON_CONFIG = "loginIcon";  // e.g.: "ogema.svg"
	
	/**
	 * Configuration property for the icon shown in apps/the menu. 
	 * Icon file name within src/main/resources/web folder. Default is "ogema_nosubline.svg", which
	 * is delivered with this bundle. Other icons can be made available via fragment bundles. 
	 */
	public static final String ICON_CONFIG = "icon";
	
	public static final String DEFAULT_LOGIN_ICON_PROPERTY = "org.ogema.login.icon";
	
	/**
	 * Optional configuration property for the general appearance of the login page.
	 * Allows to change the Bootstrap color scheme. Values:
	 * <ul>
	 *   <li>default
	 *   <li>primary
	 *   <li>secondary
	 *   <li>info
	 *   <li>success
	 *   <li>warning
	 *   <li>danger
	 * </ul>
	 */
	public static final String STYLE_CONFIG = "loginStyleBootstrap";
	/**
	 * System property for changing the default appearance of the login page.
	 * Only applicable if the {@link #STYLE_CONFIG} configuration is not set. 
	 * For admissible values see {@link #STYLE_CONFIG}. Default value is "primary".
	 */
	public static final String DEFAULT_STYLE_PROPERTY = "org.ogema.login.style.bootstrap";

	
}
