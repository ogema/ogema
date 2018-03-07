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

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ValueTestListener;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ScheduleManagement;
import org.ogema.tools.resourcemanipulator.schedulemgmt.DeletionAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.InterpolationAction;
import org.ogema.tools.resourcemanipulator.schedulemgmt.TimeSeriesReduction;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class) 
public class ScheduleManagementTest extends OsgiAppTestBase {

	// values must not be changed randomly
	private static final long DIFFERENCE = 60000 * 10; // 1h
	private static final long DELTA= 1000; // 1s
	private static long NR_POINTS = DIFFERENCE / DELTA;
	private volatile AbsoluteSchedule floatSchedule;
	
	public ScheduleManagementTest() {
		super(true);
	}
	
	@Before
	public void createSchedule() {
		FloatResource base = getApplicationManager().getResourceManagement()
				.createResource(newResourceName(), FloatResource.class);
		floatSchedule = base.program().create();
		long current = getApplicationManager().getFrameworkTime();
		List<SampledValue> vals = new ArrayList<SampledValue>((int) NR_POINTS);
		for (int i = 0; i < NR_POINTS; i++) {
			SampledValue sv = new SampledValue(new FloatValue((float) Math.random()), 
					current - DIFFERENCE + i*DELTA, Quality.GOOD);
			vals.add(sv);
		}
		floatSchedule.addValues(vals);
		base.activate(true);
	}
	
	@After 
	public void cleanUp() {
		floatSchedule.delete();
	}

	@Test
	public void deletePointsWorks() throws InterruptedException {
		ValueTestListener<Schedule> listener = new ValueTestListener<Schedule>(getApplicationManager());
		floatSchedule.addValueListener(listener);
		ResourceManipulator rm = new ResourceManipulatorImpl(getApplicationManager());
		rm.start();
		ScheduleManagement sm  = rm.createConfiguration(ScheduleManagement.class);
		NavigableMap<Long, TimeSeriesReduction> actions = new TreeMap<Long, TimeSeriesReduction>();
		actions.put(DIFFERENCE / 2, new DeletionAction(getApplicationManager()));
		sm.manageSchedule(floatSchedule, actions);
		System.out.println(" Initial schedule has " + floatSchedule.getValues(Long.MIN_VALUE).size() + " values");
		sm.commit();
		boolean event = listener.await();
		System.out.println(" Final schedule has " + floatSchedule.getValues(Long.MIN_VALUE).size() + " values");
		Assert.assertTrue("Missing resource changed event",event);
		Assert.assertTrue("Schedule points not deleted", floatSchedule.getValues(Long.MIN_VALUE).size() < NR_POINTS /2 + 2);
		floatSchedule.removeValueListener(listener);
	}
	
	@Ignore
	@Test
	public void interpolationWorks() throws InterruptedException {
		ValueTestListener<Schedule> listener = new ValueTestListener<Schedule>(getApplicationManager());
		floatSchedule.addValueListener(listener);
		ResourceManipulator rm = new ResourceManipulatorImpl(getApplicationManager());
		rm.start();
		ScheduleManagement sm  = rm.createConfiguration(ScheduleManagement.class);
		NavigableMap<Long, TimeSeriesReduction> actions = new TreeMap<Long, TimeSeriesReduction>();
		actions.put(1L, new InterpolationAction(10000, getApplicationManager())); 
		sm.manageSchedule(floatSchedule, actions);
		System.out.println(" Initial schedule has " + floatSchedule.getValues(Long.MIN_VALUE).size() + " values");
		sm.commit();
		boolean event = listener.await();
		System.out.println(" Final schedule has " + floatSchedule.getValues(Long.MIN_VALUE).size() + " values");
		Assert.assertTrue("Missing resource changed event",event);
		Assert.assertTrue("Schedule points not deleted", floatSchedule.getValues(Long.MIN_VALUE).size() < NR_POINTS /2 + 2);
		floatSchedule.removeValueListener(listener);
	}
	
}
