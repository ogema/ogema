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
package org.ogema.accesscontrol;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.ogema.core.application.AppID;

public class RedirectionURLHandler extends URLStreamHandler {

	protected String name;
	private AppID app;
	private String otp;

	public RedirectionURLHandler(AppID owner, String name, String otp) {
		this.app = owner;
		this.name = name;
		this.otp = otp;
	}

	protected URLConnection openConnection(URL url) {
		return new RedirectionURLConnection(url, name, app, otp);
	}

}
