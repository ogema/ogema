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
package org.ogema.channelmapper.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.channelmapper.ChannelMapper;
import org.ogema.channelmapper.ResourceMappingException;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

// TODO very incomplete
@ExamReactorStrategy(PerClass.class)
public class ChannelMapperComponentTest extends OsgiAppTestBase {
	
	private volatile ResourceAccess resourceAccess;
	private volatile ChannelAccess channelAccess;
	
	public ChannelMapperComponentTest() {
		super(true);
	}
	
	@Inject
	private ChannelMapper channelMapper;

	@Override
	public void doBefore() {
		System.err.println("Delete configuration file!");

		File file = new File("channelmapper.config");

		if (!file.delete()) {
			System.err.println("Failed to delete file!");
		}
		resourceAccess = getApplicationManager().getResourceAccess();
		channelAccess = getApplicationManager().getChannelAccess();
	}

	@Override
	public void doAfter() {
		resourceAccess = null;
		channelAccess = null;
		System.err.println("Delete configuration file!");

		File file = new File("channelmapper.config");

		if (!file.delete()) {
			System.err.println("Failed to delete file!");
		}
	}

	// FIXME something seems to be missing here...
	@Test
	public void testMapperCreatesNewTopLevelResource() throws ResourceMappingException {
		assertNotNull(resourceAccess);
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
		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");
		channelMapper.deleteMappedResource("tempSens1");
		Resource deletedResource = resourceAccess.getResource("tempSens1");
		assertNull(deletedResource);
	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testTryingToDeleteNotMappedResource() throws ResourceMappingException {
		channelMapper.deleteMappedResource("tempSens1");
	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testMapChannelWrongResourcePath() throws ResourceMappingException {
		try {
			channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");
//			DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
			DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
//			ChannelLocator channelLocator = channelAccess.getChannelLocator("02/862a", deviceLocator);
			ChannelLocator channelLocator = new ChannelLocator("02/862a", deviceLocator);
			assertNotNull(channelLocator);
			channelMapper.mapChannelToResource(channelLocator, "tempSens1", Direction.DIRECTION_INOUT, 0, 1, 0);
		} finally {
			resourceAccess.getResource("tempSens1").delete();
		}
	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testMapChannelResourceDoesNotExist() throws ResourceMappingException {

		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);
//		DeviceLocator deviceLocator = channelAccess.getDeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = new ChannelLocator("02/862a", deviceLocator);

		assertNotNull(channelLocator);

		channelMapper.mapChannelToResource(channelLocator, "tempSens1/mmxTemp", Direction.DIRECTION_INOUT, 0, 1, 0);

	}

	@Ignore
	@Test(expected = ResourceMappingException.class)
	public void testMapChannelAlreadyExist() throws ResourceMappingException {
		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");

		Resource resource = resourceAccess.getResource("tempSens1");

		assertNotNull(resource);
		resource.delete();
	}

	@Ignore
	@Test
	public void testMapChannel() throws ResourceMappingException {

		channelMapper.addMappedResource(TemperatureSensor.class, "tempSens1");

		DeviceLocator deviceLocator = new DeviceLocator("mbus", "/dev/ttyUSB0", "p1", null);

		ChannelLocator channelLocator = new ChannelLocator("02/862a", deviceLocator);

		assertNotNull(channelLocator);

		channelMapper.mapChannelToResource(channelLocator, "tempSens1/mmxTemp", Direction.DIRECTION_INOUT, 1000, 1, 0);

		Resource mmsTemp = resourceAccess.getResource("tempSens1/mmxTemp");

		assertNotNull(mmsTemp);
		resourceAccess.getResource("tempSens1").delete();
	}

	// @formatter:on
}
