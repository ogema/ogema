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
