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

import static org.junit.Assert.*;
import static org.ogema.exam.ResourceAssertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceOperationException;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.core.resourcemanager.transaction.TransactionFuture;
import org.ogema.core.resourcemanager.transaction.WriteConfiguration;
import org.ogema.exam.StructureTestListener;
import org.ogema.exam.TestApplication;
import org.ogema.exam.ValueTestListener;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.ArrayTimeSeries;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

@ExamReactorStrategy(PerClass.class)
public class ResourceTransactionRollbackTest extends OsgiTestBase {
	
	private void failTransaction(ResourceTransaction transaction) {
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {
			/* provoked exception */
			return;
		} finally {
			spoiler.delete();
		}
		throw new AssertionError("Transaction failed to fail");
	}
	
	@Test
	public void valueRollbackWorks() {
		final FloatResource f1 = resMan.createResource(newResourceName(), FloatResource.class);
		final FloatResource f2 = resMan.createResource(newResourceName(), FloatResource.class);
		for (int i = 0; i < 10; ++i) {
			final ResourceTransaction transaction = resAcc.createResourceTransaction();
			final float value1a = (float) Math.random();
			final float value1b = (float) Math.random();
			final float value2a = (float) Math.random();
			final float value2b = (float) Math.random();
			f1.setValue(value1a);
			f2.setValue(value2a);
			transaction.setFloat(f1, value1b);
			transaction.setFloat(f2, value2b, WriteConfiguration.FAIL); // will cause transaction to fail
			TransactionFuture<Float> result1 = transaction.getFloat(f1);
			TransactionFuture<Float> result2 = transaction.getFloat(f2);
			try {
				transaction.commit();
			} catch (ResourceOperationException e) {/* expected */}
			assertEquals(f1.getValue(), value1a, 1.e-4f);
			assertEquals(f2.getValue(), value2a, 1.e-4f);
			boolean expectedException = false;
			try {
				@SuppressWarnings("unused")
				float val = Math.random() > 0.5 ? result1.getValue() : result2.getValue();
			} catch (IllegalStateException e) {
				expectedException = true;
			}
			assertTrue(expectedException);
		}
		f1.delete();
		f2.delete();
	}
	
	@Test
	public void activateAndDeactivateRollbacksWork() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		res2.location().room().create().activate(false);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.activate(res1);
		transaction.deactivate(res2.location().room());
		failTransaction(transaction);
		assertInactive(res1);
		assertActive(res2.location().room());
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void activateAndDeactivateRollbacksWorkRecursively() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		Resource sub1 = res1.battery().electricityConnection().currentSensor().reading().create();
		Resource sub2 = res2.location().room().create();
		Resource sub3 = res3.location().room().create();
		res3.activate(true);
		res1.location().room().setAsReference(sub3);
		res2.activate(true);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.activate(res1,true,true);
		transaction.deactivate(res2.location(),true);
		failTransaction(transaction);
		assertInactive(res1);
		assertInactive(sub1);
		assertActive(res2);
		assertActive(res2.location());
		assertActive(sub2);
		assertActive(sub3); // ensure a resource connected via references is not wrongly affected by the rollback 
		res1.delete();
		res2.delete();
		res3.delete();
	}
	
	/*
	 *********  Delete tests  ********
	 *
	 * delete rollback is quite tricky, therefore there are multiple tests, with increasing level of complexity
	 */
	
	@Test
	public void deleteRollbackWorksBasic() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource res2 = resMan.createResource(newResourceName(), FloatResource.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.setFloat(res2, (float) Math.random(), WriteConfiguration.FAIL); // will cause transaction to fail, resource is inactive
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1);
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void deleteRollbackWorksWithSubresources() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource res2 = resMan.createResource(newResourceName(), FloatResource.class);
		res1.location().room().co2Sensor().create();
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.setFloat(res2, (float) Math.random(), WriteConfiguration.FAIL); // will cause transaction to fail, resource is inactive
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1);
		assertExists(res1.location().room().co2Sensor());
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void deleteRollbackWorksWithReferencesBasic() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res4 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res5 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res6 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		res1.location().room().co2Sensor().create();
		res2.location().room().setAsReference(res1.location().room());
		res3.location().room().co2Sensor().setAsReference(res1.location().room().co2Sensor());
		res4.location().room().co2Sensor().create();
		res5.location().room().setAsReference(res4.location().room());
		res6.location().room().co2Sensor().setAsReference(res4.location().room().co2Sensor());
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1); // also deletes the reference targets for the res2 and res3 references
		transaction.delete(res2.location()); // delete the parent of the reference
		transaction.delete(res3.location().room().co2Sensor()); // delete the reference itself
		transaction.delete(res4.location().room());  // delete the reference target, respectively the parent of the reference target
		transaction.setFloat(spoiler, 1F, WriteConfiguration.FAIL); // will fail
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1.location().room().co2Sensor()); 
		assertExists(res2.location().room());
		assertLocationsEqual(res1.location().room(), res2.location().room());
		assertExists(res3.location().room().co2Sensor());
		assertLocationsEqual(res3.location().room().co2Sensor(), res1.location().room().co2Sensor());
		assertExists(res4.location().room().co2Sensor());
		assertExists(res5.location().room());
		assertExists(res6.location().room().co2Sensor());
		assertLocationsEqual(res5.location().room(), res4.location().room());
		assertLocationsEqual(res6.location().room().co2Sensor(), res4.location().room().co2Sensor());
		res1.delete();res2.delete();res3.delete();res4.delete();res5.delete();res6.delete();spoiler.delete();
	}
	
	@Test
	public void deleteRollbackWorksWithDoublyDeletedReferences() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		res1.location().room().create();
		String subresname = "justatestres";
		res1.getSubResource(subresname, Room.class).setAsReference(res1.location().room());
		res2.location().room().create();
		res3.location().room().setAsReference(res2.location().room());
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.delete(res3.location());
		transaction.delete(res2);
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1);
		assertExists(res2);
		assertExists(res3.location());
		assertExists(res1.location().room());
		assertExists(res1.getSubResource(subresname,Room.class));
		assertExists(res2.location().room());
		assertExists(res3.location().room());
		assertLocationsEqual(res1.location().room(), res1.getSubResource(subresname, Room.class));
		assertLocationsEqual(res3.location().room(), res2.location().room());
		res1.delete();res2.delete();res3.delete();spoiler.delete();
	}
	
	@Test
	public void deleteRollbackWorksWithTransitiveReferences1() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		res3.location().room().create();
		res2.location().room().setAsReference(res3.location().room());
		res1.location().room().setAsReference(res2.location().room());
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res2);
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res2);
		assertExists(res2.location().room());
		assertExists(res1.location().room());
		assertLocationsEqual(res2.location().room(), res3.location().room());
		assertLocationsEqual(res1.location().room(), res3.location().room());
		res1.delete();res2.delete();res3.delete();spoiler.delete();
	}
	
	// here a different resource is deleted, compared to the above test
	@Test
	public void deleteRollbackWorksWithTransitiveReferences2() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		res3.location().room().create();
		res2.location().room().setAsReference(res3.location().room());
		res1.location().room().setAsReference(res2.location().room());
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res3.location().room());
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res3.location().room());
		assertExists(res2.location().room());
		assertExists(res1.location().room());
		assertLocationsEqual(res2.location().room(), res3.location().room());
		assertLocationsEqual(res1.location().room(), res3.location().room());
		res1.delete();res2.delete();res3.delete();spoiler.delete();
	}
	
	// here the references are not directly transitive, unlike test versions 1 and 2 above
	@Test
	public void deleteRollbackWorksWithTransitiveReferences3() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		String sub = "sub";
		res3.getSubResource(sub, Room.class).getSubResource(sub, Room.class).create();
		res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res3.getSubResource(sub, Room.class));
		res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res2.getSubResource(sub, Room.class));
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res2);
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res2);
		assertExists(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertExists(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertLocationsEqual(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class), res3.getSubResource(sub, Room.class));
		assertLocationsEqual(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).getSubResource(sub, Room.class),  
				res3.getSubResource(sub, Room.class));
		res1.delete();res2.delete();res3.delete();spoiler.delete();
	}
	
	// here the references are not directly transitive, unlike test versions 1 and 2 above
	@Test
	public void deleteRollbackWorksWithTransitiveReferences4() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		String sub = "sub";
		res3.getSubResource(sub, Room.class).getSubResource(sub, Room.class).create();
		res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res3.getSubResource(sub, Room.class));
		res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res2.getSubResource(sub, Room.class));
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res3.getSubResource(sub, Room.class));
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res3.getSubResource(sub, Room.class));
		assertExists(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertExists(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertLocationsEqual(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class), res3.getSubResource(sub, Room.class));
		assertLocationsEqual(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).getSubResource(sub, Room.class),  
				res3.getSubResource(sub, Room.class));
		res1.delete();res2.delete();res3.delete();spoiler.delete();
	}
	
	@Test
	public void deleteRollbackWorksWithLoops1() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		String sub = "sub";
		// loop in a single "tree"
		res1.getSubResource(sub, Room.class).create().getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res1.getSubResource(sub, Room.class));
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1);
		assertExists(res1.getSubResource(sub, Room.class));
		assertExists(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertLocationsEqual(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).getSubResource(sub, Room.class), res1.getSubResource(sub, Room.class));
		res1.delete();spoiler.delete();
	}
	
	@Test
	public void deleteRollbackWorksWithLoops2() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		String sub = "sub";
		res1.getSubResource(sub, Room.class).create();
		res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res1.getSubResource(sub, Room.class));
		res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class).setAsReference(res2.getSubResource(sub, Room.class)); // loop between two "trees"
		FloatResource spoiler = resMan.createResource(newResourceName(), FloatResource.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1);
		assertExists(res1.getSubResource(sub, Room.class));
		assertExists(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertExists(res2);
		assertExists(res2.getSubResource(sub, Room.class));
		assertExists(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertLocationsEqual(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class), res2.getSubResource(sub, Room.class));
		assertLocationsEqual(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class), res1.getSubResource(sub, Room.class));
		
		// same test once again, this time deleting even more resources before the rollback
		final ResourceTransaction transaction2 = resAcc.createResourceTransaction();
		transaction2.delete(res1);
		transaction2.delete(res2.getSubResource(sub, Room.class));
		transaction2.setFloat(spoiler, 2F, WriteConfiguration.FAIL); // will cause transaction to be aborted
		try {
			transaction2.commit();
		} catch (ResourceOperationException e) {/* provoked exception */}
		assertExists(res1);
		assertExists(res1.getSubResource(sub, Room.class));
		assertExists(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertExists(res2);
		assertExists(res2.getSubResource(sub, Room.class));
		assertExists(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class));
		assertLocationsEqual(res1.getSubResource(sub, Room.class).getSubResource(sub, Room.class), res2.getSubResource(sub, Room.class));
		assertLocationsEqual(res2.getSubResource(sub, Room.class).getSubResource(sub, Room.class), res1.getSubResource(sub, Room.class));
		
		res1.delete();res2.delete();spoiler.delete();
	}
	
	/*
	 ********* Schedule tests *********
	 */
	
	@Test
	public void scheduleSetRollbackWorks() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		List<SampledValue> values1 = ValueResourceUtils.getSampledValues(new int[]{1,2,5}, new long[]{1,2,5});
		schedule.addValues(values1);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		List<SampledValue> values2 = ValueResourceUtils.getSampledValues(new int[]{1,2,3}, new long[]{2,3,4});
		MemoryTimeSeries function = new ArrayTimeSeries(IntegerValue.class);
		function.addValues(values2);
		transaction.setSchedule(schedule, function);
		failTransaction(transaction);
		assertEquals(values1, schedule.getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	@Test
	public void scheduleAddRollbackWorks() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		List<SampledValue> values1= ValueResourceUtils.getSampledValues(new int[]{1,3,5}, new long[]{1,3,5});
		schedule.addValues(values1);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		List<SampledValue> values2 = ValueResourceUtils.getSampledValues(new int[]{1,2,3}, new long[]{2,4,23});
		MemoryTimeSeries function = new ArrayTimeSeries(IntegerValue.class);
		function.addValues(values2);
		transaction.addSchedule(schedule, function);
		failTransaction(transaction);
		assertEquals(values1, schedule.getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	@Test
	public void scheduleReplaceWorks() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		List<SampledValue> values1= ValueResourceUtils.getSampledValues(new int[]{1,3,5,123,2}, new long[]{1,4,7,11,15});
		schedule.addValues(values1);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		List<SampledValue> values2 = ValueResourceUtils.getSampledValues(new int[]{1,2,3}, new long[]{4,8,12});
		MemoryTimeSeries function = new ArrayTimeSeries(IntegerValue.class);
		function.addValues(values2);
		transaction.replaceScheduleValues(schedule, 3, 13, values2);
		failTransaction(transaction);
		assertEquals(values1, schedule.getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	/*
	 ********* Access mode tests **********
	 */
	
	@Test
	public void accessModeSurvivesDeleteRollback() {
		final Resource res = resMan.createResource(newResourceName(), Resource.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		final AccessMode newMode = AccessMode.EXCLUSIVE;
		final AccessPriority newPrio = AccessPriority.PRIO_GENERICMANAGEMENT;
		boolean success = res.requestAccessMode(newMode, newPrio);
		assertTrue(success);
		transaction.delete(res);
		failTransaction(transaction);
		assertExists(res);
		assertEquals(newMode, res.getAccessMode());
		assertEquals(newPrio, res.getAccessPriority());
		res.delete();
	}
	
	/*
	 * Uses several apps, to verify that the access mode/priority of appN
	 * survives a delete rollback caused by app1
	 */
	@Ignore("not implemented yet")
	@Test
	public void allAccessPrioritiesSurviveToplevelDeleteRollback() {
		TestApplication secondApp = new TestApplication();
		TestApplication thirdApp = new TestApplication();
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
		secondApp.registerAndAwaitStart(ctx);
		thirdApp.registerAndAwaitStart(ctx);
		
		// we get the same resource several times, once for app1, once for app2, ...
		final Resource res1 = resMan.createResource(newResourceName(), Resource.class);
		final Resource res2 = secondApp.getAppMan().getResourceAccess().getResource(res1.getPath()); 
		final Resource res3 = thirdApp.getAppMan().getResourceAccess().getResource(res1.getPath()); 
		final AccessMode mode2 = AccessMode.SHARED;
		final AccessPriority prio2 = AccessPriority.PRIO_HIGHEST;
		final AccessPriority prio3 = AccessPriority.PRIO_GRIDSTABILISATION;
		assertTrue(res2.requestAccessMode(mode2, prio2));
		assertEquals(prio2, res2.getAccessPriority()); // consistency check
		assertTrue(res3.requestAccessMode(mode2, prio3));
		assertEquals(prio3, res3.getAccessPriority()); // consistency check
		// the transaction is executed by app1
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		failTransaction(transaction);
		assertExists(res1);
		assertExists(res2);
		assertExists(res3);
		assertEquals(mode2, res2.getAccessMode()); // only shared anyway
		assertEquals(prio2, res2.getAccessPriority());
		assertEquals(prio3, res3.getAccessPriority());
		res1.delete();
		assertIsVirtual(res2);
		assertIsVirtual(res3);
		secondApp.unregister();
		thirdApp.unregister();
	}
	
	/*
	 * Uses several apps, to verify that the access priority of appN
	 * survives a delete rollback caused by app1
	 */
	@Test
	public void allAccessPrioritiesSurviveSubresourceDeleteRollback() {
		TestApplication secondApp = new TestApplication();
		TestApplication thirdApp = new TestApplication();
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
		secondApp.registerAndAwaitStart(ctx);
		thirdApp.registerAndAwaitStart(ctx);
		
		// we get the same resource several times, once for app1, once for app2, ...
		final Resource res1 = resMan.createResource(newResourceName(), Resource.class);
		final Resource res2 = secondApp.getAppMan().getResourceAccess().getResource(res1.getPath()); 
		final Resource res3 = thirdApp.getAppMan().getResourceAccess().getResource(res1.getPath()); 
		final AccessMode mode2 = AccessMode.SHARED;
		final AccessPriority prio2 = AccessPriority.PRIO_HIGHEST;
		final AccessPriority prio3 = AccessPriority.PRIO_GRIDSTABILISATION;
		String sub = "sub";
		assertTrue(res2.getSubResource(sub, Resource.class).create().requestAccessMode(mode2, prio2));
		assertEquals(prio2, res2.getSubResource(sub, Resource.class).getAccessPriority()); // consistency check
		assertTrue(res3.getSubResource(sub, Resource.class).requestAccessMode(mode2, prio3));
		assertEquals(prio3, res3.getSubResource(sub, Resource.class).getAccessPriority()); // consistency check
		// the transaction is executed by app1
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1.getSubResource(sub, Resource.class));
		failTransaction(transaction);
		assertExists(res1.getSubResource(sub, Resource.class));
		assertExists(res2.getSubResource(sub, Resource.class));
		assertExists(res3.getSubResource(sub, Resource.class));
		assertEquals(mode2, res2.getSubResource(sub, Resource.class).getAccessMode()); // only shared anyway
		assertEquals(prio2, res2.getSubResource(sub, Resource.class).getAccessPriority());
		assertEquals(prio3, res3.getSubResource(sub, Resource.class).getAccessPriority());
		res1.delete();
		assertIsVirtual(res2);
		assertIsVirtual(res3);
		secondApp.unregister();
		thirdApp.unregister();
	}
	
	/*
	 ********** Listener tests **************
	 */
	
	@Ignore("not implemented yet")
	@Test
	public void rollbackPreventsValueCallbacks() throws InterruptedException {
		final FloatResource res =  resMan.createResource(newResourceName(), FloatResource.class);
		res.setValue(1F);
		res.activate(false);
		ValueTestListener<FloatResource> listener = new ValueTestListener<>(getApplicationManager());
		res.addValueListener(listener);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.setFloat(res, 2F); // would cause a callback if the transaction succeeded
		failTransaction(transaction);
		boolean callback = listener.await(2, TimeUnit.SECONDS); 
		assertFalse("Listener callback although transaction failed", callback);
		res.delete();
	}
	
	@Ignore("not implemented yet")
	@Test
	public void rollbackPreventsStructureCallback() throws InterruptedException {
		final TemperatureSensor res =  resMan.createResource(newResourceName(), TemperatureSensor.class);
		res.activate(false);
		StructureTestListener listener = new StructureTestListener();
		res.addStructureListener(listener);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.create(res.reading()); // would cause a callback if the transaction succeeded
		failTransaction(transaction);
		boolean callback = listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 2, TimeUnit.SECONDS); 
		assertFalse("Listener callback although transaction failed", callback);
		res.delete();
	}
	
	// TODO might work if value listeners were stored in a dedicated list, like StructureListeners, instead of the ElementInfos
	@Ignore("not implemented yet")
	@Test
	public void valueListenerSurvivesDeleteRollback() throws InterruptedException {
		final FloatResource res1 =  resMan.createResource(newResourceName(), FloatResource.class);
		final TemperatureSensor res2 =  resMan.createResource(newResourceName(), TemperatureSensor.class);
		res1.setValue(1F);
		res1.activate(false);
		res2.reading().<FloatResource> create().setValue(23F);
		res2.activate(true);
		ValueTestListener<FloatResource> listener = new ValueTestListener<>(getApplicationManager());
		// we test on both toplevel and subresource, since they may be treated differently, internally
		res1.addValueListener(listener);
		res2.addValueListener(listener);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.delete(res2);
		failTransaction(transaction);
		res1.setValue(34F);
		listener.assertCallback();
		listener.reset();
		res2.reading().setValue(123F);
		listener.assertCallback();
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void structureListenerSurvivesDeleteRollback() throws InterruptedException {
		final FloatResource res1 =  resMan.createResource(newResourceName(), FloatResource.class);
		final TemperatureSensor res2 =  resMan.createResource(newResourceName(), TemperatureSensor.class);
		res1.setValue(1F);
		res1.activate(false);
		res2.reading().<FloatResource> create().setValue(23F);
		res2.activate(true);
		StructureTestListener listener = new StructureTestListener();
		// we test on both toplevel and subresource, since they may be treated differently, internally
		res1.addStructureListener(listener);
		res2.reading().addStructureListener(listener);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.delete(res2);
		failTransaction(transaction);
		
		res1.deactivate(false);
		listener.awaitEvent(EventType.RESOURCE_DEACTIVATED, 5, TimeUnit.SECONDS);

		listener.reset();
		res2.reading().delete();;
		listener.awaitEvent(EventType.RESOURCE_DELETED, 5, TimeUnit.SECONDS);
		res1.delete();
		res2.delete();
	}
	

}
