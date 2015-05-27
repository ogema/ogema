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

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

		private CountDownLatch latch = new CountDownLatch(1);

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
}
