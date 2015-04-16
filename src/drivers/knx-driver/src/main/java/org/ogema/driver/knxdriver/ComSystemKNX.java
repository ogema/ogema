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
package org.ogema.driver.knxdriver;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.application.Timer;
import org.ogema.core.application.TimerListener;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.TimeResource;
import org.ogema.core.resourcemanager.ResourceDemandListener;
import org.ogema.core.resourcemanager.ResourceException;
import org.ogema.core.resourcemanager.ResourceListener;
import org.ogema.core.resourcemanager.ResourceManagement;
import org.ogema.model.actors.OnOffSwitch;
import org.ogema.model.communication.CommunicationInformation;
import org.ogema.model.communication.DeviceAddress;
import org.ogema.model.communication.IPAddressV4;
import org.ogema.model.communication.KNXAddress;
import org.ogema.model.communication.PollingConfiguration;
import org.ogema.model.devices.buildingtechnology.ElectricDimmer;
import org.ogema.model.devices.connectiondevices.ThermalValve;
import org.ogema.model.prototypes.PhysicalElement;
import org.ogema.model.sensors.ElectricPowerSensor;
import org.ogema.model.sensors.LightSensor;
import org.ogema.model.sensors.MotionSensor;
import org.ogema.model.sensors.OccupancySensor;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.model.sensors.TouchSensor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;

import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteUnsigned;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

@Component(specVersion = "1.1", immediate = true)
@Service( { Application.class, KNXdriverI.class })
public class ComSystemKNX implements KNXdriverI {

	private int standardTimestep = 2;
	public static boolean activated;
	Thread readingThread;
	Thread writingThread;
	protected OgemaLogger logger;
	private String NAME_COM_INFO = "knxComInfo";
	public static ApplicationManager aManager;
	public static Bundle bundle;
	public static ComSystemKNX app;
	public static BundleContext bundleContext;
	private KNXUtils knxUtils = new KNXUtils();
	private static int statusKNX;
	private KNXStorage storage = KNXStorage.getInstance();

	@Activate
	protected void activate(ComponentContext componentContext) {
		ComSystemKNX.bundle = componentContext.getBundleContext().getBundle();
		ComSystemKNX.app = this;
		bundleContext = componentContext.getBundleContext();
	}

	private void addNotAllowed() {

		storage.getNotAllowedDevices().add("0.0.255");

	}

	private void createReadWriteableComInfo(PhysicalElement device, boolean readable, boolean writeable) {
		final CommunicationInformation comInfo = device.addDecorator(NAME_COM_INFO, CommunicationInformation.class);
		final DeviceAddress comAddress = comInfo.comAddress();
		comAddress.create();
		comAddress.readable().create();
		comAddress.writeable().create();
		comAddress.readable().setValue(readable);
		comAddress.writeable().setValue(writeable);
	}

	public int addKNXDeviceToRessource(ConnectionInfo conInfo) {

		final ResourceManagement resMan = aManager.getResourceManagement();
		int status = 0; // Status

		try {

			if (conInfo.getType().equals(LightSensor.class.getSimpleName())) {

				LightSensor device = resMan.createResource(conInfo.getName(), LightSensor.class);

				device.reading().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);

				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(TemperatureSensor.class.getSimpleName())) {

				TemperatureSensor device = resMan.createResource(conInfo.getName(), TemperatureSensor.class);

				device.reading().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);

				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(MotionSensor.class.getSimpleName())) {

				MotionSensor device = resMan.createResource(conInfo.getName(), MotionSensor.class);

				device.reading().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);
				conInfo.setListener(true);
				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(OccupancySensor.class.getSimpleName())) {

				OccupancySensor device = resMan.createResource(conInfo.getName(), OccupancySensor.class);

				device.reading().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);
				conInfo.setListener(true);
				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(TouchSensor.class.getSimpleName())) {

				TouchSensor device = resMan.createResource(conInfo.getName(), TouchSensor.class);

				device.reading().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);
				conInfo.setListener(true);
				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(ElectricPowerSensor.class.getSimpleName())) {

				ElectricPowerSensor device = resMan.createResource(conInfo.getName(), ElectricPowerSensor.class);

				device.reading().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);

				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(OnOffSwitch.class.getSimpleName())) {

				OnOffSwitch device = resMan.createResource(conInfo.getName(), OnOffSwitch.class);

				device.stateControl().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);

				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(ElectricDimmer.class.getSimpleName())) {

				ElectricDimmer device = resMan.createResource(conInfo.getName(), ElectricDimmer.class);

				device.setting().create();
				device.setting().stateControl().create();
				device.setting().stateFeedback().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);

				activateConnection(conInfo);

			}

			if (conInfo.getType().equals(ThermalValve.class.getSimpleName())) {

				ThermalValve device = resMan.createResource(conInfo.getName(), ThermalValve.class);

				device.setting().create();
				createReadWriteableComInfo(device, true, false);

				conInfo.setRessource(device);

				activateConnection(conInfo);
			}

		} catch (Exception e) {

			status = 4;

		}

		return status;

	}

	private void activateConnection(ConnectionInfo conInfo) throws CommunicationException {

		long timeInterval = 0; // Zeitintervall

		timeInterval = conInfo.getTimeStep() * 1000;
		if (timeInterval == 0) {
			timeInterval = standardTimestep;
		}
		conInfo.setTimeStep(timeInterval);

		PhysicalElement el = (PhysicalElement) conInfo.getRessource();

		CommunicationInformation comInfo = el.addDecorator(NAME_COM_INFO, CommunicationInformation.class);
		comInfo.create();

		DeviceAddress comAddress = comInfo.comAddress();
		comAddress.create();

		PollingConfiguration cycle = comInfo.pollingConfiguration();
		cycle.create();

		TimeResource time = cycle.pollingInterval();
		time.create();

		time.setValue(timeInterval);

		IPAddressV4 ip4Address = comAddress.ipV4Address();
		ip4Address.create();

		ip4Address.address().create();
		ip4Address.port().create();

		KNXAddress knxAddress = comAddress.knxAddress();
		knxAddress.create();

		IPAddressV4 local = comAddress.addDecorator("localAddressIPV4", IPAddressV4.class);

		local.address().create();
		local.address().setValue(conInfo.getIntface());

		knxAddress.physicalAddress().create();
		knxAddress.groupAddress().create();

		knxAddress.physicalAddress().setValues(knxUtils.convertPhysicalAddress(conInfo.getPhyaddress()));
		knxAddress.groupAddress().setValues(knxUtils.convertGroupAddress(conInfo.getGroupAddress()));

		comInfo.comAddress().ipV4Address().address().setValue(knxUtils.getIPAddress(conInfo));
		comInfo.comAddress().ipV4Address().port().setValue(knxUtils.getPort(conInfo));

		// Hier lokale Adresse

		synchronized (storage.getDeviceConnections()) {
			if (conInfo.isListener()) {

				startListener(conInfo);
				conInfo.setStatusListener(true);

			}

			storage.getDeviceConnections().add(conInfo);

			conInfo.setId(storage.getDeviceConnections().size() - 1);

			conInfo.getRessource().activate(true);

			addListener(conInfo);
		}

	}

	private void startListener(final ConnectionInfo conn) {
		// TODO Auto-generated method stub

		Thread t = new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub

				try {
					KNXNetworkLinkIP netLinkIp = null;

					netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());

					ProcessCommunicator pc = new ProcessCommunicatorImpl(netLinkIp);

					final GroupAddress main = new GroupAddress(conn.getGroupAddress());

					Datapoint dp = new StateDP(main, "", 0, conn.getDptStr());

					MyProcessListener process = new MyProcessListener(conn, main, storage.getKnxNetConnections(),
							logger);

					pc.addProcessListener(process);
					pc.read(dp);

					while (conn.isStatusListener() && activated) {
						try {
							Thread.sleep(1);

							if (!netLinkIp.isOpen()) {

								netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());

								logger.info("KNX (Occupancy/Present-Detector) " + conn.getKnxRouter()
										+ " no connection");

								Thread.sleep(5000);
								synchronized (netLinkIp) {
									if (netLinkIp.isOpen()) {
										pc = new ProcessCommunicatorImpl(netLinkIp);
										MyProcessListener process2 = new MyProcessListener(conn, main, storage
												.getKnxNetConnections(), logger);

										logger.info("KNX (Occupancy/Present-Detector) generate new listener");

										pc.addProcessListener(process2);
									}
								}
							}
						} catch (Exception ex) {

							if (activated) {
								logger.error(ex.getMessage());

							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();

	}

	private void addListener(final ConnectionInfo conn) {

		ResourceListener resListen = null;
		ResourceDemandListener<Resource> resDem = null;

		if (!conn.isSensor()) {

			resListen = new ResourceListener() {
				// public class TempChange implements ResourceListener {
				@Override
				public void resourceChanged(Resource resource) {
					if (resource.getResourceType().equals(FloatResource.class)) {

						conn.setUpdateToSend(true);
					}
					if (resource.getResourceType().equals(BooleanResource.class)) {

						conn.setUpdateToSend(true);
					}

				}
			};

		}

		else {

			resListen = new ResourceListener() {
				@Override
				public void resourceChanged(Resource resource) {
					// TODO Auto-generated method stub
				}
			};

		}

		resDem = new ResourceDemandListener<Resource>() {
			@Override
			public void resourceAvailable(Resource resource) {
				// TODO Auto-generated method stub
			}

			@Override
			public void resourceUnavailable(Resource resource) {
				// TODO Auto-generated method stub
			}
		};

		conn.setResDemandListener(resDem);
		conn.setResListener(resListen);

		conn.getRessource().addResourceListener(resListen, true);

		aManager.getResourceAccess().addResourceDemand(conn.getRessource().getResourceType(), resDem);
	}

	private void removeListener(ResourceListener resListener, Resource res) {

		res.removeResourceListener(resListener);

	}

	public boolean disconnectResource(String resourceName) {

		boolean OK = true; // Verarbeitung OK
		synchronized (storage.getDeviceConnections()) {
			Iterator<ConnectionInfo> it = storage.getDeviceConnections().iterator();
			ConnectionInfo conn = null;
			while (it.hasNext()) {
				try {
					conn = it.next();

					conn.setStatusListener(false);
					if (conn.getName().equals(resourceName)) {

						OK = unregisterResDemand(conn);
						removeListener(conn.getResListener(), conn.getRessource());

						int counter = 0;
						for (ConnectionInfo conn2 : storage.getDeviceConnections()) {
							if (knxUtils.getIPAddress(conn).equals(knxUtils.getIPAddress(conn2))) {
								counter++;
							}
						}
						// nur eigene Verbindung löschen
						if (counter == 1) {

							KNXNetworkLinkIP temp = storage.getKnxNetConnections().get(conn.getKnxRouter());
							temp.close();
							storage.getKnxNetConnections().remove(conn.getKnxRouter());
						}

						try {
							aManager.getResourceManagement().deleteResource(conn.getRessource().getName());

							it.remove();

						} catch (ResourceException ex) {
							ex.printStackTrace();
						}
					}
				} catch (Exception ex) {
				}
			}
		}

		return OK;
	}

	protected TimerListener timerListener = new TimerListener() {
		@Override
		public void timerElapsed(Timer timer) {
			// TODO Auto-generated method stub

			if (!readingThread.isAlive()) {
				logger.error("reading thread interrupted; start new");
				readFromSensor();
			}
			if (!writingThread.isAlive()) {
				logger.error("writing thread interrupted; start new");
				writingToActor();
			}

		}
	};

	private void readFromSensor() {

		readingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				while (activated) {

					final long curr = System.currentTimeMillis();

					try {

						synchronized (storage.getDeviceConnections()) {

							for (final ConnectionInfo conn : storage.getDeviceConnections()) {

								if (conn.getLastAccess() + conn.getTimeStep() <= curr && conn.getTimeStep() >= 0
										&& !conn.isListener()) {
									updateConnection(conn, curr);

									conn.setLastAccess(curr);

								}
							}
						}

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage());
					}
				}

			}
		});
		readingThread.setName("KNX reading thread");
		readingThread.start();

	}

	private void writingToActor() {

		writingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				while (activated) {

					final long curr = System.currentTimeMillis();

					try {

						synchronized (storage.getDeviceConnections()) {

							for (final ConnectionInfo conn : storage.getDeviceConnections()) {

								if (!conn.isSensor()) {
									if (conn.isUpdateToSend()) {
										Thread t = new Thread(new Runnable() {
											@Override
											public void run() {

												updateConnection(conn, curr);

												conn.setLastAccess(curr);

												conn.setUpdateToSend(false);

											}
										});

										t.start();
									}
								}
							}
						}

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage());
					}
				}

			}
		});
		writingThread.setName("KNX writing thread");
		writingThread.start();

	}

	private void updateConnection(final ConnectionInfo conn, Long curTime) {
		// TODO Auto-generated method stub

		String value = "0"; // Wert Float

		if (conn.isSensor()) // KNX-READ
		{
			if (conn.getRessource() instanceof LightSensor) {

				value = getGroupValue(conn);
				if (!value.equals("error")) {

					LightSensor light = (LightSensor) conn.getRessource();

					light.reading().setValue(Float.parseFloat(value.replace("lx", "")));

				}
			}
			if (conn.getRessource() instanceof ElectricPowerSensor) {

				value = getGroupValue(conn);
				if (!value.equals("error")) {
					try {
						int value2 = Integer.parseInt(value.replace(",", ".")
								.replaceAll("[^0-9][^\\.][^,][^0-9].*", ""));
						float value3 = Float.intBitsToFloat(value2);
						if (value3 != Float.NaN && value3 != Float.NEGATIVE_INFINITY
								&& value3 != Float.POSITIVE_INFINITY) {

							ElectricPowerSensor pow = (ElectricPowerSensor) conn.getRessource();

							pow.reading().setValue(value3);

						}
					} catch (Exception ex) {
					}
				}
			}
			if (conn.getRessource() instanceof TemperatureSensor) {

				value = getGroupValue(conn);
				if (!value.equals("error")) {

					Float value2 = Float.parseFloat(value.replaceAll("[^0-9\\.0-9].*", ""));

					value2 = value2 + 273.15f;

					TemperatureSensor tmp = (TemperatureSensor) conn.getRessource();

					tmp.reading().setValue(value2);
				}
			}
		}
		else {
			if (conn.isUpdateToSend()) {
				synchronized (conn) {
					if (conn.getRessource() instanceof ElectricDimmer) {
						// float value2 = apiService.resAdmin.getFloatValue(
						// conn.resId, applicationId);

						ElectricDimmer dimmer = (ElectricDimmer) conn.getRessource();
						// rescale: OGEMA uses range [0;1], KNX uses range
						// [0;100].
						float value2 = dimmer.setting().stateControl().getValue() * 100.f;

						if (value2 >= 0 && value2 <= 100) {
							setGroupValue(conn, String.valueOf((int) value2));
						}
					}

					if (conn.getRessource() instanceof OnOffSwitch) {

						OnOffSwitch swtch = (OnOffSwitch) conn.getRessource();

						boolean value2 = swtch.stateControl().getValue();

						if (value2) {
							setGroupValue(conn, "on");
						}
						else {
							setGroupValue(conn, "off");
						}
					}

					if (conn.getRessource() instanceof ThermalValve) {

						ThermalValve valve = (ThermalValve) conn.getRessource();

						float value2 = valve.setting().stateControl().getValue() * 100.f;

						if (value2 >= 0 && value2 <= 100) {
							setGroupValue(conn, String.valueOf((int) value2));
						}
					}
				}
			}
		}
	}

	private void setGroupValue(ConnectionInfo conn, String value) {
		// TODO Auto-generated method stub
		KNXNetworkLinkIP netLinkIp = null;

		ProcessCommunicator pc = null;
		try {

			netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());

			synchronized (netLinkIp) {

				pc = new ProcessCommunicatorImpl(netLinkIp);

				GroupAddress main = null;
				main = new GroupAddress(conn.getGroupAddress());
				Datapoint dp = new StateDP(main, "", 0, conn.getDptStr());
				knxUtils.writeAndDetachPrivileged(pc, dp, value);
			}
		} catch (Exception ex) {
			pc.detach();
			if (netLinkIp == null) {

				logger.info(" Value to " + conn.getKnxRouter() + "/" + conn.getGroupAddress()
						+ " could not be set => no connection");

			}
			else {
				logger.info("Value to " + conn.getKnxRouter() + "/" + conn.getGroupAddress() + " could not be set => "
						+ ex.getMessage());
			}
		}
	}

	private String getGroupValue(final ConnectionInfo conn) {
		// TODO Auto-generated method stub

		KNXNetworkLinkIP netLinkIp = null;
		try {

			netLinkIp = storage.getKnxNetConnections().get(conn.getKnxRouter());
			synchronized (netLinkIp) {
				if (!netLinkIp.isOpen()) {

					logger.info("Value from " + conn.getKnxRouter() + "/" + conn.getGroupAddress()
							+ " could not be read => no connection");

					return "error";
				}

				final ProcessCommunicator pc = new ProcessCommunicatorImpl(netLinkIp);

				final GroupAddress main = new GroupAddress(conn.getGroupAddress());

				Datapoint dp = new StateDP(main, "", 0, conn.getDptStr());

				if (conn.getDptStr().equals("9.004")) {

					pc.addProcessListener(new ProcessListener() {
						public void detached(DetachEvent e) {
							// TODO Auto-generated method stub
						}

						public void groupWrite(ProcessEvent e) {
							// TODO Auto-generated method stub

							try {

								if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
										&& e.getDestination().toString().equals(main.toString())) {

									DPT tmp = new DPT("9.004", "lux", "0", "670760");
									DPTXlator2ByteFloat tr = new DPTXlator2ByteFloat(tmp);

									tr.setData(e.getASDU());
									conn.setValue(tr.getValue());
								}

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								// e1.printStackTrace();
								pc.detach();
							}

						}
					});
					try {
						knxUtils.readAndDetachPrivileged(pc, dp);
					} catch (Exception ex) {
						pc.detach();
						logger.info(conn.getGroupAddress() + " " + ex.getMessage());

					}
				}

				if (conn.getDptStr().equals("9.001")) {

					pc.addProcessListener(new ProcessListener() {
						public void detached(DetachEvent e) {
							// TODO Auto-generated method stub
						}

						public void groupWrite(ProcessEvent e) {
							// TODO Auto-generated method stub

							try {

								if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
										&& e.getDestination().toString().equals(main.toString())) {

									DPTXlator2ByteFloat tr = new DPTXlator2ByteFloat(
											DPTXlator2ByteFloat.DPT_TEMPERATURE);

									tr.setData(e.getASDU());
									conn.setValue(tr.getValue());

								}

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								pc.detach();
							}

						}
					});
					try {

						pc.read(dp);
						pc.detach();

					} catch (Exception ex) {
						pc.detach();
						logger.info(conn.getGroupAddress() + " " + ex.getMessage());

					}
				}

				if (conn.getDptStr().equals("12.001")) {

					pc.addProcessListener(new ProcessListener() {
						public void detached(DetachEvent e) {
							// TODO Auto-generated method stub
						}

						public void groupWrite(ProcessEvent e) {
							// TODO Auto-generated method stub

							try {

								if (e.getSourceAddr().toString().trim().equals(conn.getPhyaddress())
										&& e.getDestination().toString().equals(main.toString())) {

									DPT tmp = DPTXlator4ByteUnsigned.DPT_VALUE_4_UCOUNT;

									DPTXlator4ByteUnsigned tr = new DPTXlator4ByteUnsigned(tmp);

									tr.setData(e.getASDU());
									conn.setValue(tr.getValue());

								}

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								pc.detach();
							}

						}
					});
					try {
						knxUtils.readAndDetachPrivileged(pc, dp);
					} catch (Exception ex) {
						pc.detach();
						logger.info(conn.getGroupAddress() + " " + ex.getMessage());
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return conn.getValue();
	}

	private boolean unregisterResDemand(ConnectionInfo conn) {

		if (conn.getResDemandListener() != null) {

			aManager.getResourceAccess().removeResourceDemand(conn.getRessource().getResourceType(),
					conn.getResDemandListener());

			return true;
		}
		return false;
	}

	public int addConnection(final String connectionString) {

		final ConnectionInfo conInfo = knxUtils.createConnectionInfo(connectionString);

		createConnectionToGW(conInfo);

		if (conInfo.isSensor()) {

			statusKNX = searchSensor(conInfo);

		}
		else {

			conInfo.setDptStr(knxUtils.getDpt(conInfo));

			statusKNX = addKNXDeviceToRessource(conInfo);
		}

		return statusKNX;

	}

	private void createConnectionToGW(final ConnectionInfo conInfo) {
		if (!storage.getKnxNetConnections().containsKey(conInfo.getKnxRouter())) {

			try {
				final String address = knxUtils.getIPAddress(conInfo);

				final int port = knxUtils.getPort(conInfo);

				final KNXNetworkLinkIP netLinkIp = knxUtils.getNetLinkIpPrivileged(conInfo, address, port);
				storage.getKnxNetConnections().put(address + ":" + port, netLinkIp);

				GatewayConnector connector = new GatewayConnector(netLinkIp, conInfo, port, address, logger);

				Thread t1 = new Thread(connector);
				t1.start();
			} catch (PrivilegedActionException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	private int searchSensor(final ConnectionInfo conInfo) {
		KNXNetworkLinkIP netLinkIp = null;

		try {

			final List<ConnectionInfo> foundDevice = new ArrayList<ConnectionInfo>();

			String dptStr;

			dptStr = knxUtils.getDpt(conInfo);

			if (dptStr != null) {

				conInfo.setDptStr(dptStr);

				netLinkIp = storage.getKnxNetConnections().get(conInfo.getKnxRouter());

				if (netLinkIp != null) {

					final ProcessCommunicator pc = new ProcessCommunicatorImpl(netLinkIp);

					final GroupAddress main = new GroupAddress(conInfo.getGroupAddress());

					final Datapoint dp = new StateDP(main, "", 0, dptStr);

					pc.addProcessListener(new ProcessListener() {
						public void detached(DetachEvent e) {
							// TODO Auto-generated method stub
						}

						public void groupWrite(ProcessEvent e) {
							// TODO Auto-generated method stub

							if (e.getDestination().toString().equals(main.toString())
									&& !storage.getNotAllowedDevices().contains((e.getSourceAddr().toString()))
									&& e.getSourceAddr().toString().equals(conInfo.getPhyaddress())) {

								foundDevice.add(conInfo);
							}
						}
					});

					knxUtils.readAndDetachPrivileged(pc, dp);

					int sleepCounter = 0;

					while (true) {
						if (foundDevice.size() > 0) {
							break;
						}
						if (sleepCounter == 700) {
							break;
						}

						sleepCounter++;
						Thread.sleep(10);
					}

					if (!foundDevice.isEmpty()) {
						return addKNXDeviceToRessource(foundDevice.get(0));
					}
					else {
						return 2;
					}
				}
				else {
					return 1;
				}
			}
			else {
				return 3;
			}
		} catch (KNXIllegalArgumentException ex) {
			return 3;
		} catch (PrivilegedActionException ex) {
			return 2;
		} catch (Exception ex) {
			return 1;
		}
	}

	public List<ConnectionInfo> getConnectionSorted() {

		return storage.getDeviceConnections();

	}

	@Override
	public void start(ApplicationManager appManager) {
		logger = appManager.getLogger();

		aManager = appManager;

		final List<KNXAddress> resourcesKNX = appManager.getResourceAccess().getResources(KNXAddress.class);

		addNotAllowed();

		activated = true;

		// Information aus persistenten Speicher besorgen nach HT mappen

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() { // TODO Auto-generated method stub

				List<String> tmp2 = new ArrayList<String>();
				Iterator<KNXAddress> en = resourcesKNX.iterator();

				while (en != null && en.hasNext()) {

					try {

						KNXAddress knxAddress = en.next();

						PhysicalElement top = (PhysicalElement) knxAddress.getParent().getParent().getParent();
						final CommunicationInformation comInfo = top.addDecorator(NAME_COM_INFO,
								CommunicationInformation.class);
						IPAddressV4 local = comInfo.comAddress().getSubResource("localAddressIPV4");

						String localStr = local.address().getValue();

						final IPAddressV4 ip = comInfo.comAddress().ipV4Address();
						final PollingConfiguration comCycle = comInfo.pollingConfiguration();

						String url = top.getResourceType().getSimpleName() + "," + top.getName() + ","
								+ (ip.address().getValue() + ":" + ip.port().getValue()) + ","
								+ knxAddress.groupAddress().getValues()[0] + "/"
								+ knxAddress.groupAddress().getValues()[1] + "/"
								+ knxAddress.groupAddress().getValues()[2] + ","
								+ knxAddress.physicalAddress().getValues()[0] + "."
								+ knxAddress.physicalAddress().getValues()[1] + "."
								+ knxAddress.physicalAddress().getValues()[2] + ","
								+ ((int) (comCycle.pollingInterval().getValue() / 1000)) + "," + localStr;

						int status = addConnection(url);
						logger.info("KNX add device to Ogema: " + url);

						if (status != 0) {
							logger.info("No response from: " + url + " try again next round");
							tmp2.add(url);
						}
						else {
							logger.info("sucess add " + url + " to Ogema");
						}

					} catch (Exception ex) {
					}
				}
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				logger.info("next round");

				Iterator<String> it = tmp2.iterator();

				while (it.hasNext()) {

					String next = it.next();

					int status = addConnection(next);

					logger.info("KNX add device: " + next);

					if (status != 0) {
						logger.info("KNX could not find device: " + next);
					}
					else {
						logger.info("successfull add: " + next);
					}

				}

			}
		});
		t.start();

		appManager.createTimer(3000, timerListener);

		readFromSensor();

		writingToActor();

	}

	@Override
	public void stop(AppStopReason reason) {
		// TODO Auto-generated method stub

		Iterator<KNXNetworkLinkIP> it = storage.getKnxNetConnections().values().iterator();

		while (it.hasNext()) {

			KNXNetworkLinkIP el = it.next();

			el.close();

		}
		activated = false;
		Iterator<ConnectionInfo> it2 = storage.getDeviceConnections().iterator();
		ConnectionInfo conn = null;
		while (it.hasNext()) {
			conn = it2.next();
			if (!unregisterResDemand(conn)) {
				logger.error("Error for unregister " + "ressource: " + conn.getRessource().getName());
				it2.remove();
			}
		}
	}

	public Map<String, String> getInterfaces() {

		return Collections.unmodifiableMap(storage.getAllInterface());
	}

	@Override
	public void searchInterface() {
		try {
			storage.getAllInterface().putAll(knxUtils.searchInterface());
		} catch (PrivilegedActionException e) {
			logger.error("Error while searching for interfaces!", e);
		}
	}

}
