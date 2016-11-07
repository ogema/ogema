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

import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.ogema.core.application.AppID;
import org.ogema.core.security.WebAccessManager;
import org.osgi.service.http.NamespaceException;

/**
 * WebAccessManager implementation that holds web resources for a single Application. Instances are created by
 * {@link ApplicationWebAccessFactory#createApplicationWebAccessManager(org.ogema.core.application.AppID) }.
 * 
 * @author jlapp
 */
public class ApplicationWebAccessManager implements WebAccessManager {

	final AppID appId;
	final ApplicationWebAccessFactory fac;
	final static String FILTER_APPLICATION = "FILTER_APPLICATION";

	OgemaHttpContext ctx;

	ApplicationWebAccessManager(AppID appId, ApplicationWebAccessFactory fac) {
		this.appId = appId;
		this.fac = fac;
	}

	private synchronized OgemaHttpContext getOrCreateHttpContext() {
		if (ctx == null) {
			ctx = new OgemaHttpContext(fac.pm, appId);
		}
		return ctx;
	}
	
	synchronized void close() {
		if (ctx != null)
			ctx.close();
		ctx = null;
	}

	/**
	 * This method is called by the application or a osgi bundle. The registration is delegated to the http service with
	 * a OgemaHttpContext each App/Bundle. Bundle reference and the alias are registered by WebResourceManager so these
	 * information can be provided to security handling in OgemaHttpContext.
	 */
	@Override
	public String registerWebResource(String alias, String name) {
		String result = alias;
		// In this case we use the HttpContext already created for this bundle.
		// Otherwise we need a new one.
		OgemaHttpContext httpCon = getOrCreateHttpContext();

		alias = normalizePath(alias);
		name = normalizePath(name);

		try {
			// 1. register Resource to the http service
			fac.http.registerResources(alias, name, httpCon);
		} catch (NamespaceException e) {
			throw new RuntimeException("Webresource path already in use: " + alias);
		}

		httpCon.resources.put(result, name);
		return result;
	}

	/**
	 * Like registerWebResource above but registerServlet instead of registerResource
	 */
	@Override
	public String registerWebResource(String alias, Servlet servlet) {
		OgemaHttpContext httpCon = getOrCreateHttpContext();

		alias = normalizePath(alias);

		String result = alias;

		try {
			// register Resource to the http service
			fac.http.registerServlet(alias, servlet, null, httpCon);
		} catch (NamespaceException e) {
			throw new RuntimeException("Servlet path already in use: " + alias);
		} catch (ServletException e) {
			throw new RuntimeException("Servlet exception " + alias,e);
		}
		httpCon.servlets.put(result, appId.getIDString());

		return result;
	}

	String generateAlias(String alias, AppID id) {
		return alias + id.getIDString().hashCode();
	}

	@Override
	public String registerWebResourcePath(String alias, Servlet servlet) {
		alias = normalizePath(alias);
		String newAlias = extendPath(alias);
		return registerWebResource(newAlias, servlet);
	}

	@Override
	public String registerWebResourcePath(String alias, String name) {
		alias = normalizePath(alias);
		name = normalizePath(name);
		String newAlias = extendPath(alias);
		return registerWebResource(newAlias, name);
	}

	@Override
	public void unregisterWebResource(String alias) {
		if (ctx == null || (ctx.resources.isEmpty() && ctx.servlets.isEmpty())) {
			fac.logger.warn("unregisterWebResource called on empty context. alias={}", alias);
		}
		alias = normalizePath(alias);
		if (ctx != null) { // only happens if framework is shutting down
			ctx.resources.remove(alias);
			ctx.servlets.remove(alias);
		}
		try {
			fac.http.unregister(alias);
		} catch (IllegalArgumentException iae) {
			fac.logger.error("No registration found " + alias);
		}
	}

	@Override
	public boolean unregisterWebResourcePath(String alias) {
		alias = normalizePath(alias);
		String newAlias = extendPath(alias);
//		OgemaHttpContext httpContext = getOrCreateHttpContext();
		if (ctx != null) { // only happens if framework is shutting down
			ctx.resources.remove(newAlias);
			ctx.servlets.remove(newAlias);
		}
		try {
			fac.http.unregister(newAlias);
		} catch (IllegalArgumentException iae) {
			fac.logger.error("No registration found " + newAlias);
			return false;
		}
		return true;
	}

	private String extendPath(String alias) {
		char seperator = '/';
		String newAlias = appId.getBundle().getSymbolicName() + alias;
		newAlias = seperator + newAlias.replace('.', seperator);
		newAlias = newAlias.toLowerCase();

		// if the last character is "/" then remove it due to possible path exception
		if (newAlias.charAt(newAlias.length() - 1) == '/') {
			newAlias = newAlias.substring(0, newAlias.length() - 1);
		}
		return newAlias;
	}

	private String normalizePath(String alias) {
		char seperator = '/';

		// always add "/" as prefix
		if (alias.length() > 0 && alias.charAt(0) != seperator) {
			alias = seperator + alias;
		}

		return alias;
	}

	@Override
	public String getStartUrl() {

		// was called because key exists
		if (fac.baseUrls.containsKey(appId.getIDString())) {

			String baseUrl = fac.baseUrls.get(appId.getIDString());
			if (baseUrl.equals(FILTER_APPLICATION)) {
				return null;
			}

			// key does not exist so it was never called
		}
		else {
			Map<String, String> registeredResources = getRegisteredResources(appId);
			// take the first webresource we find. this is ok because we dont know which resource is correct.
			for (String url : registeredResources.keySet()) {
				return url + "/index.html";
			}
		}

		return fac.baseUrls.get(appId.getIDString());
	}

	public void registerStartUrl(AppID appId, String url) {
		if (url == null) {
			url = FILTER_APPLICATION;
		}
		else {
			if (url.length() > 0) {
				if ('/' != url.charAt(0)) {
					url = "/" + url;
				}
			}
		}
		fac.baseUrls.put(appId.getIDString(), url);
	}

	@Override
	public void registerStartUrl(String url) {
		registerStartUrl(appId, url);
	}

	// public void setStartUrl(String url) {
	// registerStartUrl(appId, url);
	// }

	@Override
	@Deprecated
	public Map<String, String> getRegisteredResources(AppID appid) {
		return fac.getRegisteredResources(appid);
	}

	@Override
	@Deprecated
	public Set<String> getRegisteredServlets(AppID appid) {
		return fac.getRegisteredServlets(appid);
	}

	@Override
	public Map<String, String> getRegisteredResources() {
		return getRegisteredResources(appId);
	}

	@Override
	public Set<String> getRegisteredServlets() {
		return getRegisteredServlets(appId);
	}

	@Override
	public boolean authenticate(HttpSession ses, String usr, String pwd) {
		return fac.authenticate(ses, usr, pwd);
	}

}
