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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.resourcemanipulator.test.rad.MyRoomPattern;
import org.ogema.tools.resourcemanipulator.test.rad.RoomPattern;
import org.ogema.tools.resourcemanipulator.test.rad.TemperatureSensorPattern;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests for the program-enforcer manipulator.
 * @author Timo Fischer, Fraunhofer IWES
 */
@ExamReactorStrategy(PerClass.class)
public class ProgramEnforcerTest extends OsgiAppTestBase {

	ResourceManagement resman;
	ResourceAccess resacc;
	OgemaLogger logger;

	int counter = 0;

	private CountDownLatch structureLatch;

	public ProgramEnforcerTest() {
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

	@Before
	public void setup() {
		resman = getApplicationManager().getResourceManagement();
		resacc = getApplicationManager().getResourceAccess();
	}

	public FloatResource createFloatWithSchedule() {
		final FloatResource result = resman.createResource("TESTFLOAT" + (counter++), FloatResource.class);
		final DefinitionSchedule program = result.program().create();
		return result;
	}

	public IntegerResource createIntWithSchedule() {
		final IntegerResource result = resman.createResource("TESTINT" + (counter++), IntegerResource.class);
		final DefinitionSchedule program = result.program().create();
		return result;
	}

	@Test
	public void testResourceActivationDeactivation() throws InterruptedException {

		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());

		final FloatResource res = createFloatWithSchedule();

		final DefinitionSchedule program = res.program();
		assertTrue(res.exists());
		program.setInterpolationMode(InterpolationMode.STEPS);
		res.activate(true);
		assertTrue(res.isActive());

		tool.start();

		// control according to empty schedule: should make the resource inactive.
		res.addStructureListener(defaultStructureEventing);
		structureLatch = new CountDownLatch(1);

		ProgramEnforcer config = tool.createConfiguration(ProgramEnforcer.class);
		config.enforceProgram(res, 5000l);
		config.commit();

		structureLatch.await(5, TimeUnit.SECONDS);

		assertEquals(0, structureLatch.getCount());
		assertFalse(res.isActive());
		assertTrue(program.isActive());

		// Add an entry to the schedule: should activate the resource and write the value.
		structureLatch = new CountDownLatch(1);
		final float value = 100.f * (float) Math.random() - 50.f;
		program.addValue(getApplicationManager().getFrameworkTime() - 1000l, new FloatValue(value));
		final SampledValue currentValue = program.getValue(getApplicationManager().getFrameworkTime());
		assertNotNull(currentValue);
		assertTrue(currentValue.getQuality() == Quality.GOOD);
		assertEquals(value, currentValue.getValue().getFloatValue(), 0.1f);
		structureLatch.await(5, TimeUnit.SECONDS);

		assertEquals(0, structureLatch.getCount());
		assertTrue(res.isActive());
		assertTrue(program.isActive());
		assertEquals(value, res.getValue(), 1.e-2);

		res.removeStructureListener(defaultStructureEventing);
		tool.deleteAllConfigurations();
		tool.stop();
	}

	@Test
	public void testResourceActivationDeactivationForIntegers() throws InterruptedException {

		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final IntegerResource res = createIntWithSchedule();

		final DefinitionSchedule program = res.program();
		assertTrue(res.exists());
		program.setInterpolationMode(InterpolationMode.STEPS);
		res.activate(true);
		assertTrue(res.isActive());

		// control according to empty schedule: should make the resource inactive.
		res.addStructureListener(defaultStructureEventing);
		structureLatch = new CountDownLatch(1);
		ProgramEnforcer config = tool.createConfiguration(ProgramEnforcer.class);
		config.enforceProgram(res, 5000l);
		config.commit();

		structureLatch.await(5, TimeUnit.SECONDS);

		assertEquals(0, structureLatch.getCount());
		assertFalse(res.isActive());
		assertTrue(program.isActive());

		// Add an entry to the schedule: should activate the resource and write the value.
		structureLatch = new CountDownLatch(1);
		final int value = (int) (100.f * (float) Math.random() - 50.f);
		program.addValue(getApplicationManager().getFrameworkTime() - 1000l, new IntegerValue(value));
		final SampledValue currentValue = program.getValue(getApplicationManager().getFrameworkTime());
		assertNotNull(currentValue);
		assertTrue(currentValue.getQuality() == Quality.GOOD);
		assertEquals(value, currentValue.getValue().getIntegerValue());
		structureLatch.await(5, TimeUnit.SECONDS);

		assertEquals(0, structureLatch.getCount());
		assertTrue(res.isActive());
		assertTrue(program.isActive());
		assertEquals(value, res.getValue());

		res.removeStructureListener(defaultStructureEventing);
		tool.deleteAllConfigurations();
		tool.stop();
	}

	int resourceCounter = 0;

	@Test
	public void testEnforcingMultipleProgramsRADs() {
		final ResourcePatternAccess resourcePatternAccess = getApplicationManager().getResourcePatternAccess();
		final ResourceManipulator resourceManipulator = new ResourceManipulatorImpl(getApplicationManager());
		resourceManipulator.start();

		final CountDownLatch latch = new CountDownLatch(1);
		final List<MyRoomPattern> patterns = new ArrayList<>();
		resourcePatternAccess.addPatternDemand(MyRoomPattern.class, new PatternListener<MyRoomPattern>() {

			@Override
			public void patternAvailable(MyRoomPattern pattern) {
				resourceCounter++;
				patterns.add(pattern);

				if(resourceCounter == 3) {
					getApplicationManager().createTimer(1000, new TimerListener() {
						@Override
						public void timerElapsed(Timer timer) {

							for(MyRoomPattern p : patterns) {
								p.lowerLimitProgram.create();
								p.upperLimitProgram.create();

								p.settings.activate(true);

								p.lowerLimitProgram.setInterpolationMode(InterpolationMode.STEPS);
								p.lowerLimitProgram.addValue(0, new FloatValue(19.f));
								p.upperLimitProgram.setInterpolationMode(InterpolationMode.STEPS);
								p.upperLimitProgram.addValue(0, new FloatValue(23.f));

								ProgramEnforcer config = resourceManipulator.createConfiguration(ProgramEnforcer.class);
								config.enforceProgram(p.lowerLimit, 1000);
								config.commit();

								ProgramEnforcer configUpper = resourceManipulator.createConfiguration(ProgramEnforcer.class);
								configUpper.enforceProgram(p.upperLimit, 1000);
								configUpper.commit();
							}

							timer.destroy();
							latch.countDown();
						}
					});
				}
			}

			@Override
			public void patternUnavailable(MyRoomPattern pattern) {
			}
		}, AccessPriority.PRIO_DEVICESPECIFIC);

		RoomPattern room = resourcePatternAccess.createResource("LIVINGROOM", RoomPattern.class);
		room.init(1);
		
		TemperatureSensorPattern tempSens = resourcePatternAccess.createResource("DUMMY_ZIGBEE_TEMPERATURE_SENSOR", TemperatureSensorPattern.class);
		TemperatureSensorPattern tempSens2 = resourcePatternAccess.createResource("DUMMY_ZIGBEE_TEMPERATURE_SENSOR2", TemperatureSensorPattern.class);
		TemperatureSensorPattern tempSens3 = resourcePatternAccess.createResource("DUMMY_ZIGBEE_TEMPERATURE_SENSOR3", TemperatureSensorPattern.class);
		
		tempSens.model.activate(true);
		tempSens2.model.activate(true);
		tempSens3.model.activate(true);
		
		room.model.temperatureSensor().setAsReference(tempSens.model);
		room.model.activate(true);
		
		RoomPattern room2 = resourcePatternAccess.createResource("LIVINGROOM2", RoomPattern.class);
		room2.init(1);
		room2.model.temperatureSensor().setAsReference(tempSens2.model);
		room2.model.activate(true);
		
		RoomPattern room3 = resourcePatternAccess.createResource("LIVINGROOM3", RoomPattern.class);
		room3.init(1);
		room3.model.temperatureSensor().setAsReference(tempSens3.model);
		room3.model.activate(true);
		
		try {
			assertTrue("pattern not available", latch.await(3, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
		}
		
		sleep(2000);

		assertTrue(patterns.size() == 3);

		for(MyRoomPattern p : patterns) {
			assertTrue(p.lowerLimitProgram.isActive());
			assertTrue(p.upperLimitProgram.isActive());
	
			assertEquals(19.f, p.lowerLimit.getKelvin(), 0.f);
			assertEquals(23.f, p.upperLimit.getKelvin(), 0.f);
		}
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
