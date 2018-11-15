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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class LocatorTest {

	@SuppressWarnings("unused")
	private DeviceLocator locator1;
	@SuppressWarnings("unused")
	private DeviceLocator locator2;
	@SuppressWarnings("unused")
	private DeviceLocator locator3;

	@Before
	public void setup() {
		locator1 = new DeviceLocator("test-driver", "ifc1", "1", null);
		locator2 = new DeviceLocator("test-driver", "ifc1", "1", "9600-8-N-1");
		locator3 = new DeviceLocator("test-driver", "ifc2", "1", "19200-8-N-1");
	}

	@Test
	public void testDeviceLocatorEquals() {
		
		DeviceLocator device1 = new DeviceLocator("test-driver", "ifc1", "1", null);
		DeviceLocator device2 = new DeviceLocator("test-driver", "ifc1", "1", "9600-8-N-1");
		DeviceLocator device3 = new DeviceLocator("test-driver", "ifc2", "1", "9600-8-N-1");
		
		DeviceLocator device4 = new DeviceLocator("test-driver", "ifc1", "1", null);
		DeviceLocator device5 = new DeviceLocator("test-driver", "ifc1", "1", "9600-8-N-1");
		
		assertTrue(device1.equals(device1));
		assertTrue(device2.equals(device2));
		assertTrue(device3.equals(device3));
		
		assertTrue(device1.equals(device4));
		assertTrue(device2.equals(device5));
		
		assertFalse(device1.equals(device2));
		assertFalse(device2.equals(device3));
	}

	@Test
	public void testChannelLocatorEquals() {
		
		DeviceLocator device1 = new DeviceLocator("test-driver", "ifc1", "1", null);
		@SuppressWarnings("unused")
		DeviceLocator device2 = new DeviceLocator("test-driver", "ifc1", "1", "9600-8-N-1");
		DeviceLocator device3 = new DeviceLocator("test-driver", "ifc2", "1", "9600-8-N-1");		
		DeviceLocator device4 = new DeviceLocator("test-driver", "ifc1", "1", null);
		
		ChannelLocator channel1 = new ChannelLocator("1.2.1", device1);
		ChannelLocator channel2 = new ChannelLocator("1.2.1", device4);
		ChannelLocator channel3 = new ChannelLocator("1.2.1", device3);
		ChannelLocator channel4 = new ChannelLocator("1.4.1", device1);

		assertTrue(channel1.equals(channel1));
		assertTrue(channel2.equals(channel2));
		assertTrue(channel3.equals(channel3));
		assertTrue(channel4.equals(channel4));
		
		assertTrue(channel1.equals(channel2));
		assertFalse(channel1.equals(channel3));
		assertFalse(channel1.equals(channel4));
	}

}
