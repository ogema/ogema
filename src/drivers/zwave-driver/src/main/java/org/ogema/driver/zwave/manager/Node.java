package org.ogema.driver.zwave.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.measurements.Value;
import org.slf4j.Logger;
import org.zwave4j.Manager;
import org.zwave4j.ValueId;

/**
 * 
 * @author baerthbn
 * 
 */
public class Node {

	private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");

	private Map<String, NodeValue> values; // String = commandclassid:instanceid:valueid
	private final short nodeId;
	private final String name; // is limited to 16 characters
	private final LocalDevice localDevice;
	private boolean ready = false;

	ArrayList<ChannelScanListener> chListeners;
	public DeviceLocator devLocator;

	/*
	 * Full channelpath should be: INTERFACE:DEVICENAME:COMMANDCLASSID:INSTANCEID:VALUEID
	 */

	/**
	 * Constructor for known nodes
	 * 
	 * @param localDevice
	 * @param nodeId
	 */
	// public Node(LocalDevice localDevice, short nodeId) {
	// this.nodeId = nodeId;
	// this.localDevice = localDevice;
	// this.name = localDevice.getManager().getNodeName(localDevice.getHomeId(), nodeId);
	// values = new HashMap<String, NodeValue>();
	// this.chListeners = new ArrayList<>();
	// }

	/**
	 * Constructor for new UNKNOWN nodes
	 * 
	 * @param localDevice
	 * @param nodeId
	 * @param nodeName
	 */
	public Node(LocalDevice localDevice, short nodeId, String nodeName) {
		this.nodeId = nodeId;
		this.localDevice = localDevice;
		this.name = nodeName;
		localDevice.getManager().setNodeName(localDevice.getHomeId(), nodeId, nodeName);
		values = new HashMap<String, NodeValue>();
		this.chListeners = new ArrayList<>();
	}

	public Map<String, NodeValue> getValues() {
		return values;
	}

	public void addValue(NodeValue nv) {
		logger.debug(String.format("NodeValue added to %s", name));
		logger.debug(String.format("%d ChannelScanListener registered", chListeners.size()));
		synchronized (this) {
			values.put(nv.getChannelAddress(), nv);
		}
		// inform listeners
		for (ChannelScanListener listener : chListeners) {
			try {
				ChannelLocator channel = localDevice.driver.channelAccess.getChannelLocator(nv.getChannelAddress(),
						devLocator);
				if (channel != null)
					listener.channelFound(channel);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public short getNodeId() {
		return nodeId;
	}

	public String getNodeName() {
		return name;
	}

	public String getProductString() {
		Manager manager = localDevice.getManager();
		long homeId = localDevice.getHomeId();
		return (manager.getNodeManufacturerId(homeId, nodeId) + "." + manager.getNodeProductType(homeId, nodeId) + "." + manager
				.getNodeProductId(homeId, nodeId));
	}

	public String getProductName() {
		return localDevice.getManager().getNodeProductName(localDevice.getHomeId(), nodeId);
	}

	public String readNodeName() {
		return localDevice.getManager().getNodeName(localDevice.getHomeId(), nodeId);
	}

	public Manager getManager() {
		return localDevice.getManager();
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void channelChanged(byte identifier, Value value) {
		// method for DeviceCommand
		// something like find the value (id)
		// and change something on it!

	}

	public String generateChannelAddress(ValueId valueid) {
		// XXXX:XXXX:XXXX
		return (fillZero(valueid.getCommandClassId()) + ":" + fillZero(valueid.getInstance()) + ":" + fillZero(valueid
				.getIndex())).toUpperCase();
	}

	private String fillZero(short s) {
		StringBuilder tempString = new StringBuilder();
		tempString.append(Integer.toHexString(s & 0xffff));
		switch (tempString.length()) {
		case 0:
			tempString.append("0000");
			break;
		case 1:
			tempString.insert(tempString.length() - 1, "000");
			break;
		case 2:
			tempString.insert(tempString.length() - 2, "00");
			break;
		case 3:
			tempString.insert(tempString.length() - 3, "0");
			break;
		}
		return tempString.toString();

	}

	synchronized public void addChannelListener(ChannelScanListener listener) {
		logger.debug(String.format("ChannelListener added to Node %s", listener.toString()));
		chListeners.add(listener);
		// report all current values
		Set<Entry<String, NodeValue>> set = values.entrySet();
		for (Entry<String, NodeValue> e : set) {
			NodeValue val = e.getValue();
			try {
				ChannelLocator channel = localDevice.driver.channelAccess.getChannelLocator(val.getChannelAddress(),
						devLocator);
				if (channel != null)
					listener.channelFound(channel);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
