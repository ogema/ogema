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
package org.ogema.impl.apploader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;
import org.ogema.core.installationmanager.InstallationManagement;
import org.osgi.framework.Bundle;

@Component(specVersion = "1.2", immediate = true)
@Service(ApplicationSource.class)
public class AppStore implements ApplicationSource {

	static final String LOCAL_APPSTORE_NAME = "localAppDirectory";
	static final String LOCAL_APPSTORE_LOCATION = "./appstore/";

	String name;
	String location;

	@Reference
	InstallationManagement instMngr;

	public AppStore() {
		this.name = LOCAL_APPSTORE_NAME;
		this.location = LOCAL_APPSTORE_LOCATION;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	@Deprecated
	public List<InstallableApplication> getAppsAvailable() {
		ArrayList<InstallableApplication> apps = new ArrayList<>();
		File f = new File(location);
		String[] files = f.list();
		for (String name : files) {
			File file = new File(f, name);
			if (name.endsWith(".jar") && !file.isDirectory()) {
				InstallableApplication app = instMngr.createInstallableApp(location, name);
				apps.add(app);
			}
		}
		return apps;
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
	public Bundle installApp(String name, String user) {
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
