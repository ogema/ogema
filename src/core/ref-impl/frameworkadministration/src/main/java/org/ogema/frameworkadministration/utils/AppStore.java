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
package org.ogema.frameworkadministration.utils;

import java.util.List;

import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;

public class AppStore implements ApplicationSource {

	private String name;
	private String location;
	private boolean islocale;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAddress() {
		return location;
	}

	public boolean isIslocale() {
		return islocale;
	}

	public void setIslocale(boolean islocale) {
		this.islocale = islocale;
	}

	@Override
	public boolean connect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return false;
	}

}
