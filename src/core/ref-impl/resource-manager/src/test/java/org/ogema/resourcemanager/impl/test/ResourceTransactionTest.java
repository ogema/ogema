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
package org.ogema.resourcemanager.impl.test;

import static org.junit.Assert.*;
import static org.ogema.exam.ResourceAssertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.AccessMode;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceOperationException;
import org.ogema.core.resourcemanager.transaction.ReadConfiguration;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.core.resourcemanager.transaction.TransactionFuture;
import org.ogema.core.resourcemanager.transaction.WriteConfiguration;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.model.connections.ElectricityConnection;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.PowerSensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ValueResourceUtils;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.ArrayTimeSeries;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ResourceTransactionTest extends OsgiTestBase {
	
	@Test
	public void transactionWritesAndReadsFloatValues() {
		final FloatResource f1 = resMan.createResource(newResourceName(), FloatResource.class);
		final FloatResource f2 = resMan.createResource(newResourceName(), FloatResource.class);
		for (int i = 0; i < 10; ++i) {
			final ResourceTransaction transaction = resAcc.createResourceTransaction();
			final float value1 = (float) Math.random();
			final float value2 = (float) Math.random();
			transaction.setFloat(f1, value1);
			transaction.setFloat(f2, value2);
			TransactionFuture<Float> result1 = transaction.getFloat(f1);
			TransactionFuture<Float> result2 = transaction.getFloat(f2);
			transaction.commit();
			assertEquals(result1.getValue(), value1, 1.e-4f);
			assertEquals(result2.getValue(), value2, 1.e-4f);
		}
		f1.delete();
		f2.delete();
	}
	
	@Test
	public void createWorksForOptionalElements() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.create(res1.reading());
		transaction.create(res2.location().room().motionSensor());
		transaction.commit();
		assertExists(res1.reading());
		assertExists(res2.location().room().motionSensor());
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void createWorksForDecorators() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		Resource sub1 = res1.getSubResource("justatest", Room.class); // virtual
		Resource sub2 = res2.battery().electricityConnection().subPhaseConnections().<ResourceList<ElectricityConnection>> create()
				.getSubResource("justatest", ElectricityConnection.class); // virtual
		Resource sub3 = res1.location().getSubResource("jsutatest", Room.class).temperatureSensor(); // virtual
		transaction.create(sub1);
		transaction.create(sub2);
		transaction.create(sub3);
		transaction.commit();
		assertExists(sub1);
		assertExists(sub2);
		assertExists(sub3);
		res1.delete();
		res2.delete();
	}
	
	@Test 
	public void deleteWorks() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		String res1Path = res1.getPath();
		res2.location().room().create();
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res1);
		transaction.delete(res2.location());
		transaction.commit();
		assertNull(resAcc.getResource(res1Path));
		assertIsVirtual(res2.location());
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void activateAndDeactivateWork() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		res2.location().room().create().activate(false);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.activate(res1);
		transaction.deactivate(res2.location().room());
		transaction.commit();
		assertActive(res1);
		assertInactive(res2.location().room());
		res1.delete();
		res2.delete();
	}
	
	@Test
	public void activateAndDeactivateWorkRecursively() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		Resource sub1 = res1.battery().electricityConnection().currentSensor().reading().create();
		Resource sub2 = res2.location().room().create();
		Resource sub3 = res3.location().room().create();
		res1.location().room().setAsReference(sub3);
		res2.activate(true);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.activate(res1,true,true);
		transaction.deactivate(res2.location(),true);
		transaction.commit();
		assertActive(res1);
		assertActive(sub1);
		assertActive(res2);
		assertInactive(res2.location());
		assertInactive(sub2);
		assertInactive(sub3); // ensure a resource connected via references is not activated
		res1.delete();
		res2.delete();
		res3.delete();
	}
	
	@Test 
	public void deleteWorksWithReferences() {
		TemperatureSensor res1 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res2 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res3 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res4 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res5 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		TemperatureSensor res6 = resMan.createResource(newResourceName(), TemperatureSensor.class);
		res1.location().room().co2Sensor().create();
		res2.location().room().setAsReference(res1.location().room());
		res3.location().room().co2Sensor().setAsReference(res1.location().room().co2Sensor());
		res4.location().room().co2Sensor().create();
		res5.location().room().setAsReference(res4.location().room());
		res6.location().room().co2Sensor().setAsReference(res4.location().room().co2Sensor());
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.delete(res2.location()); // delete the parent of the reference
		transaction.delete(res3.location().room().co2Sensor()); // delete the reference itself
		transaction.delete(res4.location().room());  // delete the reference target, respectively the parent of the reference target
		transaction.commit();
		assertExists(res1.location().room().co2Sensor()); 
		assertIsVirtual(res2.location());
		assertIsVirtual(res2.location().room()); // double check
		assertIsVirtual(res3.location().room().co2Sensor());
		assertExists(res3.location().room());
		assertIsVirtual(res4.location().room());
		assertIsVirtual(res5.location().room());
		assertExists(res5.location());
		assertIsVirtual(res6.location().room().co2Sensor());
		assertExists(res6.location().room());
		res1.delete();res2.delete();res3.delete();res4.delete();res5.delete();res6.delete();
	}
	
	@Test(expected = ResourceOperationException.class)
	public void transactionFailConfigurationWorks() {
		final FloatResource f1 = resMan.createResource(newResourceName(), FloatResource.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		transaction.setFloat(f1, (float) Math.random(),WriteConfiguration.FAIL);
		transaction.commit(); // resource is inactive, so we expect an exception
	}

	@Test
	public void accessModeInTransactionWorks() {
		final Resource res = resMan.createResource(newResourceName(), Resource.class);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		final AccessMode newMode = AccessMode.EXCLUSIVE;
		final AccessPriority newPrio = AccessPriority.PRIO_GENERICMANAGEMENT;
		TransactionFuture<Boolean> result = transaction.requestAccessMode(res, newMode, newPrio, true);
		TransactionFuture<AccessMode> newModeResult = transaction.getAccessMode(res);
		TransactionFuture<AccessPriority> newPrioResult = transaction.getAccessPriority(res);
		transaction.commit();
		assertTrue(result.getValue());
		assertEquals(newMode, newModeResult.getValue());
		assertEquals(newPrio, newPrioResult.getValue());
		res.delete();
	}
	
	/*
	 ********* Schedule tests *********
	 */
	
	@Test
	public void scheduleSetWorks() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		schedule.addValues(ValueResourceUtils.getSampledValues(new int[]{1,2,5}, new long[]{1,2,5}));
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		List<SampledValue> values = ValueResourceUtils.getSampledValues(new int[]{1,2,3}, new long[]{2,3,4});
		MemoryTimeSeries function = new ArrayTimeSeries(IntegerValue.class);
		function.addValues(values);
		transaction.setSchedule(schedule, function);
		transaction.commit();
//		assertEquals(3, schedule.getValues(Long.MIN_VALUE).size());
//		assertEquals(2, schedule.getNextValue(Long.MIN_VALUE).getTimestamp());
		assertEquals(values, schedule.getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	@Test
	public void scheduleAddWorks() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		List<SampledValue> values1= ValueResourceUtils.getSampledValues(new int[]{1,3,5}, new long[]{1,3,5});
		schedule.addValues(values1);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		List<SampledValue> values2 = ValueResourceUtils.getSampledValues(new int[]{1,2,3}, new long[]{2,4,23});
		MemoryTimeSeries function = new ArrayTimeSeries(IntegerValue.class);
		function.addValues(values2);
		transaction.addSchedule(schedule, function);
		transaction.commit();
		List<SampledValue> allValues = new ArrayList<>();
		allValues.addAll(values1);
		allValues.addAll(values2);
		Collections.sort(allValues);
		assertEquals(allValues, schedule.getValues(Long.MIN_VALUE));
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
		transaction.commit();
		values2.addAll(ValueResourceUtils.getSampledValues(new int[]{1,2}, new long[]{1,15}));
		Collections.sort(values2);
		assertEquals(values2, schedule.getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	@Test
	public void scheduleReadWorks() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		schedule.activate(false);
		List<SampledValue> values1= ValueResourceUtils.getSampledValues(new int[]{1,3,5,123,2}, new long[]{1,4,7,11,15});
		schedule.addValues(values1);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		TransactionFuture<ReadOnlyTimeSeries> result = transaction.getSchedule(schedule);
		transaction.commit();
		assertEquals(values1, result.getValue().getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	@Test
	public void scheduleReadWorksWithLimits() {
		final IntegerResource f1 = resMan.createResource(newResourceName(), IntegerResource.class);
		final Schedule schedule = f1.forecast().create();
		schedule.activate(false);
		List<SampledValue> values1= ValueResourceUtils.getSampledValues(new int[]{1,3,5,123,2}, new long[]{1,4,7,11,15});
		schedule.addValues(values1);
		final ResourceTransaction transaction = resAcc.createResourceTransaction();
		TransactionFuture<ReadOnlyTimeSeries> result = transaction.getSchedule(schedule, 4, 12);
		transaction.commit();
		values1.remove(values1.size()-1);
		values1.remove(0);
		assertEquals(values1, result.getValue().getValues(Long.MIN_VALUE));
		f1.delete();
	}
	
	@Test
	public void returnNullConfigWorks() {
		// two inactive resource
		final FloatResource flt = resMan.createResource(newResourceName(), FloatResource.class);
		final TimeResource time = resMan.createResource(newResourceName(), TimeResource.class);
		
		final PowerSensor top = resMan.createResource(newResourceName(), PowerSensor.class);
		// two virtual resources
		final FloatResource flt2 = top.reading();
		final TimeResource time2 = top.getSubResource("dummy", TimeResource.class);
		final ResourceTransaction trans = resAcc.createResourceTransaction();
		final TransactionFuture<Float> v0 = trans.getFloat(flt, ReadConfiguration.RETURN_NULL);
		final TransactionFuture<Float> v1 = trans.getFloat(flt2, ReadConfiguration.RETURN_NULL);
		final TransactionFuture<Long> v2 = trans.getTime(time, ReadConfiguration.RETURN_NULL);
		final TransactionFuture<Long> v3 = trans.getTime(time2, ReadConfiguration.RETURN_NULL);
		trans.commit();
		Assert.assertNull("ReadConfiguration.RETURN_NULL was ignored", v0.getValue());
		Assert.assertNull("ReadConfiguration.RETURN_NULL was ignored", v1.getValue());
		Assert.assertNull("ReadConfiguration.RETURN_NULL was ignored", v2.getValue());
		Assert.assertNull("ReadConfiguration.RETURN_NULL was ignored", v3.getValue());
	}
	
	
	
	
}
