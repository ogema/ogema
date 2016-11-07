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
package org.ogema.driver.generic_xbee;

import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.NoSuchDriverException;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.NoSuchInterfaceException;
import org.ogema.driverconfig.HLDriverInterface;
import org.osgi.framework.BundleContext;

public class ShellCommands implements HLDriverInterface {
	private BundleContext context = null;
	private GenericXbeeZbDriver driver = null;

	public ShellCommands(GenericXbeeZbDriver driver, BundleContext context) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "xbhl");
		props.put("osgi.command.function", new String[] { "showCreatedChannels", "createChannel", "deleteChannel",
				"readChannel", "deviceScan" });
		this.context = context;
		this.driver = driver;
		this.context.registerService(this.getClass().getName(), this, props);
		context.registerService(HLDriverInterface.class, this, null);
	}

	public void showCreatedChannels() {
		for (Map.Entry<String, ChannelConfiguration> mapEntry : driver.channelMap.entrySet()) {
			System.out.println(mapEntry.getKey() + ": " + mapEntry.getValue().getChannelLocator().toString());
		}
	}

	@Override
	@Descriptor("Creates a new channel.")
	public void createChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress,
			@Descriptor("The period in which the ChannelManager should try to request a new value. 0 for commands.") long timeout,
			@Descriptor("A name for the resource.") String resourceName, String deviceId) {
		GenericXbeeZbConfig config = new GenericXbeeZbConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		config.timeout = timeout;
		config.resourceName = resourceName;
		config.deviceParameters = "";
		System.out.println(config.interfaceId + " " + config.deviceAddress + " " + config.channelAddress);
		driver.resourceAvailable(config);
	}

	@Descriptor("Deletes a channel.")
	public void deleteChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress) {
		GenericXbeeZbConfig config = new GenericXbeeZbConfig();
		config.interfaceId = interfaceId;
		config.deviceAddress = deviceAddress.toUpperCase();
		config.channelAddress = channelAddress.toUpperCase();
		config.deviceParameters = "";
		driver.resourceUnavailable(config);
	}

	@Descriptor("Read from a channel.")
	public JSONObject readChannel(
			@Descriptor("The interface ID/Port name.") String interfaceId,
			@Descriptor("The device address in form of 16Bit:EndpointID e. g. 0000:00.") String deviceAddress,
			@Descriptor("The channel address in form of ClusterId:Type:Id e. g. 0000:Attribute:0000.") String channelAddress) {
		return updateValues(driver.getDevices().get(
				"xbee-driver:" + interfaceId + ":" + deviceAddress.toUpperCase() + ":" + channelAddress.toUpperCase()));
	}

	public void deviceScan(@Descriptor("The interface ID/Port name.") String interfaceId) throws Throwable {
		try {
			if (driver.appManager != null) // do it only if the HLD app is running
				driver.appManager.getChannelAccess().discoverDevices("xbee-driver", interfaceId, null, driver);
		} catch (Throwable e) {
			throw e;
		}
	}

	@Override
	public void writeChannel(String interfaceId, String deviceAddress, String channelAddress, String writeValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONArray showCreatedChannels(String deviceAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String whichID() {
		return "generic-xbee-zb";
	}

	JSONObject updateValues(GenericXbeeZbDevice dev) {
		return dev.packValuesAsJSON();
	}

}
