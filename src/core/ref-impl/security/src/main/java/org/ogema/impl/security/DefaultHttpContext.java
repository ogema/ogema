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
