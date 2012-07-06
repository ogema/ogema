/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.impl.administration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;

public class AppStore implements ApplicationSource {

	String name;
	String location;
	boolean islocale;

	public AppStore(String name, String location, boolean isLocaleStore) {
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
		ArrayList<InstallableApplication> apps = new ArrayList<>();
		File f = new File(location);
		String[] files = f.list();
		for (String name : files) {
			File file = new File(f, name);
			if (name.endsWith(".jar") && !file.isDirectory()) {
				InstallableApp app = new InstallableApp(location, name);
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

}
