/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package org.ogema.drivers.homematic.xmlrpc.ll;

public enum HomematicType {
	
	Wired(2000), BidCos(2001), Ip(2010);

	private final int defaultPort;
	
	private HomematicType(int port) {
		this.defaultPort = port;
	}
	
	public int getDefaultPort() {
		return defaultPort;
	}
	
	public int getTlsPort() {
		return Integer.parseInt("4" + getDefaultPort());
	}
	
	public static HomematicType forPort(int port) {
		for (HomematicType t : values()) {
			if (t.getDefaultPort() == port)
				return t;
		}
		for (HomematicType t : values()) {
			if (t.getTlsPort() == port)
				return t;
		}
		return null;
	}
	
}
