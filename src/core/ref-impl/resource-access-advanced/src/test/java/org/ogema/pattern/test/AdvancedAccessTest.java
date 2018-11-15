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

import org.ogema.exam.ResourceAssertions;
import org.ogema.model.sensors.TemperatureSensor;
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
