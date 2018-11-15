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

