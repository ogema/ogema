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
package org.ogema.rest.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import static org.joox.JOOX.*;

import org.joox.Match;
import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.timeseries.InterpolationMode;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.locations.Room;
import org.ogema.model.locations.WorkPlace;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.ElectricPowerSensor;
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
    
    /* returns body as string, regardless of server return code */
    final static ResponseHandler<String> TOSTRINGHANDLER = new ResponseHandler<String>() {

            @Override
            public String handleResponse(HttpResponse hr) throws ClientProtocolException, IOException {
                InputStream is = hr.getEntity().getContent();
                StringBuilder sb = new StringBuilder();
                try (InputStreamReader r = new InputStreamReader(is); BufferedReader br = new BufferedReader(r)){
                    String line;
                    while ((line = br.readLine())!=null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
            }
        };

	OnOffSwitch sw;
	final static String baseUrl = "http://localhost:" + HTTP_PORT + "/rest/resources";
	final static String baseUrlDataRecorder = "http://localhost:" + HTTP_PORT + "/rest/recordeddata";
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
						"4.4.1").start(),
				CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").version(
						"4.4.1").start(), CoreOptions.mavenBundle("commons-logging", "commons-logging", "1.1.3").start(), };
	}
	
    private static String appendUserInfo(String path) {
    	StringBuilder sb = new StringBuilder(path);
    	int idx = path.indexOf('?');
    	if (idx < 0)
    		sb.append('?');
    	else
    		sb.append('&');
    	sb.append("user=rest&pw=rest");
    	return sb.toString();
    }

	@Before
	@SuppressWarnings("deprecation")
	public void setup() throws Exception {
		ApplicationManager appMan = getApplicationManager();
		ResourceManagement resMan = appMan.getResourceManagement();
		sw = resMan.createResource("switch", OnOffSwitch.class);
		sw.heatCapacity().create();
		sw.heatCapacity().setValue(47.11f);

		schedule = sw.heatCapacity().addDecorator("defSchedule", Schedule.class);
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
        waitForServlet(baseUrl);
    }

	public void waitForServlet(String baseUrl) throws IOException {
		assertNotNull(http);
		Request test = Request.Head(appendUserInfo(baseUrl)).addHeader("Accept", "application/json");
		int tries = 0;
		while (true) {
			int statusCode = test.execute().returnResponse().getStatusLine().getStatusCode();
			if (statusCode / 100 == 2) {
				break;
			}
			if (tries++ > 40) {
				Assert.fail("REST servlet not working: " + statusCode);
			}
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
		}
	}

	@Test
	public void restPutXml() throws Exception {
		waitForServer();
		final String url = appendUserInfo(baseUrl + "/switch?depth=100");
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
		final String url = appendUserInfo(baseUrl + "/switch?depth=100");
		Request req = Request.Get(url).addHeader("Accept", "application/json");
		String json = req.execute().returnContent().asString();
		System.out.println("JSON from server:\n" + json);

		assertNotEquals(json, json = json.replace("47.11", "42.0"));

		System.out.println("send to server:\n" + json);
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
		final String url =baseUrl + "/" + schedule.getPath("/");

		Request req = Request.Get(appendUserInfo(url) + "&start=3").addHeader("Accept", "application/xml");
		String xml = req.execute().returnContent().asString();

		System.out.println(xml);
		System.out.println(xml);

		Match sched = $(xml);
		assertEquals(3, sched.children("entry").each().size());

		assertEquals(3.5d, Double.valueOf(sched.children("entry").each().get(0).child("value").content()), 0d);

		sched.children("entry").each().get(0).child("value").empty().append("42");

		Request put = Request.Put(appendUserInfo(url)).addHeader("Accept", "application/xml").bodyString(sched.toString(),
				ContentType.APPLICATION_XML);

		System.out.println(put.execute().returnContent().asString());
		System.out.println("");

		assertEquals(42d, schedule.getValue(3).getValue().getDoubleValue(), 0d);
	}

	@Test
	public void restPostScheduleWorks() throws Exception {
		waitForServer();
		TemperatureResource t = getApplicationManager().getResourceAccess().getResource("restSchedulePostTest");
		assertNull(t);

		Request post = Request.Post(appendUserInfo(baseUrl)).bodyStream(getClass().getResourceAsStream("/scheduletest.xml"),
				ContentType.APPLICATION_XML);
		Response resp = post.execute();
		System.out.println(resp.returnResponse().getStatusLine());

		t = getApplicationManager().getResourceAccess().getResource("restSchedulePostTest");
		assertNotNull(t);
		assertEquals(InterpolationMode.LINEAR, t.forecast().getInterpolationMode());
		List<SampledValue> values = t.forecast().getValues(0);
		assertEquals(2, values.size());
	}
    
    @Test
    @SuppressWarnings("unchecked")
    public void restPutWorksForTopLevelResourceLists() throws Exception {
        waitForServer();
        
        ResourceList<PhysicalElement> l = getApplicationManager().getResourceManagement().createResource(newResourceName(), ResourceList.class);
        l.setElementType(PhysicalElement.class);
        
        PhysicalElement pe = l.add();
        pe.name().create();
        pe.name().setValue("testElement");
        
        String name = l.getName();
        String url = appendUserInfo(baseUrl + "/" + name + "?depth=100");
        String xml = Request.Get(url).addHeader("Content-Type", "application/xml").execute().returnContent().asString();
        
        System.out.println(xml);
        l.delete();
        assertNull("resource must be deleted", getApplicationManager().getResourceAccess().getResource(name));
        
        Response r = Request.Post(appendUserInfo(baseUrl)).bodyString(xml, ContentType.APPLICATION_XML).execute();
        
        System.out.println(r.handleResponse(TOSTRINGHANDLER));
        ResourceList<?> recreated = getApplicationManager().getResourceAccess().getResource(name);
        assertNotNull("resource has been created", recreated);
        assertEquals("correct element type has been set", PhysicalElement.class, recreated.getElementType());
        assertEquals("resource list elements correct", 1, recreated.getAllElements().size());
    }

	@Test
	public void restPutWorksForResourceLists_XML() throws Exception {
		restPutWorksForResourceLists(ContentType.APPLICATION_XML);
	}

	@Test
//	@Ignore
	public void restPutWorksForResourceLists_JSON() throws Exception {
		restPutWorksForResourceLists(ContentType.APPLICATION_JSON);
	}

	private void restPutWorksForResourceLists(ContentType contentType) throws Exception {
		waitForServer();
		Room room = getApplicationManager().getResourceManagement().createResource(newResourceName(), Room.class);
		room.workPlaces().create();
		@SuppressWarnings("unused")
		ResourceList<WorkPlace> wps = room.workPlaces();
		WorkPlace wp1 = room.workPlaces().add();
		String wpName = wp1.getName();
		assertTrue(wp1.exists());
		assertEquals(1, room.workPlaces().size());

		final String url = appendUserInfo(baseUrl + "/" + room.getName() + "?depth=100");
		waitForServer();

		String roomAsString = Request.Get(url).addHeader("Content-Type", contentType.toString()).execute()
				.returnContent().asString();
		System.out.println(roomAsString);

		wp1.delete();

		assertEquals("list should be empty", 0, room.workPlaces().size());

		Request.Put(appendUserInfo(baseUrl + "/" + room.getName())).bodyString(roomAsString, contentType).execute();

		assertEquals("list should contain 1 room", 1, room.workPlaces().getAllElements().size());
		assertEquals(wpName, room.workPlaces().getAllElements().get(0).getName());

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
		final String url = appendUserInfo(String.format("%s/%s?depth=100", baseUrl, sw.getName()));
		Request get = Request.Get(url).addHeader("Accept", "application/xml");
		String xml = get.execute().returnContent().asString();

		String newName = sw.getName() + "_copy";
		xml = xml.replaceAll(sw.getName(), newName);

		Request post = Request.Post(appendUserInfo(baseUrl)).addHeader("Accept", "application/xml").bodyString(xml,
				ContentType.APPLICATION_XML);

		String response = post.execute().returnContent().asString();
		org.ogema.core.model.Resource newResource = getApplicationManager().getResourceAccess().getResource(newName);
		assertNotNull(newResource);
		assertTrue(response.contains(newName));
	}

	/* also tests correct creation of references between 2 newly created top level resources */
	@Test
	public void restPutWorksForTopLevelResources() throws Exception {
		waitForServer();

		ResourceAccess resacc = getApplicationManager().getResourceAccess();
		ResourceManagement resman = getApplicationManager().getResourceManagement();
		String s1name = newResourceName();
		String s2name = newResourceName();

		ElectricPowerSensor s1 = resman.createResource(s1name, ElectricPowerSensor.class);
		ElectricPowerSensor s2 = resman.createResource(s2name, ElectricPowerSensor.class);

		s2.physDim().length().create();
		s2.physDim();
		s2.physDim().length().setValue(2);

		s1.physDim().setAsReference(s2.physDim());

		String url = appendUserInfo(String.format("%s/%s?depth=100", baseUrl, ""));
		Request get = Request.Get(url).addHeader("Accept", "application/xml");
		String xml = get.execute().returnContent().asString();

		System.out.println(xml);

		s1.delete();
		s2.delete();

		assertNull(resacc.getResource(s1name));
		assertNull(resacc.getResource(s2name));

		Request put = Request.Put(url).addHeader("Accept", "application/xml").bodyString(xml,
				ContentType.APPLICATION_XML);
		Response putResponse = put.execute();
		assertEquals(2, putResponse.returnResponse().getStatusLine().getStatusCode() / 100);
		//System.out.println(putResponse.returnContent().asString());

		assertNotNull(s1 = resacc.getResource(s1name));
		assertNotNull(s2 = resacc.getResource(s2name));

		assertTrue(s1.physDim().isReference(false));
		assertTrue(s1.physDim().equalsLocation(s2.physDim()));
		assertEquals(2, s2.physDim().length().getValue(), 0);
		assertEquals(2, s1.physDim().length().getValue(), 0);
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
		Request post = Request.Post(appendUserInfo(baseUrl + "?depth=100")).addHeader("Accept", "application/xml").bodyString(
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

		final String url = appendUserInfo(baseUrl + "/" + toplevelRes + "?depth=100");
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
		final String resourceUrl = appendUserInfo(baseUrl + "/switch?depth=100");

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

		String url = appendUserInfo(baseUrl + "/" + pe.location().geographicLocation().getPath());
		System.out.println("deleting " + url);
		Response r = Request.Delete(url).execute();
		assertEquals(200, r.returnResponse().getStatusLine().getStatusCode());
		assertFalse(pe.location().geographicLocation().exists());
		r = Request.Get(url).execute();
		assertEquals(404, r.returnResponse().getStatusLine().getStatusCode());

		url = appendUserInfo(baseUrl + "/" + pe.getPath());
		System.out.println("deleting " + url);
		r = Request.Delete(url).execute();
		assertEquals(200, r.returnResponse().getStatusLine().getStatusCode());
		assertEquals(404, Request.Get(url).execute().returnResponse().getStatusLine().getStatusCode());

		assertNull(resacc.getResource(name));
		assertFalse(pe.exists());
	}
    
    @Test
	public void restDeleteWorksForToplevelResourceLists() throws Exception {
		waitForServer();
        @SuppressWarnings("unchecked")
        ResourceList<Resource> l = getApplicationManager().getResourceManagement().createResource(newResourceName(), ResourceList.class);
        l.setElementType(Resource.class);
        String name = l.getName();
        
        l.add(getApplicationManager().getResourceManagement().createResource(newResourceName(), StringResource.class));
        
        String url = appendUserInfo(baseUrl + "/" + l.getPath());
        @SuppressWarnings("unused")
		Response r = Request.Delete(url).execute();
        
        //System.out.println(r.returnContent());
        
        assertNull("resource must not exist", getApplicationManager().getResourceAccess().getResource(name));
    }

	@Test
	public void restReturnsInactiveResources() throws IOException {
		ResourceManagement resman = getApplicationManager().getResourceManagement();
		Room r = resman.createResource(newResourceName(), Room.class);
		r.co2Sensor().name().create();
		r.activate(true);
		String testString = "notAUsualName" + Math.random();
		r.co2Sensor().name().setValue(testString);
		r.co2Sensor().deactivate(true);
		assertFalse(r.co2Sensor().name().isActive());

		String response = Request.Get(appendUserInfo(baseUrl + "/" + r.getPath() + "?depth=100")).execute().returnContent().asString();
		System.out.println(response);

		assertTrue(response.contains(r.co2Sensor().getResourceType().getCanonicalName()));
		assertTrue(response.contains(testString));

		// test again with top level resource
		r.deactivate(true);
		assertFalse(r.isActive());
		response = Request.Get(appendUserInfo(baseUrl + "?depth=100")).execute().returnContent().asString();
		assertTrue(response.contains(r.co2Sensor().getResourceType().getCanonicalName()));
		assertTrue(response.contains(testString));
	}

	@Test
	public void recordedDataServletWorks() throws ClientProtocolException, IOException {
        waitForServlet(baseUrlDataRecorder);
		final FloatResource r = getApplicationManager().getResourceManagement().createResource(newResourceName(), FloatResource.class);
		final StorageType type = StorageType.FIXED_INTERVAL;
		// enable logging via POST
		final int code = Request.Post(appendUserInfo(baseUrlDataRecorder + "/" + r.getPath()))
			.bodyString("{\"storageType\":\"" + type.toString() + "\"}", ContentType.APPLICATION_JSON)
			.execute().returnResponse().getStatusLine().getStatusCode();
		Assert.assertEquals("Datalogging config POST request failed: unexpected status code", 200, code);
		final RecordedDataConfiguration cfg = r.getHistoricalData().getConfiguration();
		Assert.assertNotNull("Logging should have been enabled",cfg);
		Assert.assertEquals("Unexpected storage type",type, cfg.getStorageType());
		// get log data via GET
		final int code2 = Request.Get(appendUserInfo(baseUrlDataRecorder + "/" + r.getPath()))
				.execute().returnResponse().getStatusLine().getStatusCode();
		Assert.assertEquals("Datalogging GET request failed: unexpected status code", 200, code2);
		// disable logging via DELETE
		final int code3 = Request.Delete(appendUserInfo(baseUrlDataRecorder + "/" + r.getPath()))
				.execute().returnResponse().getStatusLine().getStatusCode();
		Assert.assertEquals("Datalogging config DELETE request failed: unexpected status code", 200, code3);
		final RecordedDataConfiguration cfg3 = r.getHistoricalData().getConfiguration();
		Assert.assertNull("Logging should have been disabled",cfg3);
		r.delete();
	}
	
}
