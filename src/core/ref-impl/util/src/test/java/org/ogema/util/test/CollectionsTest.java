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
package org.ogema.util.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.model.Resource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.metering.ElectricityMeter;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExamReactorStrategy(PerClass.class)
public class CollectionsTest extends OsgiAppTestBase {

	static int counter = 0;

	ElectricityMeter meter;
	Schedule sched;
	SerializationManager sman;
	
	CoolingDevice fridge;
	List<Resource> resources;

	@ProbeBuilder
	public TestProbeBuilder build(TestProbeBuilder builder) {
		builder.setHeader("DynamicImport-Package", "*");
		return builder;
	}

	@Before
	public void setup() throws Exception {
		meter = getApplicationManager().getResourceManagement().createResource("meter" + counter++,
				ElectricityMeter.class);
		meter.connection().powerSensor().reading().create();
		sched = meter.connection().powerSensor().reading().addDecorator("sched", AbsoluteSchedule.class);
		meter.connection().powerSensor().reading().setValue(47.11f);
		fridge = getApplicationManager().getResourceManagement().createResource("fridge" + counter++, CoolingDevice.class);
		fridge.name().<StringResource> create().setValue("Fridge");
		sman = getApplicationManager().getSerializationManager();
		resources = new ArrayList<>();
		resources.add(meter);
		resources.add(fridge);
	}
	
	@Test
	public void xmlSerializationWorks() throws Exception {
		String xml = sman.toXml(resources);
		// System.out.println("   ~~ xml: " + xml);
		Assert.assertNotNull(xml);
		Collection<Resource> list = sman.createResourcesFromXml(xml);
		// FIXME
		System.out.println("   ~~~ resources created: " + list);
		assertEquals(2, list.size());
	}
	
	@Test
	public void jsonSerializationWorks() throws Exception {
		String json = sman.toJson(resources);
		// System.out.println("   ~~~ json: " + json);
		Assert.assertNotNull(json);
		JSONArray obj = new JSONArray(json);
		assertEquals(2, obj.length());

		Collection<Resource> list = sman.createResourcesFromJson(json); 
		// FIXME
		System.out.println("   ~~~ resources created: " + list);
		assertEquals(2,	list.size());
	}

}
