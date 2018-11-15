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
package org.ogema.apps.grafana.logging;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.apps.grafana.logging.SessionData.DataSupplier;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.tools.grafana.base.InfluxFake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(specVersion = "1.2")
@Service(Application.class)
public class GrafanaLogging implements Application {

	private final static String SESSION_ATTRIBUTE_INIT = "grafana_logging_data";
	final static Logger logger = LoggerFactory.getLogger(GrafanaLogging.class);
	private ApplicationManager am;
	private ResourceAccess ra;
	private InfluxFake infl;
	private DataSupplierImpl supplier;

	private String webResourceBrowserPath;
	private String servletPath;

	@SuppressWarnings("rawtypes")
	@Override
    public void start(ApplicationManager am) {
        this.am = am;
        this.ra = am.getResourceAccess();
        logger.debug("Grafana logging app started", getClass().getName());
        String webResourcePackagePath = "org/ogema/apps/grafana/logging/grafana-1.9.1";
        String appNameLowerCase = "GrafanaLogging".toLowerCase();
        webResourceBrowserPath = "/ogema/" + appNameLowerCase; 
        am.getWebAccessManager().registerWebResource(webResourceBrowserPath, webResourcePackagePath);
        this.supplier = new DataSupplierImpl(ra);
        this.infl = new InfluxFake(am,Collections.<String,Map> emptyMap(), 5000) {
        	
			private static final long serialVersionUID = 1L;

			@Override
        	protected void onGet(HttpServletRequest req) {
        		final HttpSession session = req.getSession();
        		SessionData o;
        		try {
        			o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE_INIT);
        		} catch (ClassCastException e) {
        			session.removeAttribute(SESSION_ATTRIBUTE_INIT);
        			o = null;
        		}        		if (o == null || ra == null) {
        			o = new SessionData(supplier, ra);
        			session.setAttribute(SESSION_ATTRIBUTE_INIT, o);
        		}
        	}
			
			@Override
			public Map<String, Map> getPanels(HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE_INIT);
        		if (o == null)
        			return Collections.emptyMap();
        		return o.getPanels();
			}
			
			@Override
			protected List<? extends Resource> getResources(Class<? extends Resource> clazz, HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE_INIT);
        		if (o == null)
        			return Collections.emptyList();
        		return o.getResources(clazz);
			}
			
			@Override
			protected Class getClass(String longResTypeName, HttpServletRequest req) {
				final HttpSession session = req.getSession();
        		SessionData o = (SessionData) session.getAttribute(SESSION_ATTRIBUTE_INIT);
        		if (o == null)
        			return null;
        		return o.getClass(longResTypeName);
			}
        	
        };
        this.infl.setStrictMode(true);
        servletPath = "/apps/ogema/" + appNameLowerCase + "/fake_influxdb/series";
        am.getWebAccessManager().registerWebResource(servletPath,infl);
    }

	@Override
	public void stop(AppStopReason reason) {
		if (am != null) {
			try {
				am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
				am.getWebAccessManager().unregisterWebResource(servletPath);
			} catch (Exception ignore) {}
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
