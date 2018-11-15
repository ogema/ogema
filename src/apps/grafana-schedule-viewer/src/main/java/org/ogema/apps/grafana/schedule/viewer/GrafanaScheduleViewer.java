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
package org.ogema.apps.grafana.schedule.viewer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.apps.grafana.schedule.viewer.SessionData.DataSupplier;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.tools.grafana.base.InfluxFake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(specVersion = "1.2")
@Service(Application.class)
public class GrafanaScheduleViewer implements Application {

	private final static String SESSION_ATTRIBUTE = "grafana_schedviewer_data";
	static final Logger logger = LoggerFactory.getLogger(GrafanaScheduleViewer.class);
	protected ApplicationManager am;
	protected ResourceAccess ra;
	protected List<Class<? extends Resource>> forecastScheduleTypes, programScheduleTypes, otherScheduleTypes;
	protected InfluxFake infl;
	private DataSupplierImpl supplier;

	private String webResourceBrowserPath;
	private String servletPath;

	@Override
    public void start(ApplicationManager am) {
        this.am = am;
        this.ra = am.getResourceAccess();
        this.forecastScheduleTypes = new ArrayList<>();
        this.programScheduleTypes = new ArrayList<>();
        this.otherScheduleTypes = new ArrayList<>();
        logger.debug("Grafana log app started", getClass().getName());
        String webResourcePackagePath = "org/ogema/apps/grafana/schedule/viewer/grafana-1.9.1";
        String appNameLowerCase = "GrafanaScheduleViewer".toLowerCase();
        webResourceBrowserPath = am.getWebAccessManager().registerWebResourcePath("", webResourcePackagePath);
        this.supplier = new DataSupplierImpl(ra);
        this.infl = new InfluxFake(am,Collections.<String,Map> emptyMap(), 5000) {
        	
			private static final long serialVersionUID = 1L;

			@Override
        	protected void onGet(HttpServletRequest req) {
        		final HttpSession session = req.getSession();
        		SessionData o;
        		try {
        			o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE);
        		} catch (ClassCastException e) {
        			session.removeAttribute(SESSION_ATTRIBUTE);
        			o = null;
        		}
        		if (o == null || ra == null) {
        			o = new SessionData(supplier, ra);
        			session.setAttribute(SESSION_ATTRIBUTE, o);
        		}
        	}
			
			@SuppressWarnings("rawtypes")
			@Override
			public Map<String, Map> getPanels(HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE);
        		if (o == null)
        			return Collections.emptyMap();
        		return o.getPanels();
			}
			
			@Override
			public Map<String, Map<String, Class<? extends Resource>>> getRestrictions(HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE);
        		if (o == null)
        			return Collections.emptyMap();
        		return o.getRestrictions();
			}
			
			@Override
			protected List<? extends Resource> getResources(Class<? extends Resource> clazz, HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE);
        		if (o == null)
        			return Collections.emptyList();
        		return o.getResources(clazz);
			}
			
			@Override
			protected Class getClass(String longResTypeName, HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE);
        		if (o == null)
        			return null;
        		return o.getClass(longResTypeName);
			}
        	
        };
        
        
//        webResourceBrowserPath =am.getWebAccessManager().registerWebResource("/org/ogema/apps/grafana-schedule-viewer",webResourcePackagePath);        
//        panels = new LinkedHashMap<>();
        // row 1
 /*       Map<String,Class<? extends Resource>> firstRowPanels = new LinkedHashMap<String,Class<? extends Resource>>();       
         firstRowPanels.put("Programs / Definition schedules",DefinitionSchedule.class);
         panels.put("Programs / Definition schedules", firstRowPanels);
        
         // row 2
         Map<String,Class<? extends Resource>> secondRowPanels = new LinkedHashMap<String,Class<? extends Resource>>();              
         secondRowPanels.put("Forecast schedules",ForecastSchedule.class);
         panels.put("Forecast schedules", secondRowPanels); */

//        this.infl = new InfluxFake(am, panels, -1);

        servletPath = "/apps/ogema/" + appNameLowerCase + "/fake_influxdb/series";
        am.getWebAccessManager().registerWebResource(servletPath, infl);
    }

	@Override
	public void stop(AppStopReason reason) {
		//    	am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		if (am != null) {
			am.getWebAccessManager().unregisterWebResourcePath("");
			am.getWebAccessManager().unregisterWebResource(servletPath);
		}
		am = null;
		ra = null;
		infl = null;
		final DataSupplierImpl supplier = this.supplier;
		this.supplier = null;
		if (supplier != null)
			supplier.close();
	}


	private static class DataSupplierImpl implements DataSupplier {

		private final Object dataLock = new Object();
    	private volatile WeakReference<GlobalData> globalData = new WeakReference<GlobalData>(null);
    	private final ResourceAccess ra;
    	
    	 DataSupplierImpl(ResourceAccess ra) {
    		 this.ra = ra;
    	 }
		
		@Override
		public GlobalData getGlobalData() {
			final WeakReference<GlobalData> reference = this.globalData;
			if (reference == null) // closed
				return null;
			GlobalData data = reference.get();
			if (data != null)
				return data;
			synchronized (dataLock) {
				data = this.globalData.get();
				if (data != null)
					return data;
				data = new GlobalData(ra);
				try { // wait for callbacks
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} 
				this.globalData = new WeakReference<GlobalData>(data);
				return data;
			}
		}
		
		void close() {
			GlobalData gd = globalData.get();
			if (gd != null) {
				gd.close();
			}
			globalData = null;
		}
		
	}
	
}
