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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleSum;

/**
 * Tests for schedule sum manipulator.
 * @author Marco Postigo Perez
 */
// TODO better coverage for different configuration options, interpolation modes, etc
public class ScheduleSumTest extends OsgiAppTestBase {

	private static final float EPSILON = 1E-6f;
	private static final int MAX_FOR_SEED_SELECTION = 10000;
	private static final int MAX_NMB_OF_SCHEDULES = 10;
	private static final int MAX_VALUES_FOR_EACH_SCHEDULE = 1000;

	private ResourceManagement resman;

	public ScheduleSumTest() {
		super(true);
	}

	@Before
	public void setup() {
		resman = getApplicationManager().getResourceManagement();
	}
	
	@Test
	public void scheduleSumWorksSimple() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(20, new FloatValue(10));
		sched2.addValue(0, new FloatValue(5));
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setActivationControl(true);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		Assert.assertEquals("Target schedule size incorrect", 3, target.size());
		
		SampledValue sv1 = target.getValue(0);
		Assert.assertNotNull(sv1);
		Assert.assertEquals("Schedule sum incorrect", 15, sv1.getValue().getFloatValue(), 0.01F); 
		
		sv1 = target.getValue(10);
		Assert.assertNotNull(sv1);
		Assert.assertEquals("Schedule sum incorrect", 25, sv1.getValue().getFloatValue(), 0.01F); 
		
		sv1 = target.getValue(20);
		Assert.assertNotNull(sv1);
		Assert.assertEquals("Schedule sum incorrect", 25, sv1.getValue().getFloatValue(), 0.01F); 
		
		tool.deleteAllConfigurations();
		f.delete();
	}

	@Test
	public void testScheduleSum() {
		// TODO for all interpolation modes ...
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		
		int randomInt = new Random().nextInt(MAX_FOR_SEED_SELECTION);
		// use (probable) prime as seed
		long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
		Random rdn = new Random(seed);
		
		List<Schedule> schedules = new ArrayList<>();
		int nmbOfSchedules = rdn.nextInt(MAX_NMB_OF_SCHEDULES) + 1;
		for(int i = 0; i < nmbOfSchedules; i++) {
			AbsoluteSchedule floatSchedule = createAndActivateFloatSchedule();
			assertTrue(floatSchedule.isActive());
			schedules.add(floatSchedule);
		}
		
		AbsoluteSchedule sumSchedule = createAndActivateFloatSchedule();
		assertTrue(sumSchedule.isActive());
		SumValueListener sumValueListener = new SumValueListener();
		sumSchedule.addValueListener(sumValueListener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(schedules, sumSchedule);
		scheduleSum.setDelay(5000);
		scheduleSum.commit();
		
		// fill schedules and check results ...
		List<Float> sum = new ArrayList<>();
		int nmbOfValues = rdn.nextInt(MAX_VALUES_FOR_EACH_SCHEDULE) + 1;
		System.out.println("Starting schedule sum test - nmbOfValues: " + nmbOfValues + ", seed: "
				+ seed + ", nmb of schedules: " + nmbOfSchedules);
		SummaryStatistics ss = new SummaryStatistics();
		for(int i = 0; i < nmbOfValues; i++) {
			for(Schedule s : schedules) {
				float nextFloat = rdn.nextFloat();
				s.addValue(i, new FloatValue(nextFloat));
				ss.addValue(nextFloat);
			}
			sum.add((float) ss.getSum());
			ss.clear();
		}
		
		Schedule target = scheduleSum.getTarget();
		boolean done = false;
		do {
			try {
				boolean await = sumValueListener.latch.await(20, TimeUnit.SECONDS);
				if (target.getValues(0).size() == nmbOfValues || !await) {
					done = true;
				} else {
					sumValueListener.setLatch(new CountDownLatch(1));
				}
			} catch (InterruptedException e) {
			}
		} while(!done);
		
		List<SampledValue> values = target.getValues(0);
		for(int i = 0; i < values.size(); i++) {
			float result = values.get(i).getValue().getFloatValue();
			Float expectedResult = sum.get(i);
			assertEquals("Sum for timestamp " + i + " is not correct! Expected: " +
					expectedResult + ", but got: " + result + ", seed: " + seed + "; nr schedules " + nmbOfSchedules, expectedResult,
					result, EPSILON);
		}
		
		tool.stop();
		tool.deleteAllConfigurations();
		// wait a second so the system can delete the configurations ... -> maybe register listener
		// and count down?
		sleep(1000);

        List<ManipulatorConfiguration> leftoverRules = tool.getConfigurations(ManipulatorConfiguration.class);
        assertTrue("Not all ManipulatorConfigurations were deleted", leftoverRules.isEmpty());
	}
	
	@Test
	public void ignoreGapsWorks1() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(20, new FloatValue(10));
		sched2.addValue(15, new FloatValue(5));
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setActivationControl(true);
		scheduleSum.setIgnoreGaps(true);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		Assert.assertEquals("Target schedule size incorrect", 4, target.size());
		
		final Iterator<SampledValue> it = target.iterator();
		SampledValue sv = it.next();
		Assert.assertEquals("Schedule sum incorrect", 10, sv.getValue().getFloatValue(), 0.01F);
		Assert.assertEquals("Unexpected timestamp in schedule sum", 0, sv.getTimestamp());
		
		sv = it.next();
		Assert.assertEquals("Schedule sum incorrect", 20, sv.getValue().getFloatValue(), 0.01F);
		Assert.assertEquals("Unexpected timestamp in schedule sum", 10, sv.getTimestamp());
		
		sv = it.next();
		Assert.assertEquals("Schedule sum incorrect", 25, sv.getValue().getFloatValue(), 0.01F);
		Assert.assertEquals("Unexpected timestamp in schedule sum", 15, sv.getTimestamp());
		
		tool.deleteAllConfigurations();
		f.delete();
	}
	
	@Test
	public void ignoreGapsWorks2() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(20, new FloatValue(10));
		sched2.addValue(0, new FloatValue(0));
		sched2.addValues(Collections.singletonList(new SampledValue(FloatValue.NAN, 5, Quality.BAD))); // gap between 5 and 20
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setActivationControl(true);
		scheduleSum.setIgnoreGaps(true);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		
		SampledValue sv = target.getValue(0);
		Assert.assertEquals("Schedule sum incorrect", 10, sv.getValue().getFloatValue(), 0.01F); // sched1 + sched2 value
		
		sv = target.getValue(5);
		Assert.assertEquals("Schedule sum incorrect", 10, sv.getValue().getFloatValue(), 0.01F); // sched1 value
		
		sv = target.getValue(10);
		Assert.assertEquals("Schedule sum incorrect", 20, sv.getValue().getFloatValue(), 0.01F); // sched1 value
		
		sv = target.getValue(20);
		Assert.assertEquals("Schedule sum incorrect", 25, sv.getValue().getFloatValue(), 0.01F); // sched1 + sched2 value
		
		tool.deleteAllConfigurations();
		f.delete();
	}
	
	@Test
	public void incrementalUpdateWorks1() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(20, new FloatValue(10));
		sched2.addValue(0, new FloatValue(0));
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setIncrementalUpdate(true);
		scheduleSum.setWaitForSchedules(false);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		
		listener.setLatch(new CountDownLatch(1));
		// overwrite target value; it should not be corrected later on, since incremental update is set to true
		target.addValue(0, new FloatValue(100)); 
		Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
		listener.setLatch(new CountDownLatch(1));
		sched2.addValue(50, new FloatValue(200));
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		
		SampledValue sv = target.getValue(50);
		Assert.assertNotNull("Schedule sum: point missing",sv);
		Assert.assertEquals("Schedule sum incorrect", 210, sv.getValue().getFloatValue(), 0.01F); // sched1 + sched2 value
		
		sv = target.getValue(0);
		Assert.assertEquals("Schedule sum incorrect", 100, sv.getValue().getFloatValue(), 0.01F); // modified value
		
		tool.deleteAllConfigurations();
		f.delete();
	}
	
	@Test
	public void incrementalUpdateWorks2() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(20, new FloatValue(10));
		sched2.addValue(0, new FloatValue(0));
		sched2.addValue(20, new FloatValue(15));
		sched2.addValue(50, new FloatValue(25));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setIncrementalUpdate(true);
		scheduleSum.setWaitForSchedules(false);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		
		SampledValue sv = target.getValue(50);
		Assert.assertNotNull("Schedule sum: point missing",sv);
		Assert.assertEquals("Schedule sum incorrect", 35, sv.getValue().getFloatValue(), 0.01F); // sched1 + sched2 value
		
		listener.setLatch(new CountDownLatch(1));
		sched1.addValue(30, new FloatValue(20)); // now we add a value for a timestamp for which the sum has been calculated already
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		sv = target.getValue(30);
		Assert.assertNotNull("Schedule sum: point missing",sv);
		Assert.assertEquals("Schedule sum incorrect", 35, sv.getValue().getFloatValue(), 0.01F); // sched1 + sched2 value
		
		tool.deleteAllConfigurations();
		f.delete();
	}
	
	@Test
	public void nonIncrementalUpdateWorks1() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(20, new FloatValue(10));
		sched2.addValue(0, new FloatValue(0));
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setIncrementalUpdate(false);
		scheduleSum.setWaitForSchedules(false);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		
		listener.setLatch(new CountDownLatch(1));
		// overwrite target value; it should be corrected later on, since incremental update is set to false
		target.addValue(0, new FloatValue(100)); 
		Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
		listener.setLatch(new CountDownLatch(1));
		sched2.addValue(50, new FloatValue(200));
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		
		SampledValue sv = target.getValue(50);
		Assert.assertNotNull("Schedule sum: point missing",sv);
		Assert.assertEquals("Schedule sum incorrect", 210, sv.getValue().getFloatValue(), 0.01F); // sched1 + sched2 value
		
		sv = target.getValue(0);
		Assert.assertEquals("Schedule sum incorrect", 10, sv.getValue().getFloatValue(), 0.01F); // modified value should have been corrected
		
		tool.deleteAllConfigurations();
		f.delete();
	}
	
	@Test
	public void waitForSchedulesWorks() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(30, new FloatValue(10));
		sched2.addValue(0, new FloatValue(0));
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setWaitForSchedules(true); // default anyway
		scheduleSum.setIgnoreGaps(true);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		
		Assert.assertNotNull(target.getValue(20));
		Assert.assertEquals(Quality.GOOD, target.getValue(20).getQuality());
		Assert.assertTrue("Schedule sum contains unwanted values",target.isEmpty(21, Long.MAX_VALUE));
		
		listener.setLatch(new CountDownLatch(1));
		sched2.addValue(50, new FloatValue(14));
		Assert.assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertFalse("Schedule sum missing values",target.isEmpty(21, Long.MAX_VALUE));
		final SampledValue newValue = target.getValue(30);
		Assert.assertNotNull(newValue);
		Assert.assertEquals(Quality.GOOD, newValue.getQuality());
		Assert.assertEquals("Schedule sum value incorrect",25, target.getValue(30).getValue().getFloatValue(), 0.1F);
		Assert.assertTrue("Schedule sum contains unwanted values",target.isEmpty(31, Long.MAX_VALUE));
		
		tool.deleteAllConfigurations();
		f.delete();
	}
	
	@Test
	public void waitForSchedulesWorks2() throws InterruptedException {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		final FloatResource f = resman.createResource(newResourceName(), FloatResource.class);
		final AbsoluteSchedule sched1 = f.getSubResource("schedule1", AbsoluteSchedule.class).create();
		final AbsoluteSchedule sched2 = f.getSubResource("schedule2", AbsoluteSchedule.class).create();
		final AbsoluteSchedule target = f.getSubResource("target", AbsoluteSchedule.class).create();
		sched1.setInterpolationMode(InterpolationMode.STEPS);
		sched2.setInterpolationMode(InterpolationMode.STEPS);
		sched1.activate(false);
		sched2.activate(false);
		target.activate(false);
		sched1.addValue(0, new FloatValue(10));
		sched1.addValue(10, new FloatValue(20));
		sched1.addValue(30, new FloatValue(10));
		sched2.addValue(0, new FloatValue(0));
		sched2.addValue(20, new FloatValue(15));
		final SumValueListener listener = new SumValueListener();
		target.addValueListener(listener);
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(Arrays.<Schedule> asList(sched1,sched2), target);
		scheduleSum.setWaitForSchedules(false);
		scheduleSum.setIgnoreGaps(true);
		scheduleSum.commit();
		
		Assert.assertTrue("Sum callback missing",listener.latch.await(5, TimeUnit.SECONDS));
		Assert.assertTrue(target.isActive());
		
		Assert.assertNotNull(target.getValue(20));
		Assert.assertEquals(Quality.GOOD, target.getValue(20).getQuality());
		Assert.assertFalse("Schedule sum missing values",target.isEmpty(21, Long.MAX_VALUE));
		
		tool.deleteAllConfigurations();
		f.delete();
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInputSchedReferencesOutputSched() {
		final ResourceManipulator tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
		
		List<Schedule> inputs = new ArrayList<>();
		for(int i = 0; i < 3; i++) {
			AbsoluteSchedule floatSchedule = createAndActivateFloatSchedule();
			assertTrue(floatSchedule.isActive());
			inputs.add(floatSchedule);
		}
		
		int rnd = new Random().nextInt(3);
		Schedule out = createScheduleAsReference(inputs.get(rnd));
		assertTrue(out.getLocation().equals(inputs.get(rnd).getLocation()));
		assertTrue(out.isActive());
		
		ScheduleSum scheduleSum = tool.createConfiguration(ScheduleSum.class);
		scheduleSum.setAddends(inputs, out);
		scheduleSum.setDelay(1000);
		assertFalse(scheduleSum.commit());
		
	}

	private int scheduleCounter = 0;

	private AbsoluteSchedule createAndActivateFloatSchedule() {
		String name = "testfloat" + scheduleCounter++;
		final FloatResource result = resman.createResource(name, FloatResource.class);
		result.program().create();
		result.activate(true);
		return result.program();
	}

	private AbsoluteSchedule createScheduleAsReference(Schedule ref) {
		String name = "testfloat" + scheduleCounter++;
		final FloatResource result = resman.createResource(name, FloatResource.class);
		result.program().setAsReference(ref);
		result.activate(true);
		return result.program();
	}

	private static class SumValueListener implements ResourceValueListener<Schedule> {

		private volatile CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void resourceChanged(Schedule resource) {
			latch.countDown();
		}

		private void setLatch(CountDownLatch latch) {
			this.latch = latch;
		}
	}
}
