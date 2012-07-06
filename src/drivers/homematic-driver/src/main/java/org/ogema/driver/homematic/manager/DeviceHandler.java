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
package org.ogema.driver.homematic.manager;

import java.util.Map;

import org.slf4j.Logger;

/**
 * This class is responsible for device initiation and handling "error" states of the devices
 * 
 * @author baerthbn
 * 
 */
public class DeviceHandler implements Runnable {
	private volatile boolean running;
	private Thread timerThread;
	private LocalDevice localDevice;
	protected volatile Object deviceHandlerLock = new Object();
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("homematic-driver");

	public DeviceHandler(LocalDevice localDevice) {
		running = true;
		this.localDevice = localDevice;

		/*
		 * This thread notifies the endpoint handler after a certain amount of time.
		 */
		timerThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(30000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					synchronized (deviceHandlerLock) {
						deviceHandlerLock.notify();
					}
				}
			}
		});
		timerThread.start();

	}

	public Object getDeviceHandlerLock() {
		return deviceHandlerLock;
	}

	/**
	 * 
	 * @param running
	 *            Let's the loop in run() finish and then exit.
	 */
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		while (running) {
			synchronized (deviceHandlerLock) {
				try {
					deviceHandlerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for (Map.Entry<String, RemoteDevice> device : localDevice.getDevices().entrySet()) {
				RemoteDevice remoteDevice = device.getValue();
				switch (remoteDevice.getDeviceState()) {
				case UNKNOWN:
					// TODO: NO REACTION ?! BATTERY ? OUT OF RANGE ?
					logger.warn("Device with address " + remoteDevice.getAddress() + " is "
							+ remoteDevice.getDeviceState());
					break;
				case ACTIVE:
					// TODO: Nothing to do at the moment
					break;
				case MSG_PENDING:
					logger.warn("Device with address " + remoteDevice.getAddress() + " is "
							+ remoteDevice.getDeviceState());
					// TODO: handle the situation properly
					break;
				}
				switch (remoteDevice.getInitState()) {
				case PAIRED:
					// Perfect, nothing to do
					break;
				case UNPAIRED:
					remoteDevice.init();
					// localDevice.fileStorage.saveDeviceConfig();
					// TODO if the driver starts with many device entries (>8)
					// in the file a NPE is thrown here

					break;
				}
			}
		}
	}

}
