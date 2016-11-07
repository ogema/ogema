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

import org.ogema.exam.ResourceAssertions;
import org.ogema.pattern.test.pattern.HeatPumpPattern2;
import org.ogema.pattern.test.pattern.HeatPumpRad;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class AdvancedAccessTest extends OsgiTestBase {

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("Bundle-SymbolicName", "test-probe");
		return builder;
	}

	@Test
	public void findLoseRad() throws InterruptedException {

		final HeatPumpRad rad = advAcc.createResource("ToActivationHeatPump", HeatPumpRad.class);
		assertTrue(rad.allInactive());
		assertFalse(rad.allActive());
		advAcc.activatePattern(rad);
		assertFalse(rad.allInactive());
		assertTrue(rad.allActive());
		advAcc.deactivatePattern(rad);
		assertTrue(rad.allInactive());
		assertFalse(rad.allActive());
	}
	
	@Test 
	public void createPatternWithDecoratorWorks() {
		HeatPumpPattern2 pattern = advAcc.createResource(newResourceName(), HeatPumpPattern2.class);
		ResourceAssertions.assertExists(pattern.model);
		ResourceAssertions.assertExists(pattern.battery);
		ResourceAssertions.assertExists(pattern.chargeSensor);
		ResourceAssertions.assertIsVirtual(pattern.batteryStatus);
		advAcc.createOptionalResourceFields(pattern, HeatPumpPattern2.class, false);
		ResourceAssertions.assertExists(pattern.batteryStatus);
		advAcc.activatePattern(pattern);
		ResourceAssertions.assertActive(pattern.model);
		ResourceAssertions.assertActive(pattern.battery);
		ResourceAssertions.assertActive(pattern.chargeSensor);
		ResourceAssertions.assertActive(pattern.batteryStatus);
		
		pattern.model.delete();
	}

}
