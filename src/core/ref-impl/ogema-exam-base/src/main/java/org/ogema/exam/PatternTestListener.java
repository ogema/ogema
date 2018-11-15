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
