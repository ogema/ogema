package org.ogema.apps.unclean.test;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.model.devices.whitegoods.CoolingDevice;
import org.ogema.model.sensors.TemperatureSensor;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class UncleanTestApp implements Application {

	@Override
	public void start(ApplicationManager appManager) {

		final CoolingDevice fridge = appManager.getResourceManagement().createResource("a",CoolingDevice.class);
		TemperatureSensor res = fridge.temperatureSensor();
		res.reading().create();
		res.delete();
		res.reading().create();
		
		System.out.println("    test app done...");
		
	}

	@Override
	public void stop(AppStopReason reason) {
	}

}