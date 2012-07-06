/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import org.junit.Test;
import org.ogema.channelmanager.impl.testdriver.TestDriver;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class TestCommInterface {

	@Test
	public void testHasChannel() {
		TestDriver driver = new TestDriver("test-driver", "test-driver");

		CommInterface commIfc = new CommInterface(driver, "ifc1");

		DeviceLocator device = new DefaultDeviceLocator("test-driver", "ifc1", "1", null);

		assertFalse(commIfc.hasChannel(new DefaultChannelLocator(device, "1.1.1")));

		assertTrue(commIfc.addChannel(new DefaultChannelLocator(device, "1.1.1")));

		assertTrue(commIfc.hasChannel(new DefaultChannelLocator(device, "1.1.1")));
	}

}
