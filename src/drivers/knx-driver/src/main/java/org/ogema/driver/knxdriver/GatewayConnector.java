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
package org.ogema.driver.knxdriver;

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
		// TODO Auto-generated method stub

		while (ComSystemKNX.activated) {

			try {
				Thread.sleep(1000);
				if (!netLinkIp.isOpen()) {

					try {

						netLinkIp.close();

						netLinkIp = utils.getNetLinkIpPrivileged(conInfo, address, port);

						storage.getKnxNetConnections().put(address + ":" + port, netLinkIp);

						logger.error("reconnect (connection lost) to " + netLinkIp.toString());

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}
}
