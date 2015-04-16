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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.schedule.DefinitionSchedule;
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

		Schedule schedule = sw.heatCapacity().addDecorator("defSchedule", DefinitionSchedule.class);
		schedule.addValue(1L, new FloatValue(1f), 100000);
		schedule.addValue(2L, new FloatValue(2f));
		schedule.addValue(3L, new FloatValue(3f));
		schedule.addValue(4L, new FloatValue(4f));
		schedule.addValue(5L, new FloatValue(5f));
	}

	public void validateOgemaXml(String input) throws SAXException, IOException {
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
			JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
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
	public void complexArrayRoundtrip() throws Exception {
		Room room = resman.createResource("testRoom", Room.class);
		room.addOptionalElement("workPlaces");
		WorkPlace wp1 = room.workPlaces().add();
		WorkPlace wp2 = room.workPlaces().add();

		StringWriter output = new StringWriter();
		sman.writeXml(output, room);
		System.out.println(output.toString());
		JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());
		Assert.assertTrue("workplaces is not a ResourceList: " + r.get("workPlaces"),
				r.get("workPlaces") instanceof ResourceList);
	}

	@Test
	public void complexArrayRootRoundtrip() throws Exception {
		Room room = resman.createResource("testRoom2", Room.class);
		room.addOptionalElement("workPlaces");
		WorkPlace wp1 = room.workPlaces().add();
		WorkPlace wp2 = room.workPlaces().add();

		StringWriter output = new StringWriter();
		sman.writeXml(output, room.workPlaces());
		System.out.println(output.toString());
		JAXBContext ctx = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class);
		validateOgemaXml(output.toString());
		Unmarshaller u = ctx.createUnmarshaller();
		org.ogema.serialization.jaxb.Resource r = unmarshal(u, output.toString());
		Assert.assertTrue("workplaces is not a ResourceList: " + r, r instanceof ResourceList);
	}

}
