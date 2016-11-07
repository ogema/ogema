package org.ogema.impl.wago;

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
import org.ogema.core.resourcemanager.ResourceAccess;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.wago.DigitalOut;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class Activator implements Application {

	static String MODBUS_PORT;

	static int MODBUS_BAUDRATE;

	ResourceList<BooleanResource> digitalOuts;
	Vector<BooleanResource> outsMap;
	static OgemaLogger logger;
	private boolean stopped = false;

	private Wago750315 modbusCoupler;

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
		outsMap = new Vector<>(4);
		init750504Resources(appManager.getResourceAccess(), appManager.getResourceManagement());
		modbusCoupler = new Wago750315(appManager.getChannelAccess(), portName, "1", "9600");
		DigitalOut[] douts = modbusCoupler.addDigitalOut(4);
		modbusCoupler.terminate();

		for (int i = 0; i < 4; i++) {
			BooleanResource sw = outsMap.get(i);
			sw.activate(true);
			modbusCoupler.prepareChannel(douts[i], 10000, sw);
		}

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				int index = 0;
				while (!stopped) {
					BooleanResource b = outsMap.get(index++);
					b.setValue(!b.getValue());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (index == 4)
						index = 0;
				}
			}
		});

//		t.start();

	}

	private void init750504Resources(ResourceAccess resAcc, ResourceManagement resMan) {
		List<BooleanResource> douts;
		digitalOuts = resAcc.getResource("DigitalOutputs_750504_I");
		if (digitalOuts == null) {
			digitalOuts = resMan.createResource("DigitalOutputs_750504_I", ResourceList.class);
			digitalOuts.setElementType(BooleanResource.class);
			digitalOuts.activate(true);
		}
		else if (digitalOuts.getElementType() == null) {
			digitalOuts.setElementType(BooleanResource.class);
		}
		douts = digitalOuts.getAllElements();
		int elementCount = douts.size();
		switch (elementCount) {
		case 0:
			digitalOuts.add();
			digitalOuts.add();
			digitalOuts.add();
			digitalOuts.add();
			break;
		case 1:
			digitalOuts.add();
			digitalOuts.add();
			digitalOuts.add();
			break;
		case 2:
			digitalOuts.add();
			digitalOuts.add();
			break;
		case 3:
			digitalOuts.add();
			break;
		default:
			break;
		}
		douts = digitalOuts.getAllElements();
		for (BooleanResource dout : douts) {
			String name = dout.getName();
			outsMap.add(dout);
		}
	}

	@Override
	public void stop(AppStopReason reason) {
		stopped = true;
		modbusCoupler.shutdown();
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
