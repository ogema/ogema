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
package org.ogema.apps.swtch;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.apps.swtch.listeners.ControllableMultiPatternListener;
import org.ogema.apps.swtch.listeners.ControllableOnOffPatternListener;
import org.ogema.apps.swtch.listeners.ThermostatPatternListener;
import org.ogema.apps.swtch.patterns.ControllableMultiPattern;
import org.ogema.apps.swtch.patterns.ControllableOnOffPattern;
import org.ogema.apps.swtch.patterns.ThermostatPattern;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePatternAccess;
import org.ogema.model.devices.buildingtechnology.ElectricLight;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.locations.Room;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class BasicSwitchGui implements Application, ResourceValueListener<SingleValueResource> {

	protected OgemaLogger logger;
	protected ApplicationManager am;
	protected ResourceManagement rm;
	protected ResourcePatternAccess rpa;

	private String webResourceBrowserPath;
	private String servletPath;
	private ControllableOnOffPatternListener deviceListener;
	private ControllableMultiPatternListener multiListener;
	private ThermostatPatternListener thermoListener;
	private List<PatternListener<?>> listeners;

	@Override
	public void start(ApplicationManager am) {
		this.am = am;
		this.logger = am.getLogger();
		this.rm = am.getResourceManagement();
		this.rpa = am.getResourcePatternAccess();
		this.listeners = new ArrayList<>();

		logger.debug("BasicSwitchGui started");
        String webResourcePackage = "org.ogema.apps.swtch";
        webResourcePackage = webResourcePackage.replace(".", "/");
        String appNameLowerCase = "BasicSwitchGui";
        appNameLowerCase = appNameLowerCase.toLowerCase();
        //path to find the index.html /ogema/<this app name>/index.html
        webResourceBrowserPath = "/ogema/" + appNameLowerCase;
        //package/path to find the resources inside this application
        String webResourcePackagePath = webResourcePackage + "/gui";
        //path for the http servlet /apps/ogema/<this app name>
        servletPath = "/apps/ogema/" + appNameLowerCase;
        am.getWebAccessManager().registerWebResource(webResourceBrowserPath,webResourcePackagePath);
        am.getWebAccessManager().registerWebResource(servletPath, new BasicSwitchGuiServlet(am,listeners));
        
        deviceListener = new ControllableOnOffPatternListener();
        multiListener = new ControllableMultiPatternListener();
        thermoListener = new ThermostatPatternListener();
        listeners.add(deviceListener);
        listeners.add(multiListener);
        listeners.add(thermoListener);
        rpa.addPatternDemand(ControllableOnOffPattern.class, deviceListener, AccessPriority.PRIO_LOWEST);
        rpa.addPatternDemand(ControllableMultiPattern.class, multiListener, AccessPriority.PRIO_LOWEST);
        rpa.addPatternDemand(ThermostatPattern.class, thermoListener, AccessPriority.PRIO_LOWEST);
        
        Boolean testRes = Boolean.getBoolean("org.ogema.apps.createtestresources");
        if (testRes) {
	        generateTestResource();
	        createTestThermostat();
        }
	}

	@Override
	public void stop(AppStopReason reason) {
		if (am != null) {
			am.getWebAccessManager().unregisterWebResource(webResourceBrowserPath);
			am.getWebAccessManager().unregisterWebResource(servletPath);
		}
		if (rpa != null) {
			rpa.removePatternDemand(ControllableOnOffPattern.class, deviceListener);
			rpa.removePatternDemand(ControllableMultiPattern.class, multiListener);
		}
		if (ctrl != null)
			ctrl.removeValueListener(this);
		am = null;
		rm = null;
		rpa = null;
		ctrl = null;
		logger = null;
		deviceListener = null;
		multiListener = null;
		thermoListener = null;
		listeners = null;
	}

	/**** generate test resources ********/

	private TemperatureResource remoteDesiredTemp;

	private void createTestThermostat() {
		ThermostatPattern pt = rpa.createResource("myTestThermostat", ThermostatPattern.class);
		pt.currentTemperature.setCelsius(20.5f);
		pt.remoteDesiredTemperature.setCelsius(21f);
		remoteDesiredTemp = pt.remoteDesiredTemperature;
		pt.batteryVoltage.setValue(95);
		pt.batteryCharge.setValue(95);
		pt.isSwitchControllable.setValue(false);
		pt.localDesiredTemperature.setValue(21f);
		pt.valvePosition.setValue(0.25f);
		pt.model.activate(true);
		pt.localDesiredTemperature.addValueListener(this);
	}

	private BooleanResource feedback;
	private BooleanResource ctrl;

	private void generateTestResource() {
		SingleSwitchBox box = rm.createResource("test_switch_box", SingleSwitchBox.class);
		box.name().create();
		box.name().setValue("Simulated test switch box");
		feedback = box.onOffSwitch().stateFeedback().create();
		ctrl = box.onOffSwitch().stateControl().create();
		final BooleanResource flag = box.onOffSwitch().controllable().create();
		flag.setValue(true);
		feedback.setValue(true);
		ctrl.setValue(true);
		Room room = rm.createResource("testRoom", Room.class);
		room.name().create();
		room.name().setValue("Test Room");
		room.activate(true);
		ElectricLight light = rm.createResource("test_electric_light", ElectricLight.class);
		light.location().create();
		try {
			light.location().room().setAsReference(room);
		} catch (Exception e) {
			logger.warn(e.toString());
		}
		try {
			light.onOffSwitch().setAsReference(box.onOffSwitch());
		} catch (Exception e) {
		}
		light.name().create();
		light.name().setValue("Test light");
		light.activate(true);
		try {
			box.device().setAsReference(light);
		} catch (Exception e) {
		}
		box.activate(true);
		ctrl.addValueListener(this);
	}

	@Override
	public void resourceChanged(SingleValueResource control) {
		if (control instanceof BooleanResource) {
			BooleanResource bool = (BooleanResource) control;
			if (feedback != null && feedback.isActive())
				feedback.setValue(bool.getValue());
		}
		else if (control instanceof TemperatureResource) {
			TemperatureResource temp = (TemperatureResource) control;
			remoteDesiredTemp.setValue(temp.getValue());
			
		}
	}

}
