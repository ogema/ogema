/**
 * Copyright 2011-2019 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
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
package de.iwes.ogema.sim.weather;

import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.ResourceList;
import org.ogema.core.resourcemanager.transaction.ResourceTransaction;
import org.ogema.model.devices.sensoractordevices.SensorDevice;
import org.ogema.model.locations.GeographicLocation;
import org.ogema.tools.resourcemanipulator.ResourceManipulator;
import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.tools.scheduleimporter.config.ScheduleImportConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Simulate wind speed, solar irradiation and temperature values based on data
 * from CSV files.
 * 
 * This app requires fragment bundles or other configuration providers
 * to actually simulate anything. Furthermore, it depends on
 * org.ogema.tools.schedule-import and org.ogema.tools.timeseries-import. 
 * 
 * @author cnoelle
 */
@Component(
		service=Application.class,
		configurationPid=SimulatedWeatherApp.PID,
		configurationPolicy=ConfigurationPolicy.REQUIRE
)
@Designate(ocd=Config.class, factory=true)
public class SimulatedWeatherApp implements Application {

	public static final String PID = "org.ogema.sim.SimulatedWeather";
	private static final String CONFIG_RESOURCE = "simulatedWeatherAppConfig";

	private Config config;
    private BundleContext ctx;
    private Device sensorDevice;
    
    @Activate
    protected void activate(BundleContext ctx, Config config) {
    	this.config = config;
    	this.ctx = ctx;
    }
    
    @Override
    public void start(ApplicationManager appManager) {
    	appManager.getLogger().info("Simulated weather app starting with config {}", config);
    	final GeographicLocation location = appManager.getResourceManagement().createResource(config.locationResource(), GeographicLocation.class);
    	location.activate(false);
    	initLocation(location, config, appManager.getResourceAccess().createResourceTransaction());
    	final SensorDevice sensors = appManager.getResourceManagement().createResource(config.sensorDeviceResource(), SensorDevice.class);
    	this.sensorDevice = new Device(sensors, config, location, appManager);
    	@SuppressWarnings("unchecked")
		final ResourceList<ScheduleImportConfig> configs = appManager.getResourceManagement().createResource(CONFIG_RESOURCE, ResourceList.class);
    	configs.setElementType(ScheduleImportConfig.class);
    	sensorDevice.activateSimulation(configs, ctx.getBundle());
    	if (config.activateResourceTracking()) {
    		final ResourceManipulator manipulator = new ResourceManipulatorImpl(appManager);
    		manipulator.start();
    		sensorDevice.activateResourceTracking(manipulator);
    		if (config.activateLogging()) {
    			sensorDevice.activateLogging();
    		}
    	}
    }
    
    @Override
    public void stop(AppStopReason reason) {}

    private static void initLocation(final GeographicLocation location, final Config config, final ResourceTransaction transaction) {
    	transaction.setInteger(location.latitudeFullDegrees(), config.latitudeFullDegrees());
    	transaction.setFloat(location.latitudeArcMinutes(), config.latitudeArcMinutes());
    	transaction.setInteger(location.longitudeFullDegrees(), config.longitudeFullDegrees());
    	transaction.setFloat(location.longitudeArcMinutes(), config.longitudeArcMinutes());
    	transaction.activate(location, false, true);
    	transaction.commit();
    }
    
}

