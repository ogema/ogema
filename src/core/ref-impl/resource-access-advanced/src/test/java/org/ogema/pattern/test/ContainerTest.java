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
import org.ogema.pattern.test.pattern.ContainerPattern;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ContainerTest extends OsgiTestBase {

	BooleanResource testRes;

	@Override
	public void doStart(ApplicationManager appMan) {
		super.doStart(appMan);
		testRes = resMan.createResource("testRes", BooleanResource.class);
	}

	public class HeaterListener implements PatternListener<ContainerPattern> {

		public volatile boolean available;
		public volatile CountDownLatch foundLatch;
		public volatile CountDownLatch lostLatch;
		public volatile ContainerPattern lastPattern = null;

		public HeaterListener() {
			reset();
			available = false;
		}

		@Override
		public void patternAvailable(ContainerPattern pattern) {
			//			 System.out.println("Available callback");
			available = true;
			lastPattern = pattern;
			foundLatch.countDown();
		}

		@Override
		public void patternUnavailable(ContainerPattern pattern) {
			//			 System.out.println("Unavailable callback");
			available = false;
			lostLatch.countDown();
		}

		public void reset() {
			foundLatch = new CountDownLatch(1);
			lostLatch = new CountDownLatch(1);
		}
	}

	private final List<ContainerPattern> patternsForDeletion = new ArrayList<ContainerPattern>();

	@Override
	public void doAfter() {
		Iterator<ContainerPattern> it = patternsForDeletion.iterator();
		while (it.hasNext()) {
			ContainerPattern pt = it.next();
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
		advAcc.addPatternDemand(ContainerPattern.class, listener, AccessPriority.PRIO_HIGHEST, container);
		ElectricHeater testHeater = resMan.createResource("testHeater", ElectricHeater.class);
		testHeater.location().room().temperatureSensor().reading().create();
		testHeater.onOffSwitch().stateControl().create();
		testHeater.create().activate(true);
		listener.foundLatch.await(5, TimeUnit.SECONDS);
		assertEquals("PatternListener availabe callback missing.", 0, listener.foundLatch.getCount());
		assertEquals("unexpected number", 0, listener.lastPattern.getId());
		listener.reset();
		ContainerPattern pt = advAcc.createResource("testHeater2", ContainerPattern.class);
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
		advAcc.removePatternDemand(ContainerPattern.class, listener);
	}

	@Test
	public void patternCreateWorks() throws InterruptedException {

		ContainerPattern pattern = advAcc.createResource("testHeater3", ContainerPattern.class, container);
		assert (pattern.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", true, pattern.model.onOffSwitch().stateFeedback().getValue());
		ContainerPattern pattern2 = advAcc.createResource("testHeater4", ContainerPattern.class, container);
		assert (pattern2.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", false, pattern2.model.onOffSwitch().stateFeedback().getValue());
		pattern.model.delete();
		pattern2.model.delete();
	}

	@Test
	public void patternAddDecoratorWorks() throws InterruptedException {

		Resource base = resMan.createResource("toplevel", ElectricHeater.class);
		ContainerPattern pattern = advAcc.addDecorator(base, "testHeater5", ContainerPattern.class, container);
		assert (pattern.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", true, pattern.model.onOffSwitch().stateFeedback().getValue());
		ContainerPattern pattern2 = advAcc.addDecorator(base, "testHeater6", ContainerPattern.class, container);
		assert (pattern2.model.onOffSwitch().stateFeedback().exists()) : "Pattern creation failed";
		assertEquals("Pattern init method failed", false, pattern2.model.onOffSwitch().stateFeedback().getValue());
		base.delete();

	}

	@Test
	public void getAllPatternsWorks() throws InterruptedException {

		ContainerPattern pattern = advAcc.createResource("testHeater7", ContainerPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		List<ContainerPattern> patterns = advAcc.getPatterns(ContainerPattern.class, AccessPriority.PRIO_LOWEST,
				container);
		assertEquals("Found unexpected number of patterns.", 1, patterns.size());
		pattern = advAcc.createResource("testHeater8", ContainerPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater9", ContainerPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater10", ContainerPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater11", ContainerPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		pattern = advAcc.createResource("testHeater12", ContainerPattern.class, container);
		advAcc.activatePattern(pattern);
		patternsForDeletion.add(pattern);
		patterns = advAcc.getPatterns(ContainerPattern.class, AccessPriority.PRIO_LOWEST, container);
		assertEquals("Found unexpected number of patterns.", 4, patterns.size()); // the pattern initialisation used here is anything but sensible!
		pattern.model.delete();
		for (ContainerPattern pt : patterns) {
			pt.model.delete();
		}
		patterns = advAcc.getPatterns(ContainerPattern.class, AccessPriority.PRIO_LOWEST, container);
		assertEquals("Found unexpected number of patterns.", 0, patterns.size()); //  accept() condition fails again
	}

}
