package org.ogema.driver.acudc243;

import java.util.Vector;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.security.WebAccessManager;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class Activator implements Application {

	ResourceList<BooleanResource> digitalOuts;
	Vector<BooleanResource> outsMap;
	private AcuDC243Driver driver;
	static OgemaLogger logger;

	@Override
	public void start(ApplicationManager appManager) {
		logger = appManager.getLogger();
		HardwareManager hwmngr = appManager.getHardwareManager();
		String portName = hwmngr.getPortName("org.ogema.drivers.acudc243.portname", ".+:0403:6001:.*", null);
		if (portName == null) {
			logger.error(
					"Modbus Portname undefined. Please specify a valid portname via the property org.ogema.driver.modbus.rtu.portname and start the app again!");
			return;
		}

		AcuDC243Configuration conf = appManager.getResourceManagement().createResource("AcuDC243_Iface_Conf",
				AcuDC243Configuration.class);
		conf.activate(true);
		StringResource devAddr = conf.deviceAddress().create();
		devAddr.activate(true);
		devAddr.setValue("2");
		StringResource resname = conf.resourceName().create();
		resname.setValue("AcuDC243_I");

		StringResource iface = conf.interfaceId().create();
		iface.activate(true);
		iface.setValue(portName);
		StringResource params = conf.deviceParameters().create();
		params.activate(true);
		params.setValue("9600:8:none:1:none:none:0:500");
		this.driver = new AcuDC243Driver(appManager);

		WebAccessManager wam = appManager.getWebAccessManager();
		wam.registerWebResource("/acudc243/web", "/web");
	}

	@Override
	public void stop(AppStopReason reason) {
		driver.shutdown();
	}
}
