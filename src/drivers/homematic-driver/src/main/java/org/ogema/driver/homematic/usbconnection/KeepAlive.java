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
package org.ogema.driver.homematic.usbconnection;

import org.ogema.driver.homematic.Activator;
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
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
//				e1.printStackTrace();
				if (!Activator.bundleIsRunning)
					return;
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
		while (running && Activator.bundleIsRunning) {
			if (!initiated) {
				init();
			}
			try {
				Thread.sleep(Constants.KEEPALIVETIME);
				connection.sendFrame(Constants.M_K);
			} catch (InterruptedException e) {
			}
		}
	}

}
