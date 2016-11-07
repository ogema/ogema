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
