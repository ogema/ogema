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
	
	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}
}
