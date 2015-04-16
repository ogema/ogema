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
package org.ogema.channelmanager.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class DefaultLocatorTest {

	private DeviceLocator locator1;
	private DeviceLocator locator2;
	private DeviceLocator locator3;

	@Before
	public void setup() {
		locator1 = new DefaultDeviceLocator("test-driver", "ifc1", "1", null);
		locator2 = new DefaultDeviceLocator("test-driver", "ifc1", "1", "9600-8-N-1");
		locator3 = new DefaultDeviceLocator("test-driver", "ifc2", "1", "19200-8-N-1");
	}

	@Test
	public void testDeviceLocatorEquals() {
		assertTrue(locator1.equals(locator2));
		assertFalse(locator1.equals(locator3));
		assertFalse(locator3.equals(locator2));

		DeviceLocator locator4 = new DefaultDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
		DeviceLocator locator5 = new DefaultDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		assertTrue(locator4.equals(locator5));
	}

	@Test
	public void testChannelLocatorEquals() {
		ChannelLocator channel1 = new DefaultChannelLocator(locator1, "1.2.1");
		ChannelLocator channel2 = new DefaultChannelLocator(locator2, "1.2.1");
		ChannelLocator channel3 = new DefaultChannelLocator(locator3, "1.2.1");
		ChannelLocator channel4 = new DefaultChannelLocator(locator1, "1.4.1");

		assertTrue(channel1.equals(channel2));
		assertFalse(channel3.equals(channel1));
		assertFalse(channel1.equals(channel4));
	}

}
