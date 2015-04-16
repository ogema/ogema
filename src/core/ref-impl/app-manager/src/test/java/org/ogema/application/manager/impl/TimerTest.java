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
package org.ogema.application.manager.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

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
}
