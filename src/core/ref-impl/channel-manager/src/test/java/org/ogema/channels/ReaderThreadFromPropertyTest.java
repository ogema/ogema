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
