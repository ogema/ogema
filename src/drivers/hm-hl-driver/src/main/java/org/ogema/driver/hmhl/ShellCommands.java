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
package org.ogema.driver.hmhl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.core.channelmanager.measurements.Value;
import org.osgi.framework.BundleContext;

public class ShellCommands {
	// private BundleContext context = null;
	private HM_hlDriver driver = null;

	public ShellCommands(HM_hlDriver driver, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "hmhl");
		props.put("osgi.command.function", new String[] { "showCreatedChannels", "createChannel", "deleteChannel",
				"readChannel", "writeChannel", "deviceScan" });
		// this.context = context;
		this.driver = driver;
		context.registerService(this.getClass().getName(), this, props);
	}

	@Descriptor("Lists the channelLocator String of all created Channels.")
	public void showCreatedChannels() {
		for (Map.Entry<String, HM_hlDevice> deviceEntry : driver.devices.entrySet()) {
			for (Map.Entry<String, ChannelLocator> attributeChannelEntry : deviceEntry.getValue().attributeChannel
					.entrySet()) {
				System.out.println(attributeChannelEntry.getKey());
			}
			for (Map.Entry<String, ChannelLocator> commandChannelEntry : deviceEntry.getValue().commandChannel
					.entrySet()) {
				System.out.println(commandChannelEntry.getKey());
			}
		}
	}

	@Descriptor("Creates a new channel.")
	public void createChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. 000000.") String deviceAddress,
			@Descriptor("The channel address in form e. g. Attribute:0000.") String channelAddress,
			@Descriptor("The period in which the ChannelManager should try to request a new value. 0 for commands.") long timeout,
			@Descriptor("A name for the resource.") String resourceName) {
		HM_hlConfig config = new HM_hlConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		config.timeout = timeout;
		config.deviceId = deviceAddress;
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
		HM_hlConfig config = new HM_hlConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		driver.resourceUnavailable(config);
	}

	@Descriptor("Deletes a channel.")
	public void deleteChannel(@Descriptor("The resourceId.") String resourceId) {
		ChannelLocator channelLocator = driver.channelMap.get(resourceId);
		HM_hlConfig config = new HM_hlConfig();
		config.interfaceId = channelLocator.getDeviceLocator().getInterfaceName();
		config.deviceAddress = channelLocator.getDeviceLocator().getDeviceAddress().toUpperCase();
		config.channelAddress = channelLocator.getChannelAddress().toUpperCase();
		driver.resourceUnavailable(config);
	}

	@Descriptor("Write to a channel. This can mean overwriting a Attribute or sending a Command.")
	public void writeChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. 000000.") String deviceAddress,
			@Descriptor("The channel address in form of Type:Id e. g. Type:Attribute:0000.") String channelAddress,
			@Descriptor("The value as a string with hex characters e. g. 0AFF. Byte order needs to be in Little Endian Byte order") Value writeValue) {
		HM_hlDevice device = driver.devices.get(interfaceId + ":" + deviceAddress.toUpperCase());
		ChannelLocator locator = device.attributeChannel.get(channelAddress.toUpperCase());
		if (locator == null) {
			locator = device.commandChannel.get(channelAddress.toUpperCase());
		}
		device.writeToChannel(locator, writeValue);
	}

	@Descriptor("Write to a channel. This can mean overwriting a Attribute or sending a Command.")
	public void writeChannel(
			@Descriptor("The resourceId.") String resourceId,
			@Descriptor("The value as a string with hex characters e. g. 0AFF. Byte order needs to be in Little Endian Byte order") Value writeValue) {
		ChannelLocator channelLocator = driver.channelMap.get(resourceId);
		HM_hlDevice device = driver.devices.get(channelLocator.getDeviceLocator().getInterfaceName() + ":"
				+ channelLocator.getDeviceLocator().getDeviceAddress().toUpperCase());
		device.writeToChannel(channelLocator, writeValue);
	}

	@Descriptor("Read from a channel.")
	public void readChannel(@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of DeviceId e. g. 000000.") String deviceAddress,
			@Descriptor("The channel address in form of Type:Id e. g. Type:Attribute:0000.") String channelAddress) {
		HM_hlDevice device = driver.getDevice(interfaceId + ":" + deviceAddress.toUpperCase());
		System.out.print("Channel value: ");
		device.readValue(channelAddress.toUpperCase());
		device.printValue(channelAddress.toUpperCase());
	}

	@Descriptor("Read from a channel.")
	public void readChannel(@Descriptor("The resourceId.") String resourceId) {
		ChannelLocator channelLocator = driver.channelMap.get(resourceId);
		HM_hlDevice device = driver.getDevice(channelLocator.getDeviceLocator().getInterfaceName() + ":"
				+ channelLocator.getDeviceLocator().getDeviceAddress().toUpperCase());
		System.out.print("Channel value: ");
		// device.readValue(channelLocator.getChannelAddress().toUpperCase());
		device.printValue(channelLocator.getChannelAddress().toUpperCase());
	}

	public void deviceScan(@Descriptor("The interface ID/Port name (USB, /dev/ttyUSB0, etc.).") String interfaceId) {
		try {
			driver.appManager.getChannelAccess().discoverDevices("homematic-driver", interfaceId, null, driver);
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
