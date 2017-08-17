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
package org.ogema.resourcemanager.addon.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.PowerResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent.EventType;
import org.ogema.exam.ResourceAssertions;
import org.ogema.exam.StructureTestListener;
import org.ogema.model.locations.Room;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ResourceManagerConcurrencyTest extends OsgiTestBase {
	
	// helper method to execute a certain number of tasks in parallel by multiple applications
	// here, each task is executed by a different app
	protected <S> List<Future<S>> executeTasks(final Collection<AppCallable<S>> tasks) throws InterruptedException {
		int nrApps = tasks.size();
		final CountDownLatch appsLatch = new CountDownLatch(nrApps);
		final CountDownLatch adminLatch = new CountDownLatch(1);
		final ExecutorService exec = Executors.newFixedThreadPool(nrApps);
		final List<Future<S>> results=  new ArrayList<>();
		startApps(nrApps);
		final AtomicInteger cnt = new AtomicInteger(0);
		Iterator<AppCallable<S>> it = tasks.iterator();
		while (it.hasNext()) {
			final AppCallable<S> task = it.next();
			Callable<S> callable = new Callable<S>() {

				@Override
				public S call() throws Exception {
					// wait until all apps are ready to execute their task
					appsLatch.countDown();
					Assert.assertTrue(adminLatch.await(30, TimeUnit.SECONDS));
					return task.call(apps.get(cnt.getAndIncrement()));
				}
			};
			results.add(exec.submit(callable));
		}
		Assert.assertTrue("App tasks not ready...", appsLatch.await(10, TimeUnit.SECONDS));
		adminLatch.countDown();
		exec.shutdown();
		return results;
	}
	
	// helper method to execute a certain number of tasks in parallel by multiple applications
	protected <S> List<Future<S>> executeTasks(int nrApps, final AppCallable<S> task) throws InterruptedException {
		final CountDownLatch appsLatch = new CountDownLatch(nrApps);
		final CountDownLatch adminLatch = new CountDownLatch(1);
		final ExecutorService exec = Executors.newFixedThreadPool(nrApps);
		final List<Future<S>> results=  new ArrayList<>();
		startApps(nrApps);
		for (final TestApp app: apps) {
			Callable<S> callable = new Callable<S>() {

				@Override
				public S call() throws Exception {
					// wait until all apps are ready to execute their task
					appsLatch.countDown();
					Assert.assertTrue(adminLatch.await(30, TimeUnit.SECONDS));
					return task.call(app);
				}
			};
			
			// avoid using the app thread, so callbacks can still execute in parallel
//			results.add(app.getAppManager().submitEvent(callable));
			results.add(exec.submit(callable)); 
		}
		Assert.assertTrue("App tasks not ready...", appsLatch.await(10, TimeUnit.SECONDS));
		adminLatch.countDown();
		exec.shutdown();
        //exec.awaitTermination(30, TimeUnit.SECONDS);
		return results;
	}
	
	
	// a task that can be executed once per app
	private interface AppCallable<S> {
		
		S call(final TestApp app);
		
	}
	
	@Test
	public void multipleAppsAccessResourceInParallel() throws InterruptedException, ExecutionException, TimeoutException {
		int nrApps = 50;
		final Resource res = resMan.createResource(newResourceName(), Resource.class);
		final String path  = res.getPath();
		AppCallable<Resource> task = new AppCallable<Resource>() {
			
			@Override
			public Resource call(TestApp app) {
				return app.getAppManager().getResourceAccess().getResource(path);
			}
			
		};

		List<Future<Resource>> results = executeTasks(nrApps, task);
		for (Future<Resource> future: results) {
			Resource resourceResult = future.get(5, TimeUnit.SECONDS);
			Assert.assertEquals("Unexpected Resource path " + path, path, resourceResult.getPath());;
		}
		res.delete();
		shutdownApps();
	}
	
	@Test
	public void multipleAppsManipulateResourceTreeInParallel() throws InterruptedException, ExecutionException, TimeoutException {
		final int nrApps = 50;
		final TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final Room room = resMan.createResource(newResourceName(), Room.class);
		final String sensorPath  = tempSens.getPath();
		final String roomPath = room.getPath();
		final float temperature = 20;
		AppCallable<Float> task = new AppCallable<Float>() {

			@Override
			public Float call(TestApp app) {
				TemperatureSensor res = app.getAppManager().getResourceAccess().getResource(sensorPath);
				Room room = app.getAppManager().getResourceAccess().getResource(roomPath);
				if (app.getId() % 2 == 0) {
					room.temperatureSensor().setAsReference(res);
					room.addDecorator("decorator" + app.getId(), tempSens);
					res.location().room().name().<StringResource> create().setValue("Moin: " + app.getAppManager().getAppID().getIDString());
					res.location().room().setAsReference(room);
					res.reading().<TemperatureResource> create().setCelsius(temperature);
				} else {
					res.reading().<TemperatureResource> create().setCelsius(temperature);
					res.location().room().setAsReference(room);
					res.location().room().name().<StringResource> create().setValue("Moin: " + app.getAppManager().getAppID().getIDString());
					room.addDecorator("decorator" + app.getId(), tempSens);
					room.temperatureSensor().setAsReference(res);
				}
				ResourceAssertions.assertReference(room.temperatureSensor());
				ResourceAssertions.assertReference(res.location().room());
				return room.temperatureSensor().reading().getCelsius();
			}
			
		};

		List<Future<Float>> results = executeTasks(nrApps, task);
		for (Future<Float> future: results) {
			float resourceResult = future.get(5, TimeUnit.SECONDS);
			Assert.assertEquals("Unexpected Temperature value " + resourceResult + ", expected " + temperature, temperature, resourceResult, 0.1F);;
		}
		tempSens.delete();
		room.delete();
		shutdownApps();
	}
	
	
	private void manyAppsOneStructureListener(final Class<? extends Resource> baseType) throws InterruptedException {
		final int nrApps = 250;
        
        /* XXX: without 'pre-loading' the classes, the callable will sometimes
        get a ClassNotFoundException. OSGi framework class loading bug? */
        List<Class<? extends Resource>> classes = Arrays.asList(PowerResource.class,
                FloatResource.class, TemperatureSensor.class, AbsoluteSchedule.class, SingleValueResource.class, IntegerArrayResource.class);
        System.out.println(classes);
        
		final Resource sensor = resMan.createResource(newResourceName(), baseType);
		final String sensorPath = sensor.getPath();
		final StructureTestListener listener = new StructureTestListener();
		sensor.addStructureListener(listener);
		listener.reset(nrApps);
		final AtomicInteger deleteCounter = new AtomicInteger(0);
		final AppCallable<Void> task = new AppCallable<Void>() {
			
			@Override
			public Void call(TestApp app) {
                //System.out.printf("testapp %d call()%n", app.getId());
				Resource sensor = resAcc.getResource(sensorPath);
                Assert.assertNotNull(sensor);
				Class<? extends Resource> type;
				switch (app.getId() % 4) {
				case 0:
					type = ResourceList.class;
					break;
				case 1:
					if (SingleValueResource.class.isAssignableFrom(baseType))
						type = AbsoluteSchedule.class;
					else 
						type = IntegerArrayResource.class;
					break;
				case 2: 
					type = PowerResource.class;
					break;
				default:
					type = TemperatureSensor.class;
					break;
				}
                String subName = "sub"+ app.getId();
                Resource sub = sensor.getSubResource(subName, type);
                Assert.assertNotNull(String.format("virtual subresource %s, type %s is null", subName, type), sub);
				sub.create();
                //System.out.println("created sub" + app.getId());
				Resource res = sensor.getSubResource("sub" + (app.getId()-1));
				if (res != null && res.exists()) {
					res.delete();
					deleteCounter.incrementAndGet();
				}
				return null;
			}
			
		};
		List<Future<Void>> results = executeTasks(nrApps, task);
        Assert.assertEquals(nrApps, results.size());
        for (Future<Void> f: results) {
            try {
                f.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException ee) {
                System.out.printf("exception in app call: %s%n", ee.getCause());
                ee.getCause().printStackTrace(System.out);
            } catch (TimeoutException ex) {
                System.out.printf("app call timed out: %s%n", ex);
            }
        }
        
        if (!listener.awaitEvent(EventType.SUBRESOURCE_ADDED, 15, TimeUnit.SECONDS)) {
            Assert.fail("Missing structure listener callback; " + listener.getEventCount(EventType.SUBRESOURCE_ADDED) + " of expected " + nrApps);
        }
        
		int deleted = 0;
		int deleteExpected = deleteCounter.get();
		int counter = 0;
		while (deleted < deleteExpected && counter++ < 300) { // wait for max 15s; we cannot use a latch, because the number of deleted resources is not knwon in advance 
			Thread.sleep(50);
			deleted = listener.getEventCount(EventType.SUBRESOURCE_REMOVED);
		}
		Assert.assertEquals("Missing delete callbacks",deleteExpected, listener.getEventCount(EventType.SUBRESOURCE_REMOVED));
        
		sensor.removeStructureListener(listener);
		sensor.delete();
	}
	
	//@Ignore("Fails occasionally")
	@Test
	public void manyAppsOneStructureListener1() throws InterruptedException {
		manyAppsOneStructureListener(FloatResource.class);
	}
	
	@Ignore("Fails occasionally")
	@Test
	public void manyAppsOneStructureListener2() throws InterruptedException {
		manyAppsOneStructureListener(TemperatureSensor.class);
	}
	

	@Test
	public void referencingInParallelThreadWorks() throws InterruptedException, ExecutionException, TimeoutException {
		final Room room = resMan.createResource(newResourceName(), Room.class);
		final TemperatureSensor tempSens = resMan.createResource(newResourceName(), TemperatureSensor.class);
		final TemperatureSensor sensor = room.temperatureSensor().create();
		AppCallable<Void> task1 = new AppCallable<Void>() {

			@Override
			public Void call(TestApp app) {
				room.temperatureSensor().setAsReference(tempSens);
				return null;
			}
			
		};
		AppCallable<Void> task2 = new AppCallable<Void>() {

			@Override
			public Void call(TestApp app) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
				sensor.reading().create();
				ResourceAssertions.assertLocationsEqual(sensor.reading(), tempSens.reading());
				return null;
			}
			
		};
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<AppCallable<Void>> tasks = (List) Arrays.asList(new AppCallable[]{ task1, task2 });
		for (Future<?> f: executeTasks(tasks)) {
			f.get(5, TimeUnit.SECONDS);
		}
		room.delete();
		tempSens.delete();
		shutdownApps();
	}
	
	/**
	 * The schedule iterator is supposed to be fail-safe w.r.t. concurrent modification. The test verifies that this
	 * is indeed the case.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void scheduleIterationInParallelWorks() throws InterruptedException, ExecutionException, TimeoutException {
		int nrApps = 50;
		final FloatResource res = resMan.createResource(newResourceName(), FloatResource.class);
		res.program().create().activate(false);
		final int nrDataPoints = 10000;
		final long delta = 10;
		List<SampledValue> values = new ArrayList<>();
		for (int i=0;i<nrDataPoints;i++) {
			values.add(new SampledValue(new FloatValue((float) Math.random()), delta*i, Quality.GOOD));
		}
		res.program().addValues(values);
		AppCallable<Integer> task = new AppCallable<Integer>() {
			
			@Override
			public Integer call(TestApp app) {
				if (Math.random() > 0.5) {
					Iterator<SampledValue> it = res.program().iterator();
					int cnt = 0;
					while (it.hasNext()) {
						it.next();
						cnt++;
					}
					return cnt;
				}
				else {
					double rand = Math.random();
					if (rand > 0.5) {
						res.program().addValue((long) (rand*delta*nrDataPoints), new FloatValue(23.5F));
					} else {
						try {
							SampledValue sv = res.program().getValues(Long.MIN_VALUE).get((int) (rand * nrDataPoints));
							res.program().deleteValues(sv.getTimestamp(), sv.getTimestamp()+1);
						}catch (Exception ignore) {}
					}
					// these will trivially satisfy the test
					return nrDataPoints;
				}
			}
			
		};

		List<Future<Integer>> results = executeTasks(nrApps, task);
		for (Future<Integer> future: results) {
			Integer result = future.get(5, TimeUnit.SECONDS);
			Assert.assertTrue("Unexpected number of data points from schedule iteration: got " + result + ", expected approx. " + nrDataPoints,
					result < nrDataPoints + nrApps);
			Assert.assertTrue("Unexpected number of data points from schedule iteration: got " + result + ", expected approx. " + nrDataPoints,
					result > nrDataPoints - nrApps);
		}
		res.delete();
		shutdownApps();
	}
	
}
