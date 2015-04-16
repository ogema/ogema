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
package org.ogema.channelmanager.test;

import org.ogema.channelmanager.impl.CommInterface;
import org.ogema.channelmanager.impl.DefaultChannelLocator;
import org.ogema.channelmanager.impl.DefaultDeviceLocator;
import org.ogema.channelmanager.testdriver.TestDrv;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;

public class TestCommInterface {

	// @Test
	public void testHasChannel() {
		TestDrv driver = new TestDrv("test-driver", "test-driver");

		CommInterface commIfc = new CommInterface(driver, "ifc1");

		DeviceLocator device = new DefaultDeviceLocator("test-driver", "ifc1", "1", null);

		Main.assertFalse(commIfc.hasChannel(new DefaultChannelLocator(device, "1.1.1")));

		Main.assertTrue(commIfc.addChannel(new DefaultChannelLocator(device, "1.1.1")));

		Main.assertTrue(commIfc.hasChannel(new DefaultChannelLocator(device, "1.1.1")));
	}

}
