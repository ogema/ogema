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
package org.ogema.driver.knxdriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tuwien.auto.calimero.link.KNXNetworkLinkIP;

public class KNXStorage {

	static KNXStorage instance = new KNXStorage();

	private Map<String, String> allInterface = new HashMap<String, String>();

	private Map<String, KNXNetworkLinkIP> knxNetConnections = new ConcurrentHashMap<>();

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

	public Map<String, KNXNetworkLinkIP> getKnxNetConnections() {
		return knxNetConnections;
	}

	public List<ConnectionInfo> getDeviceConnections() {
		return deviceConnections;
	}

	public List<String> getNotAllowedDevices() {
		return notAllowedDevices;
	}

	
	
	
	
	
	
	
}
