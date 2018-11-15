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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;

public class MultipleConfigurationsTest {

	private ChannelManagerImpl channelManager;

	private ChannelDriverImpl driver;
	
	private ApplicationRegistryImpl appReg;
	
	private AppIdImpl app1;
	@SuppressWarnings("unused")
	private AppIdImpl app2;
	
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
		
		app1 = new AppIdImpl("1");
		app2 = new AppIdImpl("2");
		
		appReg = new ApplicationRegistryImpl();
		appReg.appId = app1;
		
		channelManager.appreg = appReg;
		channelManager.permMan = new PermissionManagerImpl();
		
		driver = new ChannelDriverImpl("driver1", "firstDriver");
		channelManager.addDriver(driver);
		
		channelManager.activate(null);
		
		assertNotNull(appReg.listener);
	}
	
	@Ignore("Fails sporadically - timing sensitive?")
	@Test
	public void testMultipleConfigurations() throws Exception {
		DeviceLocator deviceLocator = new DeviceLocator(driver.getDriverId(), "", "", null);
		ChannelLocator channelLocator = new ChannelLocator("1", deviceLocator);

		ChannelConfiguration conf1;
		ChannelConfiguration conf3;
		ChannelConfiguration conf4;
		ChannelConfiguration conf2;
		ChannelConfiguration conf5;
		
		ChannelEventListenerImpl listener1;
		ChannelEventListenerImpl listener2;
		ChannelEventListenerImpl listener3;
		ChannelEventListenerImpl listener4;
		
		driver.channels.put(channelLocator, new AtomicInteger());
		
		long startTime = System.currentTimeMillis();
		
		synchronized (this) {
			if (startTime % 10 != 0)
				wait((startTime % 10));
		}
		synchronized(driver) {
		// open the channel with different configurations
		conf1 = channelManager.addChannel(channelLocator, Direction.DIRECTION_OUTPUT, 10);
		conf3 = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.NO_READ_NO_LISTEN);
		conf4 = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
		conf2 = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, 20);
		conf5 = channelManager.addChannel(channelLocator, Direction.DIRECTION_INPUT, 40);
		
		assertEquals(1, driver.channelAddedCount);
		assertEquals(1, driver.listenChannelsCount);
		
		// now add listener for the different configurations
		List<ChannelConfiguration> configurations = new ArrayList<ChannelConfiguration>();
		configurations.add(conf1);
		listener1 = new ChannelEventListenerImpl();
		listener1.channelLocator = channelLocator;
		channelManager.registerUpdateListener(configurations, listener1);
		
		configurations.clear();
		configurations = new ArrayList<ChannelConfiguration>();
		configurations.add(conf2);
		listener2 = new ChannelEventListenerImpl();
		listener2.channelLocator = channelLocator;
		channelManager.registerUpdateListener(configurations, listener2);

		configurations.clear();
		configurations = new ArrayList<ChannelConfiguration>();
		configurations.add(conf3);
		 listener3 = new ChannelEventListenerImpl();
		listener3.channelLocator = channelLocator;
		channelManager.registerUpdateListener(configurations, listener3);

		configurations.clear();
		configurations = new ArrayList<ChannelConfiguration>();
		configurations.add(conf4);
		listener4 = new ChannelEventListenerImpl();
		listener4.channelLocator = channelLocator;
		channelManager.registerUpdateListener(configurations, listener4);
		}
		
		synchronized (this) {
			wait(10 * 10 + 5);
		}
		
		long endTime = System.currentTimeMillis();
		
		//System.out.println("time: " + (endTime - startTime));
		
		long count = ((endTime - startTime) / 20);
		
		//System.out.println("count: " + count);
		
		channelManager.deleteChannel(conf1);
		channelManager.deleteChannel(conf2);
		channelManager.deleteChannel(conf3);
		channelManager.deleteChannel(conf4);
		channelManager.deleteChannel(conf5);

		assertEquals(1, driver.channelRemovedCount);
		
		long readCount = driver.channels.get(channelLocator).get();
		
		assertTrue(readCount >= count);
		assertTrue("expected:" + count + " actual: " + readCount, readCount <= (count + 2));
		assertEquals(0, listener1.integer.get());
		assertEquals(readCount, listener2.integer.get());
		assertEquals(readCount, listener3.integer.get());
		assertEquals(readCount, listener4.integer.get());
	}
	
	private class ChannelEventListenerImpl implements ChannelEventListener {

//		EventType type = EventType.VALUE_CHANGED;
		ChannelLocator channelLocator;
		AtomicInteger integer = new AtomicInteger();
		
		@Override
		public void channelEvent(EventType type, List<SampledValueContainer> channels) {
			//assertEquals(this.type, type);
			assertEquals(1, channels.size());
			assertEquals(channelLocator, channels.get(0).getChannelLocator());
			//assertEquals(integer.getAndIncrement(), channels.get(0).getSampledValue().getValue().getIntegerValue());
			integer.getAndIncrement();
		}
	}
}
