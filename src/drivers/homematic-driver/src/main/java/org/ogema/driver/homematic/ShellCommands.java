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
package org.ogema.driver.homematic;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.service.command.Descriptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.driver.homematic.manager.DeviceAttribute;
import org.ogema.driver.homematic.manager.DeviceCommand;
import org.ogema.driver.homematic.manager.RemoteDevice;
import org.ogema.driverconfig.LLDriverInterface;
import org.osgi.framework.BundleContext;

public class ShellCommands implements LLDriverInterface {
	private HMDriver driver;
	private Connection connection;

	public ShellCommands(HMDriver driver, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "hmll");
		props.put("osgi.command.function", new String[] { "showNetwork", "showAllCreatedChannels",
				"showClusterDetails", "showDeviceDetails", "addConnection", "showHardware", "addConnectionViaPort",
				"enablePairing", "sendFrame", "cacheDevices" });
		this.driver = driver;
		connection = driver.findConnection("USB");
		context.registerService(this.getClass().getName(), this, props);
		context.registerService(LLDriverInterface.class, this, null);
	}

	@Descriptor("Enables pairing for 60 seconds.")
	public void enablePairing() {
		driver.enablePairing("USB");
	}

	@Descriptor("Enables pairing with a specific serial for 60 seconds.")
	public void enablePairing(final String str) {
		driver.enablePairing(str);
	}

	@Descriptor("Show details related to the coordinator hardware.")
	public JSONObject showHardware() {
		JSONObject obj = new JSONObject();
		// static for now
		try {
			obj.put("Interface", "USB");
			obj.put("address", connection.getLocalDevice().getOwnerid());
			obj.put("serial", connection.getLocalDevice().getSerial());
			obj.put("name", connection.getLocalDevice().getName());
			obj.put("firmware", connection.getLocalDevice().getFirmware());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Descriptor("Show a list of all devices in the nighborhood of the coordinator device.")
	public JSONObject showNetwork() {
		JSONObject connection = new JSONObject();
		JSONObject result = new JSONObject();
		JSONArray devices = new JSONArray();
		JSONArray connections = new JSONArray();
		Iterator<Entry<String, Connection>> connectionsIt = driver.getConnections().entrySet().iterator();
		try {
			while (connectionsIt.hasNext()) {
				Map.Entry<String, Connection> connectionsEntry = connectionsIt.next();
				// System.out.println("### Interface: " + connectionsEntry.getKey());
				// System.out.println("    # Devices: ");

				Iterator<Entry<String, RemoteDevice>> devicesIt = connectionsEntry.getValue().localDevice.getDevices()
						.entrySet().iterator();
				while (devicesIt.hasNext()) {
					Map.Entry<String, RemoteDevice> devicesEntry = devicesIt.next();
					RemoteDevice remoteDevice = devicesEntry.getValue();
					JSONObject dev = new JSONObject();
					dev.put("physicalAddress", remoteDevice.getAddress());
					dev.put("networkAddress", remoteDevice.getAddress());
					dev.put("deviceType", remoteDevice.getDeviceType());
					dev.put("serialNumber", remoteDevice.getSerial());
					String name = Constants.deviceNames.get(remoteDevice.getDeviceType());
					if (name == null)
						name = "unknown";
					dev.put("deviceName", name);
					dev.put("manufacturerId", "ELV");
					// System.out.print("    #ID: " + devicesEntry.getKey() + " - Serial: " + remoteDevice.getSerial());
					if (remoteDevice.getInitState().equals(RemoteDevice.InitStates.PAIRED)) {
						dev.put("initialized", true);
					}
					else
						dev.put("initialized", false);
					devices.put(dev);
				}
				connection.put("interfaceName", "USB");
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

	@Descriptor("Shows Details of a Device.")
	public JSONObject showDeviceDetails(String address) {
		JSONObject obj = new JSONObject();
		RemoteDevice remoteDevice = connection.getLocalDevice().getDevices().get(address);
		try {
			obj.put("device", remoteDevice.getAddress());
			System.out.println("    # Device:" + remoteDevice.getAddress());
			JSONArray attrarr = new JSONArray();
			Iterator<Entry<Short, DeviceAttribute>> attributeIt = remoteDevice.deviceAttributes.entrySet().iterator();
			while (attributeIt.hasNext()) {
				Map.Entry<Short, DeviceAttribute> attributeEntry = attributeIt.next();
				DeviceAttribute attribute = attributeEntry.getValue();
				JSONObject attr = new JSONObject();
				System.out.println("        # Channel Address: " + attribute.getChannelAddress());
				attr.put("address", attribute.getChannelAddress());
				System.out.println("        # Identifier: " + attribute.getIdentifier());
				attr.put("identifier", attribute.getIdentifier());
				System.out.println("        # Channelname: " + attribute.getAttributeName());
				attr.put("channelname", attribute.getAttributeName());
				System.out.println();
				attrarr.put(attr);
			}
			obj.put("attributechannels", attrarr);
			JSONArray commarr = new JSONArray();
			Iterator<Entry<Byte, DeviceCommand>> commandIt = remoteDevice.deviceCommands.entrySet().iterator();
			while (commandIt.hasNext()) {
				Map.Entry<Byte, DeviceCommand> commandEntry = commandIt.next();
				DeviceCommand command = commandEntry.getValue();
				JSONObject comm = new JSONObject();
				System.out.println("        # Channel Address: " + command.getChannelAddress());
				comm.put("address", command.getChannelAddress());
				System.out.println("        # Identifier: " + command.getIdentifier());
				comm.put("identifier", command.getIdentifier());
				System.out.println("        # Channelname: " + command.getDescription());
				comm.put("channelname", command.getDescription());
				System.out.println();
				commarr.put(comm);
			}
			obj.put("commandchannels", commarr);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Descriptor("Returns all created channel Strings.")
	public JSONArray showAllCreatedChannels() {
		JSONObject devicesChannel;
		JSONArray result = new JSONArray();
		try {
			Iterator<Entry<String, Connection>> connectionsIt = driver.getConnections().entrySet().iterator();
			while (connectionsIt.hasNext()) {
				Map.Entry<String, Connection> connectionsEntry = connectionsIt.next();
				Iterator<Entry<String, Device>> devicesIt = connectionsEntry.getValue().getDevices().entrySet()
						.iterator();
				while (devicesIt.hasNext()) {
					Map.Entry<String, Device> devicesEntry = devicesIt.next();
					Device device = devicesEntry.getValue();

					Iterator<Entry<String, Channel>> syncChannelsIt = device.getChannels().entrySet().iterator();
					while (syncChannelsIt.hasNext()) {
						Map.Entry<String, Channel> channelsEntry = syncChannelsIt.next();
						devicesChannel = new JSONObject();
						System.out.println("          # Channel: " + channelsEntry.getValue().locator);
						devicesChannel.put("channel", channelsEntry.getValue().locator);
						result.put(devicesChannel);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Descriptor("Manually send a command to a Device.")
	public void sendFrame(String device, String channel, String list, String command) {
		if ((connection.localDevice.getDevices().get(device)) != null) {
			RemoteDevice rd = connection.localDevice.getDevices().get(device);
			rd.pushConfig(channel, list, command);
		}
		else {
			System.out.println("Device not found");
		}
	}

	@Descriptor("Cache all devices paired up to now, so they want to be paired again after restart unless a clean start is performaed.")
	public void cacheDevices(@Descriptor("The port name.") String ifid) {
		try {
			connection = driver.findConnection(ifid);
			connection.localDevice.saveDeviceConfig();
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
					connection.localDevice.saveDeviceConfig();
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
		return "HomeMatic";
	}

	@Override
	public String whichID() {
		return driver.getDriverId();
	}

	@Override
	public JSONArray showDeviceDetails(String interfaceId, String deviceAddress) {
		return null;
	}

	@Override
	public JSONObject scanForDevices() {
		JSONObject obj = new JSONObject();
		enablePairing();
		try {
			obj.put("status", "Pairing enabled for 60 seconds. Please start pairing the device.");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
