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
package org.ogema.core.security;

import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.http.HttpSession;

import org.ogema.core.application.AppID;
import org.ogema.core.application.ApplicationManager;

/**
 * Provides methods for an application to register servlets and web pages.
 * An Application's WebAccessManager is obtained via {@link ApplicationManager#getWebAccessManager() }.
 * See {@link #registerStartUrl(java.lang.String) registerStartUrl} on how to set an application's
 * start page.
 */
public interface WebAccessManager {

	/**
	 * Unregister a web resource that was previously registered with the given alias.
	 * 
	 * @param alias
	 *            Alias string of the resource which is to be unregistered.
	 */
	public void unregisterWebResource(String alias);

	/**
	 * This method is called by the application to publish files as web resources. The registration is delegated to the
	 * http service with a HttpContext instance each application. The name of the web resource is the path name of its
	 * location. The alias could be a different one as the specified alias when the resource is registered. For the first
	 * web resource registered this way by an application that contains a "/index.html", the respective index.html is
	 * considered as the application's start page.
	 * 
	 * @param alias
	 *            the alias of the registered web resource
	 * @param name
	 *            the name of the registered web resource
	 * @return the alias string which is effectively used to register this resource.
	 */
	public String registerWebResource(String alias, String name);

	/**
	 * This method is called by the application to publish a servlet as web resource. The registration is delegated to
	 * the http service with a HttpContext instance each application. The alias could be a different one as the
	 * specified alias when the resource is registered.
	 * 
	 * @param alias
	 *            the alias string
	 * @param servlet
	 *            the servlet object
	 * @return the effectively user alias string
	 */
	public String registerWebResource(String alias, Servlet servlet);

	/**
	 * Authenticate a one time user which is associated with the session object. This method is called to protect ogema
	 * resources if they are accessed via dynamic elements from a web resources.
	 * 
	 * @param ses
	 *            The http session object
	 * @param usr
	 *            The user name string
	 * @param pwd
	 *            The password string
	 * @return true if the user is associated with the session and its credentials match the specified password string,
	 *         false otherwise.
	 */
	public boolean authenticate(HttpSession ses, String usr, String pwd);

	/**
	 * Gets a map of all static web resources registered by an application which is specified with AppID object.
	 * 
	 * @param appid
	 *            The id object of the application.
	 * @return A map of (alias, name) pairs of the registered web resources.
	 * @deprecated not application-specific, use {@link #getRegisteredResources() } instead.
	 */
	@Deprecated
	public Map<String, String> getRegisteredResources(AppID appid);

	/**
	 * Gets a set of all servlet aliases registered by an application which is specified with AppID object.
	 * 
	 * @param appid
	 *            The id object of the application.
	 * @return A set of registered servlet aliases.
	 * @deprecated not application-specific, use {@link #getRegisteredServlets() } instead.
	 */
	@Deprecated
	public Set<String> getRegisteredServlets(AppID appid);

	/**
	 * Returns a map of all static web resources that have been registered with this WebAccessManager.
	 * 
	 * @return A map of (alias, name) pairs of the registered web resources.
	 */
	public Map<String, String> getRegisteredResources();

	/**
	 * Returns the set servlet aliases that have been registered with this WebAccessManager.
	 * 
	 * @return A set of registered servlet aliases.
	 */
	public Set<String> getRegisteredServlets();

	/**
	 * Set the URL of the start page of an application. Either a static webresource (HTML file) or a servlet 
	 * (generating dynamic HTML) should be registered at this URL. <br>
	 * Case 1: This method is never called: a default start page is chosen, i.e. if the app has registered
	 * any static web resources, a file index.html is assumed to exist in the base folder. If there are no static web resources,
	 * the app will not be listed in the framework GUI <br>
	 * Case 2: The method is called with argument null: The app will not be listed in the framework GUI<br>
	 * Case 3: A proper url is passed to the method: this URL will be registered as the apps start page 
	 *
	 * @param url
	 * 			  Start page URL
	 */
	public void registerStartUrl(String url);

	/**
	 * Get URL of the start page of an application; may be null, if the application has not registered a start page 
	 * via {@link #registerStartUrl(org.ogema.core.application.AppID, java.lang.String) } yet and has no static web resources.
	 * 
	 * @param appid
	 *            The id object of the application.
	 */
	public String getStartUrl();

	/**
	 * Unregisters a webresource that was registered with registerWebResourcePath before. The alias has to be equal to
	 * the one that was used during the registration.
	 * 
	 * @param alias 
	 *              the alias string
	 * @return true if unregister was successful, false otherwise. 
	 */
	public boolean unregisterWebResourcePath(String alias);

	/**
	 * This method is called by the application to publish a servlet as web resource. The registration is delegated to
	 * the http service with a HttpContext instance each application. The absolute alias is generated by ogema. The first
	 * part of the newly generated alias consists of the group id and the application name.
	 * Example: 
	 *      groupid: org.ogema.apps
	 *      name: my-app
	 *      alias: servlet
	 *      newly generated alias = /org/ogema/apps/my-app/servlet
	 * 
	 * @param alias
	 *              the alias string
	 * @param servlet
	 *              the servlet object
	 * @return      the newly generated alias
	 */
	public String registerWebResourcePath(String alias, Servlet servlet);

	/**
	 * This method is called by the application to publish files as web resources. The registration is delegated to the
	 * http service with a HttpContext instance each application. The name of the web resource is the path name of its
	 * location. The absolute alias is generated by ogema. The first part of the newly generated alias consists of the 
	 * group id and the application name. For the first web resource registered this way by an application that contains 
	 * a "/index.html", the respective index.html is considered as the application's start page.
	 * 
	 * Example: 
	 *      groupid: org.ogema.apps
	 *      name: my-app
	 *      alias: /
	 *      newly generated alias = /org/ogema/apps/my-app/
	 *      index.html =            /org/ogema/apps/my-app/index.html
	 * 
	 * @param alias
	 *              the alias string
	 * @param name
	 *              the name of the registered web resource
	 * @return 
	 */
	public String registerWebResourcePath(String alias, String name);

}
