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
package org.ogema.channels.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.ogema.channels.tests.utils.ConcurrentTestDriver;
import org.ogema.channels.tests.utils.TestDriver;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import org.junit.Assert;

/**
 * These tests verify that the ChannelManager can deal with many drivers/devices/channels in parallel, 
 * i.e. that there are no synchronization issues. 
 */
@ExamReactorStrategy(PerClass.class)
public class ChannelManagerConcurrencyTest extends ChannelManagerTestBase  {

	/**
	 * Test for ConcurrentModificationException in ChannelManager. Concurrent reads and writes 
	 * by different drivers.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void startMultipleDrivers() throws InterruptedException, ExecutionException, TimeoutException {
		 // guaranteed ConcurrentModificationException with old implementation for ~ 150 drivers, tends to run through for 10 drivers
		int maxNrDrivers = 150;
		List<Future<Integer>> futures = new ArrayList<>();
		final CountDownLatch driverlatch = new CountDownLatch(maxNrDrivers);
		final CountDownLatch adminLatch = new CountDownLatch(1);
		ExecutorService exec = Executors.newFixedThreadPool(maxNrDrivers);
		for (int i=0; i<maxNrDrivers; i++) {
			final TestDriver d = new TestDriver("driver"+i, "Dummy driver " + i);
			startDriverBundle(d);
			final int id = i;
			Callable<Integer> task = new Callable<Integer>() {

				@Override
				public Integer call() throws Exception {
					try {
						// ensure all tasks start simultaneously
						driverlatch.countDown();
						adminLatch.await(1, TimeUnit.MINUTES);
						int samplingPeriod = 100 - new Random().nextInt(80);
						String device = "p" + deviceCounter.incrementAndGet();
						ChannelConfiguration cl = addAndConfigureWritableChannel(d.getDriverId(), "/dev/ttyUSB" + id, device, 
									"02/" + channelCounter.incrementAndGet(), samplingPeriod);
						d.setExpectedChannelRead(cl, 2);
						d.assertChannelRead(cl, 5, TimeUnit.SECONDS); // wait for two readings
						ChannelConfiguration cl2 = addAndConfigureWritableChannel(d.getDriverId(), "/dev/ttyUSB" + id, device, 
								"02/" + channelCounter.incrementAndGet(), (int) ChannelConfiguration.LISTEN_FOR_UPDATE);
						d.setExpectedChannelWrites(cl2, 1);
						d.writeChannel(cl2.getChannelLocator(), new IntegerValue(id));
						d.assertAllChannelsWritten();
						SampledValueContainer svc = new SampledValueContainer(cl2.getChannelLocator());
						List<SampledValueContainer> svcs = new ArrayList<>();
						svcs.add(svc);
						channelAccess.readUnconfiguredChannels(svcs);
						SampledValue sv = svcs.get(0).getSampledValue();
						return sv.getValue().getIntegerValue();
					} catch (Throwable e) { 
						e.printStackTrace(); // for error reporting
						throw e;
					}
				}
			};
			Future<Integer> future = exec.submit(task);
			futures.add(future);
		}
		Assert.assertTrue("Drivers failed to start",driverlatch.await(20, TimeUnit.SECONDS));
		// start simultaneous read/write operations
		adminLatch.countDown();
		for (int i=0; i<maxNrDrivers; i++) {
			Future<Integer> f = futures.get(i);
			int val = f.get(10, TimeUnit.SECONDS);
			Assert.assertEquals("Unexpected channel value in channel " + i,i, val);
		}
	}
	
	/**
	 * Test for ConcurrentModificationException in ChannelManager. Concurrent reads and writes 
	 * by a single drivers and single device but multiple channels.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
//	@Ignore("ConcurrentModificationException (sporadic), UnsupportedOperationException (sporadic), failed Channel reading (sporadic)")
	@Test
	public void singleDriverConcurrentReadWriteWorksForSingleDevice() throws InterruptedException, ExecutionException, TimeoutException {
		// guaranteed ConcurrentModificationException with old implementation for ~ 50 operations; tends to fail even for only 10 operations
		int maxNrParallelOperations = 50; 
		List<Future<Integer>> futures = new ArrayList<>();
		final CountDownLatch driverlatch = new CountDownLatch(maxNrParallelOperations);
		final CountDownLatch adminLatch = new CountDownLatch(1);
		ExecutorService exec = Executors.newFixedThreadPool(maxNrParallelOperations);
		final ConcurrentTestDriver d = new ConcurrentTestDriver("driver", "Dummy driver");
		startDriverBundle(d);
		final String device = "p" + deviceCounter.incrementAndGet();
		for (int i=0; i<maxNrParallelOperations; i++) {
			final int nrChannelWrites = i + 1;
			Callable<Integer> task = new Callable<Integer>() {

				@Override
				public Integer call() throws Exception {
					try {
						int samplingPeriod = 100 - new Random().nextInt(80);
						// ensure all tasks start simultaneously
						driverlatch.countDown();
						adminLatch.await(1, TimeUnit.MINUTES);
						ChannelConfiguration cl = addAndConfigureWritableChannel(d.getDriverId(), "/dev/ttyUSB", device, 
									"02/" + channelCounter.incrementAndGet(), samplingPeriod);
						d.setExpectedChannelRead(cl.getChannelLocator(), 2);
						d.assertChannelRead(cl.getChannelLocator(), 5, TimeUnit.SECONDS); // wait for two readings
						ChannelConfiguration cl2 = addAndConfigureWritableChannel(d.getDriverId(), "/dev/ttyUSB", device, 
								"02/" + channelCounter.incrementAndGet(), (int) ChannelConfiguration.LISTEN_FOR_UPDATE);
						
						d.setExpectedChannelWrites(cl2.getChannelLocator(), nrChannelWrites);
						for (int j=0;j < nrChannelWrites; j++) {
							channelAccess.setChannelValue(cl2, new IntegerValue(j)); // final value is i-1
						}
						d.waitForChannelWrites(cl2.getChannelLocator(), 10, TimeUnit.SECONDS); // wait for write operations
						
						d.setExpectedChannelRead(cl2.getChannelLocator(), 5);
						d.assertChannelRead(cl2.getChannelLocator(), 20, TimeUnit.SECONDS); // wait for reading operations 
						channelAccess.deleteChannel(cl); // otherwise it blocks other channels and the test fails // FIXME probably indicates some bug, too
						SampledValueContainer svc = new SampledValueContainer(cl2.getChannelLocator());
						List<SampledValueContainer> svcs = new ArrayList<>();
						svcs.add(svc);
						channelAccess.readUnconfiguredChannels(svcs);
						SampledValue sv = svcs.get(0).getSampledValue();
						System.out.println( "   channel operation done " + nrChannelWrites);
						return sv.getValue().getIntegerValue();
					} catch (Throwable e) { 
						e.printStackTrace(); // for error reporting
						throw e;
					}
				}
			};
			Future<Integer> future = exec.submit(task);
			futures.add(future);
		}
		Assert.assertTrue("Driver task failed to start",driverlatch.await(1, TimeUnit.MINUTES));
		// start simultaneous read/write operations
		adminLatch.countDown();
		for (int i=0; i<maxNrParallelOperations; i++) {
			Future<Integer> f = futures.get(i);
			int val = f.get(1, TimeUnit.MINUTES);
			Assert.assertEquals("Unexpected channel value in channel " + i,i, val);
		}
	}
	
	/**
	 * Test for ConcurrentModificationException in ChannelManager. Concurrent reads and writes 
	 * by a single drivers for multiple devices with one read and one write channel, each.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
//	@Ignore("see singleDriverConcurrentReadWriteWorksForSingleDevice")
	@Test
	public void singleDriverConcurrentReadWriteWorksForMultipleDevices() throws InterruptedException, ExecutionException, TimeoutException {
		// guaranteed ConcurrentModificationException with old implementation for ~ 50 operations; tends to run through for 5 operations
		int maxNrParallelOperations = 50; 
		List<Future<Integer>> futures = new ArrayList<>();
		final CountDownLatch driverlatch = new CountDownLatch(maxNrParallelOperations);
		final CountDownLatch adminLatch = new CountDownLatch(1);
		ExecutorService exec = Executors.newFixedThreadPool(maxNrParallelOperations);
		final ConcurrentTestDriver d = new ConcurrentTestDriver("driverToo", "Dummy driver");
		startDriverBundle(d);
		for (int i=0; i<maxNrParallelOperations; i++) {
			final int nrChannelWrites = i + 1;
			Callable<Integer> task = new Callable<Integer>() {

				@Override
				public Integer call() throws Exception {
					try {
						int samplingPeriod = 100 - new Random().nextInt(80);
						// ensure all tasks start simultaneously
						driverlatch.countDown();
						adminLatch.await(1, TimeUnit.MINUTES);
						final String device = "p" + deviceCounter.incrementAndGet();
						ChannelConfiguration cl = addAndConfigureWritableChannel(d.getDriverId(), "/dev/ttyUSB", device, 
									"02/" + channelCounter.incrementAndGet(), samplingPeriod);

						ChannelConfiguration cl2 = addAndConfigureWritableChannel(d.getDriverId(), "/dev/ttyUSB", device, 
								"02/" + channelCounter.incrementAndGet(), (int) ChannelConfiguration.LISTEN_FOR_UPDATE);
						d.setExpectedChannelWrites(cl2.getChannelLocator(), nrChannelWrites);
						for (int j=0;j < nrChannelWrites; j++) {
							channelAccess.setChannelValue(cl2, new IntegerValue(j)); // final value is i
						}
						d.waitForChannelWrites(cl2.getChannelLocator(), 10, TimeUnit.SECONDS); // wait for write operations
						
						d.setExpectedChannelRead(cl2.getChannelLocator(), 5);
						d.assertChannelRead(cl2.getChannelLocator(), 20, TimeUnit.SECONDS); // wait for reading operations
						
						d.setExpectedChannelRemoveds(1);
						channelAccess.deleteChannel(cl); // otherwise it blocks other channels and the test fails // FIXME probably indicates some bug, too
						d.assertChannelsRemoved();
						SampledValueContainer svc = new SampledValueContainer(cl2.getChannelLocator());
						List<SampledValueContainer> svcs = new ArrayList<>();
						svcs.add(svc);
						channelAccess.readUnconfiguredChannels(svcs);
						SampledValue sv = svcs.get(0).getSampledValue();
						System.out.println( "   channel operation done " + nrChannelWrites);
						return sv.getValue().getIntegerValue();
					} catch (Throwable e) { 
						e.printStackTrace(); // for error reporting
						throw e;
					}
				}
			};
			Future<Integer> future = exec.submit(task);
			futures.add(future);
		}
		Assert.assertTrue("Driver task failed to start",driverlatch.await(1, TimeUnit.MINUTES));
		// start simultaneous read/write operations
		adminLatch.countDown();
		for (int i=0; i<maxNrParallelOperations; i++) {
			Future<Integer> f = futures.get(i);
			int val = f.get(1, TimeUnit.MINUTES);
			Assert.assertEquals("Unexpected channel value in channel " + i,i, val);
		}
	}
	
}
