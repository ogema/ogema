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
package org.ogema.util.test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.array.BooleanArrayResource;
import org.ogema.core.model.array.ByteArrayResource;
import org.ogema.core.model.array.FloatArrayResource;
import org.ogema.core.model.array.IntegerArrayResource;
import org.ogema.core.model.array.StringArrayResource;
import org.ogema.core.model.array.TimeArrayResource;
import org.ogema.core.model.schedule.AbsoluteSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.tools.SerializationManager;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.locations.Room;
import org.ogema.model.locations.WorkPlace;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.serialization.SchemaUtil;
import org.ogema.serialization.jaxb.ResourceList;
import org.ogema.serialization.jaxb.FloatResource;
import org.ogema.serialization.jaxb.SampledFloat;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.xml.sax.SAXException;

/**
 * OSGi/OGEMA integrated tests.
 * 
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class XmlSerializationOsgiTest extends OsgiAppTestBase {

	OnOffSwitch sw;
	SerializationManager sman;
	ResourceManagement resman;

	@Before
	public void setup() {
		sman = getApplicationManager().getSerializationManager();
		resman = getApplicationManager().getResourceManagement();
		sw = resman.createResource("switch", OnOffSwitch.class);
		sw.heatCapacity().create();
		sw.heatCapacity().setValue(42.0f);
		IntegerResource ir = sw.addDecorator("int", IntegerResource.class);
		ir.setValue(4711);

		sw.stateControl().create();
		sw.stateControl().setValue(true);

		sw.ratedValues().upperLimit().create();
		sw.ratedValues().upperLimit().setValue(true);

		sw.addOptionalElement("timeBeforeSwitchOff");
		sw.timeBeforeSwitchOff().setValue(Long.MAX_VALUE);

		Schedule schedule = sw.heatCapacity().addDecorator("defSchedule", AbsoluteSchedule.class);
		schedule.addValue(1L, new FloatValue(1f), 100000);
		schedule.addValue(2L, new FloatValue(2f));
		schedule.addValue(3L, new FloatValue(3f));
		schedule.addValue(4L, new FloatValue(4f));
		schedule.addValue(5L, new FloatValue(5f));
	}
	
	private static JAXBContext createUnmarshallingContext() {
		try {
			return JAXBContext.newInstance("org.ogema.serialization.jaxb",
					org.ogema.serialization.jaxb.Resource.class.getClassLoader());
		} catch (JAXBException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void validateOgemaXml(String input) throws SAXException, IOException {
		SchemaUtil.getSchema().newValidator().validate(new StreamSource(new StringReader(input)));
		System.out.println("validated");
	}

	@SuppressWarnings("unchecked")
	static <T> T unmarshal(Unmarshaller u, String s) throws JAXBException {
		Object o = u.unmarshal(new StringReader(s));
		if (o instanceof JAXBElement) {
			return ((JAXBElement<T>) o).getValue();
		}
		else {
			return (T) o;
		}
	}

	@Test
	public void simpleFloatRountrip() {
		StringWriter output = new StringWriter();
		try {
			sman.writeXml(output, sw.heatCapacity());
			System.out.println("XML:\n" + output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			validateOgemaXml(output.toString());
			Unmarshaller u = ctx.createUnmarshaller();
			FloatResource fr = unmarshal(u, output.toString());
			Assert.assertEquals(sw.heatCapacity().getValue(), fr.getValue(), 0.f);
		} catch (JAXBException | IOException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void simpleIntRountrip() {
		StringWriter output = new StringWriter();
		try {
			sman.writeXml(output, sw.getSubResource("int"));
			System.out.println("XML:\n" + output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			validateOgemaXml(output.toString());
			Unmarshaller u = ctx.createUnmarshaller();
			org.ogema.serialization.jaxb.IntegerResource r = unmarshal(u, output.toString());
			Assert.assertEquals(((IntegerResource) sw.getSubResource("int")).getValue(), r.getValue());
		} catch (JAXBException | IOException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void simpleBooleanRountrip() {
		StringWriter output = new StringWriter();
		try {
			sman.writeXml(output, sw.stateControl());
			System.out.println("XML:\n" + output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			validateOgemaXml(output.toString());
			Unmarshaller u = ctx.createUnmarshaller();
			org.ogema.serialization.jaxb.BooleanResource r = unmarshal(u, output.toString());
			Assert.assertEquals(sw.stateControl().getValue(), r.isValue());
		} catch (JAXBException | IOException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void simpleStringRountrip() {
		StringWriter output = new StringWriter();
		try {
			sman.writeXml(output, sw.ratedValues().upperLimit());
//                                      sw.comInfo().communicationAddress());
			System.out.println("XML:\n" + output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			validateOgemaXml(output.toString());
			Unmarshaller u = ctx.createUnmarshaller();
			org.ogema.serialization.jaxb.BooleanResource r = unmarshal(u, output.toString());
			Assert.assertEquals(sw.ratedValues().upperLimit().getValue(), r.isValue());
		} catch (JAXBException | IOException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void simpleTimeRountrip() {
		StringWriter output = new StringWriter();
		try {
			sman.writeXml(output, sw.timeBeforeSwitchOff());
			System.out.println("XML:\n" + output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			validateOgemaXml(output.toString());
			Unmarshaller u = ctx.createUnmarshaller();
			org.ogema.serialization.jaxb.TimeResource r = unmarshal(u, output.toString());
			Assert.assertEquals(sw.timeBeforeSwitchOff().getValue(), r.getValue());
		} catch (JAXBException | IOException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void scheduleRoundtripFloat() {
		StringWriter output = new StringWriter();
		try {
			Schedule sched = (Schedule) sw.heatCapacity().getSubResource("defSchedule");
			sman.writeXml(output, sched);
			System.out.println("XML:\n" + output.toString());
			validateOgemaXml(output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			Unmarshaller u = ctx.createUnmarshaller();
			org.ogema.serialization.jaxb.FloatSchedule r = unmarshal(u, output.toString());
			// Assert.assertEquals(sw.timeBeforeSwitchOff().getValue(), r.getValue());
			Assert.assertEquals(sched.getValues(0).size(), r.getEntry().size());
			List<SampledValue> lOrig = sched.getValues(0);
			for (int i = 0; i < lOrig.size(); i++) {
				Assert.assertEquals(lOrig.get(i).getTimestamp(), r.getEntry().get(i).getTime());
				Assert.assertEquals(lOrig.get(i).getValue().getFloatValue(),
						((SampledFloat) r.getEntry().get(i)).getValue(), 0.f);
			}
		} catch (IOException | JAXBException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void complexResourceRoundtrip() {
		StringWriter output = new StringWriter();
		try {
			sman.writeXml(output, sw);
			System.out.println("XML:\n" + output.toString());
			JAXBContext ctx = createUnmarshallingContext();
			validateOgemaXml(output.toString());
			Unmarshaller u = ctx.createUnmarshaller();
			org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());
			// Assert.assertEquals(sw.timeBeforeSwitchOff().getValue(), r.getValue());
		} catch (JAXBException | IOException | SAXException ex) {
			System.out.println(output.toString());
			ex.printStackTrace();
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void resourceListRoundtrip() throws Exception {
		Room room = resman.createResource("testRoom", Room.class);
		room.addOptionalElement("workPlaces");
		WorkPlace wp1 = room.workPlaces().add();
		WorkPlace wp2 = room.workPlaces().add();

		StringWriter output = new StringWriter();
		sman.writeXml(output, room);
		System.out.println(output.toString());
		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());
		Assert.assertTrue("workplaces is not a ResourceList: " + r.get("workPlaces"),
				r.get("workPlaces") instanceof ResourceList);
	}

	@Test
	public void resourceListRootRoundtrip() throws Exception {
		Room room = resman.createResource("testRoom2", Room.class);
		room.addOptionalElement("workPlaces");
		WorkPlace wp1 = room.workPlaces().add();
		WorkPlace wp2 = room.workPlaces().add();

		StringWriter output = new StringWriter();
		sman.writeXml(output, room.workPlaces());
		System.out.println(output.toString());
		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());
		Assert.assertTrue("workplaces is not a ResourceList: " + r, r instanceof ResourceList);
	}

	@Test
	public void arrayResourceValidation() throws Exception {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		arrays.addDecorator("booleans", BooleanArrayResource.class).setValues(new boolean[] { true, false, true });
		arrays.addDecorator("bytes", ByteArrayResource.class).setValues(new byte[] { 0x1b, 0x2b });
		arrays.addDecorator("floats", FloatArrayResource.class).setValues(new float[] { 2f, 3, 4, 5 });
		arrays.addDecorator("ints", IntegerArrayResource.class).setValues(new int[] { 0, 1, 2, 3 });

		arrays.addDecorator("strings", StringArrayResource.class).setValues(new String[] { "a", "b", "c" });
		arrays.addDecorator("longs", TimeArrayResource.class).setValues(new long[] { 0xdeadbeefL, 1, 2, 3 });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		sman.writeJson(new OutputStreamWriter(System.out), arrays);

		validateOgemaXml(output.toString());

		Unmarshaller u = createUnmarshallingContext().createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		Assert.assertEquals(Arrays.asList(true, false, true), ((org.ogema.serialization.jaxb.BooleanArrayResource) r
				.get("booleans")).getValues());
		Assert.assertTrue(Arrays.equals(new byte[] { 0x1b, 0x2b }, ((org.ogema.serialization.jaxb.ByteArrayResource) r
				.get("bytes")).getValues()));
		Assert.assertEquals(Arrays.asList(2f, 3f, 4f, 5f), ((org.ogema.serialization.jaxb.FloatArrayResource) r
				.get("floats")).getValues());
		Assert.assertEquals(Arrays.asList(0, 1, 2, 3), ((org.ogema.serialization.jaxb.IntegerArrayResource) r
				.get("ints")).getValues());
		Assert.assertEquals(Arrays.asList("a", "b", "c"), ((org.ogema.serialization.jaxb.StringArrayResource) r
				.get("strings")).getValues());
		Assert.assertEquals(Arrays.asList(0xdeadbeefL, 1L, 2L, 3L), ((org.ogema.serialization.jaxb.TimeArrayResource) r
				.get("longs")).getValues());
	}

	@Test
	public void arraysOfBoolWork() throws IOException, JAXBException, SAXException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		BooleanArrayResource bools = arrays.addDecorator("arr", BooleanArrayResource.class);
		bools.setValues(new boolean[] { true, false });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		List<Boolean> serializedValues = ((org.ogema.serialization.jaxb.BooleanArrayResource) r.get("arr")).getValues();
		System.out.println(serializedValues);
		Assert.assertEquals(2, serializedValues.size());

		serializedValues.add(true);
		Marshaller m = ctx.createMarshaller();
		output = new StringWriter();
		m.marshal(r, output);

		System.out.println(output);
		sman.applyXml(output.toString(), arrays, true);

		//FIXME: lazy/brittle string comparison
		assertEquals(serializedValues.toString(), Arrays.toString(bools.getValues()));
	}

	@Test
	public void arraysOfByteWork() throws IOException, JAXBException, SAXException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		ByteArrayResource bytes = arrays.addDecorator("arr", ByteArrayResource.class);
		bytes.setValues(new byte[] { 0, 1, 3 });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		byte[] serializedValues = ((org.ogema.serialization.jaxb.ByteArrayResource) r.get("arr")).getValues();
		Assert.assertEquals(3, serializedValues.length);

		serializedValues[1] = 7;
		Marshaller m = ctx.createMarshaller();
		output = new StringWriter();
		m.marshal(r, output);

		System.out.println(output);
		sman.applyXml(output.toString(), arrays, true);

		assertArrayEquals(serializedValues, bytes.getValues());
	}

	@Test
	public void arraysOfFloatWork() throws IOException, JAXBException, SAXException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		FloatArrayResource floats = arrays.addDecorator("arr", FloatArrayResource.class);
		floats.setValues(new float[] { 1f, 2f });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		List<Float> serializedValues = ((org.ogema.serialization.jaxb.FloatArrayResource) r.get("arr")).getValues();
		System.out.println(serializedValues);
		Assert.assertEquals(2, serializedValues.size());

		serializedValues.add(0f);
		Marshaller m = ctx.createMarshaller();
		output = new StringWriter();
		m.marshal(r, output);

		System.out.println(output);
		sman.applyXml(output.toString(), arrays, true);

		//FIXME: lazy/brittle string comparison
		assertEquals(serializedValues.toString(), Arrays.toString(floats.getValues()));
	}

	@Test
	public void arraysOfIntegerWork() throws IOException, JAXBException, SAXException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		IntegerArrayResource ints = arrays.addDecorator("arr", IntegerArrayResource.class);
		ints.setValues(new int[] { 3, 1 });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		List<Integer> serializedValues = ((org.ogema.serialization.jaxb.IntegerArrayResource) r.get("arr")).getValues();
		System.out.println(serializedValues);
		Assert.assertEquals(2, serializedValues.size());

		serializedValues.add(42);
		Marshaller m = ctx.createMarshaller();
		output = new StringWriter();
		m.marshal(r, output);

		System.out.println(output);
		sman.applyXml(output.toString(), arrays, true);

		//FIXME: lazy/brittle string comparison
		assertEquals(serializedValues.toString(), Arrays.toString(ints.getValues()));
	}

	@Test
	public void arraysOfStringWork() throws IOException, JAXBException, SAXException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		StringArrayResource strings = arrays.addDecorator("arr", StringArrayResource.class);
		strings.setValues(new String[] { "a", "b", "c" });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		List<String> serializedValues = ((org.ogema.serialization.jaxb.StringArrayResource) r.get("arr")).getValues();
		System.out.println(serializedValues);
		Assert.assertEquals(3, serializedValues.size());

		serializedValues.add("d");
		Marshaller m = ctx.createMarshaller();
		output = new StringWriter();
		m.marshal(r, output);

		System.out.println(output);
		sman.applyXml(output.toString(), arrays, true);
		assertEquals(serializedValues, Arrays.asList(strings.getValues()));
	}

	@Test
	public void arraysOfTimeWork() throws IOException, JAXBException, SAXException {
		Resource arrays = resman.createResource(newResourceName(), Resource.class);

		TimeArrayResource longs = arrays.addDecorator("arr", TimeArrayResource.class);
		longs.setValues(new long[] { 0xcafebabeL, 1 });

		StringWriter output = new StringWriter();
		sman.setMaxDepth(100);
		sman.writeXml(output, arrays);
		System.out.println(output);

		JAXBContext ctx = createUnmarshallingContext();
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());

		List<Long> serializedValues = ((org.ogema.serialization.jaxb.TimeArrayResource) r.get("arr")).getValues();
		System.out.println(serializedValues);
		Assert.assertEquals(2, serializedValues.size());

		serializedValues.add(42L);
		Marshaller m = ctx.createMarshaller();
		output = new StringWriter();
		m.marshal(r, output);

		System.out.println(output);
		sman.applyXml(output.toString(), arrays, true);

		//FIXME: lazy/brittle string comparison
		assertEquals(serializedValues.toString(), Arrays.toString(longs.getValues()));
	}

}
