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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class ChannelManagerImplTest {

	private ChannelManagerImpl channelManager;

	private ApplicationRegistryImpl appreg;
	
	@Before
	public void setup() {
		
		channelManager = new ChannelManagerImpl();
		
		appreg = new ApplicationRegistryImpl();
		appreg.appId = new AppIdImpl("1");
		
		channelManager.appreg = appreg;
		channelManager.permMan = new PermissionManagerImpl();
	}
	
	private DeviceLocator createDeviceLocator(String driverName, String interfaceName, String deviceAddress, String parameters) {
		return new DeviceLocator(driverName, interfaceName, deviceAddress, parameters);
	}
	
	private ChannelLocator createChannelLocator(String channelAddress, DeviceLocator deviceLocator) {
		return new ChannelLocator(channelAddress, deviceLocator);
	}
	
	@Test
	public void testDriverAdd() {
		
		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		ChannelDriver driver2 = new ChannelDriverImpl("test2", "second driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDriver(driver2);
		
		List<String> drivers = channelManager.getDriverIds();
		
		assertTrue(driverIsListed(drivers, "test2"));
		assertTrue(driverIsListed(drivers, "test1"));

		assertEquals(2, drivers.size());
	}
	
	@Test
	public void testDriverRemove() {
		
		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		ChannelDriver driver2 = new ChannelDriverImpl("test2", "second driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDriver(driver2);
		
		List<String> drivers = channelManager.getDriverIds();
		
		assertTrue(driverIsListed(drivers, "test2"));
		assertTrue(driverIsListed(drivers, "test1"));

		assertEquals(2, drivers.size());		
		
		channelManager.removeDriver(driver2);
		
		drivers = channelManager.getDriverIds();
		
		assertFalse(driverIsListed(drivers, "test2"));
		assertTrue(driverIsListed(drivers, "test1"));

		assertEquals(1, drivers.size());		
	}
	
	@Test
	public void testAddDriverVoid() {
		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDriver(null);
		
		List<String> drivers = channelManager.getDriverIds();
		
		assertTrue(driverIsListed(drivers, "test1"));

		assertEquals(1, drivers.size());		
	}

	@Test
	public void testRemoveDriverVoid() {

		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		
		channelManager.addDriver(driver1);
		channelManager.removeDriver(null);
		
		List<String> drivers = channelManager.getDriverIds();
		
		assertTrue(driverIsListed(drivers, "test1"));

		assertEquals(1, drivers.size());
	}

	@Test
	public void testAddDriverTwice() {
		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		ChannelDriver driver2 = new ChannelDriverImpl("test1", "second driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDriver(driver2);
		
		List<String> drivers = channelManager.getDriverIds();
		
		assertTrue(driverIsListed(drivers, "test1"));
		assertEquals(1, drivers.size());
		
		assertTrue(channelManager.getDriverDescription(driver1.getDriverId()).equals(driver1.getDescription()));
	}

	@Test
	public void testRemoveDriverTwice() {
		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		ChannelDriver driver2 = new ChannelDriverImpl("test2", "second driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDriver(driver2);
		
		channelManager.removeDriver(driver2);
		channelManager.removeDriver(driver2);
		
		List<String> drivers = channelManager.getDriverIds();
		
		assertTrue(driverIsListed(drivers, "test1"));
		assertEquals(1, drivers.size());
	}

	@Test
	public void testDriverDescription() {

		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		ChannelDriver driver2 = new ChannelDriverImpl("test2", "second driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDriver(driver2);
		
		assertTrue(channelManager.getDriverDescription(driver1.getDriverId()).equals(driver1.getDescription()));
		assertTrue(channelManager.getDriverDescription(driver2.getDriverId()).equals(driver2.getDescription()));
	}
	
	// @Test(expected = NoSuchDriverException.class)
	@Test
	public void testUnkownDriverDescription() {

		ChannelDriver driver1 = new ChannelDriverImpl("test1", "first driver");
		ChannelDriver driver2 = new ChannelDriverImpl("test2", "second driver");
		
		channelManager.addDriver(driver1);
		
		assertTrue(channelManager.getDriverDescription(driver1.getDriverId()).equals(driver1.getDescription()));
		assertNull(channelManager.getDriverDescription(driver2.getDriverId()));		
	}

	@Test
	public void testRegisterDeviceListener() throws ChannelAccessException {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		
		channelManager.addDriver(driver1);

		assertTrue(driver1.addDeviceListenerCount == 0);
		
		channelManager.addDeviceListener(driver1.getDriverId(), new DeviceListenerImpl());
		
		assertTrue(driver1.addDeviceListenerCount == 1);
		
		
	}

	@Test(expected = NullPointerException.class)
	public void testRegisterDeviceListenerNullId() throws ChannelAccessException {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		
		channelManager.addDriver(driver1);

		assertTrue(driver1.addDeviceListenerCount == 0);
		
		channelManager.addDeviceListener(null, new DeviceListenerImpl());
		
		assertTrue(driver1.addDeviceListenerCount == 1);
	}
	
	@Test(expected = NullPointerException.class)
	public void testRegisterDeviceNullListener() throws ChannelAccessException {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDeviceListener(driver1.getDriverId(), null);
	}

	@Test(expected = ChannelAccessException.class)
	public void testRegisterDeviceListenerInvalidId() throws ChannelAccessException {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		
		channelManager.addDriver(driver1);
		channelManager.addDeviceListener("foo", new DeviceListenerImpl());
	}

	@Test
	public void testUnRegisterDeviceListener() throws ChannelAccessException {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		DeviceListener listener =  new DeviceListenerImpl();
			
		channelManager.addDriver(driver1);

		assertTrue(driver1.addDeviceListenerCount == 0);
		assertTrue(driver1.removeDeviceListenerCount == 0);
		
		channelManager.addDeviceListener(driver1.getDriverId(), listener);
		
		assertTrue(driver1.addDeviceListenerCount == 1);
		assertTrue(driver1.removeDeviceListenerCount == 0);
		
		channelManager.removeDeviceListener(driver1.getDriverId(), listener);
		
		assertTrue(driver1.addDeviceListenerCount == 1);
		assertTrue(driver1.removeDeviceListenerCount == 1);
		assertTrue(driver1.deviceListeners.isEmpty());
		
		
	}
	
	@Test
	public void testUnRegisterDeviceListenerNullId() throws Exception {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		channelManager.addDriver(driver1);
		
		channelManager.removeDeviceListener(null, new DeviceListenerImpl());
	}

	@Test
	public void testUnRegisterDeviceListenerNullListener() throws Exception {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		channelManager.addDriver(driver1);
		
		channelManager.removeDeviceListener(driver1.getDriverId(), null);
	}

	
	@Test
	public void testUnRegisterDeviceListenerInvalidListener() throws Exception {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		channelManager.addDriver(driver1);
		
		DeviceListener listener = new DeviceListenerImpl();
		
		channelManager.removeDeviceListener(driver1.getDriverId(), listener);
	}
	
	@Test
	public void testUnRegisterDeviceListenerInvalidId() throws Exception {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		channelManager.addDriver(driver1);
		
		DeviceListener listener = new DeviceListenerImpl();
		
		channelManager.addDeviceListener(driver1.getDriverId(), listener);
		channelManager.removeDeviceListener("foo", listener);
	}

	@Test
	public void testDeviceListenerCallback() throws ChannelAccessException {
		ChannelDriverImpl driver1 = new ChannelDriverImpl("test1", "first driver");
		DeviceLocator locator = createDeviceLocator(driver1.getDriverId(), "", "", null);
		DeviceListenerImpl listener = new DeviceListenerImpl();
		
		channelManager.addDriver(driver1);

		channelManager.addDeviceListener(driver1.getDriverId(), listener);
		
		driver1.callDeviceListenersFound(locator);
		driver1.callDeviceListenersRemoved(locator);
		
		assertTrue(listener.addedCount == 1);
		assertTrue(listener.removedCount == 1);
	}

//	@Test
//	public void testGetDeviceLocator() throws Exception {
//		
//		channelManager.getDeviceLocator("foo", "", "", null);
//	}
	
	@Test
	public void testGetChannelLocator() throws Exception {
		
		DeviceLocator deviceLocator = createDeviceLocator("foo", "", "", null);
		ChannelLocator channelLocator = createChannelLocator("", deviceLocator);
		
		assertNotNull(channelLocator);
	}
	
	private boolean driverIsListed(List<String> drivers, String wantedDriver) {

		if (drivers == null || wantedDriver == null) {
			return false;
		}

		for (String driver : drivers) {

			if (driver.equals(wantedDriver)) {
				return true;
			}
		}

		return false;
	}
	
	private class DeviceListenerImpl implements DeviceListener {

		int addedCount;
		int removedCount;
		
		@Override
		public void deviceAdded(DeviceLocator device) {
			addedCount++;
			
		}

		@Override
		public void deviceRemoved(DeviceLocator device) {
			removedCount++;
			
		}
		
	}
}
