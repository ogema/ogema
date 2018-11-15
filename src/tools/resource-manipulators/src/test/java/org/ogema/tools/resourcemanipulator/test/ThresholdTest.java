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
package org.ogema.tools.resourcemanipulator.test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.StructureTestListener;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.configurations.Threshold;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests for the program-enforcer manipulator.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@ExamReactorStrategy(PerClass.class)
public class ThresholdTest extends OsgiAppTestBase {

	ResourceManagement resman;
	ResourceAccess resacc;
	OgemaLogger logger;

	int counter = 0;

	private CountDownLatch structureLatch, valueLatch;

	public ThresholdTest() {
		super(true);
	}

	private final ResourceStructureListener defaultStructureEventing = new ResourceStructureListener() {

		@Override
		public void resourceStructureChanged(ResourceStructureEvent event) {
			logger = getApplicationManager().getLogger();
			logger.info("Got event of type " + event.getType());
			final Resource cause = event.getSource();
			if (cause != null)
				logger.info("Cause was " + cause.getPath());
			final Resource changedResource = event.getChangedResource();
			if (changedResource != null)
				logger.info("Changed resource was " + changedResource.getPath());
			switch (event.getType()) {
			case RESOURCE_CREATED:
			case RESOURCE_ACTIVATED:
			case RESOURCE_DEACTIVATED:
			case RESOURCE_DELETED: {
				structureLatch.countDown();
				return;
			}
			default:
				logger.info("non-counted event.");
			}
		}
	};

	private final ResourceValueListener<Resource> valueListener = new ResourceValueListener<Resource>() {

		@Override
		public void resourceChanged(Resource resource) {
			valueLatch.countDown();
		}
	};

	@Before
	public void setup() {
		resman = getApplicationManager().getResourceManagement();
		resacc = getApplicationManager().getResourceAccess();
	}

	@Test
	public void testThreshold() throws InterruptedException {

		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();

		final FloatResource floatResource = resman.createResource("FLOAT" + (++counter), FloatResource.class);
		final BooleanResource booleanResource = resman.createResource("BOOL" + (counter), BooleanResource.class);
		booleanResource.activate(false);
		Threshold config = tool.createConfiguration(Threshold.class);
		config.setThreshold(floatResource, 0.f, booleanResource);
		assertTrue(config.commit());

		structureLatch = new CountDownLatch(1);
		booleanResource.addStructureListener(defaultStructureEventing);
		StructureTestListener stl = new StructureTestListener();
		booleanResource.addStructureListener(stl);
		floatResource.setValue(-1.f);
		floatResource.activate(false);
		structureLatch.await(5, TimeUnit.SECONDS);
		stl.awaitEvent(ResourceStructureEvent.EventType.RESOURCE_ACTIVATED);
		stl.reset();
		assertTrue(booleanResource.isActive());
		assertEquals(false, booleanResource.getValue());
		booleanResource.removeStructureListener(defaultStructureEventing);

		valueLatch = new CountDownLatch(1);
		booleanResource.addValueListener(valueListener);
		floatResource.setValue(1.f);
		stl.awaitEvent(ResourceStructureEvent.EventType.RESOURCE_ACTIVATED);
		assertTrue(booleanResource.isActive());
		assertEquals(true, booleanResource.getValue());
		booleanResource.removeValueListener(valueListener);

		tool.stop();
		tool.deleteAllConfigurations();
		sleep(1000);

		List<ManipulatorConfiguration> leftoverRules = tool.getConfigurations(ManipulatorConfiguration.class);
		assertTrue("Not all ManipulatorConfigurations were deleted", leftoverRules.isEmpty());
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
