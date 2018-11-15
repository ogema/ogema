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
