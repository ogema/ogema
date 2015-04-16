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

	private final ResourceValueListener valueListener = new ResourceValueListener() {

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

		List<ManipulatorConfiguration> leftoverRules = tool.getConfigurations(ManipulatorConfiguration.class);
		assertTrue(leftoverRules.isEmpty());
	}

}
