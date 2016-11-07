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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.sensors.TemperatureSensor;
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
		final AbsoluteSchedule program = result.program();
		program.create();
		return result;
	}

	public IntegerResource createIntWithSchedule() {
		final IntegerResource result = resman.createResource("TESTINT" + (counter++), IntegerResource.class);
		final AbsoluteSchedule program = result.program();
		program.create();
		return result;
	}

	@Test
	public void testResourceActivationDeactivation() throws InterruptedException {

		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		final FloatResource res = createFloatWithSchedule();

		final AbsoluteSchedule program = res.program();
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

		final AbsoluteSchedule program = res.program();
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

//	@Ignore
	@Test
	public void testEnforcingMultipleProgramsRADs() throws InterruptedException {
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
		
		assertTrue("pattern not available", latch.await(3, TimeUnit.SECONDS));
		
		Thread.sleep(2000);

		assertTrue(patterns.size() == 3);

		for(MyRoomPattern p : patterns) {
			assertTrue(p.lowerLimitProgram.isActive());
			assertTrue(p.upperLimitProgram.isActive());
	
			assertEquals(19.f, p.lowerLimit.getKelvin(), 0.f);
			assertEquals(23.f, p.upperLimit.getKelvin(), 0.f);
		}
	}

	class ChangedListener implements ResourceValueListener<TemperatureResource> {

		public volatile CountDownLatch latch = new CountDownLatch(1);
		private final long startTime;
		private long lastWriteTime = -1;
		private final ApplicationManager am;
		volatile float value = -1;

		public ChangedListener(ApplicationManager am) {
			this.am = am;
			this.startTime = am.getFrameworkTime();
		}

		public void reset() {
			latch = new CountDownLatch(1);
		}

		public long getLastWriteTime() {
			return lastWriteTime;
		}

		@Override
		public void resourceChanged(TemperatureResource resource) {
			value = resource.getValue();
			lastWriteTime = am.getFrameworkTime() - startTime;
			latch.countDown();
		}
	};

	/**
	 * Fills a program schedule with values 10, 20, 8, -2, and applies a {@link ProgramEnforcer}, with
	 * range filter (see {@link ProgramEnforcer#setRangeFilter(float, float, int)}) with upper limit 10;
	 * so that the second value should not be applied to the target resource. <br>
	 * The test is vulnerable to timing issues.
	 * @throws InterruptedException
	 */
	@Ignore
	@Test
	public void testRangeFilter() throws InterruptedException {
		long TEST_INTERVAL = 1000;
		TemperatureSensor sensor = resman.createResource("testTempSens", TemperatureSensor.class);
		sensor.reading().program().create();
		sensor.reading().program().setInterpolationMode(InterpolationMode.STEPS);
		int nrVal = 5;
		List<SampledValue> values = getTestValues(TEST_INTERVAL, 5);
		sensor.reading().program().addValues(values);
		sensor.activate(true);
		ChangedListener listener = new ChangedListener(getApplicationManager());
		sensor.reading().addValueListener(listener);

		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		ProgramEnforcer config = tool.createConfiguration(ProgramEnforcer.class);
		// note: timestep should be irrelevant here
		config.enforceProgram(sensor.reading(), -1l);
		config.commit();
		config.setRangeFilter(Float.NaN, 10f, 1); // only upper limit is set
		Thread.sleep(100); // make sure the first value is out of scope

		for (int i = 1; i < nrVal; i++) {
			listener.latch.await(1500, TimeUnit.MILLISECONDS); // we should wait between 1 and 2 seconds here, because in one case no callback is expected
			if (i == 2) {
				System.out.println("  Value found: i = " + i + ", value = " + listener.value);
				assertEquals("Resource value does not match expected schedule value. Probably filter does not work",
						10, listener.value, 0.1F);
			}
			else {
				System.out.println("  Value found: i = " + i + ", value = " + listener.value);
				assertEquals("Resource value does not match expected schedule value. i = " + i + ", time difference: "
						+ listener.getLastWriteTime(), getValue(i), listener.value, 0.1);
			}
			assert (sensor.reading().isActive()) : "Target resource unexpectedly found inactive."; // sometimes fails when i=4, and value is still on the one for i=3; very rarely
			listener.reset();
		}
		sensor.reading().removeValueListener(listener);
		config.remove();
		tool.stop();
		sensor.delete();
		Thread.sleep(2000); // otherwise test environment will complain that it could not stop
		// sleeping thread from tool.stop() or callbacks triggered by config.remove()
	}

	private float getValue(int i) {
		return (i < 3) ? 10 * i : (38 - 10 * i);
	}

	private List<SampledValue> getTestValues(final long TEST_INTERVAL, final int nrVal) {
		long currentTime = getApplicationManager().getFrameworkTime();
		List<SampledValue> values = new LinkedList<SampledValue>();
		for (int i = 0; i < nrVal; i++) {
			SampledValue sv = new SampledValue(new FloatValue(getValue(i)), currentTime + TEST_INTERVAL * i,
					Quality.GOOD);
			values.add(sv);
		}
		values.add(new SampledValue(new FloatValue(0), currentTime + TEST_INTERVAL * nrVal, Quality.BAD));
		return values;
	}

//	@Ignore
	@Test
	public void testDeactivationConfiguration() throws InterruptedException {
		TemperatureSensor sensor = resman.createResource("testTempSens", TemperatureSensor.class);
		sensor.reading().program().create();
		long TEST_INTERVAL = 1000;
		int nrVal = 2;
		sensor.reading().program().setInterpolationMode(InterpolationMode.STEPS);
		sensor.reading().program().addValues(getTestValues(TEST_INTERVAL, nrVal));
		sensor.activate(true);

		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		ProgramEnforcer config = tool.createConfiguration(ProgramEnforcer.class);
		// note: timestep should be irrelevant here
		config.enforceProgram(sensor.reading(), -1l);
		config.deactivateTargetIfProgramMissing(false);
		config.commit();

		Thread.sleep(TEST_INTERVAL * (nrVal + 1));
		assert (sensor.reading().isActive()) : "Target resource unexpectedly found inactive";
		config.deactivateTargetIfProgramMissing(true);
		sensor.reading().program().addValues(getTestValues(TEST_INTERVAL, nrVal));
		config.commit();
		Thread.sleep(TEST_INTERVAL * (nrVal + 2));
		assert (!sensor.reading().isActive()) : "Target resource unexpectedly found active";
		tool.deleteAllConfigurations();
		Thread.sleep(1000); // allow callbacks from delete methods to complete
		sensor.delete();
	}
}
