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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.DeviceScanListener;

public class DeviceScanTest {

	private ChannelManagerImpl channelManager;

	private ChannelDriverImpl driver;

	@Before
	public void setup() {
		
		channelManager = new ChannelManagerImpl();
		
		channelManager.appreg = new ApplicationRegistryImpl();
		channelManager.permMan = new PermissionManagerImpl();
		
		driver = new ChannelDriverImpl("driver1", "firstDriver");
		channelManager.addDriver(driver);
	}
	
	private DeviceLocator createDeviceLocator(String driverName, String interfaceName, String deviceAddress, String parameters) {
		return new DeviceLocator(driverName, interfaceName, deviceAddress, parameters);
	}
	
	@Test
	public void testDiscoverDevicesSync() throws Exception {
		
		channelManager.discoverDevices(driver.getDriverId(), "", "");
	}
	
	@Test
	public void testDiscoverDevicesSyncWithResults() throws Exception {
		
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "1", null));
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "2", null));
		
		List<DeviceLocator> devices = channelManager.discoverDevices(driver.getDriverId(), "", "");
		
		for(DeviceLocator current : devices) {
			assertTrue(driver.devices.contains(current));
		}
	}
	
	@Test
	public void testDiscoverDevicesAsync() throws Exception {
		DeviceScanListenerImpl listener = new DeviceScanListenerImpl();

		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "1", null));
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "2", null));
		
		channelManager.discoverDevices(driver.getDriverId(), "", "", listener);
		
		assertTrue(listener.finished);
		assertTrue(listener.success);
		
		for(DeviceLocator current : listener.devices) {
			assertTrue(driver.devices.contains(current));
		}
	}
	
	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesSyncException() throws Exception {
		
		driver.uoe = new UnsupportedOperationException();
		
		channelManager.discoverDevices(driver.getDriverId(), "", "");
		
	}
	
	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesSyncExceptionAtEnd() throws Exception {
		
		driver.ioe = new IOException();
		
		channelManager.discoverDevices(driver.getDriverId(), "", "");
		
	}
	
	@Test
	public void testDiscoverDevicesAbort() throws Exception {
		DeviceScanListenerImpl listener = new DeviceScanListenerImpl();
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				boolean success = channelManager.abortDiscoverDevices(driver.getDriverId(), "", "");
				
				assertTrue(success);
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
		
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "1", null));
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "2", null));
		
		driver.wait = true;
		
		channelManager.discoverDevices(driver.getDriverId(), "", "", listener);
		
		assertTrue(listener.finished == false);
		assertTrue(driver.abortDeviceScanCount == 1);
		thread.join();
	}
	
	@Test
	public void testAbortDiscoverDevicesSyncExceptionAtEnd() throws Exception {
		
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				boolean success = channelManager.abortDiscoverDevices(driver.getDriverId(), "", "");
				assertTrue(success);
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.start();		
		
		driver.ioe = new IOException();
		driver.wait = true;

		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "1", null));
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "2", null));
		
		List<DeviceLocator> devices = channelManager.discoverDevices(driver.getDriverId(), "", "");
		
		for(DeviceLocator current : devices) {
			assertTrue(driver.devices.contains(current));
		}
		assertTrue(devices.size() == 2);
		assertTrue(driver.abortDeviceScanCount == 1);
		thread.join();
	}
	
	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesDouble() throws Exception {
		DeviceScanListenerImpl listener = new DeviceScanListenerImpl();
		driver.async = true;
		
		channelManager.discoverDevices(driver.getDriverId(), "", "", listener);
		channelManager.discoverDevices(driver.getDriverId(), "", "", listener);
	}
	
	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesAsyncDriverNull() throws Exception {
		DeviceScanListenerImpl listener = new DeviceScanListenerImpl();

		channelManager.discoverDevices(null, "", "", listener);
	}
	
	@Test(expected = ChannelAccessException.class)
	public void testDiscoverDevicesAsyncDriverUnknown() throws Exception {
		DeviceScanListenerImpl listener = new DeviceScanListenerImpl();
		channelManager.discoverDevices("foo", "", "", listener);
	}
	
	@Test
	public void testDiscoverDevicesAsyncListenerNull() throws Exception {
		
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "1", null));
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "2", null));
		
		channelManager.discoverDevices(driver.getDriverId(), "", "", null);
		
		assertTrue(driver.startDeviceScanCount == 1);
	}
	
	@Test
	public void testDiscoverDevicesFreeAtEnd() throws Exception {
		// Test if the device scan is removed from the 
		// internal data structures at the end of device scanning
		DeviceScanListenerImpl listener = new DeviceScanListenerImpl();
		
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "1", null));
		driver.addDevice(createDeviceLocator(driver.getDriverId(), "a", "2", null));
		
		// should be finished at end of call
		channelManager.discoverDevices(driver.getDriverId(), "", "", listener);
		
		assertEquals(1, driver.startDeviceScanCount);
		assertTrue(listener.finished);
		
		listener.finished = false;
		
		// try again
		channelManager.discoverDevices(driver.getDriverId(), "", "", listener);
		
		assertEquals(2, driver.startDeviceScanCount);
		assertTrue(listener.finished);
	}

	private class DeviceScanListenerImpl implements DeviceScanListener {

		boolean finished;
		boolean success;

		List<DeviceLocator> devices = new ArrayList<DeviceLocator>();
		
		@Override
		public void deviceFound(DeviceLocator device) {
			devices.add(device);
		}

		@Override
		public void finished(boolean success, Exception e) {
			finished = true;
			this.success = success;
		}

		@Override
		public void progress(float ratio) {
			// ignore
		}
		
	}
}
