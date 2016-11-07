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
package org.ogema.rest.tests.patterns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.exam.ResourceAssertions;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.locations.Room;
import org.ogema.rest.patternmimic.FakePattern;
import org.ogema.rest.patternmimic.FakePatternAccess;
import org.ogema.rest.patternmimic.PatternMatch;
import org.ogema.rest.patternmimic.PatternMatchList;
import org.ogema.rest.tests.patterns.tools.TestPattern;
import org.ogema.rest.tests.patterns.tools.Util;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.slf4j.LoggerFactory;

@ExamReactorStrategy(PerClass.class)
public class FakePatternTest extends OsgiAppTestBase {

	private FakePatternAccess patternAccess;
	private FakePattern pattern;
	private ResourcePatternAccess rpa;
	private ResourceManagement resMan;
	private OgemaLogger logger;
	
	final static String baseUrl = "http://localhost:" + HTTP_PORT + "/rest/patterns";
	final static String userInfo = "?user=rest&pw=rest";

//	@Inject
//	HttpService http;

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
	
	@Before
	public void init() {
		this.patternAccess = new FakePatternAccess(getApplicationManager());
		this.pattern = Util.convert(TestPattern.class, getApplicationManager());
		this.rpa = getApplicationManager().getResourcePatternAccess();
		this.resMan = getApplicationManager().getResourceManagement();
		logger = (OgemaLogger) LoggerFactory.getLogger("org.ogema.rest.servlet.RestApp"); 
		logger.setMaximumLogLevel(LogOutput.CONSOLE, LogLevel.TRACE);
		logger.trace("Log level set to TRACE");
	}

	@Test
	public void patternDeserializationWorks() {
		Field[] patternFields = TestPattern.class.getFields();
		Assert.assertEquals("Pattern deserialization failed",patternFields.length-1,pattern.resourceFields.size());
		Assert.assertEquals("Pattern deserialization failed: unexpected demanded model type " + pattern.modelClass.getName(),CoolingDevice.class, pattern.modelClass);
	}
	
	@Test
	public void patternCreationWorks() throws ClassNotFoundException {
		PatternMatch match = patternAccess.create(pattern, null, newResourceName());
		Assert.assertNotNull(match.demandedModel);
		ResourceAssertions.assertExists(match.demandedModel.demandedModel);
		TestPattern resourcePattern  = new TestPattern(match.demandedModel.demandedModel); // does not create any resources
		rpa.activatePattern(resourcePattern); // necessary?
		Assert.assertTrue("Pattern creation failed",rpa.isSatisfied(resourcePattern, TestPattern.class));
		match.demandedModel.demandedModel.delete();
	}
	
	@Test
	public void patternMatchingWorks() {
		int nrTestTopResources = 17;
		int nrTestSubResources = 11;
		int nrDummyResources = 23;
		List<Resource> resources = new ArrayList<>();
		
		for (int i=0;i<nrTestTopResources;i++) {
			resources.add(Util.createMatchingPattern(rpa, null, newResourceName()).model);
		}
		for (int i=0;i<nrDummyResources;i++) {
			resources.add(resMan.createResource(newResourceName(), (i % 2 == 0 ? CoolingDevice.class : Room.class)));
		}
		for (int i=0;i<nrTestSubResources;i++) {
			Resource base = resMan.createResource(newResourceName(), Room.class);
			Util.createMatchingPattern(rpa, base, "sub");
			resources.add(base);
		}
		for (Resource res: resources)
			res.activate(true);
		PatternMatchList matches = patternAccess.getMatches(pattern, null, true, Integer.MAX_VALUE,0);
		// just for testing the test setting; if this fails, there is something wrong, independently of the goal of this test
		Assert.assertEquals(nrTestSubResources + nrTestTopResources, rpa.getPatterns(TestPattern.class, AccessPriority.PRIO_LOWEST).size());
		// the actual test
		Assert.assertEquals("Unexpected number of pattern matches",	nrTestSubResources + nrTestTopResources, matches.getMatches().size());
		
		for (Resource res: resources)
			res.delete();
	}
	
	@Test
	public void patternMatchingForSpecifiedParentWorks() {
		Resource base1 = resMan.createResource(newResourceName(), CoolingDevice.class);
		Resource base2 = resMan.createResource(newResourceName(), Room.class);
		TestPattern pat1a = Util.createMatchingPattern(rpa, base1, "suba");
		Util.createMatchingPattern(rpa, base1, "subb");
		Util.createMatchingPattern(rpa, base2, "suba");
		Util.createMatchingPattern(rpa, base2, "subb");
		TestPattern patTop = Util.createMatchingPattern(rpa, null, newResourceName());
		pat1a.reading.setValue(232); // now the field does not match any more
		
		PatternMatchList matches = patternAccess.getMatches(pattern, base1, true, Integer.MAX_VALUE, 0);
		Assert.assertEquals("Unexpected number of pattern matches.", 1, matches.getMatches().size());
		matches = patternAccess.getMatches(pattern, base2, true, Integer.MAX_VALUE, 0);
		Assert.assertEquals("Unexpected number of pattern matches.", 2, matches.getMatches().size());
		matches = patternAccess.getMatches(pattern, null, true, Integer.MAX_VALUE, 0);
		Assert.assertEquals("Unexpected number of pattern matches.", 4, matches.getMatches().size());
		
		base1.delete();
		base2.delete();
		patTop.model.delete();
	}
	
	private static final String requestThermostatsForSpecificRoom(String roomLocation) {
		return "{" 
		+ "	    \"@type\": \"Pattern\","
		+ "		    \"modelClass\": \"org.ogema.model.devices.buildingtechnology.Thermostat\","
		+ "		    \"resourceFields\" : [{"
		+ "		             \"field\": {"
		+ "		                 \"name\": \"room\","
		+ "		                 \"relativePath\": \"location/room\","
		+ "		                 \"type\": \"org.ogema.model.locations.Room\","
		+ "		                 \"optional\": false,"
		+ "		                 \"accessMode\": \"READ_ONLY\","
		+ "		                  \"location\": \"" + roomLocation + "\""
		+ "		            }   "
		+ "		        }]"
		+ "		}";
	}
	
	public static void waitForServer() throws Exception {
		Request test = Request.Head(baseUrl).addHeader("Accept", "application/json");
		int tries = 0;
		while (true) {
			int statusCode = test.execute().returnResponse().getStatusLine().getStatusCode();
			if (statusCode / 100 == 2 || statusCode == 405) {
				break;
			}
			if (tries++ > 40) {
				Assert.fail("REST servlet not working: " + statusCode);
			}
			Thread.sleep(50);
		}
	}
	
	@Test
	public void requestForSpecifiedLocationWorks() throws Exception {
		Room r1 = resMan.createResource(newResourceName(), Room.class);
		Room r2 = resMan.createResource(newResourceName(), Room.class);
		Thermostat th1 = resMan.createResource(newResourceName(), Thermostat.class);
		Thermostat th2 = resMan.createResource(newResourceName(), Thermostat.class);
		th1.temperatureSensor().reading().create();
		th1.location().room().setAsReference(r1);
		th2.location().room().setAsReference(r2);
		r1.activate(true);
		r2.activate(true);
		th1.activate(true);
		th2.activate(true);
		
		waitForServer();
		Request req = Request.Post(baseUrl + userInfo).addHeader("Accept", "application/json");
		StringEntity entity = new StringEntity(requestThermostatsForSpecificRoom(r1.getLocation()));
		String result = req.body(entity).execute().returnContent().asString();
		System.out.println("JSON: " + result);
		JSONObject json = new JSONObject(result);
		JSONArray matches = json.getJSONArray("matches");
		Assert.assertEquals("Expected exactly one pattern match, but got " + matches.length() + ". ", 1, matches.length());
		Assert.assertEquals("Got the right number of pattern matches, but the wrong resource", th1.getLocation(),  
				matches.getJSONObject(0).getJSONObject("match").getJSONObject("demandedModel").getString("location"));
		r1.delete();
		r2.delete();
		th1.delete();
		th2.delete();
	}
	
	
}
