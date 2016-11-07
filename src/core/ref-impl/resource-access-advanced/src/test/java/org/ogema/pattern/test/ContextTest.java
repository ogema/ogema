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
package org.ogema.pattern.test;

import org.ogema.pattern.test.pattern.Container;
import org.ogema.pattern.test.pattern.ContextPattern;
import org.ogema.pattern.test.pattern.RoomContext;
import org.ogema.pattern.test.pattern.RoomContextPattern;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.locations.Room;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ContextTest extends OsgiTestBase {

	BooleanResource testRes;

	@Override
	public void doStart(ApplicationManager appMan) {
		super.doStart(appMan);
		testRes = resMan.createResource("testRes", BooleanResource.class);
	}

	public class HeaterListener implements PatternListener<ContextPattern> {

		public volatile boolean available;
		public volatile CountDownLatch foundLatch;
		public volatile CountDownLatch lostLatch;
		public volatile ContextPattern lastPattern = null;

		public HeaterListener() {
			reset();
			available = false;
		}

		@Override
		public void patternAvailable(ContextPattern pattern) {
			//			 System.out.println("Available callback");
			available = true;
			lastPattern = pattern;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(ContextPattern pattern) {
			//			 System.out.println("Unavailable callback");
			available = false;
			lostLatch.countDown();
		}

		public void reset() {
			foundLatch = new CountDownLatch(1);
			lostLatch = new CountDownLatch(1);
		}
	}

	private final List<ContextPattern> patternsForDeletion = new ArrayList<ContextPattern>();

	@Override
	public void doAfter() {
		Iterator<ContextPattern> it = patternsForDeletion.iterator();
		while (it.hasNext()) {
			ContextPattern pt = it.next();
			Resource res = pt.model;
			if (res.exists())
				res.delete();
		}
		patternsForDeletion.clear();
	}

	private final Container container = new Container();

	@Test
	public void registerDemandWorks() throws InterruptedException {
		HeaterListener listener = new HeaterListener();
		advAcc.addPatternDemand(ContextPattern.class, listener, AccessPriority.PRIO_HIGHEST, container);
		ElectricHeater testHeater = resMan.createResource("testHeater", ElectricHeater.class);
		testHeater.location().room().temperatureSensor().reading().create();
		testHeater.onOffSwitch().stateControl().create();
		testHeater.create().activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals("PatternListener availabe callback missing.", 0, listener.foundLatch.getCount());
		assertEquals("unexpected number", 0, listener.lastPattern.getId());
		listener.reset();
		ContextPattern pt = advAcc.createResource("testHeater2", ContextPattern.class);
		advAcc.activatePattern(pt);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals("PatternListener availabe callback missing.", 0, listener.foundLatch.getCount());
		assertEquals("unexpected number", 1, listener.lastPattern.getId());
		testHeater.delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals("PatternListener unavailabe callback missing.", 0, listener.lostLatch.getCount());
		listener.reset();
		pt.model.delete();
		listener.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals("PatternListener unavailabe callback missing.", 0, listener.lostLatch.getCount());
		advAcc.removePatternDemand(ContextPattern.class, listener);
	}

	@Test
	public void patternCreateWorks() throws InterruptedException {

		ContextPattern pattern = advAcc.createResource("testHeater3", ContextPattern.class, container);
		assert (pattern.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", true, pattern.model.onOffSwitch().stateFeedback().getValue());
		ContextPattern pattern2 = advAcc.createResource("testHeater4", ContextPattern.class, container);
		assert (pattern2.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", false, pattern2.model.onOffSwitch().stateFeedback().getValue());
		pattern.model.delete();
		pattern2.model.delete();
	}

	@Test
	public void patternAddDecoratorWorks() throws InterruptedException {

		Resource base = resMan.createResource("toplevel", ElectricHeater.class);
		ContextPattern pattern = advAcc.addDecorator(base, "testHeater5", ContextPattern.class, container);
		assert (pattern.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", true, pattern.model.onOffSwitch().stateFeedback().getValue());
		ContextPattern pattern2 = advAcc.addDecorator(base, "testHeater6", ContextPattern.class, container);
		assert (pattern2.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", false, pattern2.model.onOffSwitch().stateFeedback().getValue());
		base.delete();

	}

	@Test
	public void getAllPatternsWorks() throws InterruptedException {

		ContextPattern pattern = advAcc.createResource("testHeater7", ContextPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		List<ContextPattern> patterns = advAcc.getPatterns(ContextPattern.class, AccessPriority.PRIO_LOWEST,
				container);
		assertEquals("Found unexpected number of patterns.", 1, patterns.size());
		pattern = advAcc.createResource("testHeater8", ContextPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater9", ContextPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater10", ContextPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater11", ContextPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater12", ContextPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		patterns = advAcc.getPatterns(ContextPattern.class, AccessPriority.PRIO_LOWEST, container);
		assertEquals("Found unexpected number of patterns.", 4, patterns.size()); // the pattern initialisation used here is anything but sensible!
		pattern.model.delete();
		for (ContextPattern pt : patterns) {
			pt.model.delete();
		}
		patterns = advAcc.getPatterns(ContextPattern.class, AccessPriority.PRIO_LOWEST, container);
		assertEquals("Found unexpected number of patterns.", 0, patterns.size()); //  accept() condition fails again
	}
	
	public class RoomContextListener implements PatternListener<RoomContextPattern> {
		
		volatile CountDownLatch foundLatch;
		volatile CountDownLatch lostLatch;
		volatile RoomContextPattern lastPattern = null;
		volatile int counterUp = 0;
		volatile int counterDown = 0;
		
		public RoomContextListener() {
			reset();
		}

		@Override
		public void patternAvailable(RoomContextPattern pattern) {
			// FIXME 
			System.out.println("  ooo Pattern available: " + pattern.model.getLocation());
			lastPattern = pattern;
			counterUp++;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(RoomContextPattern pattern) {
			// FIXME 
			System.out.println("  ooo Pattern unavailable: " + pattern.model.getLocation());
			counterDown++;
			lostLatch.countDown();
		}
		
		public void reset() {
			foundLatch = new CountDownLatch(1);
			lostLatch = new CountDownLatch(1);
		}
	}

	@Ignore("feature not supported, see updated documentation")
	@Test 
	public void multipleListenersWithDifferentContextsWork() throws InterruptedException {
		Room room1 = resMan.createResource("room1", Room.class);
		Room room2 = resMan.createResource("room2", Room.class);
		Room room3 = resMan.createResource("room3", Room.class);
		room1.activate(false); room2.activate(false); room3.activate(false);
		RoomContext context1 = new RoomContext(room1);
		RoomContext context2 = new RoomContext(room2);
		
		RoomContextListener listener1 = new RoomContextListener();
		RoomContextListener listener2 = new RoomContextListener();
		
		advAcc.addPatternDemand(RoomContextPattern.class, listener1, AccessPriority.PRIO_LOWEST, context1);
		advAcc.addPatternDemand(RoomContextPattern.class, listener2, AccessPriority.PRIO_LOWEST, context2);
		
		ElectricHeater heater1 = resMan.createResource("heater1", ElectricHeater.class);
		ElectricHeater heater2 = resMan.createResource("heater2", ElectricHeater.class);
		ElectricHeater heater3 = resMan.createResource("heater3", ElectricHeater.class);
		heater1.activate(false); heater2.activate(false); heater3.activate(false);
		heater1.location().room().setAsReference(room1);
		listener1.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals("Pattern callback missing",0, listener1.foundLatch.getCount());
		heater2.location().room().setAsReference(room2);
		listener2.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals("Pattern callback missing",0, listener2.foundLatch.getCount());  
		heater3.location().room().setAsReference(room3);
		Thread.sleep(200); // if there were any wrong callbacks, they'd need some time to execute
		assertEquals("Unexpected number of pattern callbacks",1,listener1.counterUp);
		assertEquals("Unexpected number of pattern callbacks",1,listener2.counterUp);
		assertEquals("Unexpected number of pattern callbacks",0,listener1.counterDown);
		assertEquals("Unexpected number of pattern callbacks",0,listener2.counterDown);
		heater1.delete();
		listener1.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals("Pattern callback missing",0, listener1.lostLatch.getCount());
		heater2.delete();
		listener2.lostLatch.await(5, TimeUnit.SECONDS);
		assertEquals("Pattern callback missing",0, listener2.lostLatch.getCount()); // fails
		heater3.delete();
		Thread.sleep(200); // if there were any wrong callbacks, they'd need some time to execute
		assertEquals("Unexpected number of pattern callbacks",1,listener1.counterUp);
		assertEquals("Unexpected number of pattern callbacks",1,listener2.counterUp);
		assertEquals("Unexpected number of pattern callbacks",1,listener1.counterDown);
		assertEquals("Unexpected number of pattern callbacks",1,listener2.counterDown);		
		
		advAcc.removePatternDemand(RoomContextPattern.class, listener1);
		advAcc.removePatternDemand(RoomContextPattern.class, listener2);
	}
	
	
}
