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
package org.ogema.impl.security.gui;

import java.util.List;

import org.ogema.core.installationmanager.ApplicationSource;
import org.ogema.core.installationmanager.InstallableApplication;

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

}
