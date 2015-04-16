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
package org.ogema.driver.generic_zb;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.felix.service.command.Descriptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.osgi.framework.BundleContext;
import org.ogema.driverconfig.HLDriverInterface;

public class ShellCommands implements HLDriverInterface {
	// private BundleContext context = null;
	private Generic_ZbDriver driver = null;

	public ShellCommands(Generic_ZbDriver driver, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "zbhl");
		props.put("osgi.command.function", new String[] { "showCreatedChannels", "createChannel", "deleteChannel",
				"readChannel", "writeChannel", "deviceScan" });
		// this.context = context;
		this.driver = driver;
		context.registerService(this.getClass().getName(), this, props);
		context.registerService(HLDriverInterface.class, this, null);
	}

	@Descriptor("Lists the channelLocator String of all created Channels.")
	public void showCreatedChannels() {
		for (Map.Entry<String, ChannelLocator> channelMapEntry : driver.channelMap.entrySet()) {
			System.out.print(channelMapEntry.getKey() + ": ");
			System.out.println(channelMapEntry.getValue().toString());
		}
	}

	@Override
	@Descriptor("Creates a new channel.")
	public void createChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress,
			@Descriptor("The period in which the ChannelManager should try to request a new value. 0 for commands.") long timeout,
			@Descriptor("A name for the resource.") String resourceName,
			@Descriptor("The endpoint's deviceId.") String deviceId) {
		Generic_ZbConfig config = new Generic_ZbConfig();
		config.interfaceId = interfaceId;
		deviceAddress = deviceAddress.toUpperCase();
		config.deviceAddress = deviceAddress = ("0000000000000000000" + deviceAddress)
				.substring(deviceAddress.length());
		config.channelAddress = channelAddress.toUpperCase();
		config.timeout = timeout;
		config.resourceName = resourceName;

		// Need leading zeros for parseHexBinary conversion
		deviceId = ("0000" + deviceId).substring(deviceId.length());
		System.out.println("deviceid " + deviceId);
		byte[] deviceIdArray = DatatypeConverter.parseHexBinary(deviceId);
		config.deviceId |= ((deviceIdArray[0] << 8) | deviceIdArray[1]);
		config.deviceParameters = "";
		System.out.println(config.resourceName + " = " + config.interfaceId + " " + config.deviceAddress + " "
				+ config.channelAddress);
		driver.resourceAvailable(config);
	}

	@Descriptor("Deletes a channel.")
	public void deleteChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress) {
		Generic_ZbConfig config = new Generic_ZbConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = ("0000000000000000000" + deviceAddress.toUpperCase()).substring(deviceAddress.length());
		config.channelAddress = channelAddress.toUpperCase();
		driver.resourceUnavailable(config);
	}

	@Descriptor("Deletes a channel.")
	public void deleteChannel(@Descriptor("The resourceId.") String resourceId) {
		ChannelLocator channelLocator = driver.channelMap.get(resourceId);
		Generic_ZbConfig config = new Generic_ZbConfig();
		config.interfaceId = channelLocator.getDeviceLocator().getInterfaceName();
		config.deviceAddress = channelLocator.getDeviceLocator().getDeviceAddress().toUpperCase();
		config.channelAddress = channelLocator.getChannelAddress().toUpperCase();
		driver.resourceUnavailable(config);
	}

	@Descriptor("Write to a channel. This can mean overwriting a Attribute or sending a Command.")
	public void writeChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress,
			@Descriptor("The value as a string with hex characters e. g. 0AFF. Byte order needs to be in Little Endian Byte order") String writeValue) {
		Generic_ZbDevice device = driver.devices.get(interfaceId + ":"
				+ ("0000000000000000000" + deviceAddress.toUpperCase()).substring(deviceAddress.length()));
		ChannelLocator locator = device.attributeChannel.get(channelAddress.toUpperCase());
		if (locator == null) {
			locator = device.commandChannel.get(channelAddress.toUpperCase());
		}
		device.writeToChannel(locator, writeValue);
	}

	@Descriptor("Write to a channel. This can mean overwriting a Attribute or sending a Command.")
	public void writeChannel(
			@Descriptor("The resourceId.") String resourceId,
			@Descriptor("The value as a string with hex characters e. g. 0AFF. Byte order needs to be in Little Endian Byte order") String writeValue) {
		ChannelLocator channelLocator = driver.channelMap.get(resourceId);
		Generic_ZbDevice device = driver.devices.get(channelLocator.getDeviceLocator().getInterfaceName() + ":"
				+ channelLocator.getDeviceLocator().getDeviceAddress().toUpperCase());
		device.writeToChannel(channelLocator, writeValue);
	}

	@Descriptor("Read from a channel.")
	public JSONObject readChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress) {
		Generic_ZbDevice device = driver.getDevice(interfaceId + ":"
				+ ("0000000000000000000" + deviceAddress.toUpperCase()).substring(deviceAddress.length()));
		System.out.print("Channel value: ");
		Value val = device.readValue(channelAddress.toUpperCase());
		// device.printValue(channelAddress.toUpperCase());
		JSONObject returnJSONObject = new JSONObject();
		printValue(val, returnJSONObject);
		return returnJSONObject;
	}

	public void printValue(Value value, JSONObject returnJSONObject) {
		System.out.print("New value: ");
		JSONObject json;
		if (value instanceof StringValue) {
			System.out.println(value.getStringValue());
			try {
				returnJSONObject.put("value", value.getStringValue());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (value instanceof IntegerValue) {
			System.out.println(Integer.toString(value.getIntegerValue()));
			try {
				returnJSONObject.put("value", Integer.toString(value.getIntegerValue()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (value instanceof FloatValue) {
			System.out.println(Float.toString(value.getFloatValue()));
			try {
				returnJSONObject.put("value", Float.toString(value.getFloatValue()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (value instanceof DoubleValue) {
			System.out.println(Double.toString(value.getDoubleValue()));
			try {
				returnJSONObject.put("value", Double.toString(value.getDoubleValue()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (value instanceof ByteArrayValue) {
			System.out.println("byteArrayValue");
			try {
				returnJSONObject.put("value", "byteArrayValue");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (value instanceof BooleanValue) {
			System.out.println(Boolean.toString(value.getBooleanValue()));
			try {
				returnJSONObject.put("value", Boolean.toString(value.getBooleanValue()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else if (value instanceof ObjectValue) {
			Object objectValue = value.getObjectValue();
			if (objectValue instanceof Map) {
				json = new JSONObject();
				@SuppressWarnings("unchecked")
				HashMap<Short, SampledValue> sampledValueMap = (HashMap<Short, SampledValue>) objectValue;
				for (Map.Entry<Short, SampledValue> mapEntry : sampledValueMap.entrySet()) {
					printValue(mapEntry.getValue().getValue(), json);
				}
			}
		}
	}

	@Descriptor("Read from a channel.")
	public void readChannel(@Descriptor("The resourceId.") String resourceId) {
		ChannelLocator channelLocator = driver.channelMap.get(resourceId);
		Generic_ZbDevice device = driver.getDevice(channelLocator.getDeviceLocator().getInterfaceName() + ":"
				+ channelLocator.getDeviceLocator().getDeviceAddress().toUpperCase());
		System.out.print("Channel value: ");
		// device.readValue(channelLocator.getChannelAddress().toUpperCase());
		device.printValue(channelLocator.getChannelAddress().toUpperCase());
	}

	public void deviceScan(@Descriptor("The interface ID/Port name.") String interfaceId) {
		try {
			if (driver.appManager != null) // do it only if the HLD app is running
				driver.appManager.getChannelAccess().discoverDevices("xbee-driver", interfaceId, null, driver);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		} catch (NoSuchDriverException e) {
			e.printStackTrace();
		}
	}

	public void createChannel(String interfaceId, String deviceAddress, String channelAddress, long timeout,
			String resourceName) {
		Generic_ZbConfig config = new Generic_ZbConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		config.timeout = timeout;

		// Need leading zeros for parseHexBinary conversion
		String deviceId = deviceAddress.substring(deviceAddress.lastIndexOf(':') + 1);
		deviceId = ("0000" + deviceId).substring(deviceId.length());
		System.out.println("deviceid " + deviceId);
		byte[] deviceIdArray = DatatypeConverter.parseHexBinary(deviceId);
		config.deviceId |= ((deviceIdArray[0] << 8) | deviceIdArray[1]);
		config.deviceParameters = "";
		System.out.println(config.resourceName + " = " + config.interfaceId + " " + config.deviceAddress + " "
				+ config.channelAddress);
		driver.resourceAvailable(config);
	}

	@Override
	public JSONArray showCreatedChannels(String deviceAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String whichID() {
		return "generic-zb";
	}
}
