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
package org.ogema.apps.sensor.logging;

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
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.actors.Actor;
import org.ogema.model.sensors.Sensor;
import org.ogema.model.smartgriddata.Price;
import org.ogema.tools.grafana.base.InfluxFake;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
@SuppressWarnings("deprecation")
public class GrafanaLogging implements Application, ResourceDemandListener<org.ogema.core.model.SimpleResource> {

	protected OgemaLogger logger;
	protected ApplicationManager am;
	protected ResourceManagement rm;
	protected ResourceAccess ra;
	protected List<Class<? extends Resource>> resourceTypes;
    @SuppressWarnings("rawtypes")
	protected Map<String, Map> panels;
	protected long updateInterval = 5000; // 5s
	protected InfluxFake infl;

	private String webResourceBrowserPath;
	private String servletPath;

	@Override
    @SuppressWarnings("deprecation")
    public void start(ApplicationManager am) {
        this.am = am;
        this.logger = am.getLogger();
        this.rm = am.getResourceManagement();
        this.ra = am.getResourceAccess();

        logger.debug("Grafana logging app started", getClass().getName());
        String webResourcePackagePath = "org/ogema/apps/sensor/logging/grafana-1.9.1";
        String appNameLowerCase = "SensorLogging".toLowerCase();
        webResourceBrowserPath = "/ogema/" + appNameLowerCase; 
        am.getWebAccessManager().registerWebResource(webResourceBrowserPath, webResourcePackagePath);
        this.resourceTypes = new ArrayList<>();
        this.panels = new LinkedHashMap<>(); 
        this.infl = new InfluxFake(am,panels,updateInterval);
        this.infl.setStrictMode(true);
        servletPath = "/apps/ogema/" + appNameLowerCase + "/fake_influxdb/series";
        am.getWebAccessManager().registerWebResource(servletPath,infl);
        ra.addResourceDemand(org.ogema.core.model.SimpleResource.class, this);

    }

	@Override
    @SuppressWarnings("deprecation")
	public void stop(AppStopReason reason) {
		am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
		am.getWebAccessManager().unregisterWebResource(servletPath);
		ra.removeResourceDemand(org.ogema.core.model.SimpleResource.class, this);
	}

	public List<Class<? extends Resource>> getResourceTypes() {
		return resourceTypes;
	}

	@Override
	public void resourceAvailable(org.ogema.core.model.SimpleResource resource) {
		Resource res = resource.getParent();
		Class<? extends Resource> type;
		if (res == null || !(res instanceof Sensor || res instanceof Actor || res instanceof Price)) {
			return;
			//			type = resource.getResourceType();
		}
		else {
			type = res.getResourceType();
		}
		if (!resourceTypes.contains(type)) {
			resourceTypes.add(type);
			Map<String, Class<? extends Resource>> rowPanels = new LinkedHashMap<>();
			rowPanels.put(type.getSimpleName(), type);
			panels.put(type.getSimpleName(), rowPanels);
			infl.setPanels(panels);
		}
		//System.out.println("Panels: " + panels.toString());
	}

	@Override
	public void resourceUnavailable(org.ogema.core.model.SimpleResource resource) {
		// TODO
	}

}
