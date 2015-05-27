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
/**
 * Copyright 2009 - 2014
 *
 * Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Fraunhofer IIS Fraunhofer ISE Fraunhofer IWES
 *
 * All Rights reserved
 */
package org.ogema.apps.grafana.schedule.viewer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.SimpleResource;
import org.ogema.core.model.schedule.DefinitionSchedule;
import org.ogema.core.model.schedule.ForecastSchedule;
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.actors.Actor;
import org.ogema.model.sensors.Sensor;
import org.ogema.tools.grafana.base.InfluxFake;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class GrafanaScheduleViewer implements Application, ResourceDemandListener<Schedule> {

	protected OgemaLogger logger;
	protected ApplicationManager am;
	protected ResourceManagement rm;
	protected ResourceAccess ra;
	protected List<Class<? extends Resource>> definitionScheduleTypes;
	protected List<Class<? extends Resource>> forecastScheduleTypes;
	protected InfluxFake infl;
	protected Map<String, Map> panels;
	protected Map<String, Map<String, Class<? extends Resource>>> restrictions;

	private String webResourceBrowserPath;
	private String servletPath;

	@Override
    public void start(ApplicationManager am) {
        this.am = am;
        this.logger = am.getLogger();
        this.rm = am.getResourceManagement();
        this.ra = am.getResourceAccess();
        this.definitionScheduleTypes = new ArrayList<>();
        this.forecastScheduleTypes = new ArrayList<>();
        this.restrictions = new LinkedHashMap<String,Map<String,Class<? extends Resource>>>();
        logger.debug("Grafana log app started", getClass().getName());
        String webResourcePackagePath = "org/ogema/apps/grafana/schedule/viewer/grafana-1.9.1";
        String appNameLowerCase = "GrafanaScheduleViewer".toLowerCase();
        webResourceBrowserPath =am.getWebAccessManager().registerWebResourcePath("", webResourcePackagePath);
//        webResourceBrowserPath =am.getWebAccessManager().registerWebResource("/org/ogema/apps/grafana-schedule-viewer",webResourcePackagePath);        
        panels = new LinkedHashMap<String,Map>();
        // row 1
 /*       Map<String,Class<? extends Resource>> firstRowPanels = new LinkedHashMap<String,Class<? extends Resource>>();       
        firstRowPanels.put("Programs / Definition schedules",DefinitionSchedule.class);
        panels.put("Programs / Definition schedules", firstRowPanels);
        
        // row 2
        Map<String,Class<? extends Resource>> secondRowPanels = new LinkedHashMap<String,Class<? extends Resource>>();              
        secondRowPanels.put("Forecast schedules",ForecastSchedule.class);
        panels.put("Forecast schedules", secondRowPanels); */
        
        this.infl = new InfluxFake(am,panels,-1);
        infl.setStrictMode(true);
        infl.setRestrictions(restrictions);
        
        servletPath = "/apps/ogema/" + appNameLowerCase + "/fake_influxdb/series";
        am.getWebAccessManager().registerWebResource(servletPath,infl);
        ra.addResourceDemand(Schedule.class, this);
    }

	@Override
	public void stop(AppStopReason reason) {
		//    	am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		am.getWebAccessManager().unregisterWebResourcePath("");
		am.getWebAccessManager().unregisterWebResource(servletPath);
		ra.removeResourceDemand(Schedule.class, this);
	}

	@Override
	public void resourceAvailable(Schedule schedule) {
		Class<? extends Resource> type = schedule.getResourceType();
		Class<? extends Resource> parentType = null;
		System.out.println("  Callback for " + schedule.getLocation() + ", type " + type.getSimpleName()
				+ ", instance of DefinitionSchedule: " + (schedule instanceof DefinitionSchedule)
				+ ", instance of ForecastSchedule " + (schedule instanceof ForecastSchedule));
		try {
			parentType = schedule.getParent().getResourceType();
		} catch (Exception e) {
			logger.warn("Could not determine type of schedule " + schedule.getLocation() + ". Ignoring it.");
			return;
		}
		//		if (schedule instanceof DefinitionSchedule) {
		if (DefinitionSchedule.class.isAssignableFrom(type)) {
			if (!definitionScheduleTypes.contains(parentType)) {
				definitionScheduleTypes.add(parentType);
				Map<String, Class<? extends Resource>> rowPanels = new LinkedHashMap<String, Class<? extends Resource>>();
				Map<String, Class<? extends Resource>> rowRestrictions = new LinkedHashMap<String, Class<? extends Resource>>();
				String rowId = "Programs: " + parentType.getSimpleName();
				rowPanels.put(rowId, type);
				rowRestrictions.put(rowId, parentType);
				panels.put(rowId, rowPanels);
				restrictions.put(rowId, rowRestrictions);
				infl.setPanels(panels);
			}
		}
		//		else if (schedule instanceof ForecastSchedule) {
		else if (ForecastSchedule.class.isAssignableFrom(type)) {
			if (!forecastScheduleTypes.contains(parentType)) {
				forecastScheduleTypes.add(parentType);
				Map<String, Class<? extends Resource>> rowPanels = new LinkedHashMap<String, Class<? extends Resource>>();
				Map<String, Class<? extends Resource>> rowRestrictions = new LinkedHashMap<String, Class<? extends Resource>>();
				String rowId = "Forecasts: " + parentType.getSimpleName();
				rowPanels.put(rowId, type);
				rowRestrictions.put(rowId, parentType);
				panels.put(rowId, rowPanels);
				restrictions.put(rowId, rowRestrictions);
				infl.setPanels(panels);
			}
		}
	}

	@Override
	public void resourceUnavailable(Schedule schedule) {
		// TODO
	}

}
