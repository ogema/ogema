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
package org.ogema.channelmanager.test;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class DefaultLocatorTest {

	private DeviceLocator locator1;
	private DeviceLocator locator2;
	private DeviceLocator locator3;

	//	@Before
	public void setup() {
		locator1 = new DeviceLocator("test-driver", "ifc1", "1", null);
		locator2 = new DeviceLocator("test-driver", "ifc1", "1", "9600-8-N-1");
		locator3 = new DeviceLocator("test-driver", "ifc2", "1", "19200-8-N-1");
	}

	//	@Test
	public void testDeviceLocatorEquals() {
		Main.assertTrue(locator1.equals(locator2));
		Main.assertFalse(locator1.equals(locator3));
		Main.assertFalse(locator3.equals(locator2));

		DeviceLocator locator4 = new DeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		DeviceLocator locator5 = new DeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		Main.assertTrue(locator4.equals(locator5));
	}

	//	@Test
	public void testChannelLocatorEquals() {
		ChannelLocator channel1 = new ChannelLocator("1.2.1", locator1);
		ChannelLocator channel2 = new ChannelLocator("1.2.1", locator2);
		ChannelLocator channel3 = new ChannelLocator("1.2.1", locator3);
		ChannelLocator channel4 = new ChannelLocator("1.4.1", locator1);

		Main.assertTrue(channel1.equals(channel2));
		Main.assertFalse(channel3.equals(channel1));
		Main.assertFalse(channel1.equals(channel4));
	}

}
