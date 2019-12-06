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
package ogema.logging.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.recordeddata.RecordedData;
import org.ogema.core.recordeddata.RecordedDataConfiguration;
import org.ogema.core.recordeddata.RecordedDataConfiguration.StorageType;
import org.ogema.model.actors.Actor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.targetranges.TargetRange;
import org.osgi.framework.ServiceRegistration;

@Component(specVersion = "1.2")
@Service(Application.class)
public class LoggingApp implements Application {

	ApplicationManager am;

	private String webResourceBrowserPath;
	private String servletPath;
	private ServiceRegistration<?> shellCommands;

	@Override
	public void start(ApplicationManager appManager) {
		this.am = appManager;

		am.getLogger().debug("{} started", getClass().getName());
		String webResourcePackage = "ogema.logging.app";
		webResourcePackage = webResourcePackage.replace(".", "/");

		String appNameLowerCase = "LoggingApp";
		appNameLowerCase = appNameLowerCase.toLowerCase();

		//path to find the index.html /ogema/<this app name>/index.html
		webResourceBrowserPath = "/ogema/" + appNameLowerCase;
		//package/path to find the resources inside this application
		String webResourcePackagePath = webResourcePackage + "/gui";
		//path for the http servlet /apps/ogema/<this app name>
		servletPath = "/apps/ogema/" + appNameLowerCase;

		appManager.getWebAccessManager().registerWebResource(webResourceBrowserPath, webResourcePackagePath);
		appManager.getWebAccessManager().registerWebResource(servletPath, new LoggingAppServlet(this));
		try {
        	if (Boolean.getBoolean("org.ogema.gui.usecdn")) {
        		am.getWebAccessManager().registerStartUrl(webResourceBrowserPath + "/index2.html");
        	}
        } catch (SecurityException ok) {}
		final Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "ogm");
		props.put("osgi.command.function", new String[] { 
			"getLoggedResources",
			"getLoggingConfig",
			"isLoggingActive",
			"logAllActors",
			"logAllSensors",
			"startLogging",
			"stopLogging"
		});
		try {
			shellCommands = am.getAppID().getBundle().getBundleContext()
				.registerService(ShellCommands.class, new ShellCommands(this), props);
		} catch (NoClassDefFoundError ignore) {}

		
	}

	@Override
	public void stop(AppStopReason reason) {
		final ServiceRegistration<?> sreg = this.shellCommands;
		this.shellCommands = null;
		if (sreg != null) {
			try {
				sreg.unregister();
			} catch (Exception ignore) {}
		}
		if (am != null) {
			am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
			am.getWebAccessManager().unregisterWebResource(servletPath);
		}
		am = null;
	}
	
	int logAllSensors() {
		int cnt = 0;
		for (Sensor sensor : am.getResourceAccess().getResources(Sensor.class)) {
			if (logIfNotActive(sensor.reading()))
				cnt++;
		}
		for (Actor actor : am.getResourceAccess().getResources(Actor.class )) {
			if (logIfNotActive(actor.stateFeedback()))
				cnt++;
		}
		return cnt;
	}
	
	int logAllActors() {
		int cnt = 0;
		for (Actor actor : am.getResourceAccess().getResources(Actor.class )) {
			if (logIfNotActive(actor.stateControl()))
				cnt++;
		}
		for (TargetRange range : am.getResourceAccess().getResources(TargetRange.class)) {
			if (logIfNotActive(range.setpoint()))
				cnt++;
		}
		return cnt;
	}
	
	boolean activateLogging(final String path) {
		final Resource r = am.getResourceAccess().getResource(path);
		if (r == null)
			return false;
		return logIfNotActive(r);
	}
	
	boolean stopLogging(final String path) {
		final Resource r = am.getResourceAccess().getResource(path);
		if (r == null)
			return false;
		final RecordedData rd = getRecordedData(r);
		if (rd == null || rd.getConfiguration() == null)
			return false;
		rd.setConfiguration(null);
		return true;
	}
	
	Collection<Resource> getLoggedResources() {
		final List<Resource> list= new ArrayList<>();
		for (SingleValueResource res : am.getResourceAccess().getResources(SingleValueResource.class)) {
			if (isLoggingActive(res))
				list.add(res);
		}
		return list;
	}
	
	boolean isLoggingActive(final String path) {
		final Resource r = am.getResourceAccess().getResource(path);
		if (r == null)
			return false;
		return isLoggingActive(r);
	}
	
	RecordedDataConfiguration getLoggingConfiguration(final String path) {
		final Resource r = am.getResourceAccess().getResource(path);
		if (r == null)
			return null;
		final RecordedData rd = getRecordedData(r);
		if (rd == null)
			return null;
		return rd.getConfiguration();
	}
	
	private static boolean isLoggingActive(final Resource r) {
		final RecordedData rd = getRecordedData(r);
		if (rd == null)
			return false;
		return rd.getConfiguration() != null;
	}
	
	private static boolean logIfNotActive(final Resource r) {
		if (!r.isActive())
			return false;
		final RecordedData rd = getRecordedData(r);
		if (rd == null || rd.getConfiguration() != null) // alreading logging
			return false;
		final RecordedDataConfiguration cfg = new RecordedDataConfiguration();
		cfg.setStorageType(StorageType.ON_VALUE_UPDATE);
		rd.setConfiguration(cfg);
		return true;
	}
	
	static RecordedData getRecordedData(Resource res) {
		RecordedData rd = null;
		if (res instanceof FloatResource) {
			FloatResource fl = (FloatResource) res;
			rd = fl.getHistoricalData();
		}
		else if (res instanceof IntegerResource) {
			IntegerResource fl = (IntegerResource) res;
			rd = fl.getHistoricalData();
		}
		else if (res instanceof BooleanResource) {
			BooleanResource fl = (BooleanResource) res;
			rd = fl.getHistoricalData();
		}
		else if (res instanceof TimeResource) {
			TimeResource tr = (TimeResource) res;
			rd = tr.getHistoricalData();
		}
		return rd;
	}

}
