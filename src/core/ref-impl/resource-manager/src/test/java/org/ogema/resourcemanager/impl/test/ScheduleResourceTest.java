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
package org.ogema.resourcemanager.impl.test;

import org.ogema.exam.DemandTestListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Tests the m_floatSchedule resources.
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
@ExamReactorStrategy(PerClass.class)
public class ScheduleResourceTest extends OsgiTestBase {

	public static final String RESNAME = ScheduleResourceTest.class.getSimpleName();
	private final Map<Class<? extends Resource>, Schedule> m_schedules = new HashMap<>();

	/**
	 * Creates a simple resource of the given class with an auto-generated name and returns a handle to the resource.
	 *
	 * @param clazz
	 *            A simple resource type defining the resource type to use.
	 * @return Returns the resource that has been created.
	 */
	private Resource createSimpleResource(Class<? extends Resource> clazz) {
		final String name = newResourceName();
		final Resource simple = resMan.createResource(name, clazz);
		assertNotNull(simple);
		// if (Math.random()<0.5)
		simple.activate(true);
		return simple;
	}

	/**
	 * Returns the test m_floatSchedule used in this test. Creates it, if it does not exist, yet.
	 *
	 * @return
	 */
	Schedule getSchedule(Class<? extends Resource> primitiveClass) {
		if (m_schedules.containsKey(primitiveClass)) {
			return m_schedules.get(primitiveClass);
		}
		final Resource simple = createSimpleResource(primitiveClass);
		final Schedule schedule = simple.addDecorator("schedule", Schedule.class);
		m_schedules.put(primitiveClass, schedule);
		return schedule;
	}

	@After
	public void tearDown() {
		for (Resource r : m_schedules.values()) {
			r.delete();
		}
		m_schedules.clear();
	}

	/**
	 * Empties the test m_floatSchedule and returns it. If the test schedule did not exist yet, a new m_floatSchedule is
	 * being created.
	 *
	 * @return
	 */
	Schedule getEmptySchedule(Class<? extends Resource> primitiveType) {
		Schedule result = getSchedule(primitiveType);
		result.deleteValues(0, Long.MAX_VALUE);
		return result;
	}

	/**
	 * Creates a list of sampled values at t = t0, t0+delta, t0+2delta, ... for all t < t1. The values are calcualted
	 * according to f=mt+b. All qualities are good.
	 */
	List<SampledValue> getFloatInterval(long t0, long t1, long delta, float m, float b) {
		List<SampledValue> result = new LinkedList<>();
		for (long t = t0; t < t1; t += delta) {
			Value value = new FloatValue(m * t + b);
			result.add(new SampledValue(value, t, Quality.GOOD));
		}
		return result;
	}

	/**
	 * Tests the creation of schedules.
	 */
	@Test
	public void testCreateSchedules() {
		final FloatResource simple = (FloatResource) createSimpleResource(FloatResource.class);

		{
			final String forecastName = "myTestForecast";
			Schedule schedule = simple.addDecorator(forecastName, Schedule.class);
			assertNotNull(schedule);
			assertNotNull("forecast schedule not created", simple.getSubResource(forecastName));
			assertEquals(forecastName, simple.getSubResource(forecastName).getName());
			final Schedule schedule1 = (Schedule) simple.getSubResource(forecastName);
			final Schedule schedule2 = (Schedule) simple.getSubResource(forecastName);
			assert schedule1.equalsPath(schedule2);
			Class<?> scheduleClass = simple.getSubResource(forecastName).getResourceType();
			assertEquals(Schedule.class, scheduleClass);
			assertEquals(schedule.getSubResources(false).size(), 0);
			assertEquals(schedule.getSubResources(true).size(), 0);
			List<Resource> subres = schedule.getDirectSubResources(false);
			assertEquals(subres.size(), 0);
			assertEquals(schedule.getDirectSubResources(true).size(), 0);
		}

		{
			final String definitionName = "definition";
			Schedule schedule = simple.addDecorator(definitionName, Schedule.class);
			assertNotNull(schedule);
			assertNotNull("definition schedule not created", simple.getSubResource(definitionName));
			assertEquals(definitionName, simple.getSubResource(definitionName).getName());
			Class<?> scheduleClass = simple.getSubResource(definitionName).getResourceType();
			assertEquals(Schedule.class, scheduleClass);
			assertEquals(schedule.getSubResources(false).size(), 0);
			assertEquals(schedule.getSubResources(true).size(), 0);
			assertEquals(schedule.getDirectSubResources(false).size(), 0);
			assertEquals(schedule.getDirectSubResources(true).size(), 0);
		}
	}

	@Test
	public void testSetInterpolation() {
		Schedule schedule = getEmptySchedule(FloatResource.class);

		schedule.setInterpolationMode(InterpolationMode.LINEAR);
		assertEquals(InterpolationMode.LINEAR, schedule.getInterpolationMode());
		schedule.setInterpolationMode(InterpolationMode.STEPS);
		assertEquals(InterpolationMode.STEPS, schedule.getInterpolationMode());
		schedule.setInterpolationMode(InterpolationMode.NEAREST);
		assertEquals(InterpolationMode.NEAREST, schedule.getInterpolationMode());
		schedule.setInterpolationMode(InterpolationMode.NONE);
		assertEquals(InterpolationMode.NONE, schedule.getInterpolationMode());
	}

	@Test
	public void testReadWriteFloatSchedules() throws ResourceException {

		final Schedule schedule = getEmptySchedule(FloatResource.class);

		// test adding values.
		final long MAX = 100, STEP = 10;
		for (long t = 0; t <= MAX; t += STEP) {
			final Value value = new FloatValue((float) t);
			schedule.addValue(t, value, t);
		}
		assertEquals(MAX, (long) schedule.getLastCalculationTime());

		schedule.setInterpolationMode(InterpolationMode.LINEAR);
		assertEquals(InterpolationMode.LINEAR, schedule.getInterpolationMode());

		// test correctness of values, both exact and interpolated
		for (long t = 0; t < MAX; t += STEP) {
			final float exactValue = schedule.getValue(t).getValue().getFloatValue();
			final float interpolatedValue = schedule.getValue(t + STEP / 2).getValue().getFloatValue();
			assertEquals(exactValue, (float) t, 0.01f);
			assertEquals(interpolatedValue, (float) t + 0.5f * STEP, 0.01f);
		}

		SampledValue outOfBoundsValue = schedule.getValue(MAX + 1);
        assertNull(outOfBoundsValue);
	}

	@Test
	public void testReadWriteBooleanSchedules() throws ResourceException {

		final Schedule schedule = getEmptySchedule(BooleanResource.class);

		// test adding values.
		final long MAX = 100, STEP = 10;
		for (long t = 0; t <= MAX; t += STEP) {
			final Value value = new BooleanValue(t % (2 * STEP) == 0);
			schedule.addValue(t, value, t);
		}
		assertEquals(MAX, (long) schedule.getLastCalculationTime());

		schedule.setInterpolationMode(InterpolationMode.LINEAR);
		assertEquals(InterpolationMode.LINEAR, schedule.getInterpolationMode());

		for (long t = 0; t < MAX; t += STEP) {
			final boolean exactValue = t % (2 * STEP) == 0;
			final boolean interpolatedValue = schedule.getValue(t + STEP / 2 - 1).getValue().getBooleanValue();
			assertEquals(exactValue, interpolatedValue);
		}
		for (long t = 0; t < MAX; t += STEP) {
			final boolean exactValue = t % (2 * STEP) != 0;
			final boolean interpolatedValue = schedule.getValue(t + STEP / 2 + 2).getValue().getBooleanValue();
			assertEquals(exactValue, interpolatedValue);
		}

		SampledValue outOfBoundsValue = schedule.getValue(MAX + 1);
		assertNull(outOfBoundsValue);
	}

	@Test
	public void testReadWriteLongSchedules() throws ResourceException {

		final Schedule schedule = getEmptySchedule(TimeResource.class);

		// test adding values.
		final long MAX = 100, STEP = 10;
		for (long t = 0; t <= MAX; t += STEP) {
			final Value value = new LongValue(t);
			schedule.addValue(t, value, t);
		}
		assertEquals(MAX, (long) schedule.getLastCalculationTime());

		schedule.setInterpolationMode(InterpolationMode.LINEAR);
		assertEquals(InterpolationMode.LINEAR, schedule.getInterpolationMode());

		// test correctness of values, both exact and interpolated
		for (long t = 0; t < MAX; t += STEP) {
			final SampledValue value = schedule.getValue(t);
			final long exactValue = value.getValue().getLongValue();
			final long interpolatedValue = schedule.getValue(t + STEP / 2).getValue().getLongValue();
			assertEquals(exactValue, t);
			assertEquals(interpolatedValue, t + STEP / 2);
		}

		SampledValue outOfBoundsValue = schedule.getValue(MAX + 1);
		assertNull(outOfBoundsValue);
	}

	@Test
	public void testReadWriteIntegerSchedules() throws ResourceException {

		final Schedule schedule = getEmptySchedule(IntegerResource.class);

		// test adding values.
		final long MAX = 100, STEP = 10;
		for (long t = 0; t <= MAX; t += STEP) {
			final Value value = new IntegerValue((int) t);
			schedule.addValue(t, value, t);
		}
		assertEquals(MAX, (long) schedule.getLastCalculationTime());

		schedule.setInterpolationMode(InterpolationMode.LINEAR);
		assertEquals(InterpolationMode.LINEAR, schedule.getInterpolationMode());

		// test correctness of values, both exact and interpolated
		for (long t = 0; t < MAX; t += STEP) {
			final int exactValue = schedule.getValue(t).getValue().getIntegerValue();
			final int interpolatedValue = schedule.getValue(t + STEP / 2).getValue().getIntegerValue();
			assertEquals(exactValue, (int) t);
			assertEquals(interpolatedValue, (int) (t + STEP / 2));
		}

		SampledValue outOfBoundsValue = schedule.getValue(MAX + 1);
		assertNull(outOfBoundsValue);
	}

	@Test
	public void testReadWriteStringSchedules() throws ResourceException {

		final Schedule schedule = getEmptySchedule(StringResource.class);

		// test adding values.
		final long MAX = 100, STEP = 10;
		for (long t = 0; t <= MAX; t += STEP) {
			final Value value = new StringValue((new Long(t)).toString());
			schedule.addValue(t, value, t);
		}
		assertEquals(MAX, (long) schedule.getLastCalculationTime());

		schedule.setInterpolationMode(InterpolationMode.NEAREST);
		assertEquals(InterpolationMode.NEAREST, schedule.getInterpolationMode());

		for (long t = 0; t < MAX; t += STEP) {
			final String exactValue = (new Long(t)).toString();
			final String scheduleValue = schedule.getValue(t + STEP / 2 - 1).getValue().getStringValue();
			assert exactValue.equals(scheduleValue);
		}
		for (long t = 0; t < MAX; t += STEP) {
			final String exactValue = (new Long(t + STEP)).toString();
			final String scheduleValue = schedule.getValue(t + STEP / 2 + 2).getValue().getStringValue();
			assert exactValue.equals(scheduleValue);
		}
	}

	/**
	 * Test the equality of two lists in the sense of their entries being identical value-wise.
	 */
	void testEquality(List<SampledValue> l1, List<SampledValue> l2) {
		assertEquals(l1.size(), l2.size());
		for (int i = 0; i < l1.size(); ++i) {
			SampledValue value = l1.get(i);
			SampledValue scheduleValue = l2.get(i);
			assertEquals(value.getTimestamp(), scheduleValue.getTimestamp());
			assertEquals(value.getValue().getFloatValue(), scheduleValue.getValue().getFloatValue(), 0.001f);
			assertEquals(value.getQuality(), scheduleValue.getQuality());
		}

	}

	@Test
	public void testInsertionDeletion() {
		Schedule schedule = getEmptySchedule(FloatResource.class);
		final long N = 26; // must be an even number for this test.
		final long min = 0, delta = 500, max = min + N * delta;

		List<SampledValue> values, scheduleValues;

		// test inserting an re-reading of data.
		values = getFloatInterval(min, max, delta, 1.f, 0.f);
		schedule.addValues(values);
		scheduleValues = schedule.getValues(min);
		testEquality(values, scheduleValues);

		// test re-inserting different set of values in the same interval.
		values = getFloatInterval(min, max, 2 * delta, -4.f, 100.f);
		schedule.replaceValues(min, max, values);
		scheduleValues = schedule.getValues(min, max);
		testEquality(values, scheduleValues);

		// test emptying the schedule
		schedule.deleteValues();
		scheduleValues = schedule.getValues(0);
		assertEquals(scheduleValues.size(), 0);
	}

	@Test
	public void testAddValueSchedule() {
		Schedule schedule = getEmptySchedule(FloatResource.class);
		final long N = 73; // must be an even number for this test.
		@SuppressWarnings("unused")
		final long min = 0, delta = 100000, max = min + N * delta;

		final List<Value> values = new ArrayList<>();
		for (int i = 0; i < N; ++i) {
			values.add(new FloatValue(1.f * i));
		}

		schedule.replaceValuesFixedStep(min, values, delta, 6232);
//                        addValueSchedule(min, delta, values, 6232);
		assertEquals(N, schedule.getValues(0).size());
		values.add(new FloatValue(652.f));
		schedule.replaceValuesFixedStep(min, values, delta, 6232);
		assertEquals(N + 1, schedule.getValues(0).size());
	}

	@Test
	public void addValuesWorks() {
		Schedule sched = getEmptySchedule(FloatResource.class);
		List<SampledValue> values1 = new ArrayList<>();
		values1.add(new SampledValue(new FloatValue(1f), 0, Quality.GOOD));
		values1.add(new SampledValue(new FloatValue(2f), 1, Quality.GOOD));

		sched.addValues(values1);
		List<SampledValue> entries = sched.getValues(0);
		assertEquals(values1, entries);
	}

	@Test
	public void replaceValuesWorks() {
		Schedule sched = getEmptySchedule(FloatResource.class);
		List<SampledValue> values1 = new ArrayList<>();
		values1.add(new SampledValue(new FloatValue(1f), 0, Quality.GOOD));
		values1.add(new SampledValue(new FloatValue(2f), 1, Quality.GOOD));
		sched.addValues(values1);
		assertEquals(values1, sched.getValues(0));

		// replace individually
		List<SampledValue> values2 = new ArrayList<>();
		values2.add(new SampledValue(new FloatValue(3f), 0, Quality.GOOD));
		values2.add(new SampledValue(new FloatValue(4f), 1, Quality.GOOD));
		Assert.assertFalse(values2.equals(sched.getValues(0)));

		for (SampledValue value : values2) {
			sched.addValue(value.getTimestamp(), value.getValue());
		}
		assertEquals(values2, sched.getValues(0));

		// replace collectively
		List<SampledValue> values3 = new ArrayList<>();
		values3.add(new SampledValue(new FloatValue(3f), 0, Quality.GOOD));
		values3.add(new SampledValue(new FloatValue(4f), 1, Quality.GOOD));

		sched.addValues(values3);
		assertEquals(values3, sched.getValues(0));
	}

	@Test
	public void replacingValuesInARangeWorks() throws Exception {
		FloatResource fr = resMan.createResource("scheduleTest_" + System.currentTimeMillis(), FloatResource.class);
		Schedule schedule = fr.addDecorator("data", Schedule.class);
		schedule.addValue(1, new FloatValue((41)));
		schedule.addValue(2, new FloatValue((42)));
		schedule.addValue(3, new FloatValue((43)));

		assertEquals(3, schedule.getValues(0).size());
		assertEquals(42, schedule.getValue(2).getValue().getIntegerValue());
		schedule.replaceValues(2, 3, Arrays.asList(new SampledValue(new FloatValue(2), 2, Quality.GOOD)));
		assertEquals(3, schedule.getValues(0).size());
		assertEquals(2, schedule.getValue(2).getValue().getIntegerValue());
	}

	/*
	 * Tests that callbacks are received for newly created schedules.
	 */
	@Test
	public void resourceDemandCallbackWorks() throws InterruptedException {
		final Schedule schedule = getSchedule(FloatResource.class);

		DemandTestListener<Schedule> listener = new DemandTestListener<>(schedule);

		resAcc.addResourceDemand(Schedule.class, listener);
		schedule.activate(true);
		assertTrue("did not receive resource available callback", listener.awaitAvailable());
		resAcc.removeResourceDemand(Schedule.class, listener);
		// TODO check for the other way round (currently works)
		// TODO check for Schedule and Schedule.
	}

	/*
	 * Checks that a callback is received when a value in a schedule is updated.
	 */
	@Test
	public void addValueCallbackWorks() throws InterruptedException {
		final CountDownLatch callbackCount = new CountDownLatch(1);
		final ResourceValueListener<Resource> listener = new ResourceValueListener<Resource>() {

			@Override
			public void resourceChanged(Resource resource) {
				callbackCount.countDown();
			}
		};

		final Schedule schedule = getSchedule(FloatResource.class);
		schedule.activate(true);
		schedule.addValueListener(listener, true);

		schedule.addValue(26, new FloatValue(4.3f));
		assertTrue("did not receive update callback", callbackCount.await(5, TimeUnit.SECONDS));
	}
    
	/**
	 * Minimal test for checking that getNextValue works on empty schedules.
	 */
	@Test
	public void getNextValueWorksOnEmptySchedule() {
		final Schedule schedule = getEmptySchedule(FloatResource.class);
		schedule.activate(true);
		SampledValue value = schedule.getNextValue(0);
		assert (value == null);
	}

	@Test
	public void addDecoratorWithReferenceReplacesExistingElement() {
		FloatResource test1 = resMan.createResource(RESNAME + counter++, FloatResource.class);
		FloatResource test2 = resMan.createResource(RESNAME + counter++, FloatResource.class);
		Resource a = test1.addDecorator("schedule", Schedule.class);
		Resource b = test2.addDecorator("schedule", Schedule.class);
		assert !a.equalsLocation(b);
		test2.addDecorator("schedule", a);
		assert a.equalsLocation(test2.getSubResource("schedule"));
	}

	@Test
	public void testScheduleListener() {
		final Schedule schedule = getEmptySchedule(FloatResource.class);
		final int N = 10;
		schedule.activate(true);
		final CountDownLatch latch = new CountDownLatch(1);
		schedule.addValueListener(new ResourceValueListener<Resource>() {

			@Override
			public void resourceChanged(Resource resource) {
				latch.countDown();
			}
		}, false);

		final List<Value> values = new ArrayList<>();
		for (int i = 0; i < N; ++i) {
			values.add(new FloatValue(1.f * i));
		}

                schedule.replaceValuesFixedStep(0, values, 3600);

		// wait for listener callback
		try {
			Assert.assertTrue("resourceChanged for schedule not called!", latch.await(5, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	private void fillHistoricalScheduleWithLogData(FloatResource fr) throws InterruptedException {  // adds maximum 100 log data points, but typically much less (1-10)
		RecordedData rd = fr.getHistoricalData();
		RecordedDataConfiguration rdc = new RecordedDataConfiguration();
		rdc.setFixedInterval(20);  // log every ms
		rdc.setStorageType(StorageType.FIXED_INTERVAL);
		rd.setConfiguration(rdc); // start logging
		System.out.println("  Logging started");
		fr.activate(true);
		Thread.sleep(200);  // 
		rd.setConfiguration(null); // stop logging
		System.out.println("  Logging stopped");	
		Thread.sleep(100);
	}
	
	private void fillHistoricalScheduleWithExplicitData(FloatResource fr) throws InterruptedException {  // adds 100 explicit data points, starting in 1ms from now
		List<SampledValue> artificialData = new LinkedList<SampledValue>();
		long now =getApplicationManager().getFrameworkTime();
		for (int i=0;i<100;i++) {
			SampledValue sv= new SampledValue(new FloatValue(17*i), now + i+1, Quality.GOOD);
			artificialData.add(sv);
		}
		fr.historicalData().addValues(artificialData);
	}
	
	@Test 
	public void testHistoricalSchedule() throws InterruptedException {
		FloatResource fr = resMan.createResource("testFloat", FloatResource.class);
		fr.historicalData().create();
		assert(fr.historicalData().getClass().getSimpleName().equals("HistoricalSchedule")) : "historical data is not a HistoricalSchedule";
		fr.setValue(17);
		fillHistoricalScheduleWithLogData(fr);
		List<SampledValue> historicalValues = fr.historicalData().getValues(0);
		System.out.println("  There are " + historicalValues.size() + " log data values out of expected 100. Time difference: "
				+ (historicalValues.get(historicalValues.size()-1).getTimestamp() -historicalValues.get(0).getTimestamp()) );
		fillHistoricalScheduleWithExplicitData(fr);
		historicalValues = fr.historicalData().getValues(0);
		System.out.println("  There are " + historicalValues.size() + " historical values out of expected 200. Time difference: "
				+ (historicalValues.get(historicalValues.size()-1).getTimestamp() -historicalValues.get(0).getTimestamp()) );
		assert(historicalValues.size() > 101) : "Logged values not accessible via historical data"; // actually we would expect 200 values here, 
						// but it is not realistic to log a value every ms. Here we only demand that 2 values within 200ms can be logged; may still fail on very slow machines
		fr.delete();
	}
	

	@Test
	public void historicalScheduleWorksWithReferences() {
		FloatResource fr1 = resMan.createResource("testFloat1", FloatResource.class);
		FloatResource fr2 = resMan.createResource("testFloat2", FloatResource.class);
		fr1.historicalData().create();
		fr1.program().create();
		fr2.program().setAsReference(fr1.historicalData());
		fr2.historicalData().setAsReference(fr1.program());
		System.out.println("  Classes: program: " + fr2.program().getClass().getSimpleName() + ", historicalData: " + fr2.historicalData().getClass().getSimpleName());
//		assert (fr2.program() instanceof HistoricalSchedule) : "Reference to HistoricalSchedule is not itself a HistoricalSchedule";						// not possible
//		assert (!(fr2.historicalData() instanceof HistoricalSchedule)) : "Reference to a non-HistoricalSchdedule unexpectedly is a HistoricalSchedule";		// not possible
		assert (fr2.program().getClass().getSimpleName().equals("HistoricalSchedule")) : "Reference to HistoricalSchedule is not itself a HistoricalSchedule";						
		assert (!(fr2.historicalData().getClass().getSimpleName().equals("HistoricalSchedule"))) : "Reference to a non-HistoricalSchdedule unexpectedly is a HistoricalSchedule";
		fr1.delete();
		fr2.delete();
	}
	
//	@Ignore  // FIXME logging problem? Sometimes fr.historicalData().getValue() does not get access to logged data
	@Test
	public void historicalScheduleInterpolationModeWorks() throws InterruptedException {
		FloatResource fr = resMan.createResource("testFloat1", FloatResource.class);
		fr.historicalData().create();
		fr.historicalData().setInterpolationMode(InterpolationMode.LINEAR);
		fillHistoricalScheduleWithLogData(fr);
		List<SampledValue> values = fr.historicalData().getValues(0);
		assert (values.size() > 0) : "HistoricalSchedule found empty, although log data should be available";
		long lastTimeStamp = values.get(values.size() -1).getTimestamp();
//		System.out.println("    Last logging time stamp is " + lastTimeStamp + ", nr values: " + values.size());
		Thread.sleep(400);
		fillHistoricalScheduleWithExplicitData(fr);
		long midTime = lastTimeStamp + 200;
		values = fr.historicalData().getValues(0);
		SampledValue sv = fr.historicalData().getValue(midTime); 
//		System.out.println("             Mid time stamp is " + midTime+ ", nr values: " + values.size());
//		System.out.println("        last overall timestamp " + values.get(values.size()-1).getTimestamp());
		assert (sv != null && sv.getQuality() == Quality.GOOD) : "Found invalid historical data point, despite Interpolation mode LINEAR: " + sv;
		System.out.println("  HistoricalSchedule.getValue() works fine with Interpolation mode LINEAR. " + sv);
		fr.historicalData().setInterpolationMode(InterpolationMode.NONE);
		sv = fr.historicalData().getValue(midTime); 
		assert (sv == null || sv.getQuality() == Quality.BAD) : "Found valid historical data point, despite Interpolation mode NONE";
		System.out.println("  HistoricalSchedule.getValue() works fine with Interpolation mode NONE. " + sv);
		fr.delete();		
	}
	
	@Test
	public void explicitDataTakesPrecedenceOverLogData() throws InterruptedException {
		FloatResource fr = resMan.createResource("testFloat1", FloatResource.class);
		fr.historicalData().create();
		fr.setValue(17);
		fr.activate(true);
		fr.historicalData().setInterpolationMode(InterpolationMode.LINEAR);
		fillHistoricalScheduleWithLogData(fr);
		List<SampledValue> values = fr.historicalData().getValues(0);
		assert (values.size() > 0) : "HistoricalSchedule found empty, although log data should be available";
		long lastTimeStamp = values.get(values.size() -1).getTimestamp();
		int sz = values.size();
		fr.historicalData().addValue(lastTimeStamp,new FloatValue(19)); // override last log data
		values = fr.historicalData().getValues(0);
		assert (values.size() == sz) : "Size of historicalData schedule has changed, although only a value has been replaced";
		float val = values.get(values.size()-1).getValue().getFloatValue();
		assertEquals("Unexpected value.",19,val,0.5F );
		System.out.println("  Expected value found " + val);
		fr.delete();		
	}
	
	@Test 
	public void deletedScheduleDoesNotRetainValues() {
		String path = newResourceName();
		FloatResource fr = resMan.createResource(path, FloatResource.class);
		fr.program().create();
		long t0 = System.currentTimeMillis();
		List<SampledValue> values = new ArrayList<>();
		for (int i=0;i<50;i++) {
			SampledValue sv = new SampledValue(new FloatValue((float) Math.random()), t0 + i*100, Quality.GOOD);
			values.add(sv);
		}
		fr.program().addValues(values);
		fr.delete();
		fr = resMan.createResource(path, FloatResource.class);
		fr.program().create();
		Assert.assertEquals("Zombie schedule survived deletion.",0, fr.program().getValues(0).size());
		fr.delete();
	}
	
	@Test 
	public void historicalScheduleValuesSurviveDeletion() throws InterruptedException {
		// this test has a problem with persistent log data that is not cleaned up between tests -> use random path postfix to make it unique
		String path = newResourceName() + ((int) (Math.random() * 100000)); 
		FloatResource fr = resMan.createResource(path, FloatResource.class);
		fr.historicalData().create();
		fr.activate(true);
		RecordedData rc = fr.getHistoricalData();
		RecordedDataConfiguration rcd = new RecordedDataConfiguration();
		rcd.setFixedInterval(10);
		rcd.setStorageType(StorageType.FIXED_INTERVAL);
		rc.setConfiguration(rcd);
		Thread.sleep(1000); // wait for some log data to be generated
		int nr = fr.historicalData().getValues(0).size();
		Assert.assertTrue("Unexpectedly low number of log data points: " + nr + ", expected: " + 100,nr > 3); 
		fr.historicalData().delete();
		fr.historicalData().create();
		int newNr = fr.historicalData().getValues(0).size();
		Assert.assertTrue("Seems like deleting historicalData schedule removed log actual data; old size: " + nr + ", new size: " + newNr,newNr*1.5 > nr);
		System.out.println("Old log data points: " + nr + ", new: " + newNr);
		fr.delete();
	}

}
