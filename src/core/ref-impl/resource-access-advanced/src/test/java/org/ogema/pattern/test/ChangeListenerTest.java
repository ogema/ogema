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
package org.ogema.pattern.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.CompoundResourceEvent;
import org.ogema.core.resourcemanager.pattern.PatternChangeListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.locations.Room;
import org.ogema.pattern.test.pattern.ChangeListenerPattern;
import org.ogema.pattern.test.pattern.ChangeListenerPattern2;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class ChangeListenerTest extends OsgiTestBase {
	
	private ResourcePatternAccess rpa;
	private final ChangeListener<ChangeListenerPattern> listener = new ChangeListener<>();

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	@Before
	public void init() {
		this.rpa = getApplicationManager().getResourcePatternAccess();
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void structureCallbackWorks() throws InterruptedException {
		ChangeListenerPattern pattern1 = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		rpa.activatePattern(pattern1);
		Room room = resMan.createResource(newResourceName(), Room.class);
		pattern1.room.setAsReference(room);
		pattern1.name.<StringResource> create().setValue("test");
		listener.reset(1);
		rpa.addPatternChangeListener(pattern1, listener, ChangeListenerPattern.class);
		org.ogema.core.resourcemanager.Transaction transaction = resAcc.createTransaction();
		transaction.addResource(pattern1.name);
		transaction.addResource(room);
		transaction.activate();		
		Assert.assertTrue("Missing pattern change callback",listener.eventLatch.await(5, TimeUnit.SECONDS));
		Assert.assertFalse("Too many callbacks for pattern change listener",listener.latch.await(2, TimeUnit.SECONDS));
		Assert.assertEquals("Callback contains wrong number of event information",2,listener.lastEvents.size());
		pattern1.model.delete();
	}
	
	@Test
	public void structureCallbackWorksWithNonAtomicChanges() throws InterruptedException {
		ChangeListenerPattern pattern1 = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		rpa.activatePattern(pattern1);
		Room room = resMan.createResource(newResourceName(), Room.class);
		pattern1.room.setAsReference(room);
		pattern1.name.<StringResource> create().setValue("test");
		// we do not know how many callbacks there are going to be,
		// since the PatternChanged listener does not make a guarantee as to whether 
		// non-atomic structure changes are reported in one or multiple callbacks.
		// But four is the upper limit here, since there are going to be four changes.
		listener.reset(4); 
		rpa.addPatternChangeListener(pattern1, listener, ChangeListenerPattern.class);
		pattern1.name.activate(false);
		room.activate(false);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pattern1.name.addDecorator("test", FloatResource.class);
		room.delete(); 
		listener.eventLatch.await(2, TimeUnit.SECONDS);
		Assert.assertEquals("Missing structure events in pattern change listener",4,listener.allEvents.size());
		pattern1.model.delete();
	}
	
	@Test
    @SuppressWarnings("deprecation")
	public void structureCallbackWorksTwice() throws InterruptedException {
		ChangeListenerPattern pattern1 = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		rpa.activatePattern(pattern1);
		Room room = resMan.createResource(newResourceName(), Room.class);
		pattern1.room.setAsReference(room);
		pattern1.name.<StringResource> create().setValue("test");
		listener.reset(1);
		rpa.addPatternChangeListener(pattern1, listener, ChangeListenerPattern.class);
		org.ogema.core.resourcemanager.Transaction transaction = resAcc.createTransaction();
		transaction.addResource(pattern1.name);
		transaction.addResource(room);
		transaction.activate();		
		Assert.assertTrue("Missing pattern change callback",listener.eventLatch.await(5, TimeUnit.SECONDS));
		listener.reset(1);
		transaction = resAcc.createTransaction();
		transaction.addResource(pattern1.name);
		transaction.addResource(room);
		transaction.deactivate();
		Assert.assertTrue("Missing pattern change callback",listener.eventLatch.await(5, TimeUnit.SECONDS));
		Assert.assertFalse("Too many callbacks for pattern change listener",listener.latch.await(2, TimeUnit.SECONDS));
		Assert.assertEquals("Callback contains wrong number of event information",2,listener.lastEvents.size());
		pattern1.model.delete();
	}
	
	@Test
    @SuppressWarnings("deprecation")
	public void noSpuriousStructureCallbacks() throws InterruptedException {
		ChangeListenerPattern pattern1 = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		rpa.activatePattern(pattern1);
		Room room = resMan.createResource(newResourceName(), Room.class);
		pattern1.room.setAsReference(room);
		pattern1.name.<StringResource> create().setValue("test");
		listener.reset(1);
		rpa.addPatternChangeListener(pattern1, listener, ChangeListenerPattern.class);
		org.ogema.core.resourcemanager.Transaction transaction = resAcc.createTransaction();
		transaction.addResource(pattern1.name);
		transaction.addResource(room);
		transaction.activate();		
		Assert.assertTrue("Missing pattern change callback",listener.eventLatch.await(5, TimeUnit.SECONDS));
		listener.reset(1);
		rpa.removePatternChangeListener(pattern1, listener);
		transaction = resAcc.createTransaction();
		transaction.addResource(pattern1.name);
		transaction.addResource(room);
		transaction.deactivate();
		Assert.assertFalse("Spurious structure callback in pattern change listener",listener.eventLatch.await(2, TimeUnit.SECONDS));
		pattern1.model.delete();
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void valueCallbackWorks() throws InterruptedException {
		ChangeListenerPattern pattern1 = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		Room room = resMan.createResource(newResourceName(), Room.class);
		pattern1.room.setAsReference(room);
		pattern1.name.<StringResource> create().setValue("test");
		rpa.activatePattern(pattern1);
		listener.reset(1);
		rpa.addPatternChangeListener(pattern1, listener, ChangeListenerPattern.class);
		org.ogema.core.resourcemanager.Transaction transaction = resAcc.createTransaction();
		transaction.addResource(pattern1.name);
		transaction.addResource(pattern1.reading);
		transaction.setString(pattern1.name, "newValue");
		transaction.setFloat(pattern1.reading, 271.3F);
		transaction.write();		
		Assert.assertTrue("Missing pattern change callback",listener.eventLatch.await(5, TimeUnit.SECONDS));
		Assert.assertFalse("Too many callbacks for pattern change listener",listener.latch.await(2, TimeUnit.SECONDS));
		Assert.assertEquals("Callback contains wrong number of event information",2,listener.lastEvents.size());
		listener.reset(1);
		rpa.removePatternChangeListener(pattern1, listener);
		pattern1.model.delete();
		Assert.assertFalse("Callback for pattern change listener although it has been removed.",
				listener.eventLatch.await(1, TimeUnit.SECONDS));
	}
	
	@Test
	public void multipleChangeListenersWork() throws InterruptedException {
		ChangeListenerPattern pattern1a = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		ChangeListenerPattern pattern1b = rpa.createResource(newResourceName(), ChangeListenerPattern.class);
		ChangeListenerPattern2 pattern2 = rpa.createResource(newResourceName(), ChangeListenerPattern2.class);
		pattern2.type.create();
		rpa.activatePattern(pattern1a);
		rpa.activatePattern(pattern1b);
		rpa.activatePattern(pattern2);
		
		final ChangeListener<ChangeListenerPattern> listener1a = new ChangeListener<>();
		final ChangeListener<ChangeListenerPattern> listener1b = new ChangeListener<>();
		final ChangeListener<ChangeListenerPattern2> listener2 = new ChangeListener<>();
		
		rpa.addPatternChangeListener(pattern1a, listener1a, ChangeListenerPattern.class);
		rpa.addPatternChangeListener(pattern1b, listener1b, ChangeListenerPattern.class);
		rpa.addPatternChangeListener(pattern2, listener2, ChangeListenerPattern2.class);
		
		pattern1a.reading.setValue(4711);
		Assert.assertTrue("Callback missing",listener1a.eventLatch.await(5, TimeUnit.SECONDS));
		Thread.sleep(500);
		Assert.assertEquals("Listener got an unexpected callback",1,listener1b.eventLatch.getCount()); // no callback expected
		Assert.assertEquals("Listener got an unexpected callback",1,listener2.eventLatch.getCount()); // no callback expected
		
		listener1a.reset(1);
		
		pattern2.type.setValue(4712);
		Assert.assertTrue("Callback missing",listener2.eventLatch.await(5, TimeUnit.SECONDS));
		Thread.sleep(500);
		Assert.assertEquals("Listener got an unexpected callback",1,listener1a.eventLatch.getCount()); // no callback expected
		Assert.assertEquals("Listener got an unexpected callback",1,listener1b.eventLatch.getCount()); // no callback expected
		
		rpa.removePatternChangeListener(pattern1a, listener1a);
		rpa.removePatternChangeListener(pattern1b, listener1b);
		rpa.removePatternChangeListener(pattern2, listener2);
		
		pattern1a.model.delete();
		pattern1b.model.delete();
		pattern2.model.delete();
		
	}
	
	
	// TODO mixed events.
	
	private static class ChangeListener<P extends ResourcePattern<?>> implements PatternChangeListener<P> {
		
		// used to check if any callbacks occur at all
		private volatile CountDownLatch eventLatch;
		// used to check if the right number of callbacks occur
		private volatile CountDownLatch latch;
		private volatile List<CompoundResourceEvent<?>> lastEvents = null;
		private final List<CompoundResourceEvent<?>> allEvents = Collections.synchronizedList(new ArrayList<CompoundResourceEvent<?>>());
		
		public ChangeListener() {
			reset(1);
		}

		@Override
		public void patternChanged(P instance, List<CompoundResourceEvent<?>> changes) {
			// FIXME
			System.out.println(" Pattern changed callback, " + instance);
			lastEvents = changes;
			allEvents.addAll(changes);
			eventLatch.countDown();
			latch.countDown();
		}
		
		public void reset(int expectedNrOfCallbacks) {
			allEvents.clear();
			this.eventLatch = new CountDownLatch(expectedNrOfCallbacks);
			this.latch = new CountDownLatch(expectedNrOfCallbacks+1);
		}
		
	}
	
	
}
