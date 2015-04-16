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

import java.util.Arrays;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ogema.core.channelmanager.measurements.LongValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.OpaqueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.NoSuchResourceException;
import org.ogema.core.resourcemanager.Transaction;
import org.ogema.core.resourcemanager.VirtualResourceException;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.core.timeseries.ReadOnlyTimeSeries;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.tools.timeseries.api.MemoryTimeSeries;
import org.ogema.tools.timeseries.implementations.TreeTimeSeries;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 *
 * @author Jan Lapp, Timo Fischer, Fraunhofer IWES
 */
@ExamReactorStrategy(PerClass.class)
public class TransactionTest extends OsgiTestBase {

	/*
	 Tests for the new transactions using a transaction object.
	 */
	@Test
	public void transactionWritesBooleanValues() {
		final BooleanResource b1 = resMan.createResource("b1", BooleanResource.class);
		final BooleanResource b2 = resMan.createResource("b2", BooleanResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(b1);
		transaction.addResource(b2);
		assertNull(transaction.getBoolean(b1));
		assertNull(transaction.getBoolean(b2));

		for (int i = 0; i < 10; ++i) {
			final boolean value = (Math.random() < 0.5);
			transaction.setBoolean(b1, value);
			transaction.setBoolean(b2, value);
			transaction.write();
			assertEquals(b1.getValue(), value);
			assertEquals(b2.getValue(), value);
			transaction.read();
			final Boolean v1 = transaction.getBoolean(b1);
			assertNotNull(b1);
			assertEquals((boolean) v1, value);
			final Boolean v2 = transaction.getBoolean(b2);
			assertNotNull(v2);
			assertEquals((boolean) v2, value);
		}
	}

	@Test
	public void transactionWritesFloatAndIntValues() {
		final FloatResource f1 = resMan.createResource("f1", FloatResource.class);
		final FloatResource f2 = resMan.createResource("f2", FloatResource.class);
		final IntegerResource i1 = resMan.createResource("i1", IntegerResource.class);

		Transaction transaction = resAcc.createTransaction();
		transaction.addResources(Arrays.asList(new Resource[] { f1, f2, i1 }));
		assertNull(transaction.getFloat(f1));
		assertNull(transaction.getFloat(f2));
		assertNull(transaction.getInteger(i1));

		for (int i = 0; i < 10; ++i) {
			final float value = (float) Math.random();
			transaction.setFloat(f1, value);
			transaction.setFloat(f2, value);
			transaction.setInteger(i1, i);
			transaction.write();
			assertEquals(f1.getValue(), value, 1.e-4f);
			assertEquals(f2.getValue(), value, 1.e-4f);
			assertEquals(i1.getValue(), i);
			transaction.read();
			final Float v1 = transaction.getFloat(f1);
			assertNotNull(v1);
			assertEquals((float) v1, value, 1.e-4f);
			final Float v2 = transaction.getFloat(f2);
			assertNotNull(v2);
			assertEquals((float) v2, value, 1.e-4f);
			final Integer vi = transaction.getInteger(i1);
			assertNotNull(vi);
			assertEquals((int) vi, i);
		}
	}

	@Test
	public void transactionWritesStringValues() {
		final StringResource res1 = resMan.createResource("s1", StringResource.class);
		final StringResource res2 = resMan.createResource("s2", StringResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(res1);
		transaction.addResource(res2);
		assertNull(transaction.getString(res1));
		assertNull(transaction.getString(res2));

		for (int i = 0; i < 10; ++i) {
			final String value = Double.toString(1000 * Math.random());
			transaction.setString(res1, value);
			transaction.setString(res2, value);
			transaction.write();
			assertEquals(res1.getValue(), value);
			assertEquals(res2.getValue(), value);
			transaction.read();
			final String v1 = transaction.getString(res1);
			assertNotNull(res1);
			assertEquals(v1, value);
			final String v2 = transaction.getString(res2);
			assertNotNull(v2);
			assertEquals(v2, value);
		}
	}

	@Test
	public void transactionWritesTimeValues() {
		final TimeResource res1 = resMan.createResource("t1", TimeResource.class);
		final TimeResource res2 = resMan.createResource("t2", TimeResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(res1);
		transaction.addResource(res2);
		assertNull(transaction.getTime(res1));
		assertNull(transaction.getTime(res2));

		for (int i = 0; i < 10; ++i) {
			final long value = (long) (1000 * Math.random());
			transaction.setTime(res1, value);
			transaction.setTime(res2, value);
			transaction.write();
			assertEquals(res1.getValue(), value);
			assertEquals(res2.getValue(), value);
			transaction.read();
			final Long v1 = transaction.getTime(res1);
			assertNotNull(res1);
			assertEquals((long) v1, value);
			final Long v2 = transaction.getTime(res2);
			assertNotNull(v2);
			assertEquals((long) v2, value);
		}
	}

	@Test
	public void transactionWritesOpaqueValues() {
		final OpaqueResource res1 = resMan.createResource("o1", OpaqueResource.class);
		final OpaqueResource res2 = resMan.createResource("o2", OpaqueResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(res1);
		transaction.addResource(res2);
		assertNull(transaction.getByteArray(res1));
		assertNull(transaction.getByteArray(res2));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final byte value = (byte) (100 * Math.random());
			final byte[] values = new byte[L];
			for (int j = 0; j < L; ++j)
				values[j] = (byte) (value + j);

			transaction.setByteArray(res1, values);
			transaction.setByteArray(res2, values);
			transaction.write();

			assertEquals(res1.getValue().length, L);
			assertEquals(res2.getValue().length, L);
			transaction.read();

			final byte[] v1 = transaction.getByteArray(res1);
			assertNotNull(res1);
			assertEquals(v1.length, L);

			final byte[] v2 = transaction.getByteArray(res2);
			assertNotNull(v2);
			assertEquals(v2.length, L);

			for (int j = 0; j < L; ++j) {
				final byte entry = (byte) (value + j);
				assertEquals(v1[j], entry);
				assertEquals(v2[j], entry);
			}
		}
	}

	@Test
	public void transactionWritesByteArrayValues() {
		final ByteArrayResource res1 = resMan.createResource("bya1", ByteArrayResource.class);
		final ByteArrayResource res2 = resMan.createResource("bya2", ByteArrayResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(res1);
		transaction.addResource(res2);
		assertNull(transaction.getByteArray(res1));
		assertNull(transaction.getByteArray(res2));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final byte value = (byte) (256. * Math.random());
			final byte[] values = new byte[L];
			for (int j = 0; j < L; ++j)
				values[j] = value;

			transaction.setByteArray(res1, values);
			transaction.setByteArray(res2, values);
			transaction.write();

			assertEquals(res1.getValues().length, L);
			assertEquals(res2.getValues().length, L);
			transaction.read();

			final byte[] v1 = transaction.getByteArray(res1);
			assertNotNull(res1);
			assertEquals(v1.length, L);

			final byte[] v2 = transaction.getByteArray(res2);
			assertNotNull(v2);
			assertEquals(v2.length, L);

			for (int j = 0; j < L; ++j) {
				final byte entry = value;
				assertEquals(v1[j], entry);
				assertEquals(v2[j], entry);
			}
		}
	}

	@Test
	public void transactionWritesBooleanArrayValues() {
		final BooleanArrayResource res1 = resMan.createResource("ba1", BooleanArrayResource.class);
		final BooleanArrayResource res2 = resMan.createResource("ba2", BooleanArrayResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(res1);
		transaction.addResource(res2);
		assertNull(transaction.getBooleanArray(res1));
		assertNull(transaction.getBooleanArray(res2));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final boolean value = Math.random() < 0.5;
			final boolean[] values = new boolean[L];
			for (int j = 0; j < L; ++j)
				values[j] = value;

			transaction.setBooleanArray(res1, values);
			transaction.setBooleanArray(res2, values);
			transaction.write();

			assertEquals(res1.getValues().length, L);
			assertEquals(res2.getValues().length, L);
			transaction.read();

			final boolean[] v1 = transaction.getBooleanArray(res1);
			assertNotNull(res1);
			assertEquals(v1.length, L);

			final boolean[] v2 = transaction.getBooleanArray(res2);
			assertNotNull(v2);
			assertEquals(v2.length, L);

			for (int j = 0; j < L; ++j) {
				final boolean entry = value;
				assertEquals(v1[j], entry);
				assertEquals(v2[j], entry);
			}
		}
	}

	@Test
	public void transactionWritesFloatArrayValues() {
		final FloatArrayResource res1 = resMan.createResource("fa1", FloatArrayResource.class);
		final FloatArrayResource res2 = resMan.createResource("fa2", FloatArrayResource.class);

		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(res1);
		transaction.addResource(res2);
		assertNull(transaction.getFloatArray(res1));
		assertNull(transaction.getFloatArray(res2));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final float value = (float) (1.f * Math.random() - 0.5f);
			final float[] values = new float[L];
			for (int j = 0; j < L; ++j)
				values[j] = value + 0.01f * j;

			transaction.setFloatArray(res1, values);
			transaction.setFloatArray(res2, values);
			transaction.write();

			assertEquals(res1.getValues().length, L);
			assertEquals(res2.getValues().length, L);
			transaction.read();

			final float[] v1 = transaction.getFloatArray(res1);
			assertNotNull(res1);
			assertEquals(v1.length, L);

			final float[] v2 = transaction.getFloatArray(res2);
			assertNotNull(v2);
			assertEquals(v2.length, L);

			for (int j = 0; j < L; ++j) {
				final float entry = values[j];
				assertEquals(v1[j], entry, 1.e-4);
				assertEquals(v2[j], entry, 1.e-4);
			}
		}
	}

	@Test
	public void transactionWritesIntArrayValues() {
		final IntegerArrayResource resource = resMan.createResource("ia1", IntegerArrayResource.class);
		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(resource);
		assertNull(transaction.getIntegerArray(resource));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final int value = (int) (1000.f * Math.random() - 500.f);
			final int[] values = new int[L];
			for (int j = 0; j < L; ++j)
				values[j] = value + j;

			transaction.setIntegerArray(resource, values);
			transaction.write();
			assertEquals(resource.getValues().length, L);

			transaction.read();
			final int[] v1 = transaction.getIntegerArray(resource);
			assertNotNull(resource);
			assertEquals(v1.length, L);

			for (int j = 0; j < L; ++j) {
				final float entry = values[j];
				assertEquals(v1[j], entry, 1.e-4);
			}
		}
	}

	@Test
	public void transactionWritesStringArrayValues() {
		final StringArrayResource resource = resMan.createResource("sa1", StringArrayResource.class);
		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(resource);
		assertNull(transaction.getStringArray(resource));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final double value = (1000 * Math.random() - 500.f);
			final String[] values = new String[L];
			for (int j = 0; j < L; ++j) {
				values[j] = Double.toString(value + j);
			}
			transaction.setStringArray(resource, values);
			transaction.write();
			assertEquals(resource.getValues().length, L);

			transaction.read();
			final String[] v1 = transaction.getStringArray(resource);
			assertNotNull(resource);
			assertEquals(v1.length, L);

			for (int j = 0; j < L; ++j) {
				assertEquals(v1[j], values[j]);
			}
		}
	}

	@Test
	public void transactionWritesTimeArrayValues() {
		final TimeArrayResource resource = resMan.createResource("ta1", TimeArrayResource.class);
		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(resource);
		assertNull(transaction.getTimeArray(resource));
		for (int i = 0; i < 10; ++i) {
			final int L = i;
			final int value = (int) (1000.f * Math.random() - 500.f);
			final long[] values = new long[L];
			for (int j = 0; j < L; ++j)
				values[j] = value + j;

			transaction.setTimeArray(resource, values);
			transaction.write();
			assertEquals(resource.getValues().length, L);

			transaction.read();
			final long[] v1 = transaction.getTimeArray(resource);
			assertNotNull(resource);
			assertEquals(v1.length, L);

			for (int j = 0; j < L; ++j) {
				final long entry = values[j];
				assertEquals(v1[j], entry);
			}
		}
	}

	@Test
	public void transactionWritesSchedules() {
		final TimeResource resource = resMan.createResource("ta1scheduleAdded", TimeResource.class);
		final Schedule schedule = resource.addDecorator("schedule", DefinitionSchedule.class);
		final Transaction transaction = resAcc.createTransaction();
		transaction.addResource(schedule);
		assertNull(transaction.getSchedule(schedule));
		for (int i = 0; i < 10; ++i) {
			final long value = (long) (1000.f * Math.random() - 500.f);
			final MemoryTimeSeries values = new TreeTimeSeries(LongValue.class);
			values.addValue(new SampledValue(new LongValue(value), 0, Quality.GOOD));
			values.setInterpolationMode(InterpolationMode.STEPS);

			transaction.setSchedule(schedule, values);
			transaction.write();

			MemoryTimeSeries result = new TreeTimeSeries(LongValue.class);
			result.read(schedule);
			assertEquals(result.getValue(0).getValue().getLongValue(), value);

			transaction.read();
			final ReadOnlyTimeSeries readResult = transaction.getSchedule(schedule);
			assertNotNull(readResult);
			assertEquals(readResult.getValue(0).getValue().getLongValue(), value);
		}
	}

	@Test
	public void settingValuesToUninitializedFieldsThrowsException() {
		final FloatResource f1 = resMan.createResource("f1", FloatResource.class);
		final FloatResource f2 = resMan.createResource("f2", FloatResource.class);
		Transaction transaction = resAcc.createTransaction();
		boolean exceptionOccurred = false;
		try {
			transaction.setFloat(f1, 1.f);
		} catch (NoSuchResourceException ex) {
			exceptionOccurred = true;
		}
		assertTrue(exceptionOccurred);
	}

	@Test
	public void writingToVirtualResourceThrowsException() {
		final ElectricityMeter meter = resMan.createResource("dummyMeter", ElectricityMeter.class);
		final FloatResource f1 = meter.price().price();
		Transaction transaction = resAcc.createTransaction();
		transaction.addResource(f1);
		transaction.setFloat(f1, 33.1f);
		boolean exceptionOccurred = false;
		try {
			transaction.write();
		} catch (VirtualResourceException ex) {
			exceptionOccurred = true;
		}
		assertTrue(exceptionOccurred);
	}

}
