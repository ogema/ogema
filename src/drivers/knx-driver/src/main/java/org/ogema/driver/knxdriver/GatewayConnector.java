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

import java.security.PrivilegedActionException;
import org.ogema.core.logging.OgemaLogger;

import tuwien.auto.calimero.link.KNXNetworkLinkIP;

public class GatewayConnector implements Runnable {

	private KNXNetworkLinkIP netLinkIp;

	private ConnectionInfo conInfo;

	private int port;

	private String address;

	private KNXUtils utils = new KNXUtils();

	private OgemaLogger logger;

	private KNXStorage storage = KNXStorage.getInstance();

	public GatewayConnector(KNXNetworkLinkIP netLinkIp, ConnectionInfo conInfo, int port, String address,
			OgemaLogger logger) {

		this.netLinkIp = netLinkIp;
		this.conInfo = conInfo;
		this.port = port;
		this.address = address;
		this.logger = logger;
	}

	@Override
	public void run() {
		while (!Thread.interrupted() && ComSystemKNX.activated) {
			try {
				if (!netLinkIp.isOpen()) {
					try {
						netLinkIp.close();
						netLinkIp = utils.getNetLinkIpPrivileged(conInfo.getIntface(), address, port);
						storage.getKnxNetConnections().put(address + ":" + port, netLinkIp);
						logger.error("reconnect (connection lost) to " + netLinkIp.toString());
					} catch (PrivilegedActionException e1) {
						logger.warn("reconnect failed for {}", netLinkIp.toString(), e1);
					}
				}
                Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.debug("connector '{}' shutting down", netLinkIp.toString());
			}
		}
	}
}
