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
package org.ogema.channels;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ogema.core.application.AppID;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;

class DeviceScanList {

	private final Driver driver;

	/** set of active device scans */
	private final Map<DeviceScanner, DeviceScanner> scanners = Collections.synchronizedMap(new HashMap<DeviceScanner, DeviceScanner>());

	DeviceScanList(Driver driver) {	
		this.driver = driver;
	}
	
	DeviceScanner startDeviceScan(String interfaceId,
			String filter, DeviceScanListener listener, AppID appID) throws IOException, UnsupportedOperationException, NoSuchInterfaceException
	{
		// NOTE: null listener is allowed
		// in this case no listener is called by the wrapper listener
		
		DeviceScanner scanner = new DeviceScanner(interfaceId, filter, appID, this, listener);
		
		// replaces Java8 method Map#putIfAbsent
		synchronized (scanners) {
			if (scanners.containsKey(scanner))
				throw new IOException("same scan already in progress: " + this.driver.getId() + "," + interfaceId + "," + filter + "," + listener);
			scanners.put(scanner, scanner);
		}
//		if (scanners.putIfAbsent(scanner, scanner) != null) {
//			throw new IOException("same scan already in progress: " + this.driver.getId() + "," + interfaceId + "," + filter + "," + listener);
//		}
		
		try {
			driver.getDriver().startDeviceScan(interfaceId, filter, scanner);
		} catch (Throwable t) {
			// make sure the scanner is removed from the list of active scans
			scanners.remove(scanner);
			throw t;
		}
		
		return scanner;
	}
	
	boolean abortDeviceScan(String interfaceId, String filter, AppID appId)
	{
		boolean success = false;

		// create temporary key object
		DeviceScanner tmp = new DeviceScanner(interfaceId, filter, appId, this, null);
		DeviceScanner current;
		
		current = scanners.remove(tmp);
		
		if (current != null) {
			current.abort();
			driver.getDriver().abortDeviceScan(interfaceId, filter);
			success = true;
		}
		
		return success;
	}

	void abortDeviceScanForAppID(AppID appID) {
		synchronized(scanners) {
			Iterator<DeviceScanner> iter = scanners.values().iterator();
			
			while(iter.hasNext()) {
				DeviceScanner current = iter.next();
				if (current.getAppID().equals(appID))
				{
					current.abort();
					iter.remove();
					driver.getDriver().abortDeviceScan(current.getInterfaceId(), current.getFilter());
				}
			}
		}
	}
	
	
	void deviceScanFinished(DeviceScanner scanner) {
			scanners.remove(scanner);
	}

	void close() {
		// cancel and remove all pending scans
		synchronized (scanners) {
			for (DeviceScanner scanner : scanners.values()) {
				scanner.abort();
			}
			scanners.clear();
		}
	}
}

