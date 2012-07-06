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
package org.ogema.driver.knxdriver.gui;

import java.util.ArrayList;
import java.util.List;

import org.ogema.driver.knxdriver.ConnectionInfo;

public class KnxDAO {

	private List<String> availableInterfaces = new ArrayList<String>();
	private List<String> availableTypes = new ArrayList<String>();
	private List<ConnectionInfoDAO> connectionInfos = new ArrayList<ConnectionInfoDAO>();

	public List<String> getAvailableInterfaces() {
		return availableInterfaces;
	}

	public void setAvailableInterfaces(List<String> availableInterfaces) {
		this.availableInterfaces = availableInterfaces;
	}

	public List<String> getAvailableTypes() {
		return availableTypes;
	}

	public void setAvailableTypes(List<String> availableTypes) {
		this.availableTypes = availableTypes;
	}

	public List<ConnectionInfoDAO> getConnectionInfos() {
		return connectionInfos;
	}

	public void setConnectionInfos(List<ConnectionInfo> connectionInfos) {
		for (ConnectionInfo info : connectionInfos) {
			this.connectionInfos.add(new ConnectionInfoDAO(info));
		}
	}

	/**
	 * If {@link ConnectionInfo} fields gets refactored our knx-util.js won't be refactored
	 * so that we wrap those info in this DAO. If fields of this DAO are renamed / refactored
	 * pls adjust knx-util.js appropriately... 
	 */
	@SuppressWarnings("unused")
	private class ConnectionInfoDAO {
		private String interfaceName;
		private String groupAddress;
		private String physicalAddress;
		private String knxRouter;
		private String name;
		private String type;
		private long id;

		public ConnectionInfoDAO(ConnectionInfo info) {
			this.id = info.getId();
			this.interfaceName = info.getIntface();
			this.groupAddress = info.getGroupAddress();
			this.physicalAddress = info.getPhyaddress();
			this.knxRouter = info.getKnxRouter();
			this.name = info.getName();
			this.type = info.getType();
		}

		public String getInterfaceName() {
			return interfaceName;
		}

		public String getGroupAddress() {
			return groupAddress;
		}

		public String getPhysicalAddress() {
			return physicalAddress;
		}

		public String getKnxRouter() {
			return knxRouter;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public long getId() {
			return id;
		}
	}
}
