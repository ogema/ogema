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
package org.ogema.driver.homematic.usbconnection;

import org.ogema.driver.homematic.Constants;
import org.ogema.driver.homematic.tools.Converter;

public class KeepAlive implements Runnable {

	private IUsbConnection connection;
	private volatile boolean running;
	private boolean initiated = false;
	private volatile String address = null;

	public KeepAlive(IUsbConnection context) {
		this.connection = context;
		running = true;
	}

	public void stop() {
		running = false;
	}

	public void setConnectionAddress(String address) {
		this.address = address;
	}

	private void init() {
		connection.sendFrame(Constants.M_K);
		while (this.address == null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		connection.sendFrame(Converter.hexStringToByteArray("41" + address));
		connection.sendFrame(Constants.M_C);
		connection.sendFrame(Constants.M_Y1);
		connection.sendFrame(Constants.M_Y2);
		connection.sendFrame(Constants.M_Y3);
		// TODO: sendFrame(M_T);
		initiated = true;
	}

	@Override
	public void run() {
		while (running) {
			if (!initiated) {
				init();
			}
			try {
				Thread.sleep(Constants.KEEPALIVETIME);
				connection.sendFrame(Constants.M_K);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
