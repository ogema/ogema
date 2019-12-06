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
package org.ogema.rest.servlet;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.accesscontrol.PermissionManager;
import org.ogema.accesscontrol.RestAccess;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.recordeddata.DataRecorder;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register REST debug GUI
 */
@Component(specVersion = "1.2")
@Service(Application.class)
public class RestApp implements Application {

	static final Logger logger = LoggerFactory.getLogger(RestApp.class);
	private ApplicationManager appMan;
	
	@Reference
	private HttpService http;
	
	@Reference 
	private PermissionManager permMan;
	
	@Reference
	private RestAccess restAccess;
	
	@Reference
	private DataRecorder dataRecorder;
	
	@Override
	public void start(ApplicationManager appManager) {
		this.appMan = appManager;
		RestServlet restServlet = new RestServlet(permMan, restAccess);
		try {
			http.registerServlet(RestServlet.ALIAS, restServlet, null, null);
			appManager.getLogger().info("REST servlet registered");
		} catch (ServletException | NamespaceException ex) {
			appManager.getLogger().error("could not register servlet", ex);
		}
		RestTypesServlet typesServlet  =new RestTypesServlet(permMan, restAccess);
		try {
			http.registerServlet(RestTypesServlet.ALIAS, typesServlet, null, null);
			appManager.getLogger().info("REST types servlet registered");
		} catch (ServletException | NamespaceException ex) {
			appManager.getLogger().error("could not register servlet", ex);
		}
		RestPatternServlet patternServlet = new RestPatternServlet(permMan, restAccess);
		try {
			http.registerServlet(RestPatternServlet.ALIAS, patternServlet, null, null);
			appManager.getLogger().info("REST pattern servlet registered");
		} catch (ServletException | NamespaceException ex) {
			appManager.getLogger().error("could not register servlet", ex);
		}
		try {
			http.registerServlet(RecordedDataServlet.ALIAS, new RecordedDataServlet(restAccess, dataRecorder), null, null);
			appManager.getLogger().info("Recorded data servlet registered");
		} catch (ServletException | NamespaceException ex) {
			appManager.getLogger().error("could not register servlet", ex);
		}
		String url = appManager.getWebAccessManager().registerWebResourcePath("/rest-gui", "rest/gui");
		appManager.getLogger().info("Pattern debug page registered under url {}", url);
	}
	
	@Override
	public void stop(AppStopReason reason) {
		final ApplicationManager appMan = this.appMan;
		this.appMan = null;
		if (appMan != null) { 
			try {
				appMan.getWebAccessManager().unregisterWebResourcePath("/rest-gui");
			} catch (Exception e) {/*ignore*/}
		}
		try {
			http.unregister(RestServlet.ALIAS);
		} catch (Exception e) {/*ignore*/}
		try {
			http.unregister(RestTypesServlet.ALIAS);
		} catch (Exception e) {/*ignore*/}
		try {
			http.unregister(RestPatternServlet.ALIAS);
		} catch (Exception e) {/*ignore*/}
		try {
			http.unregister(RecordedDataServlet.ALIAS);
		} catch (Exception e) {/*ignore*/}
	}
	
}
