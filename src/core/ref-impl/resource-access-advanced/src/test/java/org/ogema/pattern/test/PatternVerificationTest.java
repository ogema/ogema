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
