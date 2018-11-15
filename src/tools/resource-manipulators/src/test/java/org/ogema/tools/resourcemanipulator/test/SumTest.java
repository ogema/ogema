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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.resourcemanipulator.configurations.ManipulatorConfiguration;
import org.ogema.tools.resourcemanipulator.configurations.Sum;

/**
 * Tests for sum manipulator tool.
 * 
 * @author Marco Postigo Perez
 */
public class SumTest extends OsgiAppTestBase {

	private static final float EPSILON = 1E-6f;
	private static final int MAX_FOR_SEED_SELECTION = 10000;
	private static final int MAX_VALUES = 10000;
	private ResourceManagement resman;
	private ResourceManipulator tool;

	public SumTest() {
		super(true);
	}

	@Before
	public void setup() {
		resman = getApplicationManager().getResourceManagement();
		tool = new ResourceManipulatorImpl(getApplicationManager());
		tool.start();
	}

	@Test
	public void testFloatSum() {
		int randomInt = new Random().nextInt(MAX_FOR_SEED_SELECTION);
		// use (probable) prime as seed
		long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
		Random rdn = new Random(seed);

		Collection<FloatResource> floats = createAndActivateFloatResources(rdn.nextInt(MAX_VALUES));
		for (SingleValueResource res : floats) {
			assertTrue(res.isActive());
		}

		FloatResource sum = createAndActivateFloatResource();
		assertTrue(sum.isActive());

		SumValueListener listener = new SumValueListener();
		sum.addValueListener(listener);

		Sum sumConfig = tool.createConfiguration(Sum.class);
		sumConfig.setAddends(floats, sum);
		sumConfig.setDelay(1000);
		sumConfig.commit();

		Float expectedSum = 0f;
		for (FloatResource f : floats) {
			f.setValue(rdn.nextFloat());
			expectedSum += f.getValue();
		}

		boolean done = false;
		FloatResource result = (FloatResource) sumConfig.getTarget();
		do {
			try {
				boolean await = listener.latch.await(3, TimeUnit.SECONDS);
				Float realSum = result.getValue();
				if (nearlyEqual(realSum, expectedSum, EPSILON) || !await) {
					// sum is already correct or the sum isn't correct. we've waited
					// two seconds (and have a delay of one second) so if await returned
					// false then there were no update within this two seconds and we can
					// assume that the result is wrong.
					done = true;
				}
				else {
					// wait for next update ...
					listener.setLatch(new CountDownLatch(1));
				}
			} catch (InterruptedException e) {
			}
		} while (!done);

		assertTrue("Sum is not correct! Expected: " + expectedSum + ", sum: " + result.getValue() + ", seed: " + seed,
				nearlyEqual(result.getValue(), expectedSum, EPSILON));

		tool.stop();
		tool.deleteAllConfigurations();
		sleep(1000);

		List<ManipulatorConfiguration> leftoverRules = tool.getConfigurations(ManipulatorConfiguration.class);
		assertTrue(leftoverRules.isEmpty());
	}

	@Test
	public void testIntSum() {
		int randomInt = new Random().nextInt(MAX_FOR_SEED_SELECTION);
		// use (probable) prime as seed
		long seed = new BigInteger(String.valueOf(randomInt)).nextProbablePrime().longValue();
		Random rdn = new Random(seed);

		Collection<IntegerResource> ints = createAndActivateIntegerResources(rdn.nextInt(MAX_VALUES) + 1);
		for (SingleValueResource res : ints) {
			assertTrue(res.isActive());
		}

		IntegerResource sum = createAndActivateIntResource();
		assertTrue(sum.isActive());

		SumValueListener listener = new SumValueListener();
		sum.addValueListener(listener);

		Sum sumConfig = tool.createConfiguration(Sum.class);
		sumConfig.setAddends(ints, sum);
		sumConfig.setDelay(1000);
		sumConfig.commit();

		int expectedSum = 0;
		// prevent int overflow ...
		int maxValue = Integer.MAX_VALUE / ints.size();
		for (IntegerResource i : ints) {
			i.setValue(rdn.nextInt(maxValue));
			expectedSum += i.getValue();
		}

		boolean done = false;
		IntegerResource result = (IntegerResource) sumConfig.getTarget();
		do {
			try {
				boolean await = listener.latch.await(3, TimeUnit.SECONDS);
				int realSum = result.getValue();
				if (expectedSum == realSum || !await) {
					// sum is already correct or the sum isn't correct. we've waited
					// two seconds (and have a delay of one second) so if await returned
					// false then there were no update within this two seconds and we can
					// assume that the result is wrong.
					done = true;
				}
				else {
					// wait for next update ...
					listener.setLatch(new CountDownLatch(1));
				}
			} catch (InterruptedException e) {
			}
		} while (!done);

		assertTrue("Sum is not correct! Expected: " + expectedSum + ", sum: " + result.getValue() + ", seed: " + seed,
				result.getValue() == expectedSum);

		tool.stop();
		tool.deleteAllConfigurations();
		sleep(1000);

		List<ManipulatorConfiguration> leftoverRules = tool.getConfigurations(ManipulatorConfiguration.class);
		assertTrue("Not all ManipulatorConfigurations were deleted", leftoverRules.isEmpty());
	}

	private List<FloatResource> createAndActivateFloatResources(int n) {
		List<FloatResource> result = new ArrayList<>();
		for(int i = 0; i < n; ++i) {
			final FloatResource f = createAndActivateFloatResource();
			result.add(f);
		}
		return result;
	}

	private List<IntegerResource> createAndActivateIntegerResources(int n) {
		List<IntegerResource> result = new ArrayList<>();
		for(int i = 0; i < n; ++i) {
			final IntegerResource f = createAndActivateIntResource();
			result.add(f);
		}
		return result;
	}

	private int floatCounter = 0;

	private FloatResource createAndActivateFloatResource() {
		final FloatResource f = resman.createResource("test_" + ++floatCounter, FloatResource.class);
		f.activate(false);
		return f;
	}

	private int intCounter = 0;

	private IntegerResource createAndActivateIntResource() {
		final IntegerResource i = resman.createResource("test_" + ++intCounter, IntegerResource.class);
		i.activate(false);
		return i;
	}

	/**
	 * We use our own comparison for float values here because
	 * Float.compare(...) won't use an epsilon due to rounding errors.
	 *
	 * @return <code>true</code> if a and b are nearly equal, <code>false</code>
	 * otherwise.
	 */
	private boolean nearlyEqual(float a, float b, float epsilon) {
		if (Double.compare(a, b) == 0) {
			return true;
		}
		double diff = Math.abs(a - b);
		if (a == 0 || b == 0 || diff < Float.MIN_NORMAL) {
			// a or b is zero or both are extremely close to it
			// relative error is less meaningful here
			return diff < (epsilon * Float.MIN_NORMAL);
		}
		else { // use relative error
			return diff / (Math.abs(a) + Math.abs(b)) < epsilon;
		}
	}

	private class SumValueListener implements ResourceValueListener<SingleValueResource> {

		private volatile CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void resourceChanged(SingleValueResource resource) {
			latch.countDown();
		}

		private void setLatch(CountDownLatch latch) {
			this.latch = latch;
		}
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void deactivationWorks() throws InterruptedException {
		int i = 1;
		Collection<IntegerResource> floats = createAndActivateIntegerResources(2);

		IntegerResource sum = createAndActivateIntResource();
		int currentSum = 0;

		SumValueListener listener = new SumValueListener();
		Sum sumConfig = tool.createConfiguration(Sum.class);
		sumConfig.setAddends(floats, sum);
		sumConfig.setDelay(500);
		sumConfig.commit();

		sum.addValueListener(listener);
        @SuppressWarnings("deprecation")
		org.ogema.core.resourcemanager.Transaction t = getApplicationManager().getResourceAccess().createTransaction();
		for (IntegerResource fl : floats) {
			t.addResource(fl);
			t.setInteger(fl, ++i);
			//fl.setValue(++i);
			currentSum += i;
		}
		t.write();

		assertTrue("sum should update", listener.latch.await(5, TimeUnit.SECONDS));
		assertEquals("ResourceManipulator (Sum) failed", currentSum, sum.getValue());

		listener.latch = new CountDownLatch(1);
		sumConfig.deactivate();
		for (IntegerResource fl : floats) {
			fl.setValue(++i);
		}
		assertFalse("sum should not update again", listener.latch.await(1, TimeUnit.SECONDS));
		assertEquals("sum should still have old value", currentSum, sum.getValue());

		sumConfig.activate();
		sleep(1000); // activate triggers a rewrite of sum already, wait here to ensure that this will not mistakenly trigger the latch
		listener.setLatch(new CountDownLatch(1));
		currentSum = 0;
		for (IntegerResource fl : floats) {
			t.setInteger(fl, ++i);
			//fl.setValue(++i);
			currentSum += i;
		}
		t.write();
		assertTrue("sum should update", listener.latch.await(5, TimeUnit.SECONDS));
		assertEquals("ResourceManipulator (Sum) failed", currentSum, sum.getValue());
		sum.removeValueListener(listener);
		tool.deleteAllConfigurations();
	}
}
