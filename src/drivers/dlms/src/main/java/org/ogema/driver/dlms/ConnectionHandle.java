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
package org.ogema.driver.dlms;

import org.openmuc.jdlms.client.IClientConnection;

public class ConnectionHandle {

	private final IClientConnection connection;
	private final SettingsHelper settings;

	public ConnectionHandle(IClientConnection connection, SettingsHelper settings) {
		this.connection = connection;
		this.settings = settings;
	}

	public IClientConnection getConnection() {
		return connection;
	}

	public SettingsHelper getSettings() {
		return settings;
	}
}
