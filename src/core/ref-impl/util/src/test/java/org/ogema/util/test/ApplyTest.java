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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.serialization.JaxbResource;
import org.ogema.serialization.jaxb.FloatSchedule;
import org.ogema.serialization.jaxb.Resource;
import org.ogema.serialization.jaxb.ResourceLink;
import org.ogema.serialization.jaxb.SampledFloat;
import org.ogema.serialization.jaxb.SampledValue;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ApplyTest extends OsgiAppTestBase {
	// XXX tests use XML, should be duplicated with JSON?

	// ResourceAccess resacc = getApplicationManager().getResourceAccessManager();
	static int counter = 0;

	ElectricityMeter meter;
	Schedule sched;
	SerializationManager sman;

	JAXBContext jaxbUnmarshalling;
	JAXBContext jaxbMarshalling;

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
		sman = getApplicationManager().getSerializationManager();
		jaxbUnmarshalling = JAXBContext
				.newInstance("org.ogema.serialization.jaxb", ResourceLink.class.getClassLoader());
		jaxbMarshalling = JAXBContext.newInstance("org.ogema.serialization", JaxbResource.class.getClassLoader());
	}

	@Test
	public void updatingSimpleResourceWorksJson() throws IOException {
		sman.setSerializeSchedules(true);
		String s = sman.toJson(meter);
		System.out.println(s);
		assertTrue(s.contains("47.11"));
		s = s.replace("47.11", "82.00");
		assertTrue(s.contains("82.00"));
		sman.applyJson(s, meter, true);
		assertEquals(82, meter.connection().powerSensor().reading().getValue(), 0.5);
	}

	@Test
	public void updatingSimpleResourceWorksXml() throws Exception {
		Resource res = unmarshal(sman.toXml(meter), Resource.class);
		final float newVal = 1234.5678f;
		((org.ogema.serialization.jaxb.FloatResource) res.get("connection").get("powerSensor").get("reading"))
				.setValue(newVal);
		sman.applyXml(marshal(res), meter, true);
		System.out.println(marshal(res));
		assertEquals(newVal, meter.connection().powerSensor().reading().getValue(), 0);
	}

	@Test
	public void addingOptionalElementWorks_Xml() throws Exception {
		Resource meter1 = unmarshal(sman.toXml(meter), Resource.class);
		Resource resBox = new Resource();
		resBox.setActive(Boolean.TRUE);
		resBox.setType(ElectricityConnectionBox.class.getCanonicalName());
		resBox.setName("distributionBox");
		meter1.getSubresources().add(resBox);

		sman.applyXml(marshal(meter1), meter, true);
		assertNotNull(meter.distributionBox());
		assertTrue(meter.distributionBox().exists());
		assertTrue(meter.distributionBox().isActive());
	}

	@Test
	public void addingDecoratorWorks_Xml() throws Exception {
		Resource meter1 = unmarshal(sman.toXml(meter), Resource.class);
		Resource decorator = new Resource();
		decorator.setActive(Boolean.TRUE);
		decorator.setType(ElectricityConnectionBox.class.getCanonicalName());
		decorator.setName("foo");
		meter1.getSubresources().add(decorator);

		assertNull(meter.getSubResource("foo"));
		sman.applyXml(marshal(meter1), meter, true);
		assertNotNull(meter.getSubResource("foo"));
	}

	@Test
	public void settingOptionalElementAsReferenceWorks_Xml() throws Exception {
		ElectricityMeter meter2 = getApplicationManager().getResourceManagement().createResource(
				"meter2_" + System.currentTimeMillis(), ElectricityMeter.class);
		meter2.addOptionalElement("distributionBox");
		Resource meter1 = unmarshal(sman.toXml(meter), Resource.class);
		ResourceLink link = new ResourceLink();
		link.setLink(meter2.distributionBox().getLocation("/"));
		link.setName("distributionBox");
		link.setType(meter2.distributionBox().getResourceType().getCanonicalName());
		meter1.getSubresources().add(link);
		assertFalse(meter.distributionBox().exists());
		sman.applyXml(marshal(meter1), meter, true);
		assertTrue(meter.distributionBox().exists());
		assertTrue(meter.distributionBox().equalsLocation(meter2.distributionBox()));
	}

	<T> T unmarshal(String s, Class<T> clazz) throws JAXBException {
		return jaxbUnmarshalling.createUnmarshaller().unmarshal(new StreamSource(new StringReader(sman.toXml(meter))),
				clazz).getValue();
	}

	String marshal(Object o) throws JAXBException {
		StringWriter sw = new StringWriter(200);
		jaxbUnmarshalling.createMarshaller().marshal(o, sw);
		return sw.toString();
	}

	FloatSchedule createTestSchedule() {
		FloatSchedule schedule = new FloatSchedule();
		schedule.setName("data");
		schedule.setType(AbsoluteSchedule.class);

		SampledValue v = new SampledFloat();
		v.setQuality(Quality.GOOD);
		v.setTime(1);
		v.setValue(new FloatValue(42));
		schedule.getEntry().add(v);
		v = new SampledFloat();
		v.setQuality(Quality.GOOD);
		v.setTime(2);
		v.setValue(new FloatValue(43));
		schedule.getEntry().add(v);
		v = new SampledFloat();
		v.setQuality(Quality.GOOD);
		v.setTime(3);
		v.setValue(new FloatValue(44));
		schedule.getEntry().add(v);

		return schedule;
	}

	@Test
	public void addingScheduleWorks() throws Exception {
		Resource floatRes = new Resource();
		floatRes.setName("scheduleTest");
		floatRes.setType(FloatResource.class);
		// floatRes.setDecorating(Boolean.TRUE);
		FloatSchedule schedule = createTestSchedule();

		floatRes.getSubresources().add(schedule);
		Resource meter1 = unmarshal(sman.toXml(meter), Resource.class);
		meter1.getSubresources().add(floatRes);

		sman.applyXml(marshal(meter1), meter, true);
		assertNotNull(meter.getSubResource("scheduleTest"));
		assertNotNull(meter.getSubResource("scheduleTest").getSubResource("data"));
		Schedule ogemaSchedule = (Schedule) meter.getSubResource("scheduleTest").getSubResource("data");
		assertEquals(schedule.getEntry().size(), ogemaSchedule.getValues(0).size());
	}

	@Test
	public void modifyingScheduleWorks() throws Exception {
		/*
		 * modify a schedule with [start,end) range set.
		 */
		Resource floatRes = new Resource();
		floatRes.setName("scheduleTest");
		floatRes.setType(FloatResource.class);
		// floatRes.setDecorating(Boolean.TRUE);
		FloatSchedule schedule = createTestSchedule();

		floatRes.getSubresources().add(schedule);
		Resource meter1 = unmarshal(sman.toXml(meter), Resource.class);
		meter1.getSubresources().add(floatRes);
		sman.applyXml(marshal(meter1), meter, true);

		schedule.getEntry().clear();
		schedule.setStart(2L);
		schedule.setEnd(3L);

		SampledValue v = new SampledFloat();
		v.setQuality(Quality.GOOD);
		v.setTime(2);
		v.setValue(new FloatValue(2));
		schedule.getEntry().add(v);

		sman.applyXml(marshal(meter1), meter, true);

		Schedule ogemaSchedule = (Schedule) meter.getSubResource("scheduleTest").getSubResource("data");

		assertEquals(3, ogemaSchedule.getValues(0).size());
		assertEquals(2, ogemaSchedule.getValues(0).get(1).getValue().getFloatValue(), 0);
	}

	@Test
	public void deletingScheduleIntervalWorks() throws Exception {
		/*
		 * modify a schedule with [start,end) range set.
		 */
		Resource floatRes = new Resource();
		floatRes.setName("scheduleTest");
		floatRes.setType(FloatResource.class);
		// floatRes.setDecorating(Boolean.TRUE);
		FloatSchedule schedule = createTestSchedule();

		floatRes.getSubresources().add(schedule);
		Resource meter1 = unmarshal(sman.toXml(meter), Resource.class);
		meter1.getSubresources().add(floatRes);
		sman.applyXml(marshal(meter1), meter, true);

		schedule.getEntry().clear();
		schedule.setStart(2L);
		schedule.setEnd(3L);

		sman.applyXml(marshal(meter1), meter, true);

		Schedule ogemaSchedule = (Schedule) meter.getSubResource("scheduleTest").getSubResource("data");

		assertEquals(2, ogemaSchedule.getValues(0).size());
		assertEquals(42, ogemaSchedule.getValues(0).get(0).getValue().getFloatValue(), 0);
		assertEquals(44, ogemaSchedule.getValues(0).get(1).getValue().getFloatValue(), 0);
	}

}
