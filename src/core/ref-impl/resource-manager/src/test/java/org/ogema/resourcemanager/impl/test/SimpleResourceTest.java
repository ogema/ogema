/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.resourcemanager.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.ForecastSchedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.OpaqueResource;
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
	public void settingOpaqueValueWorks() throws ResourceException {
		OpaqueResource opaqueRes = resMan.createResource(RESNAME + counter++, OpaqueResource.class);
		//		res.addOptionalElement("newFirmware");
		//		OpaqueResource opaqueRes = res.newFirmware();
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
		final ForecastSchedule forecast = resource.forecast();
		final DefinitionSchedule program = resource.program();
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
		DefinitionSchedule program = (DefinitionSchedule) resource.addOptionalElement("program");
		assertNotNull(program);
		assertTrue(program.exists());
	}
}
