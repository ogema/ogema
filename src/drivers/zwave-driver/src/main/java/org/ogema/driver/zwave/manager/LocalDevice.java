package org.ogema.driver.zwave.manager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ogema.core.channelmanager.driverspi.DeviceListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.driver.zwave.ZWaveDriver;
import org.slf4j.Logger;
import org.zwave4j.Manager;
import org.zwave4j.NativeLibraryLoader;
import org.zwave4j.Options;
import org.zwave4j.ZWave4j;

/**
 * 
 * @author baerthbn
 * 
 */
public class LocalDevice {
	public final Map<Short, Node> nodes; // NodeName, Node
	final private Options options;
	final private Manager manager;
	final private InputHandler inputHandler;
	final private Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");
	private long homeId;
	private boolean ready;
	private String waitingNodeName = null;
	private boolean isWaitingNodeName = false;

	ArrayList<DeviceListener> devListeners;
	ArrayList<Node> devices2Dispatch;

	private String ifaceName;
	ZWaveDriver driver;
	public Thread deviceEventDispatcher;
	private String driverPath;

	private static String OS = System.getProperty("os.name").toLowerCase();

	public LocalDevice(String port, ZWaveDriver drv) {
		this.driver = drv;
		this.nodes = new ConcurrentHashMap<Short, Node>();
		this.devListeners = new ArrayList<>();
		this.devices2Dispatch = new ArrayList<>();

		this.deviceEventDispatcher = new Thread(new Runnable() {

			@Override
			public void run() {
				while (org.ogema.driver.zwave.Activator.bundleIsRunning) {
					synchronized (driver) {
						try {
							driver.wait();
							// inform listeners
							int index = 0;
							int size = devices2Dispatch.size();
							Node tempNode = null;
							if (size > 0)
								tempNode = devices2Dispatch.remove(index);

							while (org.ogema.driver.zwave.Activator.bundleIsRunning) {
								index++;
								for (DeviceListener listener : devListeners) {
									try {
										DeviceLocator device = tempNode.devLocator;
										if (device == null) {
											device = driver.createDeviceLocator(ifaceName, tempNode);
											tempNode.devLocator = device;
										}
										if (device != null) {
											logger.debug(String.format("Device %s delivered via callback",
													device.toString()));
											listener.deviceAdded(device);
										}
									} catch (Throwable t) {
										t.printStackTrace();
									}
								}
								if (index < size)
									tempNode = devices2Dispatch.remove(index);
								else
									break;
							}
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		this.deviceEventDispatcher.setName("zwave-eventdispatcher");
		this.deviceEventDispatcher.start();

		NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);

		options = Options.create("./config/zwave", "./config/zwave_cache", "");
		options.addOptionBool("ConsoleOutput", false);
		options.lock();

		manager = Manager.create();
		inputHandler = new InputHandler(this);
		manager.addWatcher(inputHandler, null);
		this.ifaceName = port;
		if (OS.indexOf("win") >= 0)
			port = "//./" + port;
		manager.addDriver(port);
		driverPath = port;
	}

	public void restart() {
		manager.addDriver(driverPath);
	}

	public void printNodes() {
		Set<Entry<Short, Node>> set = nodes.entrySet();
		for (Entry<Short, Node> e : set) {
			System.out.print(e.getKey());
			System.out.print(":");
			System.out.print(e.getValue().readNodeName());
			System.out.print(", ");
			System.out.print(e.getValue().getProductName());
			System.out.print(", ");
			System.out.println(e.getValue().getProductString());
		}
	}

	public boolean isConnected() {
		return true;
	}

	public Manager getManager() {
		return manager;
	}

	public long getHomeId() {
		return homeId;
	}

	public void setHomeId(long homeId) {
		this.homeId = homeId;
	}

	public short getNodeId() {
		return manager.getControllerNodeId(homeId);
	}

	public void setReadyId() {
		ready = true;
	}

	public Map<Short, Node> getNodes() {
		return nodes;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		if (manager != null) {
			this.ready = ready;
			manager.writeConfig(homeId);

			boolean failed = false;
			for (Map.Entry<Short, Node> entry : nodes.entrySet()) {
				failed = manager.isNodeFailed(homeId, entry.getValue().getNodeId());
				if (failed) {
					logger.error("Node " + entry.getKey() + " failed");
					nodes.remove(entry.getKey());
				}
				else
					logger.info("Node " + entry.getKey() + " ok");
			}
		}
	}

	public void enableInclusion(String nodeName) {
		this.waitingNodeName = nodeName;
		isWaitingNodeName = true;
		manager.addNode(homeId, false); // TODO: Security disabled, just turn it on when everything else is working
	}

	public String getWaitingNodeName() {
		isWaitingNodeName = false;
		return waitingNodeName;
	}

	public boolean isWaitingNodeName() {
		return isWaitingNodeName;
	}

	public void enableExclusion() {
		manager.removeNode(homeId);
	}

	public void close() {
		// manager.removeWatcher(inputHandler, null);
		manager.removeDriver(driverPath);
		// Manager.destroy();
	}

	public void resetController() {
		manager.resetController(homeId);
	}

	public void addDeviceListener(DeviceListener listener) {
		logger.debug("DeviceListener added to LocalDevice");
		devListeners.add(listener);
		// commit all present devices
		Set<Entry<Short, Node>> set = nodes.entrySet();
		for (Entry<Short, Node> e : set) {
			Node tempNode = e.getValue();
			DeviceLocator device = driver.createDeviceLocator(ifaceName, tempNode);
			if (device != null)
				listener.deviceAdded(device);
		}
	}

	public void removeDeviceListener(DeviceListener listener) {
		devListeners.remove(listener);
	}

	public void addNode(Node tempNode) {
		logger.debug(String.format("Node added to LocalDevice, %d Devicelistenrs registered.", devListeners.size()));
		nodes.put(tempNode.getNodeId(), tempNode);
		synchronized (driver) {
			devices2Dispatch.add(tempNode);
			driver.notify();
		}
	}

	public void removeNode(short nodeID) {
		logger.debug("Node removed from LocalDevice");
		Node node = nodes.remove(nodeID);
		// inform listeners
		if (node != null)
			for (DeviceListener listener : devListeners) {
				// DeviceLocator device = driver.createDeviceLocator(ifaceName, node);
				DeviceLocator device = node.devLocator;
				if (device != null)
					listener.deviceRemoved(device);
			}
	}

	public void addDeviceListeners(ArrayList<DeviceListener> listeners) {
		logger.debug(String.format("%d DeviceListeners added to LocalDevice", listeners.size()));
		devListeners.addAll(listeners);
		// commit all present devices to all listeners
		for (DeviceListener l : devListeners) {
			Set<Entry<Short, Node>> set = nodes.entrySet();
			for (Entry<Short, Node> e : set) {
				Node tempNode = e.getValue();
				DeviceLocator device = driver.createDeviceLocator(ifaceName, tempNode);
				if (device != null) {
					logger.debug("\tDevice reported to DeviceListener");
					l.deviceAdded(device);
				}
			}
		}
	}

	public void removeDeviceListeners(ArrayList<DeviceListener> devListeners) {
		devListeners = null;
	}

}
