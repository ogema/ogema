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
package org.ogema.exam;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;

public class PatternTestListener<P extends ResourcePattern<?>> implements PatternListener<P> {

	private volatile CountDownLatch foundLatch;
	private volatile CountDownLatch lostLatch;
	public volatile P expectedFoundPattern;
	public volatile P expectedLostPattern;
	public volatile P lastAvailable;
	public volatile P lastUnavailable;
	
	public PatternTestListener() {
		reset();
	}
	
	@Override
	public void patternAvailable(P pattern) {
		System.out.println(" -- pattern availble: " + pattern.model);
		if (expectedFoundPattern != null)
			Assert.assertEquals("Unexpected pattern in callback",expectedFoundPattern, pattern);
		expectedFoundPattern = null;
		lastAvailable = pattern;
		foundLatch.countDown();
		
	}

	@Override
	public void patternUnavailable(P pattern) {
		System.out.println(" -- pattern unavailble: " + pattern.model);
		if (expectedLostPattern != null)
			Assert.assertEquals("Unexpected pattern in callback",expectedLostPattern, pattern);
		expectedLostPattern = null;
		lastUnavailable = pattern;
		lostLatch.countDown();
	}

	public void reset() {
		reset(1);
	}
	
	public void reset(int nr) {
		foundLatch = new CountDownLatch(nr);
		lostLatch = new CountDownLatch(nr);
		expectedFoundPattern = null;
		expectedLostPattern = null;
		lastAvailable = null;
		lastUnavailable = null;
	}
	
    public boolean awaitFoundEvent(long timeout, TimeUnit unit) throws InterruptedException {
        return foundLatch.await(timeout, unit);
    }
	
    public boolean awaitFoundEvent() throws InterruptedException {
        return awaitFoundEvent(5, TimeUnit.SECONDS);
    }
    
    public boolean awaitLostEvent(long timeout, TimeUnit unit) throws InterruptedException {
        return lostLatch.await(timeout, unit);
    }
	
    public boolean awaitLostEvent() throws InterruptedException {
    	return awaitLostEvent(5, TimeUnit.SECONDS);
    }
    
}
