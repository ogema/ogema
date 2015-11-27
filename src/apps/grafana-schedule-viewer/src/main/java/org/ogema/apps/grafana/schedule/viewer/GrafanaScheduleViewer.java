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
import org.ogema.core.model.schedule.Schedule;
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.tools.grafana.base.InfluxFake;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class GrafanaScheduleViewer implements Application, ResourceDemandListener<Schedule> {

	protected OgemaLogger logger;
	protected ApplicationManager am;
	protected ResourceManagement rm;
	protected ResourceAccess ra;
	protected List<Class<? extends Resource>> forecastScheduleTypes, programScheduleTypes, otherScheduleTypes;
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
        this.forecastScheduleTypes = new ArrayList<>();
        this.programScheduleTypes = new ArrayList<>();
        this.otherScheduleTypes = new ArrayList<>();
        this.restrictions = new LinkedHashMap<String, Map<String, Class<? extends Resource>>>();
        logger.debug("Grafana log app started", getClass().getName());
        String webResourcePackagePath = "org/ogema/apps/grafana/schedule/viewer/grafana-1.9.1";
        String appNameLowerCase = "GrafanaScheduleViewer".toLowerCase();
        webResourceBrowserPath = am.getWebAccessManager().registerWebResourcePath("", webResourcePackagePath);
//        webResourceBrowserPath =am.getWebAccessManager().registerWebResource("/org/ogema/apps/grafana-schedule-viewer",webResourcePackagePath);        
        panels = new LinkedHashMap<>();
        // row 1
 /*       Map<String,Class<? extends Resource>> firstRowPanels = new LinkedHashMap<String,Class<? extends Resource>>();       
         firstRowPanels.put("Programs / Definition schedules",DefinitionSchedule.class);
         panels.put("Programs / Definition schedules", firstRowPanels);
        
         // row 2
         Map<String,Class<? extends Resource>> secondRowPanels = new LinkedHashMap<String,Class<? extends Resource>>();              
         secondRowPanels.put("Forecast schedules",ForecastSchedule.class);
         panels.put("Forecast schedules", secondRowPanels); */

        this.infl = new InfluxFake(am, panels, -1);
        infl.setStrictMode(true);
        infl.setRestrictions(restrictions);

        servletPath = "/apps/ogema/" + appNameLowerCase + "/fake_influxdb/series";
        am.getWebAccessManager().registerWebResource(servletPath, infl);
        ra.addResourceDemand(Schedule.class, this);
    }

	@Override
	public void stop(AppStopReason reason) {
		//    	am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		am.getWebAccessManager().unregisterWebResourcePath("");
		am.getWebAccessManager().unregisterWebResource(servletPath);
		ra.removeResourceDemand(Schedule.class, this);
	}

	private void tryAddScheduleType(List<Class<? extends Resource>> targetList, Class<? extends Resource> parentType, String prefix, Schedule schedule) {
        if (targetList.contains(parentType)) {
            return;
        }
        Class<? extends Resource> type = schedule.getResourceType();
        targetList.add(parentType);
        Map<String, Class<? extends Resource>> rowPanels = new LinkedHashMap<>();
        Map<String, Class<? extends Resource>> rowRestrictions = new LinkedHashMap<>();
        String rowId = prefix + " " + parentType.getSimpleName();
        rowPanels.put(rowId, type);
        rowRestrictions.put(rowId, parentType);
        panels.put(rowId, rowPanels);
        restrictions.put(rowId, rowRestrictions);
        infl.setPanels(panels);
    }

	@Override
	public void resourceAvailable(Schedule schedule) {
		final Class<? extends Resource> type = schedule.getResourceType();
		logger.debug("  Callback for " + schedule.getLocation() + ", type " + type.getSimpleName());

		final Class<? extends Resource> parentType;
		try {
			parentType = schedule.getParent().getResourceType();
		} catch (Exception e) {
			logger.warn("Could not determine type of schedule " + schedule.getLocation() + ". Ignoring it.");
			return;
		}

		switch (schedule.getName()) {
		case "program":
			tryAddScheduleType(programScheduleTypes, parentType, "Program:", schedule);
			break;
		case "forecast":
			tryAddScheduleType(forecastScheduleTypes, parentType, "Forecast:", schedule);
			break;
		default:
			tryAddScheduleType(otherScheduleTypes, parentType, "Other:", schedule);
			break;
		}
		//        if (!forecastScheduleTypes.contains(parentType)) {
		//            forecastScheduleTypes.add(parentType);
		//            Map<String, Class<? extends Resource>> rowPanels = new LinkedHashMap<>();
		//            Map<String, Class<? extends Resource>> rowRestrictions = new LinkedHashMap<>();
		//            String rowId = "Programs: " + parentType.getSimpleName();
		//            rowPanels.put(rowId, type);
		//            rowRestrictions.put(rowId, parentType);
		//            panels.put(rowId, rowPanels);
		//            restrictions.put(rowId, rowRestrictions);
		//            infl.setPanels(panels);
		//        }

	}

	@Override
	public void resourceUnavailable(Schedule schedule) {
		// TODO
	}
}
