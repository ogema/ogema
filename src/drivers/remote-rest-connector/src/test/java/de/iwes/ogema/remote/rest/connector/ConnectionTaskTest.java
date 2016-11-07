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
package de.iwes.ogema.remote.rest.connector;


import javax.inject.Inject;
import org.apache.http.client.fluent.Request;

import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.StringResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.http.HttpService;

import de.iwes.ogema.remote.rest.connector.model.ConnectionTask;
import de.iwes.ogema.remote.rest.connector.model.RestConnection;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ConnectionTaskTest extends OsgiAppTestBase {

	final String baseUrl = "http://localhost:" + HTTP_PORT + "/rest/resources";

	@Inject
	HttpService http;
    
    ApplicationManager appman;

    public ConnectionTaskTest() {
        super(true);
    }

	@Override
	@Configuration
	public Option[] config() {
		return new Option[] {
				CoreOptions.systemProperty("org.ogema.security").value("off"),
				CoreOptions.composite(super.config()),
				CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").versionAsInProject().start(),
				CoreOptions.mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").versionAsInProject().start(), CoreOptions.mavenBundle("commons-logging", "commons-logging", "1.1.3").start(), 
				CoreOptions.mavenBundle("org.json", "json", "20160212").start()};
		
	}

	@Before
	@SuppressWarnings("deprecation")
	public void setup() throws Exception {
		appman = getApplicationManager();
	}

	public void waitForServer() throws Exception {
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
	public void doPullWorksForStringResources() throws Exception {
        StringResource src = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        StringResource target = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        RestConnection conn = target.addDecorator("remote", RestConnection.class);
        conn.remotePath().create();
        conn.remotePath().setValue(baseUrl + "/" + src.getPath());
        System.out.println("remote path=" + conn.remotePath().getValue());
        
        src.setValue("Hello OGEMA!");
        assertTrue(target.getValue().isEmpty());
        
        ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPull();
        
        assertEquals(src.getValue(), target.getValue());
        System.out.println(target.getValue());
	}
    
    @Test
	public void doPushWorksForStringResources() throws Exception {
        StringResource remote = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        StringResource origin = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        RestConnection conn = origin.addDecorator("remote", RestConnection.class);
        conn.remotePath().create();
        conn.remotePath().setValue(baseUrl + "/" + remote.getPath());
        System.out.println("remote path=" + conn.remotePath().getValue());
        
        origin.setValue("Hello OGEMA!");
        assertTrue(remote.getValue().isEmpty());
        
        ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPush();
        
        assertEquals(origin.getValue(), remote.getValue());
        System.out.println(remote.getValue());
	}

}
