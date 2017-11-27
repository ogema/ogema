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
package org.ogema.webadmin;

import java.security.AccessControlException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import org.ogema.core.security.WebAccessManager;

/**
 * This extension of {@link WebAccessManager} allows to register HTML pages
 * generated dynamically via a servlet. Http requests to the servlet need not
 * provide a one-time-password, contrary to servlets registered via
 * {@link WebAccessManager#registerWebResource(String, Servlet)}. 
 * <br> 
 * Furthermore, there is a method to register static resources (similar to 
 * {@link #registerWebResource(String, String)}), which prevents the injection 
 * of one-time-passwords into HTML files. This is useful for instance for template HTML files.
 */
public interface AdminWebAccessManager extends WebAccessManager {

	/**
	 * Register a servlet that is treated like a static web resource, i.e.
	 * it can be accessed without a one-time-password. Furthermore,
	 * one-time-passwords can be generated for the page, which should then be injected into
	 * the HTML page and be included in all servlet requests sent from the page.
	 * <br>
	 * Note: all callers in the call stack need an import permission for the org.ogema.webadmin package
	 * @param alias
	 * @param name
	 * @return
	 * @throws AccessControlException 
	 */
	StaticRegistration registerStaticWebResource(String alias, Servlet name);
	
	/**
	 * Like {@link #registerWebResource(String, String)}, except that no one-time-password 
	 * will be injected into any HTML files associated with this registration. Unregister via
	 * {@link #unregisterWebResource(String)}.
	 * <br>
	 * Note: all callers in the call stack need an import permission for the org.ogema.webadmin package
	 * @param alias
	 * @param path
	 * @return
	 * @throws AccessControlException 
	 */
	String registerBasicResource(String alias, String path);
	
	interface StaticRegistration {
		
		String getPath();
		
		/**
		 * @param req
		 * @return
		 * 		a two-element array consisting of one-time-user and one-time-password;
		 * 		or null, if the web access manager has been shut down.
		 * @throws NullPointerException if req is null
		 * @throws IllegalStateException if resource has been unregistered
		 */
		String[] generateOneTimePwd(HttpServletRequest req);
		void unregister();
		
	}
	
}
