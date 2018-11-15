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
package org.ogema.resourcemanager.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.devices.generators.HeatPump;
import org.ogema.model.stakeholders.Language;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Test get and set on the different simple resource types (int, float, long, boolean, String & opaque(=byte[])). Also
 * test that decorators work on simple resources.
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class SimpleResourceTest extends OsgiTestBase {

	public static final String RESNAME = SimpleResourceTest.class.getSimpleName();

	@Test
	public void lastUpdateTimeWorks() throws Exception {
		OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		assertEquals(-1, res.stateControl().getLastUpdateTime());
		res.stateControl().create();
		assertEquals(-1, res.stateControl().getLastUpdateTime());
		long beforeUpdate = getApplicationManager().getFrameworkTime();
		res.stateControl().setValue(true);
		assertNotEquals(-1, res.stateControl().getLastUpdateTime());
		assertTrue(res.stateControl().getLastUpdateTime() >= beforeUpdate);
	}
    
    @Test
	public void lastUpdateTimeWorksOnReferences() throws Exception {
        OnOffSwitch res = resMan.createResource(newResourceName(), OnOffSwitch.class);
		assertEquals(-1, res.stateControl().getLastUpdateTime());
		res.stateControl().create();
		assertEquals(-1, res.stateControl().getLastUpdateTime());
		long beforeUpdate = getApplicationManager().getFrameworkTime();
        BooleanResource ref = res.addDecorator("ref", res.stateControl());
		ref.setValue(true);
        assertNotEquals(-1, res.stateControl().getLastUpdateTime());
		//assertNotEquals(-1, ref.getLastUpdateTime());
		assertTrue(res.stateControl().getLastUpdateTime() >= beforeUpdate);
	}

	@Test
	public void decoratorsWorkOnSimpleResources() throws ResourceException {
		OnOffSwitch res = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		res.addOptionalElement("stateControl");
		BooleanResource br = res.stateControl();
		br.addDecorator("fnord", HeatPump.class);
		assertNotNull("decorator not created", br.getSubResource("fnord"));
		assertEquals("fnord", br.getSubResource("fnord").getName());
	}

	@Test
	public void settingBooleanValuesWorks() throws ResourceException {
		OnOffSwitch res = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		res.addOptionalElement("stateControl");
		BooleanResource br = res.stateControl();
		assertNotNull(br);
		boolean value = br.getValue();
		boolean newValue = !value;
		br.setValue(newValue);
		assertEquals(newValue, br.getValue());
	}

	@Test
	public void settingFloatValuesWorks() throws ResourceException {
		OnOffSwitch res = resMan.createResource(RESNAME + counter++, OnOffSwitch.class);
		FloatResource br = res.heatCapacity().create();
		assertNotNull(br);
		float value = br.getValue();
		float newValue = value == 1 ? -1 : 1;
		br.setValue(newValue);
		assertEquals(newValue, br.getValue(), 0.1f);
	}

	@Test
	public void settingStringValueWorks() throws ResourceException {
		Language res = resMan.createResource(RESNAME + counter++, Language.class);
		res.name().create();
		StringResource stringRes = res.name();
		String s = stringRes.getValue();
		String newVal = ">>>" + s + "<<<";
		stringRes.setValue(newVal);
		assertEquals(newVal, stringRes.getValue());
	}

	@Test
	public void settingIntValueWorks() throws ResourceException {
		IntegerResource intRes = resMan.createResource(RESNAME + counter++, IntegerResource.class);
		//                res.
		//		res.addOptionalElement("deviceInfo");
		//		res.deviceInfo().addOptionalElement("deviceSoftware");
		//		res.deviceInfo().deviceSoftware().addOptionalElement("controllerStatus");
		//		IntegerResource intRes = res.deviceInfo().deviceSoftware().controllerStatus();
		int v = intRes.getValue();
		int newVal = v ^ -1;
		intRes.setValue(newVal);
		assertEquals(newVal, intRes.getValue());
	}

	@Test
	public void settingLongValueWorks() throws ResourceException {
		TimeResource longRes = resMan.createResource(RESNAME + counter++, TimeResource.class);
		//		res.startupTimeCold().create();
		//		TimeResource longRes = res.startupTimeCold();
		long v = longRes.getValue();
		long newVal = v ^ -1;
		longRes.setValue(newVal);
		assertEquals(newVal, longRes.getValue());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void settingOpaqueValueWorks() throws ResourceException {
		org.ogema.core.model.simple.OpaqueResource opaqueRes = resMan.createResource(newResourceName(),
				org.ogema.core.model.simple.OpaqueResource.class);
		opaqueRes.getValue();
		byte[] newVal = new byte[10];
		ByteBuffer.wrap(newVal).putLong(0xCAFEBABEL);
		opaqueRes.setValue(newVal);
		assertEquals(0xCAFEBABEL, ByteBuffer.wrap(opaqueRes.getValue()).getLong());
		assertEquals(10, opaqueRes.getValue().length);
	}

	@Test
	public void optionalSchedulesWork() {
		final StringResource resource = resMan.createResource(RESNAME + counter++, StringResource.class);
		final Schedule forecast = resource.forecast();
		final Schedule program = resource.program();
		assertNotNull(forecast);
		assertNotNull(program);

		assertFalse(forecast.exists());
		assertFalse(program.exists());
		program.create();
		forecast.create();
		assertTrue(forecast.exists());
		assertTrue(program.exists());

		assertFalse(forecast.isActive());
		assertFalse(program.isActive());
		resource.activate(true);
		assertTrue(forecast.isActive());
		assertTrue(program.isActive());

		forecast.addValue(10, new StringValue("text"));
		assertEquals(1, forecast.getValues(0).size());
	}

	@Test
	public void optionalScheduleWorksWithoutVirtualResources() {
		final BooleanResource resource = resMan.createResource(RESNAME + counter++, BooleanResource.class);
		Schedule program = (Schedule) resource.addOptionalElement("program");
		assertNotNull(program);
		assertTrue(program.exists());
	}
}
