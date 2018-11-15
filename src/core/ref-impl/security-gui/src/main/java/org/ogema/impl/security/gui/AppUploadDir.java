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
package org.ogema.impl.security.gui;

import java.util.List;

import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.osgi.framework.Bundle;

public class AppUploadDir implements ApplicationSource {

	String name;
	String location;
	boolean islocale;

	public AppUploadDir(String name, String location, boolean isLocaleStore) {
		this.name = name;
		this.location = location;
		this.islocale = isLocaleStore;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<InstallableApplication> getAppsAvailable() {
		return null;
	}

	@Override
	public String getAddress() {
		return location;
	}

	@Override
	public boolean connect() {
		return true;
	}

	@Override
	public boolean disconnect() {
		return true;
	}

	@Override
	public List<InstallableApplication> getAppsAvailable(String dir) {
		return null;
	}

	@Override
	public Bundle installApp(String name,String user) {
		return null;
	}

	@Override
	public boolean addStoreUser(String gwUsrName, String mpUsrName, String gwPWD, String mpPWD) {
		return false;
	}

	@Override
	public boolean removeStoreUser(String storeUserName) {
		return false;
	}
}
