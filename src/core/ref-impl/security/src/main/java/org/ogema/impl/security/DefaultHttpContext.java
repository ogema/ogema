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

//--------------------------------------------------------------------------
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ogema.core.application.AppID;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public class DefaultHttpContext implements HttpContext {

	/**
	 * This class holds the context of the registered servlet the context encapsulates the way to request resources,
	 * mime types and the check for Authentication the default is: get the resources from bundles jar get the standard
	 * mime types allow any request
	 */

	// alias vs. app id
	LinkedHashMap<String, String> resources;
	HashMap<String, AppID> servlets;

	private final Bundle bundle;

	// test szenario:
	// Mimes.get(str) was tested - see Mimes
	// bundle.getResource is tested in BundleImpl

	// ----------------------------------------------------------------------
	/**
	 * constructor
	 * 
	 * @param bundle
	 */
	public DefaultHttpContext(Bundle bundle) {
		this.resources = new LinkedHashMap<>();
		this.servlets = new HashMap<>();
		this.bundle = bundle;
	}

	// ----------------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
	 */
	@Override
	public URL getResource(String name) {
		return bundle.getResource(name);
		// this should only return the bundles jar resources
		// permissions are checked inside
	}

	// ----------------------------------------------------------------------
	/*
	 * gets a mime type from the extension
	 * 
	 * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
	 */
	@Override
	public String getMimeType(String str) {
		return null;
	}

	// ----------------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean handleSecurity(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
			throws IOException {
		// this is a correct implementation of a default HttpContext
		// as the default in our case does not need to have authenticatin
		// In the case Authentication is needed request.getScheme would return
		// https when an SSL connection is used
		// Req has headers user and header password if an authetication was used
		// Attributes have to be set:
		// AUTHENTICATION_TYPE
		// REMOTE_USER
		// AUTHORIZATION authentication object by User Admin service
		// see R3 14.7.1
		return true;
	}
	// ----------------------------------------------------------------------
}
