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
package org.ogema.apps.swtch;

import org.ogema.apps.swtch.patterns.ThermostatPattern;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.model.devices.buildingtechnology.ElectricLight;
import org.ogema.model.devices.sensoractordevices.SingleSwitchBox;
import org.ogema.model.locations.Room;

class TestResourceManagement implements ResourceValueListener<SingleValueResource> {
	
	private final TemperatureResource remoteDesiredTemp;
	private final TemperatureResource localDesiredTemp;
	private final BooleanResource feedback;
	private final BooleanResource ctrl;

	TestResourceManagement(final ApplicationManager appMan) {
		final TemperatureResource[] thermostatResources = createTestThermostat(appMan);
		this.localDesiredTemp = thermostatResources[0];
		this.remoteDesiredTemp = thermostatResources[1];
		final BooleanResource[] switchBoxResources = generateTestResource(appMan);
		this.feedback = switchBoxResources[0];
		this.ctrl = switchBoxResources[1];
	}
	
	void close() {
		ctrl.removeValueListener(this);
		localDesiredTemp.removeValueListener(this);
	}
	
	private TemperatureResource[] createTestThermostat(ApplicationManager am) {
		ThermostatPattern pt = am.getResourcePatternAccess().createResource("myTestThermostat", ThermostatPattern.class);
		pt.currentTemperature.setCelsius(20.5f);
		pt.remoteDesiredTemperature.setCelsius(21f);
		pt.batteryVoltage.setValue(95);
		pt.batteryCharge.setValue(95);
		pt.isSwitchControllable.setValue(false);
		pt.localDesiredTemperature.setValue(21f);
		pt.valvePosition.setValue(0.25f);
		pt.model.activate(true);
		pt.localDesiredTemperature.addValueListener(this);
		return new TemperatureResource[] { pt.localDesiredTemperature, pt.remoteDesiredTemperature };
	}
	
	private BooleanResource[] generateTestResource(ApplicationManager am) {
		final ResourceManagement rm = am.getResourceManagement();
		SingleSwitchBox box = rm.createResource("test_switch_box", SingleSwitchBox.class);
		box.name().create();
		box.name().setValue("Simulated test switch box");
		final BooleanResource feedback = box.onOffSwitch().stateFeedback().create();
		final BooleanResource ctrl = box.onOffSwitch().stateControl().create();
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
			am.getLogger().warn(e.toString());
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
		return new BooleanResource[] { feedback, ctrl };
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
