/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import javax.servlet.Servlet;
import javax.servlet.http.HttpSession;

import org.ogema.core.application.AppID;

/**
 * Methods for applications to register resources that are accessible vie web interface. This
 * implies setting the web-resource that is considered the application's start page: All registration
 * calls to {@link #registerWebResource(java.lang.String, java.lang.String) } are recorded in order.
 * The start page of the application is considered the first/oldest so-registered web-resource that contains
 * an index.html file and which has not been unregistered, since.
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
	 * Gets a map of all web resources registered by an application which is specified with AppID object.
	 * 
	 * @param appid
	 *            The id object of the application.
	 * @return A map of (alias, name) pairs of the registered web resources.
	 */
	public Map<String, String> getRegisteredResources(AppID appid);

}
