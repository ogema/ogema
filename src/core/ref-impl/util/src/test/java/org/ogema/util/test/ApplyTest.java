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
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.Quality;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.devices.connectiondevices.ElectricityConnectionBox;
import org.ogema.model.devices.generators.ElectricHeater;
import org.ogema.model.locations.Room;
import org.ogema.model.locations.WorkPlace;
import org.ogema.model.metering.ElectricityMeter;
import org.ogema.model.sensors.PowerSensor;
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
	public void addingOptionalElementWorks_Json() throws Exception {
		String id = "JsonTest";
		ElectricityMeter meter1 = getApplicationManager().getResourceManagement().createResource("meter" + id + "1",
				ElectricityMeter.class);
		ElectricityMeter meter2 = getApplicationManager().getResourceManagement().createResource("meter" + id + "2",
				ElectricityMeter.class);
		meter1.connection().create();
		meter2.connection().currentSensor().create().activate(false);;
		String str2 = sman.toJson(meter2);
		String str1 = str2.replace("meter" + id + "2", "meter" + id + "1"); 
		sman.applyJson(str1, meter1, false);
		
		assertNotNull(meter1.connection().currentSensor());
		assertTrue(meter1.connection().currentSensor().exists());
		assertTrue(meter1.connection().currentSensor().isActive());
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
		return jaxbUnmarshalling.createUnmarshaller().unmarshal(new StreamSource(new StringReader(s)),
				clazz).getValue();
	}

	String marshal(Object o) throws JAXBException {
		StringWriter sw = new StringWriter(200);
		Marshaller m = jaxbUnmarshalling.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(o, sw);
		return sw.toString();
	}

	static FloatSchedule createTestSchedule() {
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
    public void applyCreatesLinksToNewResources() throws JAXBException {
        ElectricHeater heater = getApplicationManager().getResourceManagement().createResource(newResourceName(), ElectricHeater.class);
        String heaterName = heater.getName();
        PowerSensor psens = getApplicationManager().getResourceManagement().createResource(newResourceName(), PowerSensor.class);
        String psensName = psens.getName();
        heater.electricityConnection().powerSensor().reading().create();
        psens.reading().setAsReference(heater.electricityConnection().powerSensor().reading());
        
        sman.setFollowReferences(false);
        Resource heaterXml = unmarshal(sman.toXml(heater), Resource.class);
        Resource psensXml = unmarshal(sman.toXml(psens), Resource.class);
        
        Resource root = new Resource();
        root.setPath("/");
        root.setName("");
        root.setType(org.ogema.core.model.Resource.class);
        root.getSubresources().add(psensXml);
        root.getSubresources().add(heaterXml);
        
        heater.delete();
        psens.delete();
        heater = getApplicationManager().getResourceAccess().getResource(heaterName);
        psens = getApplicationManager().getResourceAccess().getResource(psensName);
        assertNull(heater);
        assertNull(psens);
        
        String msg = marshal(root);
        System.out.println(msg);
        
        sman.createFromXml(msg);
        
        heater = getApplicationManager().getResourceAccess().getResource(heaterName);
        psens = getApplicationManager().getResourceAccess().getResource(psensName);
        
        assertNotNull(heater);
        assertNotNull(psens);
        
        ResourceAssertions.assertExists(psens.reading());
        assertTrue(psens.reading().equalsLocation(heater.electricityConnection().powerSensor().reading()));
    }
	
	@Test
	public void addingEmptyScheduleWorks_Json() throws Exception {
		sman.setSerializeSchedules(true);
		String id = "JsonTest";
		ElectricityMeter meter1 = getApplicationManager().getResourceManagement().createResource("meter" + id + "1", ElectricityMeter.class);
		ElectricityMeter meter2 = getApplicationManager().getResourceManagement().createResource("meter" + id + "2", ElectricityMeter.class);
		meter1.connection().currentSensor().reading().create();
		meter2.connection().currentSensor().reading().program().create().activate(false);
		
		String str2 = sman.toJson(meter2);
		String str1 = str2.replace("meter" + id + "2", "meter" + id + "1");
		System.out.println("  --- Updating resource with json: " + str1);
		sman.applyJson(str1, meter1, false);  
		
		assertNotNull(meter1.connection().currentSensor().reading().program());
		assertTrue(meter1.connection().currentSensor().reading().program().exists()); 
		assertTrue(meter1.connection().currentSensor().reading().program().isActive());
		assertEquals(0, meter1.connection().currentSensor().reading().program().getValues(Long.MIN_VALUE).size());
		meter1.delete();
		meter2.delete();
	}
	
	@Test
	public void addingScheduleWithValuesWorks_Json() throws Exception {
		sman.setSerializeSchedules(true);
		String id = "JsonTest";
		ElectricityMeter meter1 = getApplicationManager().getResourceManagement().createResource("meter" + id + "3",
				ElectricityMeter.class);
		ElectricityMeter meter2 = getApplicationManager().getResourceManagement().createResource("meter" + id + "4",
				ElectricityMeter.class);
		meter1.connection().currentSensor().reading().create();
		meter2.connection().currentSensor().reading().program().create().activate(false);
		meter2.connection().currentSensor().reading().program().addValue(1, new FloatValue(23));
		meter2.connection().currentSensor().reading().program().addValue(4, new FloatValue(-65.2F));
		meter2.connection().currentSensor().reading().program().addValue(System.currentTimeMillis(), new FloatValue(234.2F));
		
		String str2 = sman.toJson(meter2);
		String str1 = str2.replace("meter" + id + "4", "meter" + id + "3"); 
		System.out.println("  --- Updating resource with json: " + str1);
		sman.applyJson(str1, meter1, false); 
		
		assertNotNull(meter1.connection().currentSensor().reading().program());
		assertTrue(meter1.connection().currentSensor().reading().program().exists()); 
		assertTrue(meter1.connection().currentSensor().reading().program().isActive());
		assertEquals(3, meter1.connection().currentSensor().reading().program().getValues(Long.MIN_VALUE).size());
		meter1.delete();
		meter2.delete();
	}
	
	@Test
	public void addingIntegerScheduleWithValuesWorks_Json() throws Exception {
		sman.setSerializeSchedules(true);
		String id = "JsonTest";
		ElectricityMeter meter1 = getApplicationManager().getResourceManagement().createResource("meter" + id + "5",
				ElectricityMeter.class);
		ElectricityMeter meter2 = getApplicationManager().getResourceManagement().createResource("meter" + id + "6",
				ElectricityMeter.class);
		meter1.getSubResource("intSub", IntegerResource.class).create();
		meter2.getSubResource("intSub", IntegerResource.class).program().create().activate(false);
		meter2.getSubResource("intSub", IntegerResource.class).program().addValue(1, new IntegerValue(23));
		meter2.getSubResource("intSub", IntegerResource.class).program().addValue(4, new IntegerValue(-65));
		meter2.getSubResource("intSub", IntegerResource.class).program().addValue(System.currentTimeMillis(), new IntegerValue(234));
		
		String str2 = sman.toJson(meter2);
		String str1 = str2.replace("meter" + id + "6", "meter" + id + "5"); 
		System.out.println("  --- Updating resource with json: " + str1);
		sman.applyJson(str1, meter1, false); 
		
		assertNotNull(meter1.getSubResource("intSub", IntegerResource.class).program());
		assertTrue(meter1.getSubResource("intSub", IntegerResource.class).program().exists()); 
		assertTrue(meter1.getSubResource("intSub", IntegerResource.class).program().isActive());
		assertEquals(3, meter1.getSubResource("intSub", IntegerResource.class).program().getValues(Long.MIN_VALUE).size());
		meter1.delete();
		meter2.delete();
	}
	
	@Test
	public void addingIntegerScheduleWithValuesWorks_Xml() throws Exception {
		sman.setSerializeSchedules(true);
		String id = "JsonTest";
		ElectricityMeter meter1 = getApplicationManager().getResourceManagement().createResource("meter" + id + "7",
				ElectricityMeter.class);
		ElectricityMeter meter2 = getApplicationManager().getResourceManagement().createResource("meter" + id + "8",
				ElectricityMeter.class);
		meter1.getSubResource("intSub", IntegerResource.class).create();
		meter2.getSubResource("intSub", IntegerResource.class).program().create().activate(false);
		meter2.getSubResource("intSub", IntegerResource.class).program().addValue(1, new IntegerValue(23));
		meter2.getSubResource("intSub", IntegerResource.class).program().addValue(4, new IntegerValue(-65));
		meter2.getSubResource("intSub", IntegerResource.class).program().addValue(System.currentTimeMillis(), new IntegerValue(234));
		
		String str2 = sman.toXml(meter2);
		String str1 = str2.replace("meter" + id + "8", "meter" + id + "7"); 
		System.out.println("  --- Updating resource with xml: " + str1);
		sman.applyXml(str1, meter1, false); 
		
		assertNotNull(meter1.getSubResource("intSub", IntegerResource.class).program());
		assertTrue(meter1.getSubResource("intSub", IntegerResource.class).program().exists()); 
		assertTrue(meter1.getSubResource("intSub", IntegerResource.class).program().isActive());
		assertEquals(3, meter1.getSubResource("intSub", IntegerResource.class).program().getValues(Long.MIN_VALUE).size());
		meter1.delete();
		meter2.delete();
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
	
	@Test
	public void addResourceListElementWorks() {
		@SuppressWarnings("unchecked")
		ResourceList<ElectricityMeter> list = getApplicationManager().getResourceManagement().createResource(newResourceName(), ResourceList.class);
		list.setElementType(ElectricityMeter.class);
		list.add();
		String json = sman.toJson(list.getAllElements().get(0));
		System.out.println("    json: " + json);
		JSONObject subres = new JSONObject(json);
		String path = subres.getString("path");
		path = path.substring(0, path.length()-1) + "1"; // replace last 0 by 1
		String name = subres.getString("name");
		name = name.substring(0, name.length()-1) + "1";
		subres.put("path", path);
		subres.put("name", name);
		try {
			sman.createFromJson(subres.toString(), list);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError(e);
		}
		System.out.println(sman.toJson(list));
		assertEquals("Resource list with unexpected number of elements", 2, list.getAllElements().size());
	}
	
	@Test
	public void applyWorksForResourceList_JSON() {
		applyWorksForResourceList(DataType.JSON);
	}
	
	@Test
	public void applyWorksForResourceList_XML() {
		applyWorksForResourceList(DataType.XML);
	}
	
	private void applyWorksForResourceList(DataType dataType) {
		Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		room.workPlaces().create();
		ResourceList<WorkPlace> wps = room.workPlaces();
		WorkPlace wp1 = wps.add();
		assertTrue(wp1.exists());
		assertEquals(1, room.workPlaces().size());
		String roomAsString;
		switch(dataType) {
		case JSON:
			roomAsString = sman.toJson(room);
			break;
		case XML:
			roomAsString = sman.toXml(room);
			break;
		default:
			throw new RuntimeException();
		}
		System.out.println(roomAsString);
		wp1.delete();
		switch(dataType) {
		case JSON:
			sman.applyJson(roomAsString, room, false);
			break;
		case XML:
			sman.applyXml(roomAsString, room, false);
			break;
		default:
			throw new RuntimeException();
		}
		assertEquals("Resource list should have one element by now", 1, room.workPlaces().size());
	}
	
	@Test
	public void applyWorksForSimpleResource_JSON() {
		applyWorksForSimpleResource(DataType.JSON);
	}
	
	@Test
	public void applyWorksForSimpleResource_XML() {
		applyWorksForSimpleResource(DataType.XML);
	}
	
	private void applyWorksForSimpleResource(DataType dataType) {
		Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		room.name().<StringResource> create().setValue("test");
		String roomAsString;
		switch(dataType) {
		case JSON:
			roomAsString = sman.toJson(room);
			break;
		case XML:
			roomAsString = sman.toXml(room);
			break;
		default:
			throw new RuntimeException();
		}
		System.out.println(roomAsString);
		room.name().delete();
		switch(dataType) {
		case JSON:
			sman.applyJson(roomAsString, room, false);
			break;
		case XML:
			sman.applyXml(roomAsString, room, false);
			break;
		default:
			throw new RuntimeException();
		}
		ResourceAssertions.assertExists(room.name());
	}

}
