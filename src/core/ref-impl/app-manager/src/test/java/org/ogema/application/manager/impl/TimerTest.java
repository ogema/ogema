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
package org.ogema.application.manager.impl;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredTimer;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class TimerTest extends OsgiAppTestBase {

	@Test
	/**
	 * does nearly nothing, since the tests are actually in the before() and
	 * after() methods of the super class {@link OsgiAppTestBase}
	 */
	public void applicationIsStarted() {
		assertNotNull(getApplicationManager());
	}

	@Test
	public void listenerIsCalled() throws InterruptedException {
		final CountDownLatch cdl = new CountDownLatch(1);

		TimerListener tl = new TimerListener() {
			@Override
			public void timerElapsed(Timer timer) {
				cdl.countDown();
			}
		};

		getApplicationManager().createTimer(50, tl);
		cdl.await(5, TimeUnit.SECONDS);
		Assert.assertEquals("timer not called", 0, cdl.getCount());
	}

	@Test
	public void stopAndResumeWork() throws InterruptedException {
		final AtomicInteger counter = new AtomicInteger(0);

		TimerListener tl = new TimerListener() {
			@Override
			public void timerElapsed(Timer timer) {
				synchronized (counter) {
					counter.incrementAndGet();
					counter.notifyAll();
				}
			}
		};
		Timer t = getApplicationManager().createTimer(2, tl);
		synchronized (counter) {
			counter.wait(200);
		}
		Assert.assertTrue("listener not called", counter.get() > 0);
		t.stop();
		int c1 = counter.get();
		synchronized (counter) {
			counter.wait(200);
		}
		Assert.assertEquals("listener called on stopped timer", c1, counter.get());
		t.resume();
		synchronized (counter) {
			counter.wait(200);
		}
		Assert.assertTrue("listener not called", counter.get() > c1);
	}
	
	@Test
	public void timerIsRemovedUponDestruction() throws InterruptedException {
		AdminApplication app = getApplicationManager().getAdministrationManager().getAppById(getApplicationManager().getAppID().getIDString());
		List<RegisteredTimer> timers = app.getTimers();
		for (RegisteredTimer t: timers)
			t.getTimer().destroy();
		Assert.assertEquals("Timer removal failed",0, app.getTimers().size());
		final CountDownLatch cdl = new CountDownLatch(1);
		final TimerListener tl = new TimerListener() {
			
			@Override
			public void timerElapsed(Timer timer) {
				cdl.countDown();
			}
		};
		Timer t = getApplicationManager().createTimer(100, tl);
		Assert.assertEquals("Unexpected number of timers",1, app.getTimers().size());
		Assert.assertTrue("Missing timer callback", cdl.await(5, TimeUnit.SECONDS));
		t.destroy();
		Assert.assertEquals("Timer removal failed",0, app.getTimers().size());
	}
}
