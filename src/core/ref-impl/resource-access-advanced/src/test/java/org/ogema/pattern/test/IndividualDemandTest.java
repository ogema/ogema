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

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.exam.PatternTestListener;
import org.ogema.pattern.test.pattern.RoomPattern;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class IndividualDemandTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	@Test
	public void individualPatternDemandWorksInSimpleSetting() throws InterruptedException {
		String suffix = newResourceName();
		PatternTestListener<RoomPattern> listener = new PatternTestListener<RoomPattern>();
		RoomPattern pattern = advAcc.createResource("room1_" + suffix, RoomPattern.class);
		advAcc.addIndividualPatternDemand(RoomPattern.class, pattern, listener, AccessPriority.PRIO_LOWEST);
		boolean callback = listener.awaitFoundEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("Pattern callback, although the pattern is inactive", callback);
		
		activateAndVerifyCallback(pattern, listener);
		
		listener.reset();
		// check that we don't get a callback from an unwanted pattern
		RoomPattern pattern2 = advAcc.createResource("room2_" + suffix, RoomPattern.class);
		advAcc.activatePattern(pattern2);
		callback = listener.awaitFoundEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("Pattern callback for the wrong pattern", callback);
		
		pattern2.model.delete();
		callback = listener.awaitLostEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("Pattern callback for the wrong pattern", callback);
		
		deleteAndVerifyCallback(pattern, listener,true);
		advAcc.removeAllIndividualPatternDemands(RoomPattern.class, listener);
	}
	
	// in this test, two patterns are created and registered to the listener, and an additional three are 
	// created but not registered.
	@Test
	public void individualPatternDemandWorksForMultiplePatterns() throws InterruptedException {
		String suffix = newResourceName();
		PatternTestListener<RoomPattern> listener = new PatternTestListener<RoomPattern>();
		RoomPattern pattern = advAcc.createResource("room1_" + suffix, RoomPattern.class);
		advAcc.addIndividualPatternDemand(RoomPattern.class, pattern, listener, AccessPriority.PRIO_LOWEST);

		activateAndVerifyCallback(pattern, listener);

		listener.reset();
		// check that we don't get a callback from an unwanted pattern
		RoomPattern pattern2 = advAcc.createResource("room2_" + suffix, RoomPattern.class);
		advAcc.activatePattern(pattern2);
		RoomPattern pattern3 = advAcc.createResource("room3_" + suffix, RoomPattern.class);
		advAcc.activatePattern(pattern3);
		RoomPattern pattern4 = advAcc.createResource("room4_" + suffix, RoomPattern.class);
		advAcc.activatePattern(pattern4);
		boolean callback = listener.awaitFoundEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("Pattern callback from the wrong pattern", callback);
		
		RoomPattern pattern5 = advAcc.createResource("room5_" + suffix, RoomPattern.class);
		advAcc.addIndividualPatternDemand(RoomPattern.class, pattern5, listener, AccessPriority.PRIO_LOWEST);

		activateAndVerifyCallback(pattern5, listener);
		deleteAndVerifyCallback(pattern, listener,true);
		
		listener.reset();
		
		pattern2.model.delete();
		pattern3.model.delete();
		pattern4.model.delete();
		callback = listener.awaitLostEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("Pattern callback for the wrong pattern", callback);
		
		deleteAndVerifyCallback(pattern5, listener, false);
		
		listener.reset();
		
		pattern5.model.delete();
		callback = listener.awaitLostEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("patternUnavailable callback for an already inactive pattern", callback);
		advAcc.removeAllIndividualPatternDemands(RoomPattern.class, listener);
	}
	
	@Test
	public void reactivationWorksForIndividualDemand() throws InterruptedException {
		String suffix = newResourceName();
		PatternTestListener<RoomPattern> listener = new PatternTestListener<RoomPattern>();
		RoomPattern pattern = advAcc.createResource("room1_" + suffix, RoomPattern.class);
		advAcc.addIndividualPatternDemand(RoomPattern.class, pattern, listener, AccessPriority.PRIO_LOWEST);

		activateAndVerifyCallback(pattern, listener);
		deleteAndVerifyCallback(pattern, listener, false);
		activateAndVerifyCallback(pattern, listener);
		deleteAndVerifyCallback(pattern, listener, true);
		advAcc.removeAllIndividualPatternDemands(RoomPattern.class, listener);
	}
	
	@Test
	public void listenerUnregistrationAndRegistrationWorks() throws InterruptedException {
		String suffix = newResourceName();
		PatternTestListener<RoomPattern> listener = new PatternTestListener<RoomPattern>();
		RoomPattern pattern = advAcc.createResource("room1_" + suffix, RoomPattern.class);
		advAcc.addIndividualPatternDemand(RoomPattern.class, pattern, listener, AccessPriority.PRIO_LOWEST);

		activateAndVerifyCallback(pattern, listener);
		advAcc.removeAllIndividualPatternDemands(RoomPattern.class, listener);
		
		advAcc.deactivatePattern(pattern);
		boolean callback = listener.awaitLostEvent(1, TimeUnit.SECONDS);
		Assert.assertFalse("Pattern callback, although the listener has been unregistered", callback);
		
		advAcc.addIndividualPatternDemand(RoomPattern.class, pattern, listener, AccessPriority.PRIO_LOWEST);
		activateAndVerifyCallback(pattern, listener);
		
		deleteAndVerifyCallback(pattern, listener, true);
		advAcc.removeIndividualPatternDemand(RoomPattern.class, pattern, listener);
	}
	
	private <P extends ResourcePattern<?>> void activateAndVerifyCallback(P pattern, PatternTestListener<P> listener) 
			throws InterruptedException {
		listener.reset();
		listener.expectedFoundPattern = pattern;
		advAcc.activatePattern(pattern);
		boolean callback = listener.awaitFoundEvent();
		Assert.assertTrue("Missing pattern callback", callback);
	}
	
	/**
	 * 
	 * @param pattern
	 * @param listener
	 * @param deleteOrDeactivate
	 * 		true: delete; false: deactivate
	 * @throws InterruptedException
	 */
	private <P extends ResourcePattern<?>> void deleteAndVerifyCallback(P pattern, PatternTestListener<P> listener, boolean deleteOrDeactivate) 
			throws InterruptedException {
		listener.reset();
		listener.expectedLostPattern = pattern;
		if (deleteOrDeactivate)
			pattern.model.delete();
		else
			advAcc.deactivatePattern(pattern);
		boolean callback = listener.awaitLostEvent();
		Assert.assertTrue("Missing pattern callback", callback);
	}
	
}
