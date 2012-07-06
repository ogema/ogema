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
package org.ogema.channelmapper.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogema.channelmapper.ChannelMapper;
import org.ogema.channelmapper.ResourceMappingException;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class ChannelMapperComponentTests extends OsgiAppTestBase {

	private ResourceAccess resourceAccess;

	private ChannelAccess channelAccess;

	@Inject
	private final ChannelMapper channelMapper = null;

	@Override
	public void doBefore() {
		System.err.println("Delete configuration file!");

		File file = new File("channelmapper.config");

		if (!file.delete()) {
			System.err.println("Failed to delete file!");
		}
	}

	@Override
	public void doAfter() {
		System.err.println("Delete configuration file!");

		File file = new File("channelmapper.config");

		if (!file.delete()) {
			System.err.println("Failed to delete file!");
		}
	}

	private final Application app = new Application() {
		@Override
		public void start(ApplicationManager appManager) {
			assertNotNull(appManager);
			resourceAccess = appManager.getResourceAccess();
			channelAccess = appManager.getChannelAccess();
		}

		@Override
		public void stop(AppStopReason whatever) {
		}
	};

	@Test
	public void testMapperCreatesNewTopLevelResource() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Resource resource = resourceAccess.getResource("tempSens1");

		assertNull(resource);

		// channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");
		//
		// Resource newResource = resourceAccess.getResource("tempSens1");
		//
		// assertNotNull(newResource);
		//
		// List<Resource> subResources = newResource.getDirectSubResources(false);
		//
		// assertEquals(0, subResources.size());
	}

	@Ignore
	@Test
	public void testMapperDeletesExistingResource() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");

		channelMapper.deleteMappedResource("tempSens1");

		Resource deletedResource = resourceAccess.getResource("tempSens1");

		assertNull(deletedResource);
	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testTryingToDeleteNotMappedResource() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		channelMapper.deleteMappedResource("tempSens1");
	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testMapChannelWrongResourcePath() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		assertNotNull(channelLocator);

		channelMapper.mapChannelToResource(channelLocator, "tempSens1", Direction.DIRECTION_INOUT, 0, 1, 0);
	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testMapChannelResourceDoesNotExist() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		assertNotNull(channelLocator);

		channelMapper.mapChannelToResource(channelLocator, "tempSens1/mmxTemp", Direction.DIRECTION_INOUT, 0, 1, 0);

	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testMapChannelAlreadyExist() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");

		Resource resource = resourceAccess.getResource("tempSens1");

		assertNotNull(resource);
	}

	@Ignore
	@Test
	public void testMapChannel() throws ResourceMappingException {
		ctx.registerService(Application.class, app, null);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");

		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);

		assertNotNull(channelLocator);

		channelMapper.mapChannelToResource(channelLocator, "tempSens1/mmxTemp", Direction.DIRECTION_INOUT, 1000, 1, 0);

		Resource mmsTemp = resourceAccess.getResource("tempSens1/mmxTemp");

		assertNotNull(mmsTemp);

	}

	// @formatter:on
}
