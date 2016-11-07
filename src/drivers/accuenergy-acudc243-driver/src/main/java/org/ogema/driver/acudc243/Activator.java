package org.ogema.driver.acudc243;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.StringResource;

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
		String portName = getPortName(appManager.getHardwareManager());
		if (portName == null) {
			HardwareManager hwmngr = appManager.getHardwareManager();
			Collection<HardwareDescriptor> descriptors = hwmngr.getHardwareDescriptors(".+:0403:6001:FTUEMLV4");
			for (HardwareDescriptor descr : descriptors) {
				// TODO handle multiple connections
				portName = ((UsbHardwareDescriptor) descr).getPortName();
			}
		}
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
	}

	@Override
	public void stop(AppStopReason reason) {
		driver.shutdown();
	}

	String getPortName(HardwareManager hwMngr) {
		String portName = System.getProperty("org.ogema.driver.modbus.rtu.portname");
		if (portName == null) {
			// String hardwareDesriptors = System.getProperty(Constants.HARDWARE_DESCRIPTOR,
			// Constants.DEFAULT_HW_DESCRIPTOR);
			// logger.info(String.format(
			// "No device file specified on the command line. The Hardware descriptor %s is used instead.",
			// hardwareDesriptors));
			// // Collection<HardwareDescriptor> descriptors = hwMngr.getHardwareDescriptors(".+:0658:0200:");
			// Collection<HardwareDescriptor> descriptors = hwMngr.getHardwareDescriptors(hardwareDesriptors);
			// logger.info(
			// String.format("Portname via hardware descriptor: %s.%s", hardwareDesriptors, descriptors.size()));
			// for (HardwareDescriptor descr : descriptors) {
			// portName = ((UsbHardwareDescriptor) descr).getPortName();
			// if (portName != null)
			// break;
			// }
		}
		logger.info(String.format("Port name detected %s", portName));
		return portName;
	}

}
