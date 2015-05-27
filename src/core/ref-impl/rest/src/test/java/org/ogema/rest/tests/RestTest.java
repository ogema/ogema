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
package org.ogema.rest.tests;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import static org.joox.JOOX.*;

import org.joox.Match;
import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.locations.Room;
import org.ogema.model.locations.WorkPlace;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.http.HttpService;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class RestTest extends OsgiAppTestBase {

	/*
	@Inject
	@Filter("(osgi.web.symbolicname=org.ogema.ref-impl.rest)")
	ServletContext servletContext;
	 */

	OnOffSwitch sw;
	final String baseUrl = "http://localhost:" + HTTP_PORT + "/rest/resources";
	// schedule to use in the test.
	Schedule schedule;

	@Inject
	HttpService http;

	@Override
	@Configuration
	public Option[] config() {
		return new Option[] {
				CoreOptions.systemProperty("org.ogema.security").value("off"),
				CoreOptions.composite(super.config()),
				CoreOptions.wrappedBundle("mvn:org.jooq/joox/1.2.0"),
				CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").version(
						"4.3").start(),
				CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").version(
						"4.3").start(), CoreOptions.mavenBundle("commons-logging", "commons-logging", "1.1.3").start(), };
	}

	@Before
	@SuppressWarnings("deprecation")
	public void setup() throws Exception {
		ApplicationManager appMan = getApplicationManager();
		ResourceManagement resMan = appMan.getResourceManagement();
		sw = resMan.createResource("switch", OnOffSwitch.class);
		sw.heatCapacity().create();
		sw.heatCapacity().setValue(47.11f);

		schedule = sw.heatCapacity().addDecorator("defSchedule", DefinitionSchedule.class);
		schedule.addValue(1L, new FloatValue(1.5f), 100000);
		schedule.addValue(2L, new FloatValue(2.5f));
		schedule.addValue(3L, new FloatValue(3.5f));
		schedule.addValue(4L, new FloatValue(4.5f));
		schedule.addValue(5L, new FloatValue(5.5f));

		sw.stateFeedback().create();
		sw.stateFeedback().setValue(true);

		org.ogema.core.model.simple.OpaqueResource opaque = sw.addDecorator("opaque",
				org.ogema.core.model.simple.OpaqueResource.class);
		ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putLong(0xdeadbeefL);
		opaque.setValue(bb.array());

		StringResource string = sw.addDecorator("string", StringResource.class);
		string.setValue("test");

	}

	public void waitForServer() throws Exception {
		//assertNotNull(servletContext);
		assertNotNull(http);
		Request test = Request.Head(baseUrl).addHeader("Accept", "application/json");
		int tries = 0;
		while (true) {
			int statusCode = test.execute().returnResponse().getStatusLine().getStatusCode();
			if (statusCode / 100 == 2) {
				break;
			}
			if (tries++ > 40) {
				Assert.fail("REST servlet not working: " + statusCode);
			}
			Thread.sleep(50);
		}
	}

	@Test
	public void restPutXml() throws Exception {
		waitForServer();
		final String url = baseUrl + "/switch?depth=100";
		Request req = Request.Get(url).addHeader("Accept", "application/xml");
		String xml = req.execute().returnContent().asString();
		System.out.println(xml);
		Match doc = $(xml);
		Match value = doc.xpath("resource[name='heatCapacity']/value");
		assertTrue(value.isNotEmpty());
		float oldVal = Float.valueOf(value.content());
		float newVal = oldVal == 0 ? 4711 : oldVal * 2;
		assertEquals(oldVal, sw.heatCapacity().getValue(), 0.f);
		value.empty().append(Float.toString(newVal));

		Request put = Request.Put(url).addHeader("Accept", "application/xml").bodyString(doc.toString(),
				ContentType.APPLICATION_XML);
		xml = put.execute().returnContent().asString();
		Match newDoc = $(xml);
		assertEquals(newVal, sw.heatCapacity().getValue(), 0.f);
		Match updatedValue = newDoc.xpath("resource[name='heatCapacity']/value");
		assertTrue(updatedValue.isNotEmpty());
		assertEquals(newVal, Float.valueOf(updatedValue.content()), 0.f);
	}

	/**
	 * Try to modify a resource with an HTTP PUT to the REST interface
	 */
	@Test
	public void restPutWorks() throws Exception {
		waitForServer();
		final String url = baseUrl + "/switch?depth=100";
		Request req = Request.Get(url).addHeader("Accept", "application/json");
		String json = req.execute().returnContent().asString();
		System.out.println("JSON from server:\\n" + json);

		assertNotEquals(json, json = json.replace("47.11", "42.0"));

		System.out.println("send to server:\\n" + json);
		Assert.assertEquals(47.11, sw.heatCapacity().getValue(), 0.1d);
		Request put = Request.Put(url).bodyString(json, ContentType.APPLICATION_JSON);
		put.execute();

		Assert.assertEquals(42.0, sw.heatCapacity().getValue(), 0.1d);
	}

	/**
	 * Try to modify a float schedule with an HTTP PUT to the REST interface
	 */
	@Test
	public void restPutScheduleWorks() throws Exception {
		waitForServer();
		final String url = baseUrl + "/" + schedule.getPath("/");

		Request req = Request.Get(url + "/3").addHeader("Accept", "application/xml");
		String xml = req.execute().returnContent().asString();

		System.out.println(xml);
		System.out.println(xml);

		Match sched = $(xml);
		assertEquals(3, sched.children("entry").each().size());

		assertEquals(3.5d, Double.valueOf(sched.children("entry").each().get(0).child("value").content()), 0d);

		sched.children("entry").each().get(0).child("value").empty().append("42");

		Request put = Request.Put(url).addHeader("Accept", "application/xml").bodyString(sched.toString(),
				ContentType.APPLICATION_XML);

		System.out.println(put.execute().returnContent().asString());
		System.out.println("");

		assertEquals(42d, schedule.getValue(3).getValue().getDoubleValue(), 0d);
	}

	@Test
	public void restPutWorksForResourceLists() throws Exception {
		waitForServer();
		Room room = getApplicationManager().getResourceManagement().createResource(
				"room1_" + System.currentTimeMillis(), Room.class);
		room.addOptionalElement("workPlaces");
		WorkPlace wp1 = room.workPlaces().add();
		/* TODO
		 String url = baseUrl + "/" + room.getName() + "?depth=100";
		 waitForServer();
		 org.ogema.serialization.jaxb.Resource roomFromRest = getJson(url);

		 org.ogema.serialization.jaxb.Resource wpNew = new org.ogema.serialization.jaxb.Resource();
		 wpNew.setName("wpPutTest");
		 wpNew.setType(WorkPlace.class);

		 roomFromRest.get("workPlaces").getSubresources().add(wpNew);
		 putXml(url, roomFromRest);

		 assertEquals(2, room.workPlaces().getAllElements().size());
		 assertNotNull(room.workPlaces().getSubResource("wpPutTest"));
		 assertTrue(room.workPlaces().contains(room.workPlaces().getSubResource("wpPutTest")));
		 assertEquals(room.workPlaces().getSubResource("wpPutTest"), room.workPlaces().getAllElements().get(1));
		 */
	}

	org.ogema.serialization.jaxb.Resource getXml(String url) throws Exception {
		Request get = Request.Get(url).addHeader("Accept", "application/xml");
		Unmarshaller unmarshaller = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class)
				.createUnmarshaller();
		return unmarshaller.unmarshal(new StreamSource(get.execute().returnContent().asStream()),
				org.ogema.serialization.jaxb.Resource.class).getValue();
	}

	Response putXml(String url, org.ogema.serialization.jaxb.Resource res) throws Exception {
		Request put = Request.Put(url).addHeader("Content-Type", "application/xml");
		Marshaller marshaller = JAXBContext.newInstance(org.ogema.serialization.jaxb.Resource.class).createMarshaller();
		StringWriter sw = new StringWriter();
		marshaller.marshal(res, sw);
		return put.bodyString(sw.toString(), ContentType.APPLICATION_XML).execute();
	}

	@Test
	public void restPostWorksForTopLevelResource() throws Exception {
		waitForServer();
		String url = String.format("%s/%s?depth=100", baseUrl, sw.getName());
		Request get = Request.Get(url).addHeader("Accept", "application/xml");
		String xml = get.execute().returnContent().asString();

		String newName = sw.getName() + "_copy";
		xml = xml.replaceAll(sw.getName(), newName);

		Request post = Request.Post(baseUrl).addHeader("Accept", "application/xml").bodyString(xml,
				ContentType.APPLICATION_XML);

		String response = post.execute().returnContent().asString();
		org.ogema.core.model.Resource newResource = getApplicationManager().getResourceAccess().getResource(newName);
		assertNotNull(newResource);
		assertTrue(response.contains(newName));
	}

	@Test
	public void restPostWorksWithReferences() throws Exception {
		waitForServer();
		String toplevelRes = "postWithReferenceTest";
		String actualResource = "actualResource";
		String directReference = "directReference";
		String referenceToReference = "referenceToReference";
		String postContent = "<og:resource xmlns:og=\"http://www.ogema-source.net/REST\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
				+ "<name>"
				+ toplevelRes
				+ "</name><type>org.ogema.model.sensors.HumiditySensor</type>"
				+ "<resourcelink><link>"
				+ toplevelRes
				+ "/"
				+ directReference
				+ "</link><type>org.ogema.core.model.simple.FloatResource</type><name>"
				+ referenceToReference
				+ "</name></resourcelink>"
				+ "<resourcelink><link>"
				+ toplevelRes
				+ "/"
				+ actualResource
				+ "</link><type>org.ogema.core.model.simple.FloatResource</type><name>"
				+ directReference
				+ "</name></resourcelink>"
				+ "<resource xsi:type=\"og:FloatResource\"><name>"
				+ actualResource
				+ "</name><type>org.ogema.core.model.simple.FloatResource</type><value>42.0</value></resource>"
				+ "</og:resource>"; // one top-level resource with three subresources, where subRes3 references subRes2 references subRes1, and subRes3 is created first

		//FIXME: output with &references=true seems to be broken
		Request post = Request.Post(baseUrl + "?depth=100").addHeader("Accept", "application/xml").bodyString(
				postContent, ContentType.APPLICATION_XML);
		System.out.println(postContent);
		System.out.println(post.execute().returnContent().asString());
		org.ogema.core.model.Resource newResource = getApplicationManager().getResourceAccess()
				.getResource(toplevelRes);
		assertNotNull(newResource);
		assertNotNull("link '" + actualResource + "' is missing", newResource.getSubResource(actualResource));
		assertNotNull("link '" + directReference + "' is missing", newResource.getSubResource(directReference));
		assertNotNull("link '" + referenceToReference + "' is missing", newResource
				.getSubResource(referenceToReference));

		final String url = baseUrl + "/" + toplevelRes + "?depth=100";
		Request req = Request.Get(url).addHeader("Accept", "application/xml");
		String xml2 = req.execute().returnContent().asString();
		//System.out.println("xml2: " + xml2);
		String referenceXML = "<link>" + toplevelRes + "/" + actualResource + "</link>";
		assertTrue("XML serialization for references misses the location of the referenced resource", xml2
				.contains(referenceXML));
	}

	/**
	 * Test checks if REST-interface denies illegal Resource modifications
	 * correcly by returning an appropriate Http-Error (currently 500)
	 *
	 * @throws Exception
	 */
	@Test
	public void putWithIllegalResourceTypeCausesError() throws Exception {
		waitForServer();

		//REST-Url to OGEMA-ElectricBinarySwitch-Resource with resource-tree depth=100
		final String resourceUrl = baseUrl + "/switch?depth=100";

		//send a request to the server and get the ElectricBinarySwitch-resource as json-string
		String jsonFromRest = Request.Get(resourceUrl).addHeader("Accept", "application/json").execute()
				.returnContent().asString();

		//replace FloatResource by non existing / illegal LongResource
		String illegalJson = jsonFromRest.replaceAll("FloatResource", "LongResource");

		//send response with changed (now illegal) json. 
		//Use http-put for resource update (illegal update)
		String response = Request.Put(resourceUrl).bodyString(illegalJson, ContentType.APPLICATION_JSON).execute()
				.returnContent().asString();
		Assert.assertFalse(response.contains("LongResource"));
		//TODO assert correct error-code in httpStatus, at the moment 200 / OK is returned

		//try to change a resources name to null
		illegalJson = jsonFromRest.replaceFirst("\"heatCapacity\"", "null");
		StatusLine httpStatus = Request.Put(resourceUrl).bodyString(illegalJson, ContentType.APPLICATION_JSON)
				.execute().returnResponse().getStatusLine();
		Assert.assertEquals(500, httpStatus.getStatusCode());
		//Assert.assertEquals("name must not be null", httpStatus.getReasonPhrase());

		//try to set an illegal value for FloatResource
		illegalJson = jsonFromRest.replaceFirst("\"value\" : (\\d+)[.](\\d+)", "\"value\" : \"notfloat\" ");
		httpStatus = Request.Put(resourceUrl).bodyString(illegalJson, ContentType.APPLICATION_JSON).execute()
				.returnResponse().getStatusLine();
		//TODO
		/*
		 Assert.assertEquals(500, httpStatus.getStatusCode());
		 Assert.assertEquals("incompatible value-type", httpStatus.getReasonPhrase());
		 */

		//try to manipulate a resources path, first convert the json-string to java object representation
		//JsonFactory jFac = new JsonFactory();
		//JsonParser jp = jFac.createJsonParser(jsonFromRest);
		//manipulate FloatResource-path
		illegalJson = jsonFromRest.replaceFirst("switch/heatCapacity", "heatCapacity");

		httpStatus = Request.Put(resourceUrl).bodyString(illegalJson, ContentType.APPLICATION_JSON).execute()
				.returnResponse().getStatusLine();
		//TODO
		/*
		 Assert.assertEquals(500, httpStatus.getStatusCode());
		 Assert.assertEquals("incompatible value-type", httpStatus.getReasonPhrase());
		 */

	}

	@Test
	public void restDeleteWorks() throws Exception {
		waitForServer();

		ResourceAccess resacc = getApplicationManager().getResourceAccess();

		String name = newResourceName();
		PhysicalElement pe = getApplicationManager().getResourceManagement()
				.createResource(name, PhysicalElement.class);

		pe.location().geographicLocation().create();

		assertTrue(pe.location().geographicLocation().exists());

		String url = baseUrl + "/" + pe.location().geographicLocation().getPath();
		System.out.println("deleting " + url);
		Response r = Request.Delete(url).execute();
		assertEquals(200, r.returnResponse().getStatusLine().getStatusCode());
		assertFalse(pe.location().geographicLocation().exists());
		r = Request.Get(url).execute();
		assertEquals(404, r.returnResponse().getStatusLine().getStatusCode());

		url = baseUrl + "/" + pe.getPath();
		System.out.println("deleting " + url);
		r = Request.Delete(url).execute();
		assertEquals(200, r.returnResponse().getStatusLine().getStatusCode());
		assertEquals(404, Request.Get(url).execute().returnResponse().getStatusLine().getStatusCode());

		assertNull(resacc.getResource(name));
		assertFalse(pe.exists());
	}

}
