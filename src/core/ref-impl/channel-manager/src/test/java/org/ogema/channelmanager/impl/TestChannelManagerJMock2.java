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

import static org.junit.Assert.assertNotNull;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.driverspi.ChannelDriver;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestChannelManagerJMock2 {

	private final ChannelAccess channelAccess = null;

	private ChannelDriver dummyDriver;

	private final Mockery context = new Mockery();

	@Ignore
	@Test
	public void AA_testStartBundle() throws Exception {

		assertNotNull(channelAccess);

		context.assertIsSatisfied();
	}

	@Ignore
	@Test
	public void AB_getAccessToRegisteredDriver() throws Exception {

		// registerDummyDriver();
		//
		// ChannelDriver driver = channelManagement.claimDriver("test-driver");

		// assertNotNull(driver);

		context.assertIsSatisfied();
	}

	@Ignore
	@Test
	public void AE_configureChannels() throws Exception {

		registerDummyDriver();

		// List<String> channelList = channelAccess.getAllChannelIds();
		//
		// assertEquals(0, channelList.size());
		//
		// createTestChannels();
		//
		// channelList = channelAccess.getAllChannelIds();
		//
		// assertEquals(2, channelList.size());
	}

	private void registerDummyDriver() {
		dummyDriver = context.mock(ChannelDriver.class);

		context.checking(new Expectations() {
			{
				atLeast(1).of(dummyDriver).getDriverId();
				will(returnValue("test-driver"));
			}
		});
	}

}
