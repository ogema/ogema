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
package de.iwes.ogema.remote.rest.connector;

import javax.inject.Inject;
import org.apache.http.client.fluent.Request;

import org.junit.Assert;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.LogLevel;
import org.ogema.core.logging.LogOutput;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.exam.OsgiAppTestBase;
import org.ogema.model.sensors.TemperatureSensor;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.service.http.HttpService;
import org.slf4j.LoggerFactory;

import de.iwes.ogema.remote.rest.connector.model.RestConnection;
import de.iwes.ogema.remote.rest.connector.model.RestPullConfig;
import de.iwes.ogema.remote.rest.connector.model.RestPushConfig;
import de.iwes.ogema.remote.rest.connector.tasks.ConnectionTask;
import de.iwes.ogema.remote.rest.connector.tasks.PullTask;
import de.iwes.ogema.remote.rest.connector.tasks.PushTask;
import de.iwes.ogema.remote.rest.connector.tasks.TaskScheduler;

/**
 *
 * @author jlapp
 */
@ExamReactorStrategy(PerClass.class)
public class ConnectionTaskTest extends OsgiAppTestBase {

	final static String baseUrl = "http://localhost:" + HTTP_PORT + "/rest/resources";

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
				CoreOptions.mavenBundle("org.json", "json", "20170516").start()};
		
	}
	
	@Before
	public void setup() throws Exception {
		waitForServer();
		appman = getApplicationManager();
		try {
			((OgemaLogger) LoggerFactory.getLogger("de.iwes.ogema.remote.rest.connector.RemoteRestConnector"))
				.setMaximumLogLevel(LogOutput.CONSOLE, LogLevel.TRACE);
		} catch (ClassCastException e) {
			// ignore
		}
	}

	protected final static TaskScheduler dummyScheduler = new TaskScheduler() {
		
		@Override
		public void reschedule(ConnectionTask task) {}
	};
	
	/**
	 * Creates a configuration without any pull or push configs; they need to be added afterwards;
	 * does not activate the config resource
	 * @param target
	 * @param remotePath
	 * @return
	 */
	protected static RestConnection createConfig(Resource target, String remotePath) {
		 RestConnection conn = target.addDecorator("remote", RestConnection.class);
         conn.remotePath().create();
         conn.remotePath().setValue(baseUrl + "/" + remotePath);
         conn.remoteUser().<StringResource> create().setValue("rest");
         conn.remotePw().<StringResource> create().setValue("rest");
         return conn;
	}
	 
	public void waitForServer() throws Exception {
		assertNotNull(http);
		Request test = Request.Head(baseUrl + "?user=rest&pw=rest").addHeader("Accept", "application/json");
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
		try {
        StringResource src = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        StringResource target = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        RestConnection conn = createConfig(target, src.getPath());  
        conn.pullConfig().create().activate(false);
        System.out.println("remote path=" + conn.remotePath().getValue());
        
        src.setValue("Hello OGEMA!");
        assertTrue(target.getValue().isEmpty());
        
        PullTask ct = new PullTask(conn, appman, dummyScheduler);
//        ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPull();
        
        assertEquals(src.getValue(), target.getValue());
        System.out.println(target.getValue());
        src.delete();
        target.delete();
		} catch (Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}
    
    @Test
	public void doPushWorksForStringResources() throws Exception {
        StringResource remote = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        StringResource origin = appman.getResourceManagement().createResource(newResourceName(), StringResource.class);
        RestConnection conn = createConfig(origin, remote.getPath()); 
        conn.pushConfig().create().activate(false);
        System.out.println("remote path=" + conn.remotePath().getValue());
        
        origin.setValue("Hello OGEMA!");
        assertTrue(remote.getValue().isEmpty());
        
        PushTask ct = new PushTask(conn, appman, dummyScheduler);
        
//        ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPush();
        System.out.println("Local: " + origin.getValue() + ", Remote: " + remote.getValue());
        assertEquals(origin.getValue(), remote.getValue());
        remote.delete();
        origin.delete();
	}
    
    @Test
    public void individualPushConfigWorks() throws IOException {
    	TemperatureSensor tempSens1 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	TemperatureSensor tempSens2 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	tempSens1.reading().<TemperatureResource> create().setCelsius(17);
    	tempSens1.settings().setpoint().create();
    	tempSens2.reading().create();
    	RestConnection conn = createConfig(tempSens1, tempSens2.getPath());
    	RestPushConfig pushConfig = conn.individualPushConfigs().<ResourceList<RestPushConfig>> create().add();
    	pushConfig.remoteRelativePath().<StringResource> create().setValue(tempSens1.reading().getName());
//    	pushConfig.subresource().setAsReference(tempSens1.reading());
    	pushConfig.depth().<IntegerResource> create().setValue(10);
    	conn.individualPushConfigs().activate(true);
    	
    	PushTask ct = new PushTask(conn, appman, dummyScheduler);
    	
//    	ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPush();
        System.out.println("Local: " + tempSens1.reading().getCelsius() + ", Remote: " + tempSens2.reading().getCelsius());
        assertEquals(tempSens1.reading().getCelsius(), tempSens2.reading().getCelsius(), 0.1F);
        assertFalse("Subresource should not have been created: " + tempSens2.settings(), tempSens2.settings().exists());
        tempSens1.delete();
        tempSens2.delete();
    }
    
    @Test
    public void individualPullConfigWorks() throws IOException {
    	TemperatureSensor tempSens1 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	TemperatureSensor tempSens2 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	tempSens1.reading().<TemperatureResource> create().setCelsius(17);
    	tempSens1.settings().setpoint().create();
    	tempSens2.reading().create(); // the target subresource must exist already, otherwise it fails
    	RestConnection conn = createConfig(tempSens2, tempSens1.getPath());
    	
    	RestPullConfig pullConfig = conn.individualPullConfigs().<ResourceList<RestPullConfig>> create().add();
    	pullConfig.remoteRelativePath().<StringResource> create().setValue(tempSens1.reading().getName());
    	pullConfig.depth().<IntegerResource> create().setValue(10);
    	conn.individualPullConfigs().activate(true);
    	
    	PullTask ct = new PullTask(conn, appman, dummyScheduler);
//    	ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPull();
        System.out.println("Local: " + tempSens2.reading().getCelsius() + ", Remote: " + tempSens1.reading().getCelsius());
        assertEquals(tempSens1.reading().getCelsius(), tempSens2.reading().getCelsius(), 0.1F);
        assertFalse("Subresource should not have been created: " + tempSens2.settings(), tempSens2.settings().exists());
        tempSens1.delete();
        tempSens2.delete();
    }
    
    @Test
    public void pushOnInitWorks() throws IOException {
    	@SuppressWarnings("unchecked")
		ResourceList<TemperatureSensor> sensors = appman.getResourceManagement().createResource(newResourceName(), ResourceList.class);
    	sensors.setElementType(TemperatureSensor.class);
    	TemperatureSensor tempSens1 = sensors.add();
    	tempSens1.reading().<TemperatureResource> create().setCelsius(17);
    	tempSens1.settings().setpoint().create();
    	
    	final String remotePath = sensors.getPath() + "/testResource";
    	RestConnection conn = createConfig(tempSens1, remotePath);
    	
    	RestPullConfig pullConfig = conn.individualPullConfigs().<ResourceList<RestPullConfig>> create().add();
    	pullConfig.remoteRelativePath().<StringResource> create().setValue(tempSens1.reading().getName());
    	pullConfig.depth().<IntegerResource> create().setValue(10);
    	pullConfig.pushOnInit().<BooleanResource> create().setValue(true);
    	conn.individualPullConfigs().activate(true);
    	
    	PullTask ct = new PullTask(conn, appman, dummyScheduler);
//    	ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPull();
        TemperatureSensor tempSens2 = appman.getResourceAccess().getResource(remotePath);
        assertNotNull("Remote resource has not been created, despite pushOnInit poll configuration", tempSens2);
        assertTrue("Remote resource has been created through pushOnInit poll configuration, but lacks subresources", tempSens2.reading().exists());
        assertFalse("Remote resource has been created through pushOnInit poll configuration, but has too many subresources", tempSens2.settings().setpoint().exists());
        
        tempSens2.reading().setValue(1234);
        ct.doPull();
        System.out.println("Local: " + tempSens2.reading().getCelsius() + ", Remote: " + tempSens1.reading().getCelsius());
        assertEquals(tempSens1.reading().getCelsius(), tempSens2.reading().getCelsius(), 0.1F);
        tempSens1.delete();
        tempSens2.delete();
    }
    
    @Test
    public void pushOnInitWorksForToplevelRemote() throws IOException {
    	TemperatureSensor tempSens1 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	tempSens1.reading().<TemperatureResource> create().setCelsius(17);
    	tempSens1.settings().setpoint().create();
    	
    	final String remotePath = "testResource";
    	RestConnection conn = createConfig(tempSens1, remotePath);
    	
    	RestPullConfig pullConfig = conn.individualPullConfigs().<ResourceList<RestPullConfig>> create().add();
    	pullConfig.remoteRelativePath().<StringResource> create().setValue(tempSens1.reading().getName());
    	pullConfig.depth().<IntegerResource> create().setValue(10);
    	pullConfig.pushOnInit().<BooleanResource> create().setValue(true);
    	conn.individualPullConfigs().activate(true);
    	
    	PullTask ct = new PullTask(conn, appman, dummyScheduler);
//    	ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
        ct.doPull();
        TemperatureSensor tempSens2 = appman.getResourceAccess().getResource(remotePath);
        assertNotNull("Remote resource has not been created, despite pushOnInit poll configuration", tempSens2);
        assertTrue("Remote resource has been created through pushOnInit poll configuration, but lacks subresources", tempSens2.reading().exists());
        assertFalse("Remote resource has been created through pushOnInit poll configuration, but has too many subresources", tempSens2.settings().setpoint().exists());
        
        tempSens2.reading().setValue(1234);
        ct.doPull();
        System.out.println("Local: " + tempSens2.reading().getCelsius() + ", Remote: " + tempSens1.reading().getCelsius());
        assertEquals(tempSens1.reading().getCelsius(), tempSens2.reading().getCelsius(), 0.1F);
        tempSens1.delete();
        tempSens2.delete();
    }
    
    @Test
    public void individuallyPulledResourcesAreExcludedFromGlobalPush() throws IOException {
    	TemperatureSensor tempSens1 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	TemperatureSensor tempSens2 = appman.getResourceManagement().createResource(newResourceName(), TemperatureSensor.class);
    	final float val1 = 17;
    	final float val2 = 35;
    	tempSens1.reading().<TemperatureResource> create().setCelsius(val1);
    	tempSens1.settings().setpoint().<TemperatureResource> create().setCelsius(val1);
    	tempSens2.reading().<TemperatureResource> create().setCelsius(val2);
    	tempSens2.settings().setpoint().<TemperatureResource> create().setCelsius(val2);
    	
    	// we pull the reading value from tempSens2, and push the setpoint value from tempSens1
    	final String remotePath = tempSens2.getPath();
    	RestConnection conn = createConfig(tempSens1, remotePath);
    	RestPullConfig pullConfig = conn.individualPullConfigs().<ResourceList<RestPullConfig>> create().add();
    	pullConfig.remoteRelativePath().<StringResource> create().setValue(tempSens2.reading().getName());
    	pullConfig.depth().<IntegerResource> create().setValue(4);
    	conn.pushConfig().create();
    	conn.pushConfig().depth().<IntegerResource> create().setValue(4);
    	conn.individualPullConfigs().activate(true);
    	conn.pushConfig().activate(true);
    	
    	PushTask ct = new PushTask(conn, appman, dummyScheduler);
//    	ConnectionTask ct = new ConnectionTask(conn, appman, appman.getLogger());
    	final int code = ct.doPush();
    	Assert.assertEquals("Unexpected server response " + code,200, code);
    	Assert.assertEquals("Push failed", val1, tempSens2.settings().setpoint().getCelsius(),0.1F);
    	Assert.assertEquals("Server value to be pulled has been overwritten by push", val2, tempSens2.reading().getCelsius(),0.1F);
    	PullTask ct2 = new PullTask(conn, appman, dummyScheduler);
    	ct2.doPull();
    	Assert.assertEquals("Pull failed", val2, tempSens1.reading().getCelsius(),0.1F);
    	tempSens1.delete();
    	tempSens2.delete();
    }

}
