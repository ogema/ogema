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

import org.junit.Assert;
import org.junit.Test;
import org.ogema.model.locations.Room;
import org.ogema.pattern.test.pattern.RoomContext;
import org.ogema.pattern.test.pattern.RoomContextPattern;
import org.ogema.pattern.test.pattern.RoomPattern;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class PatternVerificationTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	@Test
	public void patternVerificationWorks() {
		String suffix = newResourceName();
		RoomPattern pattern = advAcc.createResource("room1_" + suffix, RoomPattern.class);
		advAcc.activatePattern(pattern);
		Assert.assertTrue(advAcc.isSatisfied(pattern, RoomPattern.class));

		pattern.name.deactivate(false);
		Assert.assertFalse(advAcc.isSatisfied(pattern, RoomPattern.class));

		pattern.name.activate(false);
		pattern.model.delete();
		Assert.assertFalse(advAcc.isSatisfied(pattern, RoomPattern.class));
		
		pattern.model.delete();
	}
	
	@Test
	public void patternVerificationWorksForContextSensitivePattern() {
		String suffix = newResourceName();
		Room room = resMan.createResource(newResourceName(), Room.class);
		RoomContextPattern pattern = advAcc.createResource("heater1_" + suffix, RoomContextPattern.class);
		pattern.model.activate(true);
		room.activate(true);
		
		RoomContext ctx = new RoomContext(room);
		Assert.assertFalse("Pattern satisfied, although accept method should return false",
				advAcc.isSatisfied(pattern,RoomContextPattern.class, ctx));
		
		pattern.room.setAsReference(room);
		Assert.assertTrue("Pattern unexpectedly not satisfied",
				advAcc.isSatisfied(pattern,RoomContextPattern.class, ctx));
		
		pattern.model.delete();
		room.delete();
	}
	
}
