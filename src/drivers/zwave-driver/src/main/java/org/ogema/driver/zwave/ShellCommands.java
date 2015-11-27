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
package org.ogema.driver.zwave;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.felix.service.command.Descriptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.driver.zwave.manager.Node;
import org.ogema.driver.zwave.manager.NodeValue;
import org.ogema.driverconfig.LLDriverInterface;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.zwave4j.Manager;
import org.zwave4j.ValueGenre;
import org.zwave4j.ValueType;

/**
 * 
 * @author baerthbn
 * 
 */
public class ShellCommands implements LLDriverInterface {
	private ZWaveDriver driver;
	private final Logger logger = org.slf4j.LoggerFactory.getLogger("zwave-driver");

	public ShellCommands(ZWaveDriver driver, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "zwave");
		props.put("osgi.command.function", new String[] { "showNetwork", "showAllCreatedChannels",
				"showClusterDetails", "showDeviceDetails", "addConnection", "showHardware", "addConnectionViaPort",
				"enableInclusion", "enableExclusion", "resetController", "sendFrame", "cacheDevices", "readDevConfig",
				"setDevConfig", "listSupportedConfigs", "showConfigs", "writeConfig" });
		this.driver = driver;
		context.registerService(this.getClass().getName(), this, props);
		context.registerService(LLDriverInterface.class, this, null);
	}

	@Descriptor("Enables Inclusion.")
	public void enableInclusion(String interfaceId, String nodeName) {
		Connection connection = driver.findConnection(interfaceId);
		if (connection != null) {
			if (nodeName.length() <= 16)
				connection.getLocalDevice().enableInclusion(nodeName);
			else
				logger.error("Node name must have 16 characters or less!");
		}
		else
			logger.error("InterfaceId seems not to be valid!");
	}

	@Descriptor("Enables Exclusion.")
	public void enableExclusion(String interfaceId) {
		Connection connection = driver.findConnection(interfaceId);
		if (connection != null) {
			connection.getLocalDevice().enableExclusion();
		}
		else
			logger.error("InterfaceId seems not to be valid!");
	}

	@Descriptor("Reset the USB-Configurator.")
	public void resetController(String interfaceId) {
		Connection connection = driver.findConnection(interfaceId);
		if (connection != null) {
			connection.getLocalDevice().resetController();
			// TODO: delete all nodes from driver
		}
		else
			logger.error("InterfaceId seems not to be valid!");
	}

	@Override
	@Descriptor("Show details related to the coordinator hardware.")
	public JSONObject showHardware() {
		JSONObject subobj = new JSONObject();
		JSONArray arr = new JSONArray();
		JSONObject obj = new JSONObject();
		Set<Entry<String, Connection>> connections = driver.getConnections().entrySet();
		if (connections.size() <= 0)
			logger.debug("Currently there are no connections alive to ZWave coordinator hardware.");
		for (Entry<String, Connection> connection : connections) {
			long homeId = connection.getValue().getLocalDevice().getHomeId();
			short nodeId = connection.getValue().getLocalDevice().getManager().getControllerNodeId(
					connection.getValue().getLocalDevice().getHomeId());
			try {
				obj.put("Interface", connection.getValue().getInterfaceId());
				obj.put("nodeid", nodeId);
				obj.put("name", connection.getValue().getLocalDevice().getManager().getNodeManufacturerName(homeId,
						nodeId)
						+ " " + connection.getValue().getLocalDevice().getManager().getNodeProductName(homeId, nodeId));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		arr.put(obj);
		try {
			subobj.put("coordinators", arr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return subobj;
	}

	@Descriptor("Show a list of all devices in the neighborhood of the coordinator device.")
	public JSONObject showNetwork() {
		JSONObject connection = new JSONObject();
		JSONObject result = new JSONObject();
		JSONArray devices = new JSONArray();
		JSONArray connections = new JSONArray();
		Iterator<Entry<String, Connection>> connectionsIt = driver.getConnections().entrySet().iterator();
		try {
			while (connectionsIt.hasNext()) {
				Map.Entry<String, Connection> connectionsEntry = connectionsIt.next();
				Iterator<Entry<Short, Node>> devicesIt = connectionsEntry.getValue().getLocalDevice().getNodes()
						.entrySet().iterator();
				while (devicesIt.hasNext()) {
					Map.Entry<Short, Node> devicesEntry = devicesIt.next();
					Node node = devicesEntry.getValue();
					JSONObject dev = new JSONObject();
					dev.put("nodeId", node.getNodeId());
					dev.put("nodeName", node.getNodeName());
					dev.put("networkAddress", node.getNodeName());
					dev.put("deviceType", node.getManager().getNodeType(
							connectionsEntry.getValue().getLocalDevice().getHomeId(), node.getNodeId()));
					String name = node.getManager().getNodeProductName(
							connectionsEntry.getValue().getLocalDevice().getHomeId(), node.getNodeId());
					if (name == null)
						name = "unknown";
					dev.put("deviceName", name);
					String manu = node.getManager().getNodeManufacturerName(
							connectionsEntry.getValue().getLocalDevice().getHomeId(), node.getNodeId());
					if (manu == null)
						manu = "unknown";
					dev.put("manufacturerId", manu);
					dev.put("ready", node.isReady());
					devices.put(dev);
				}
				connection.put("interfaceName", connectionsEntry.getValue().getInterfaceId());
				connection.put("devices", devices);
				connections.put(connection);
			}
			result.put("driverId", driver.getDriverId());
			result.put("busses", connections);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	@Override
	@Descriptor("Shows Details of a Device.")
	public JSONArray showDeviceDetails(String interfaceId, String nodeName) {
		JSONArray arr = new JSONArray();
		JSONObject obj = new JSONObject();
		Connection connection = driver.findConnection(interfaceId);
		if (connection != null) {
			Node node = connection.getLocalDevice().getNodes().get(Short.valueOf(nodeName));
			try {
				obj.put("device", node.getNodeName());
				System.out.println("    # Device:" + node.getNodeName());
				JSONArray attrarr = new JSONArray();
				Iterator<Entry<String, NodeValue>> valuesIt = node.getValues().entrySet().iterator();
				while (valuesIt.hasNext()) {
					Map.Entry<String, NodeValue> valueEntry = valuesIt.next();
					NodeValue nodeValue = valueEntry.getValue();
					JSONObject val = new JSONObject();
					System.out.println("        # Channel Address: " + nodeValue.getChannelAddress());
					val.put("address", nodeValue.getChannelAddress());
					System.out.println("        # Channelname: " + nodeValue.getValueName());
					val.put("channelname", nodeValue.getValueName());
					System.out.println();
					attrarr.put(val);
				}
				obj.put("valuechannels", attrarr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else
			logger.error("InterfaceId seems not to be valid!");
		arr.put(obj);
		return arr;
	}

	@Descriptor("Returns all created channel Strings.")
	public JSONArray showAllCreatedChannels() {
		JSONObject devicesChannel;
		JSONArray result = new JSONArray();
		try {
			Iterator<Entry<String, Connection>> connectionsIt = driver.getConnections().entrySet().iterator();
			while (connectionsIt.hasNext()) {
				Map.Entry<String, Connection> connectionsEntry = connectionsIt.next();
				Iterator<Entry<Short, Device>> devicesIt = connectionsEntry.getValue().getDevices().entrySet()
						.iterator();
				while (devicesIt.hasNext()) {
					Map.Entry<Short, Device> devicesEntry = devicesIt.next();
					Device device = devicesEntry.getValue();

					Iterator<Entry<String, Channel>> syncChannelsIt = device.getChannels().entrySet().iterator();
					while (syncChannelsIt.hasNext()) {
						Map.Entry<String, Channel> channelsEntry = syncChannelsIt.next();
						devicesChannel = new JSONObject();
						System.out.println("          # Channel: " + channelsEntry.getValue().getChannelLocator());
						devicesChannel.put("channel", channelsEntry.getValue().getChannelLocator());
						result.put(devicesChannel);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void addConnection(String hardwareIdentifier) {
	}

	@Override
	public void addConnectionViaPort(String portName) {
	}

	@Override
	public JSONObject showClusterDetails(String interfaceId, String device, String endpoint, String clusterId) {
		return null;
	}

	@Override
	public JSONObject showNetwork(String option) {
		return showNetwork();
	}

	@Override
	public String whichTech() {
		return "ZWave";
	}

	@Override
	public String whichID() {
		return driver.getDriverId();
	}

	@Override
	public JSONObject cacheDevices() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("status", "Cache function is not yet implemented for Zwave!");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public JSONObject scanForDevices() {
		JSONObject obj = new JSONObject();
		// enableInclusion(); // TODO: Name parameter for device ?
		try {
			obj.put("status", "Inclusion enabled. Please start pairing the device.");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Descriptor("Shows all possible configs of a device/node.")
	public JSONArray showConfigs(String interfaceId, short deviceAddress) {
		JSONArray arr = new JSONArray();
		Connection connection = driver.findConnection(interfaceId);
		if (connection != null) {
			Node node = connection.getLocalDevice().getNodes().get(deviceAddress);
			String partialNodeAddress = interfaceId + ":" + node.getNodeName() + ":";
			if (node.isReady()) {
				for (Entry<String, NodeValue> valueEntry : node.getValues().entrySet()) {
					if (valueEntry.getValue().getValueid().getGenre().equals(ValueGenre.CONFIG)
							|| valueEntry.getValue().getValueid().getGenre().equals(ValueGenre.SYSTEM)) {
						JSONObject obj = new JSONObject();
						try {
							obj.put("channelAddress", partialNodeAddress + valueEntry.getValue().getChannelAddress());
							obj.put("label", Manager.get().getValueLabel(valueEntry.getValue().getValueid()));
							obj.put("help", Manager.get().getValueHelp(valueEntry.getValue().getValueid()));
							if (valueEntry.getValue().getValueid().getType() == ValueType.LIST) {
								ArrayList<String> list = new ArrayList<String>();
								Manager.get().getValueListItems(valueEntry.getValue().getValueid(), list);
								JSONArray listArray = new JSONArray();
								for (String s : list) {
									listArray.put(s);
								}
								obj.put("valueList", listArray);
								AtomicReference<String> val = new AtomicReference<>();
								Manager.get().getValueListSelectionString(valueEntry.getValue().getValueid(), val);
								obj.put("selection", val.get());
							}
							else {
								obj.put("value", valueEntry.getValue().getValue());
							}
							obj.put("valueMin", Manager.get().getValueMin(valueEntry.getValue().getValueid()));
							obj.put("valueMax", Manager.get().getValueMax(valueEntry.getValue().getValueid()));
							obj.put("valueUnits", Manager.get().getValueUnits(valueEntry.getValue().getValueid()));
							obj.put("readonly", Manager.get().isValueReadOnly(valueEntry.getValue().getValueid()));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						arr.put(obj);
					}
				}
			}
		}
		else
			logger.error("InterfaceId seems not to be valid!");
		return arr;
	}

	@Descriptor("Set a config with ChannelAddress COM3:nodeID:CommandClass:Instance:Index (e.g. COM3:12:XXXX:XXXX:XXXX.")
	public void writeConfig(String channelAddress, String value) {
		String[] ca = channelAddress.split(":");
		Connection connection = driver.findConnection(ca[0]);
		if (connection != null) {
			Node node = connection.getLocalDevice().getNodes().get(Short.valueOf(ca[1]));
			if (node != null) {
				NodeValue nodeValue = node.getValues().get(ca[2] + ":" + ca[3] + ":" + ca[4]);
				if (nodeValue != null) {
					if (nodeValue.getValueid().getGenre() == ValueGenre.SYSTEM
							|| nodeValue.getValueid().getGenre() == ValueGenre.CONFIG) {
						if (!nodeValue.readOnly()) {
							nodeValue.setValue(value);
						}
					}
				}
			}
		}
	}
}
