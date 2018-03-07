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

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ogema.core.application.AppID;
import org.ogema.core.application.Application;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.osgi.framework.Bundle;

public class ChannelTest {

	private ChannelManagerImpl channelManager;

	private ChannelDriverImpl driver;

	private ApplicationRegistryImpl appReg;
	
	@Rule
	public TestRule watcher = new TestWatcher() {
		
		@Override
		protected void starting(Description description) {
	      System.out.println("Starting test: " + description.getMethodName());
	   }
	};
	
	@Before
	public void setup() {
		
		channelManager = new ChannelManagerImpl();
		
		appReg = new ApplicationRegistryImpl();
		appReg.appId = new AppIdImpl("foo");
		
		channelManager.appreg = appReg;
		channelManager.permMan = new PermissionManagerImpl();
		
		driver = new ChannelDriverImpl("driver1", "firstDriver");
		channelManager.addDriver(driver);
	}
	
	private DeviceLocator createDeviceLocator(String driverName, String interfaceName, String deviceAddress, String parameters) {
		return new DeviceLocator(driverName, interfaceName, deviceAddress, parameters);
	}
	
	private ChannelLocator createChannelLocator(String channelAddress, DeviceLocator deviceLocator) {
		return new ChannelLocator(channelAddress, deviceLocator);
	}
	
	@Test
	public void testAddChannelOndemand() throws Exception {
		
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		
		assertTrue(driver.channelAddedCount == 1);
	}
	
	@Test
	public void testReadChannelOndemand() throws Exception {

		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);		

		for (int i = 0; i < 10; i++) {
			SampledValue value = channelManager.getChannelValue(configuration);
			
			assertTrue(driver.readChannelsCount == i + 1);
			assertTrue(value.getValue().getIntegerValue() == i);
		}
		
		assertTrue(driver.channels.get(channelLocator).get() == 10);
	}
	
	@Test
	public void testWriteChannel() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_OUTPUT, ChannelConfiguration.NO_READ_NO_LISTEN);		

		for (int i = 0; i < 10; i++) {
			channelManager.setChannelValue(configuration, new IntegerValue(i));
			
			assertTrue(driver.writeChannelsCount == i + 1);
			assertTrue(driver.channels.get(channelLocator).get() == i);
		}
	}
	
	@Ignore("Timing dependent, fails sporadically")
	@Test
	public void testReadChannelPolled() throws Exception {
		
		final int COUNT = 50;
		final int PERIOD = 5;
		int count;
		
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		long ticks = System.currentTimeMillis();
		
		synchronized(this) {
			if (ticks % PERIOD != 0)
				wait((ticks % PERIOD));
		}
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, PERIOD);		

		synchronized(this) {
			wait(COUNT * PERIOD + PERIOD / 2);
		}
		
		SampledValue value = channelManager.getChannelValue(configuration);
		
		channelManager.deleteChannel(configuration);
		
		count = value.getValue().getIntegerValue();
		
		assertTrue(count >= COUNT);
		assertTrue(count <= (COUNT + 1));
		
		assertEquals(count, driver.channels.get(channelLocator).get() - 1);
		assertEquals(count, driver.readChannelsCount - 1);
	}
	
	@Test
	public void testReadChannelListen() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.LISTEN_FOR_UPDATE);		
		
		assertEquals(1, driver.listenChannelsCount);
		assertEquals(1, driver.listenChannels.size());
		assertEquals(channelLocator, driver.listenChannels.get(0).getChannelLocator());
		
		List<SampledValueContainer> list = new ArrayList<SampledValueContainer>();
		SampledValueContainer container = new SampledValueContainer(channelLocator);
		container.setSampledValue(new SampledValue(new IntegerValue(1), System.currentTimeMillis(), Quality.GOOD));
		list.add(container);
		
		driver.channelUpdatelistener.channelsUpdated(list);
		SampledValue sampledValue = channelManager.getChannelValue(configuration);
		assertEquals(1, sampledValue.getValue().getIntegerValue());
		
		container.setSampledValue(new SampledValue(new IntegerValue(2), System.currentTimeMillis(), Quality.GOOD));
		list.clear();
		list.add(container);
		
		driver.channelUpdatelistener.channelsUpdated(list);
		sampledValue = channelManager.getChannelValue(configuration);
		assertEquals(2, sampledValue.getValue().getIntegerValue());

	}
	
	@Test
	public void testChannelUpdateListener() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, 10);
		
		List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
		list.add(configuration);
		ChannelEventListenerImpl listener = new ChannelEventListenerImpl();
		listener.type = EventType.VALUE_CHANGED;
		listener.channelLocator = channelLocator;
		
		channelManager.registerUpdateListener(list, listener);
		
		synchronized(this) {
			wait(10 * 10 + 5);
		}
	}
	
	@Test
	public void testChannelChangedListener() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.LISTEN_FOR_UPDATE);		
		
		assertEquals(1, driver.listenChannelsCount);
		assertEquals(1, driver.listenChannels.size());
		assertEquals(channelLocator, driver.listenChannels.get(0).getChannelLocator());

		ChannelEventListenerImpl listener = new ChannelEventListenerImpl();
		listener.type = EventType.VALUE_CHANGED;
		listener.channelLocator = channelLocator;

		{
			List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
			list.add(configuration);
			channelManager.registerChangedListener(list, listener);
		}
		
		for (int i = 0; i < 10; i++) {
			List<SampledValueContainer> list = new ArrayList<SampledValueContainer>();
			SampledValueContainer container = new SampledValueContainer(channelLocator);
			container.setSampledValue(new SampledValue(new IntegerValue(i), System.currentTimeMillis(), Quality.GOOD));
			list.clear();
			list.add(container);
			
			driver.channelUpdatelistener.channelsUpdated(list);
			driver.channelUpdatelistener.channelsUpdated(list);
		}
		
		assertEquals(10, listener.integer.get());
	}
	
	@Test
	public void testRemoveChannelOndemand() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		
		assertEquals(1, driver.channelAddedCount);
		assertEquals(1, driver.addedChannels.size());
		assertEquals(channelLocator, driver.addedChannels.get(0));
		
		channelManager.deleteChannel(configuration);
		
		assertEquals(1, driver.channelRemovedCount);
		assertEquals(0, driver.addedChannels.size());
	}
	
	@Test
	public void testRemoveChannelPolled() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, 10);
		
		assertEquals(1, driver.channelAddedCount);
		assertEquals(1, driver.addedChannels.size());
		assertEquals(channelLocator, driver.addedChannels.get(0));
		
		channelManager.deleteChannel(configuration);
		
		assertEquals(1, driver.channelRemovedCount);
		assertEquals(0, driver.addedChannels.size());
	}	
	
	@Test
	public void testRemoveChannelListen() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		
		assertEquals(1, driver.channelAddedCount);
		assertEquals(1, driver.addedChannels.size());
		assertEquals(channelLocator, driver.addedChannels.get(0));

		assertEquals(1, driver.listenChannels.size());
		assertEquals(channelLocator, driver.listenChannels.get(0).getChannelLocator());
		
		channelManager.deleteChannel(configuration);
		
		assertEquals(1, driver.channelRemovedCount);
		assertEquals(0, driver.addedChannels.size());
		assertEquals(0, driver.listenChannels.size());
	}
	
	@Test
	public void testGetAllConfiguredChannels() throws Exception {

		ChannelDriverImpl driver2 = new ChannelDriverImpl("baz", "second driver"); 
		channelManager.addDriver(driver2);
		
		ChannelLocator[] chs;
		
		chs = addChannelsToDriver(driver, 10);
		
		for (int i = 0; i < chs.length; i++) {
			channelManager.addChannel(chs[i], Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		}
		
		chs = addChannelsToDriver(driver2, 10);
		
		for (int i = 0; i < chs.length; i++) {
			channelManager.addChannel(chs[i], Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		}
		
		List<ChannelLocator> result = channelManager.getAllConfiguredChannels();
		
		assertEquals(20, result.size());
	}

	private ChannelLocator[] addChannelsToDriver(ChannelDriverImpl drv, int count) {
		
		ChannelLocator[] result = new ChannelLocator[count];
		DeviceLocator deviceLocator = createDeviceLocator(drv.getDriverId(), "", "", null);
		
		for(int i = 0; i < count; i++) {
			ChannelLocator channelLocator = createChannelLocator(Integer.toString(i), deviceLocator);
			driver.channels.put(channelLocator, new AtomicInteger());
			result[i] = channelLocator;
		}
		
		return result;
	}
	
	@Test
	public void testReadChannelMultiple() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		ChannelLocator channelLocator2 = createChannelLocator("2", deviceLocator);
		ChannelLocator channelLocator3 = createChannelLocator("3", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		driver.channels.put(channelLocator2, new AtomicInteger());
		driver.channels.put(channelLocator3, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		ChannelConfiguration configuration2 = channelManager.addChannel(channelLocator2, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		ChannelConfiguration configuration3 = channelManager.addChannel(channelLocator3, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		
		List<ChannelConfiguration> configurations = new ArrayList<ChannelConfiguration>();
		
		configurations.add(configuration);
		configurations.add(configuration2);
		configurations.add(configuration3);
		
		List<SampledValue> values = channelManager.getMultipleChannelValues(configurations);
		
		assertEquals(3, driver.readChannelsCount);
		
		assertEquals(1, driver.channels.get(channelLocator).get());
		assertEquals(1, driver.channels.get(channelLocator2).get());
		assertEquals(1, driver.channels.get(channelLocator3).get());
		
		assertEquals(3, values.size());
		assertEquals(0, values.get(0).getValue().getIntegerValue());
		assertEquals(0, values.get(1).getValue().getIntegerValue());
		assertEquals(0, values.get(2).getValue().getIntegerValue());
	}
	
	@Test
	public void testWriteChannelMultiple() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		ChannelLocator channelLocator2 = createChannelLocator("2", deviceLocator);
		ChannelLocator channelLocator3 = createChannelLocator("3", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		driver.channels.put(channelLocator2, new AtomicInteger());
		driver.channels.put(channelLocator3, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_OUTPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		ChannelConfiguration configuration2 = channelManager.addChannel(channelLocator2, Direction.DIRECTION_OUTPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		ChannelConfiguration configuration3 = channelManager.addChannel(channelLocator3, Direction.DIRECTION_OUTPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		
		List<ChannelConfiguration> configurations = new ArrayList<ChannelConfiguration>();
		
		configurations.add(configuration);
		configurations.add(configuration2);
		configurations.add(configuration3);
		
		List<Value> values = new ArrayList<Value>();
		
		values.add(new IntegerValue(42));
		values.add(new IntegerValue(43));
		values.add(new IntegerValue(44));
		
		channelManager.setMultipleChannelValues(configurations, values);
		
		assertEquals(3, driver.writeChannelsCount);
		
		assertEquals(42, driver.channels.get(channelLocator).get());
		assertEquals(43, driver.channels.get(channelLocator2).get());
		assertEquals(44, driver.channels.get(channelLocator3).get());
	}
	
	@Ignore("Fails sporadically - timing sensitive?")
	@Test
	public void testRemoveUpdateListener() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, 10);
		
		List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
		list.add(configuration);
		ChannelEventListenerImpl listener = new ChannelEventListenerImpl();
		listener.type = EventType.VALUE_CHANGED;
		listener.channelLocator = channelLocator;
		
		channelManager.registerUpdateListener(list, listener);
		
		synchronized(this) {
			wait(10 * 10 + 5);
		}
		
		channelManager.unregisterUpdateListener(list, listener);
		
		int count;
		int driverCount;

		synchronized (driver) {
			count = listener.integer.get();
			driverCount = driver.channels.get(channelLocator).get();
		}

		assertEquals(driverCount, count); // -> fails
		assertEquals(count, listener.integer.get());

		synchronized(this) {
			wait(10 * 10 + 5);
		}

		synchronized (driver) {
			driverCount = driver.channels.get(channelLocator).get();
		}
		
		assertTrue("driver count: " + driverCount + " count: " + count, driverCount > count);
		assertEquals(count, listener.integer.get());
		
	}

	@Test
	public void testRemoveChangedListener() throws Exception {
		DeviceLocator deviceLocator = createDeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = createChannelLocator("1", deviceLocator);
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		ChannelConfiguration configuration = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, 10);
		
		List<ChannelConfiguration> list = new ArrayList<ChannelConfiguration>();
		list.add(configuration);
		ChannelEventListenerImpl listener = new ChannelEventListenerImpl();
		listener.type = EventType.VALUE_CHANGED;
		listener.channelLocator = channelLocator;
		
		channelManager.registerChangedListener(list, listener);
		
		synchronized(this) {
			wait(10 * 10 + 5);
		}
		
		channelManager.unregisterChangedListener(list, listener);
		
		int count;
		int driverCount;

		synchronized (driver) {
			synchronized (listener) {
				count = listener.integer.get();
				driverCount = driver.channels.get(channelLocator).get();
			}
		}

		assertEquals(driverCount, count);
		assertEquals(count, listener.integer.get());

		synchronized(this) {
			wait(10 * 10 + 5);
		}

		synchronized (driver) {
			driverCount = driver.channels.get(channelLocator).get();
		}
		
		assertTrue("driver count: " + driverCount + " count: " + count, driverCount > count);
		assertEquals(count, listener.integer.get());
		
	}
	
	private class ChannelEventListenerImpl implements ChannelEventListener {

		EventType type;
		ChannelLocator channelLocator;
		AtomicInteger integer = new AtomicInteger();
		
		@Override
		synchronized public void channelEvent(EventType type, List<SampledValueContainer> channels) {
			assertEquals(this.type, type);
			assertEquals(1, channels.size());
			assertEquals(channelLocator, channels.get(0).getChannelLocator());
			assertEquals(integer.getAndIncrement(), channels.get(0).getSampledValue().getValue().getIntegerValue());
		}
	}
	
	private class AppIdImpl implements AppID {

		String id;
		
		AppIdImpl(String id) {
			this.id = id; 
		}
		
		@Override
		public String getIDString() {
			return id;
		}

		@Override
		public String getLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getBundle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Application getApplication() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getOwnerUser() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getOwnerGroup() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getVersion() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URL getOneTimePasswordInjector(String path, HttpSession ses) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
