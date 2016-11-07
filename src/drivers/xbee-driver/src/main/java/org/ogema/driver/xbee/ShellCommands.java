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
package org.ogema.driver.xbee;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import org.apache.felix.service.command.Descriptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.hardwaremanager.HardwareDescriptor;
import org.ogema.core.hardwaremanager.HardwareDescriptor.HardwareType;
import org.ogema.core.hardwaremanager.HardwareManager;
import org.ogema.core.hardwaremanager.SerialHardwareDescriptor;
import org.ogema.core.hardwaremanager.UsbHardwareDescriptor;
import org.ogema.driver.xbee.manager.Endpoint;
import org.ogema.driver.xbee.manager.LocalDevice;
import org.ogema.driver.xbee.manager.RemoteDevice;
import org.ogema.driver.xbee.manager.XBeeDevice;
import org.ogema.driver.xbee.manager.zcl.Cluster;
import org.ogema.driver.xbee.manager.zcl.ClusterAttribute;
import org.ogema.driver.xbee.manager.zcl.ClusterCommand;
import org.ogema.driverconfig.LLDriverInterface;
import org.osgi.framework.BundleContext;

import jssc.SerialPortException;

public class ShellCommands implements LLDriverInterface {

	private XBeeDriver driver;
	private Connection connection;
	private HardwareManager hwMngr;

	public ShellCommands(XBeeDriver driver, BundleContext context, HardwareManager hardwareManager) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "zbll");
		props.put("osgi.command.function",
				new String[] { "sendFrame", "showNetwork", "showCreatedChannels", "showClusterDetails", "addConnection",
						"showHardware", "addConnectionViaPort", "setNodeJoinTime", "cacheDevices", "listHardware" });
		this.driver = driver;
		this.hwMngr = hardwareManager;
		context.registerService(this.getClass().getName(), this, props);
		context.registerService(LLDriverInterface.class, this, null);
	}

	@Descriptor("Sends the given frame via serial connection.")
	public void sendFrame(
			@Descriptor("The frame as a string with the hexadecimal charcters e. g. 7E001A.") String frame,
			@Descriptor("The interfaceId/portName of the XBee device that should send this frame") String interfaceId) {
		System.out.println("sendFrames: " + frame + " - " + interfaceId);
		connection = driver.findConnection(interfaceId);
		byte[] byteFrame = DatatypeConverter.parseHexBinary(frame);
		connection.localDevice.sendFrame(byteFrame);
	}

	@Descriptor("Lists the hardwareIds and corresponding portNames.")
	public void listHardware() {
		System.out.println("Hardware:");
		System.out.println(hwMngr.toString());
		for (HardwareDescriptor desc : hwMngr.getHardwareDescriptors()) {
			String identifier = desc.getIdentifier();
			String portName = null;
			System.out.println("Identifier: " + identifier);
			if (desc.getHardwareType() == HardwareType.USB) {
				portName = ((UsbHardwareDescriptor) desc).getPortName();
			}
			else if (desc.getHardwareType() == HardwareType.SERIAL) {
				portName = ((SerialHardwareDescriptor) desc).getPortName();
			}
			System.out.println("Port name: " + portName + "\n");
		}
	}

	public JSONObject showHardware() {
		JSONObject obj = new JSONObject();
		// static for now
		try {
			obj.put("interfaceName", connection.getInterfaceId());
			obj.put("address", connection.getLocalDevice().get64BitAddress((short) 0));
			obj.put("serial", "unknown");
			obj.put("name", "unknown");
			obj.put("firmware", "unknown");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Descriptor("Lists the ports and the devices, including supported clusters, that are connected to the XBee on that port.")
	public JSONObject showNetwork(String option) {
		switch (option) {
		case "-l":
		case "--long":
			return showNetwork_l();
		case "-s":
		case "--short":
			return showNetwork_s();
		case "-test":
			return showNetwork_test();
		}
		return null;
	}

	public JSONObject showNetwork_s() {
		JSONObject jsonObject = new JSONObject();
		JSONArray connectionArray = new JSONArray();

		for (Map.Entry<String, Connection> connectionsEntry : driver.getConnections().entrySet()) {

			JSONObject connectionArrayElement = new JSONObject();
			JSONArray deviceArray = new JSONArray();
			for (Map.Entry<Long, RemoteDevice> devicesEntry : connectionsEntry.getValue().localDevice.getDevices()
					.entrySet()) {
				JSONObject deviceArrayElement = new JSONObject();

				try {
					deviceArrayElement.put("networkAddress",
							Integer.toHexString((devicesEntry.getValue().getAddress16Bit()) & 0xffff));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				RemoteDevice remoteDevice = devicesEntry.getValue();
				if (remoteDevice.getInitState().equals(RemoteDevice.InitStates.UNINITIALIZED)) {
					try {
						deviceArrayElement.put("initialized", false);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					deviceArray.put(deviceArrayElement);
					continue;
				}
				else {
					try {
						deviceArrayElement.put("initialized", true);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				boolean isXBee = remoteDevice instanceof XBeeDevice;

				if (isXBee) {
					try {
						deviceArrayElement.put("deviceType", "XBee");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						deviceArrayElement.put("deviceType", "ZigBee");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				deviceArray.put(deviceArrayElement);
				try {
					connectionArrayElement.put("devices", deviceArray);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			connectionArray.put(connectionArrayElement);
		}
		try {
			jsonObject.put("busses", connectionArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public JSONObject showNetwork_l() {
		JSONObject jsonObject = new JSONObject();
		JSONArray connectionArray = new JSONArray();

		Map<String, Connection> connections = driver.getConnections();
		if (!(connections.size() > 0)) {
			return jsonObject;
		}

		String driverId = driver.getDriverId();
		try {
			jsonObject.put("driverId", driverId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (Map.Entry<String, Connection> connectionsEntry : connections.entrySet()) {

			JSONObject connectionArrayElement = new JSONObject();
			try {
				connectionArrayElement.put("interfaceName", connectionsEntry.getKey());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			JSONArray deviceArray = new JSONArray();
			LocalDevice dev = connectionsEntry.getValue().localDevice;
			if (dev == null) {
				continue;
			}
			for (Map.Entry<Long, RemoteDevice> devicesEntry : dev.getDevices().entrySet()) {
				JSONObject deviceArrayElement = new JSONObject();

				try {
					deviceArrayElement.put("networkAddress",
							Integer.toHexString((devicesEntry.getValue().getAddress16Bit()) & 0xffff));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				try {
					deviceArrayElement.put("physicalAddress",
							Long.toHexString((devicesEntry.getKey() & 0xffffffffffffffffL)));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				try {
					String descr = devicesEntry.getValue().getNodeIdentifier();
					if (descr != null) {
						deviceArrayElement.put("deviceName", descr);
					}
					else {
						deviceArrayElement.put("deviceName", "unnamed");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				RemoteDevice remoteDevice = devicesEntry.getValue();
				if (remoteDevice.getInitState().equals(RemoteDevice.InitStates.UNINITIALIZED)) {
					try {
						deviceArrayElement.put("initialized", false);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					deviceArray.put(deviceArrayElement);
					continue;
				}
				else {
					try {
						deviceArrayElement.put("initialized", true);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				String manufacturerCode = Integer
						.toHexString(remoteDevice.getNodeDescriptor().getManufacturerCode() & 0xffff);
				manufacturerCode = ("0000" + manufacturerCode).substring(manufacturerCode.length());

				try {
					deviceArrayElement.put("manufacturerId", manufacturerCode.toUpperCase());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				boolean isXBee = remoteDevice instanceof XBeeDevice;

				if (isXBee) {
					try {
						deviceArrayElement.put("deviceType", "XBee");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						deviceArrayElement.put("deviceType", "ZigBee");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				JSONArray endpointArray = new JSONArray();
				for (Map.Entry<Byte, Endpoint> endpoint : remoteDevice.getEndpoints().entrySet()) {
					JSONObject endpointArrayElement = new JSONObject();

					String endpointId = Integer.toHexString(endpoint.getKey() & 0xff);
					endpointId = ("00" + endpointId).substring(endpointId.length());
					try {
						endpointArrayElement.put("Endpoint", endpointId.toUpperCase());
					} catch (JSONException e) {
						e.printStackTrace();
					}

					if (!isXBee) {
						String deviceId = Integer.toHexString(
								endpoint.getValue().getSimpleDescriptor().getApplicationDeviceId() & 0xffff);
						deviceId = ("0000" + deviceId).substring(deviceId.length());
						try {
							endpointArrayElement.put("DeviceID", deviceId.toUpperCase());
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}

					String profileId = Integer.toHexString(endpoint.getValue().getProfileId() & 0xffff);
					profileId = ("0000" + profileId).substring(profileId.length());
					try {
						endpointArrayElement.put("Profile", profileId.toUpperCase());
					} catch (JSONException e1) {
						e1.printStackTrace();
					}

					JSONArray clusterArray = new JSONArray();
					if (isXBee) {
						clusterArray.put("0011");
					}
					for (Map.Entry<Short, Cluster> cluster : endpoint.getValue().getClusters().entrySet()) {
						String clusterId = Integer.toHexString(cluster.getKey() & 0xffff);
						String clusterName = cluster.getValue().getName();
						clusterId = ("0000" + clusterId).substring(clusterId.length());
						JSONObject job = new JSONObject();
						try {
							job.put("ID", clusterId.toUpperCase());
							job.put("name", clusterName);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						clusterArray.put(job);
					}
					try {
						endpointArrayElement.put("Cluster", clusterArray);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					endpointArray.put(endpointArrayElement);
				}
				try {
					deviceArrayElement.put("Endpoints", endpointArray);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (isXBee) {
					try {
						deviceArrayElement.put("ChannelAddress",
								"0011:XBee:" + ((XBeeDevice) remoteDevice).getNodeIdentifier());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				deviceArray.put(deviceArrayElement);
				try {
					connectionArrayElement.put("devices", deviceArray);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			connectionArray.put(connectionArrayElement);
		}
		try {
			jsonObject.put("busses", connectionArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public JSONObject showNetwork_test() {
		JSONObject jsonObject = new JSONObject();
		JSONArray connectionArray = new JSONArray();

		String driverId = driver.getDriverId();
		try {
			jsonObject.put("driverId", driverId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONObject connectionArrayElement = new JSONObject();
		try {
			connectionArrayElement.put("interfaceName", "COM9");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JSONArray deviceArray = new JSONArray();
		JSONObject deviceArrayElement = new JSONObject();

		try {
			deviceArrayElement.put("networkAddress", "1234");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			deviceArrayElement.put("physicalAddress", "1234567890ABCDEF");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			deviceArrayElement.put("deviceName", "myDevice");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			deviceArrayElement.put("initialized", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			deviceArrayElement.put("manufacturerId", "0815");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			deviceArrayElement.put("deviceType", "XBee");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray endpointArray = new JSONArray();
		JSONObject endpointArrayElement = new JSONObject();

		try {
			endpointArrayElement.put("Endpoint", "E8");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			endpointArrayElement.put("DeviceID", "0302");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		try {
			endpointArrayElement.put("Profile", "0011");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		JSONArray clusterArray = new JSONArray();
		JSONObject job = new JSONObject();
		try {
			job.put("ID", "2200");
			job.put("name", "Lighting");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		clusterArray.put(job);
		try {
			endpointArrayElement.put("Cluster", clusterArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		endpointArray.put(endpointArrayElement);
		try {
			deviceArrayElement.put("Endpoints", endpointArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			deviceArrayElement.put("ChannelAddress", "0011:XBee:myDevice");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		deviceArray.put(deviceArrayElement);
		try {
			connectionArrayElement.put("devices", deviceArray);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		connectionArray.put(connectionArrayElement);
		try {
			jsonObject.put("busses", connectionArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	@Descriptor("Lists the channelLocator String of all created Channels.")
	public List<String> showCreatedChannels() {
		ArrayList<String> createdChannels = new ArrayList<String>();

		for (Map.Entry<String, Connection> connectionsEntry : driver.getConnections().entrySet()) {

			for (Map.Entry<String, Device> devicesEntry : connectionsEntry.getValue().getDevices().entrySet()) {

				for (Map.Entry<String, Channel> channelsEntry : devicesEntry.getValue().getChannels().entrySet()) {
					System.out.println("          # Channel: " + (channelsEntry.getValue()).locator.toString());
					createdChannels.add((channelsEntry.getValue()).locator.toString());
				}
			}

		}
		return createdChannels;
	}

	@Override
	public JSONArray showAllCreatedChannels() {

		JSONObject tempjsonObject;
		JSONArray returnArray = new JSONArray();
		List<String> createdChannels = showCreatedChannels();

		for (String channel : createdChannels) {
			try {
				tempjsonObject = new JSONObject();
				tempjsonObject.put("channel", channel);
				returnArray.put(tempjsonObject);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return returnArray;
	}

	@Descriptor("Shows more information about a specific cluster of a connected device.")
	public JSONObject showClusterDetails(@Descriptor("Port name/interface ID.") String interfaceId,
			@Descriptor("16Bit device address.") String device, @Descriptor("8Bit endpoint ID.") String endpoint,
			@Descriptor("16Bit cluster ID.") String clusterId) {

		JSONObject jsonObject = new JSONObject();
		JSONObject tempjsonObject;
		JSONArray attributesArray = new JSONArray();
		JSONArray commandsArray = new JSONArray();

		// Need leading zeros for parseHexBinary conversion
		clusterId = ("0000" + clusterId).substring(clusterId.length());
		device = ("0000000000000000" + device).substring(device.length());

		byte[] clusterIdArr = DatatypeConverter.parseHexBinary(clusterId);
		ByteBuffer bb = ByteBuffer.wrap(clusterIdArr);
		short clusterIdShort = bb.getShort();
		byte[] deviceArr = DatatypeConverter.parseHexBinary(device);
		bb = ByteBuffer.wrap(deviceArr);
		long deviceId = bb.getLong();
		byte endpointId = DatatypeConverter.parseHexBinary(endpoint)[0];

		Cluster cluster = driver.findConnection(interfaceId).localDevice.getRemoteDevice(deviceId)
				.getEndpoint(endpointId).getClusters().get(clusterIdShort);

		try {
			System.out.println(cluster.getName());
			jsonObject.put("ClusterName", cluster.getName());

			System.out.println("  Attributes:");
			for (Map.Entry<Short, ClusterAttribute> attributeEntry : cluster.clusterAttributes.entrySet()) {
				System.out.println("    "
						+ Constants.bytesToHex(new byte[] { (byte) (attributeEntry.getValue().getIdentifier() >>> 8),
								(byte) (attributeEntry.getValue().getIdentifier() & 0x00ff) })
						+ ": " + attributeEntry.getValue().getAttributeName());
				System.out.println("    Addr: " + attributeEntry.getValue().getChannelAddress());

				tempjsonObject = new JSONObject();
				tempjsonObject.put("Identifier",
						Constants.bytesToHex(new byte[] { (byte) (attributeEntry.getValue().getIdentifier() >>> 8),
								(byte) (attributeEntry.getValue().getIdentifier() & 0x00ff) }));
				tempjsonObject.put("Name", attributeEntry.getValue().getAttributeName());
				tempjsonObject.put("Addr", attributeEntry.getValue().getChannelAddress());
				attributesArray.put(tempjsonObject);
			}
			jsonObject.put("Attributes", attributesArray);

			System.out.println("  Commands:");
			for (Entry<Byte, ClusterCommand> commandEntry : cluster.clusterCommands.entrySet()) {
				System.out.println("    " + Constants.bytesToHex(commandEntry.getValue().getIdentifier()) + ": "
						+ commandEntry.getValue().getDescription());
				System.out.println("    Addr: " + commandEntry.getValue().getChannelAddress());

				tempjsonObject = new JSONObject();
				tempjsonObject.put("Identifier", Constants.bytesToHex(commandEntry.getValue().getIdentifier()));
				tempjsonObject.put("Name", commandEntry.getValue().getDescription());
				tempjsonObject.put("Addr", commandEntry.getValue().getChannelAddress());
				commandsArray.put(tempjsonObject);
			}
			jsonObject.put("Commands", commandsArray);

		} catch (JSONException e) {

			e.printStackTrace();
		}
		return jsonObject;
	}

	@Descriptor("Add a new Connection to the driver via hardware ID.")
	public void addConnection(@Descriptor("The hardware ID of the XBee.") String identifier) {
		Connection con;
		try {
			con = new Connection(getPortName(identifier), driver);
			driver.addConnection(con);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	private String getPortName(String identifier) {
		System.out.println("getPortName via identifier: " + identifier);
		String portName = null;
		// HardwareManager hw = Activator.getHardwareManager();
		HardwareDescriptor desc = hwMngr.getDescriptor(identifier);

		if (desc.getHardwareType() == HardwareType.USB) {
			portName = ((UsbHardwareDescriptor) desc).getPortName();
		}
		else if (desc.getHardwareType() == HardwareType.SERIAL) {
			portName = ((SerialHardwareDescriptor) desc).getPortName();
		}
		System.out.println("portName: " + portName);
		return portName;
	}

	@Descriptor("Add a new Connection to the driver via port name.")
	public void addConnectionViaPort(@Descriptor("The port name.") String interfaceId) {
		Connection con;
		try {
			con = new Connection(interfaceId, driver);
			driver.addConnection(con);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	@Descriptor("Set the NJ register of the XBee. This value represents how many seconds the XBee will allow new devices to join.")
	public void setNodeJoinTime(@Descriptor("Port name/Interface ID.") String interfaceId,
			@Descriptor("0x00-0xFF seconds.") String nodeJoinTime) {
		byte[] njArray = DatatypeConverter.parseHexBinary(nodeJoinTime);
		if (njArray.length > 1) {
			System.out.println("Error, value too long, will only use the first byte");
		}
		Connection con = driver.findConnection(interfaceId);
		con.localDevice.setNodeJoinTime(njArray[0]);
	}

	@Override
	public String whichTech() {
		return "zigbee/xbee";
	}

	@Override
	public String whichID() {
		return driver.getDriverId();
	}

	public void cacheDevices(@Descriptor("The port name.") String ifid) {
		try {
			connection = driver.findConnection(ifid);
			connection.localDevice.writeRemoteDevicesFile();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public JSONObject cacheDevices() {

		Iterator<Connection> iterator = driver.getConnections().values().iterator();
		JSONObject obj = new JSONObject();

		try {
			if (iterator.hasNext()) {
				connection = iterator.next();

				if (connection.localDevice != null) {
					connection.localDevice.writeRemoteDevicesFile();
					obj.put("status", "Devices cached successfully.");
				}
				else
					obj.put("status", "Cache of devices failed, because there is no localDevice present.");
			}
			else
				obj.put("status", "Cache of devices failed, there is no connection established.");

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return obj;
	}

	@Override
	public JSONArray showDeviceDetails(String interfaceId, String deviceAddress) {
		return null;
	}

	@Override
	public JSONObject scanForDevices() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("status", "Please start network joining procedure of the device.");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
