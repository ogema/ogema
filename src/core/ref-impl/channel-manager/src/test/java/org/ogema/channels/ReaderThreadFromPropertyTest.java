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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ogema.core.channelmanager.ChannelAccessException;

public class ReaderThreadFromPropertyTest {

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
	}
	
	@Test
	public void testReaderThreadFactoryFromProperty() throws Exception {
		
		Properties systemProperties = System.getProperties();
		
		ReaderThreadFactoryImpl.constructorCount = 0;
		
		systemProperties.remove(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY + "." + driver.getDriverId());
		systemProperties.setProperty(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY, ReaderThreadFactoryImpl.class.getName());

		channelManager.addDriver(driver);

		assertEquals(1, ReaderThreadFactoryImpl.constructorCount);
		
		systemProperties.remove(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY);
	}
	
	@Test
	public void testReaderThreadFactoryFromDriverProperty() throws Exception {

		Properties systemProperties = System.getProperties();
		
		ReaderThreadFactoryImpl.constructorCount = 0;
		ReaderThreadFactoryImpl2.constructorCount = 0;
		
		systemProperties.setProperty(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY, ReaderThreadFactoryImpl.class.getName());
		systemProperties.setProperty(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY + "." + driver.getDriverId(), ReaderThreadFactoryImpl2.class.getName());
		
		channelManager.addDriver(driver);

		assertEquals(0, ReaderThreadFactoryImpl.constructorCount);
		assertEquals(1, ReaderThreadFactoryImpl2.constructorCount);
		
		systemProperties.remove(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY);
		systemProperties.remove(ChannelManagerImpl.PROP_CHANNELMANAGER_READERTHREADFACTORY + "." + driver.getDriverId());
	}
}

class ReaderThreadFactoryImpl2 implements ReaderThreadFactory {

	static long constructorCount;
	
	public ReaderThreadFactoryImpl2() {
		constructorCount++;
	}
	
	@Override
	public void noChannels(ReaderThread readerThread) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ReaderThread getReaderThread(Channel channel) throws ChannelAccessException {
		// TODO Auto-generated method stub
		return null;
	}
}

class ReaderThreadFactoryImpl implements ReaderThreadFactory {

	static long constructorCount;
	
	public ReaderThreadFactoryImpl() {
		constructorCount++;
	}
	
	@Override
	public void noChannels(ReaderThread readerThread) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ReaderThread getReaderThread(Channel channel) throws ChannelAccessException {
		// TODO Auto-generated method stub
		return null;
	}
}
