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
