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
package org.ogema.driver.iec61850;

import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ServerModel;

public final class Iec61850Connection {

	private final ClientAssociation clientAssociation;
	private final ServerModel serverModel;

	public Iec61850Connection(ClientAssociation clientAssociation, ServerModel serverModel) {
		this.clientAssociation = clientAssociation;
		this.serverModel = serverModel;
	}

	public ClientAssociation getClientAssociation() {
		return clientAssociation;
	}

	public ServerModel getServerModel() {
		return serverModel;
	}

}
