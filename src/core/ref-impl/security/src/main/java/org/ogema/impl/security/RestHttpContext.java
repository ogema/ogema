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

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;

public class RestHttpContext implements HttpContext {

	boolean securemode = true;

	private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

	// WebAccessManagerImpl wam;

	public RestHttpContext() {
		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				// For development purposes secure mode could be disabled
				if (System.getProperty("org.ogema.secure.httpcontex", "true").equals("false"))
					securemode = false;
				return null;
			}
		});
		// this.wam = wam;
	}

	/*
	 * Check if a valid one time password was assigned. If no OTP exist for the return false.
	 * 
	 * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/*
		 * If the request requires a secure connection and the getScheme method in the request does not return 'https'
		 * or some other acceptable secure protocol, then this method should set the status in the response object to
		 * Forbidden(403) and return false.
		 */

		String scheme = request.getScheme();
		if (!scheme.equals("https")) {
			logger.info("\tSecure connection is required.");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getOutputStream().write("\tSecure connection is required.".getBytes());
			response.flushBuffer();
			return false;
		}
		return true;
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	@Override
	public String getMimeType(String name) {
		// MimeType of the default HttpContext will be used.
		return null;
	}
}
