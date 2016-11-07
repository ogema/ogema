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
package org.ogema.driver.zwavehl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.measurements.Value;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 * @author baerthbn
 * 
 */
public class ShellCommands {
	private ZWaveHlDriver driver = null;
	private final ServiceRegistration<ShellCommands> srCommands;

	public ShellCommands(ZWaveHlDriver driver, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "zwavehl");
		props.put("osgi.command.function", new String[] { "showCreatedChannels", "createChannel", "deleteChannel",
				"readChannel", "writeChannel", "deviceScan" });
		this.driver = driver;
		srCommands = context.registerService(ShellCommands.class, this, props);
	}
	
	void close() {
		try {
			srCommands.unregister();
		} catch (Exception e) {}
	}

	@Descriptor("Lists the channelLocator String of all created Channels.")
	public void showCreatedChannels() {
		for (Map.Entry<String, ZWaveHlDevice> deviceEntry : driver.devices.entrySet()) {
			for (Map.Entry<String, ChannelConfiguration> attributeChannelEntry : deviceEntry.getValue().valueChannel
					.entrySet()) {
				System.out.println(attributeChannelEntry.getKey());
			}
		}
	}

	@Descriptor("Creates a new channel.")
	public void createChannel(@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. funnydevice.") String deviceAddress,
			@Descriptor("The channel address in form e. g. commandclassid:instanceid:valueid.") String channelAddress,
			@Descriptor("A name for the resource.") String resourceName) {
		ZWaveHlConfig config = new ZWaveHlConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		config.resourceName = driver.appManager.getResourceManagement().getUniqueResourceName(resourceName);
		config.deviceParameters = "";
		System.out.println(config.resourceName + " = " + config.interfaceId + " " + config.deviceAddress + " "
				+ config.channelAddress);
		driver.resourceAvailable(config);
	}

	@Descriptor("Deletes a channel.")
	public void deleteChannel(@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. 000000.") String deviceAddress,
			@Descriptor("The channel address in form of Type:Id e. g. Type:Attribute:0000.") String channelAddress) {
		ZWaveHlConfig config = new ZWaveHlConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		driver.resourceUnavailable(config);
	}

	@Descriptor("Deletes a channel.")
	public void deleteChannel(@Descriptor("The resourceId.") String resourceId) {
		ChannelConfiguration chConf = driver.getChannel(resourceId);
		ZWaveHlConfig config = new ZWaveHlConfig();
		config.interfaceId = chConf.getDeviceLocator().getInterfaceName();
		config.deviceAddress = chConf.getDeviceLocator().getDeviceAddress().toUpperCase();
		config.channelAddress = chConf.getChannelLocator().getChannelAddress().toUpperCase();
		driver.resourceUnavailable(config);
	}

	@Descriptor("Write to a channel. This can mean overwriting a Attribute or sending a Command.")
	public void writeChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. funnydevice.") String deviceAddress,
			@Descriptor("The channel address in form of Type:Id e. g. Commandclassid:Instanceid:valueid.") String channelAddress,
			@Descriptor("The value as a string with hex characters e. g. 0AFF. Byte order needs to be in Little Endian Byte order") Value writeValue) {
		ZWaveHlDevice device = driver.devices.get(interfaceId + ":" + deviceAddress.toUpperCase());
		ChannelConfiguration locator = device.valueChannel.get(channelAddress.toUpperCase());
		device.writeToChannel(locator, writeValue);
	}

	@Descriptor("Write to a channel. This can mean overwriting a Attribute or sending a Command.")
	public void writeChannel(
			@Descriptor("The resourceId.") String resourceId,
			@Descriptor("The value as a string with hex characters e. g. 0AFF. Byte order needs to be in Little Endian Byte order") Value writeValue) {
		ChannelConfiguration chConf = driver.getChannel(resourceId);
		ZWaveHlDevice device = driver.devices.get(chConf.getDeviceLocator().getInterfaceName() + ":"
				+ chConf.getDeviceLocator().getDeviceAddress().toUpperCase());
		device.writeToChannel(chConf, writeValue);
	}

	@Descriptor("Read from a channel.")
	public void readChannel(@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. 000000.") String deviceAddress,
			@Descriptor("The channel address in form of Type:Id e. g. Type:Attribute:0000.") String channelAddress) throws ChannelAccessException {
		ZWaveHlDevice device = driver.getDevice(interfaceId + ":" + deviceAddress.toUpperCase());
		System.out.print("Channel value: ");
		device.readValue(channelAddress.toUpperCase());
		device.printValue(channelAddress.toUpperCase());
	}

	@Descriptor("Read from a channel.")
	public void readChannel(@Descriptor("The resourceId.") String resourceId) {
		ChannelConfiguration chConf = driver.getChannel(resourceId);
		ZWaveHlDevice device = driver.getDevice(chConf.getDeviceLocator().getInterfaceName() + ":"
				+ chConf.getDeviceLocator().getDeviceAddress().toUpperCase());
		System.out.print("Channel value: ");
		// device.readValue(channelLocator.getChannelAddress().toUpperCase());
		device.printValue(chConf.getChannelLocator().getChannelAddress().toUpperCase());
	}

	public void deviceScan(@Descriptor("The interface ID/Port name (COM3, /dev/ttyUSB0, etc.).") String interfaceId) {
		try {
			driver.appManager.getChannelAccess().discoverDevices("zwave-driver", interfaceId, null, driver);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
