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
package org.ogema.driver.knxdriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tuwien.auto.calimero.link.KNXNetworkLinkIP;

public class KNXStorage {

	static KNXStorage instance = new KNXStorage();

	private Map<String, String> allInterface = new HashMap<String, String>();

	private HashMap<String, KNXNetworkLinkIP> knxNetConnections = new HashMap<>();

	private List<ConnectionInfo> deviceConnections = new ArrayList<>();

	private List<String> notAllowedDevices = new ArrayList<>();

	private KNXStorage() {
	}

	public static KNXStorage getInstance() {
		return instance;
	}

	public Map<String, String> getAllInterface() {
		return allInterface;
	}

	public HashMap<String, KNXNetworkLinkIP> getKnxNetConnections() {
		return knxNetConnections;
	}

	public List<ConnectionInfo> getDeviceConnections() {
		return deviceConnections;
	}

	public List<String> getNotAllowedDevices() {
		return notAllowedDevices;
	}

	
	
	
	
	
	
	
}
