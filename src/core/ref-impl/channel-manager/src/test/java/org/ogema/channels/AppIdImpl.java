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
package org.ogema.channels;

import java.net.URL;

import javax.servlet.http.HttpSession;

import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.osgi.framework.Bundle;

public class AppIdImpl implements AppID {

	String id;
	
	AppIdImpl(String id) {
		this.id = id; 
	}
	
	@Override
	public String getIDString() {
		return id;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getBundle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Application getApplication() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOwnerUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOwnerGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getOneTimePasswordInjector(String path, HttpSession ses) {
		// TODO Auto-generated method stub
		return null;
	}
}
