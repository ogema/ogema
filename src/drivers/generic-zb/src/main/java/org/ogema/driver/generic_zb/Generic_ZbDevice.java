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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * This class represents an open connection to a ZigBee device (address+endpoint).
 * 
 * @author puschas
 * 
 */
public abstract class Generic_ZbDevice implements ChannelEventListener {
	protected final ChannelAccess channelAccess;
	protected final Generic_ZbConfig generic_ZbConfig;
	protected final ResourceManagement resourceManager;
	protected final DeviceLocator deviceLocator;
	protected final ApplicationManager appManager;
	protected final Generic_ZbDriver driver;

	protected final Map<String, ChannelConfiguration> attributeChannel = new HashMap<String, ChannelConfiguration>(); // <channelAddress, ChannelLocator>
	protected final List<ChannelConfiguration> attributeChannelList = new ArrayList<ChannelConfiguration>();
	protected final Map<String, ChannelConfiguration> commandChannel = new HashMap<String, ChannelConfiguration>(); // <channelAddress, ChannelLocator>

	// protected ChannelEventListener channelEventListener;
	protected long timeout;
	protected String dataResourceId;

	public Generic_ZbDevice(Generic_ZbDriver driver, ApplicationManager appManager, Generic_ZbConfig config) {
		this.driver = driver;
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		this.appManager = appManager;
		generic_ZbConfig = config;
		unifyResourceName(generic_ZbConfig);

		deviceLocator = new DeviceLocator(config.driverId, config.interfaceId, config.deviceAddress,
				config.deviceParameters);
		// channelEventListener = new ChannelEventListener() {
		//
		// @Override
		// public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		// for (SampledValueContainer c : channels) {
		// try {
		// updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
		// } catch (IllegalConversionException e) {
		// System.out.println("Changed channel value could not be read");
		// }
		// }
		// }
		//
		// private void updateChannelValue(String channelAddress, Value value) {
		// // TODO
		// printValue(value, channelAddress);
		// }
		// };
		//
		// try {
		// Thread.sleep(10);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// System.out.println("calling addChannel");
		addChannel(config);
		// System.out.println("added channel");
	}

	/**
	 * This constructor is called from the deviceFound method in the driver. It does not create a channel like the other
	 * constructor.
	 * 
	 * @param driver
	 * @param appManager
	 * @param deviceLocator
	 */
	public Generic_ZbDevice(Generic_ZbDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		this.driver = driver;
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		this.appManager = appManager;
		this.deviceLocator = deviceLocator;
		generic_ZbConfig = new Generic_ZbConfig();
		generic_ZbConfig.deviceAddress = deviceLocator.getDeviceAddress();
		generic_ZbConfig.interfaceId = deviceLocator.getInterfaceName();
		String[] splitStringArray = deviceLocator.getParameters().split(":");
		generic_ZbConfig.resourceName = splitStringArray[1];
		unifyResourceName(generic_ZbConfig);

	}

	@Override
	public void channelEvent(EventType type, List<SampledValueContainer> channels) {
		for (SampledValueContainer c : channels) {
			try {
				updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
			} catch (Throwable e) {
				System.out.println("Changed channel value could not be read");
				e.printStackTrace();
			}
		}
	}

	public abstract void updateChannelValue(String channelAddress, Value value);

	protected ChannelLocator createChannelLocator(String channelAddress) {
		return new ChannelLocator(channelAddress, deviceLocator);
	}

	public void close() {
		// TODO
	}

	public void writeToChannel(ChannelConfiguration channelConfiguration, String writeValue) {
		Value value = null;
		while (writeValue.length() % 2 != 0) {
			// value is uneven but it needs to be even to be parsed to a byte
			// array. Possible cases are uneven length and/or length of zero
			writeValue = "0" + writeValue;
		}
		value = new ByteArrayValue(DatatypeConverter.parseHexBinary(writeValue));

		try {
			channelAccess.setChannelValue(channelConfiguration, value);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public abstract ChannelConfiguration addChannel(Generic_ZbConfig config);

	protected void addToUpdateListener(ChannelConfiguration chConf) throws ChannelAccessException {
		attributeChannelList.add(chConf);
		channelAccess.registerUpdateListener(attributeChannelList, this);
	}

	private void removeFromUpdateListener(ChannelConfiguration chConf) {
		attributeChannelList.remove(chConf);
	}

	long lastTime = System.currentTimeMillis();

	public void printValue(Value value, String channelAddress) {
		System.out.print("New value: ");
		System.out.print(deviceLocator);
		System.out.print("\t");
		System.out.print(channelAddress);
		System.out.print("\t");
		if (value instanceof StringValue) {
			System.out.println(value.getStringValue());
		}
		else if (value instanceof IntegerValue) {
			System.out.println(Integer.toString(value.getIntegerValue()));
		}
		else if (value instanceof FloatValue) {
			System.out.println(Float.toString(value.getFloatValue()));
		}
		else if (value instanceof DoubleValue) {
			System.out.println(Double.toString(value.getDoubleValue()));
		}
		else if (value instanceof ByteArrayValue) {
			System.out.println("byteArrayValue");
		}
		else if (value instanceof BooleanValue) {
			System.out.println(Boolean.toString(value.getBooleanValue()));
		}
		else if (value instanceof ObjectValue) {
			Object objectValue = value.getObjectValue();
			if (objectValue instanceof Map) {
				@SuppressWarnings("unchecked")
				HashMap<Short, SampledValue> sampledValueMap = (HashMap<Short, SampledValue>) objectValue;
				for (Map.Entry<Short, SampledValue> mapEntry : sampledValueMap.entrySet()) {
					printValue(mapEntry.getValue().getValue(), channelAddress);
				}
			}
		}
		long period = (System.currentTimeMillis() - lastTime);
		lastTime = System.currentTimeMillis();
		System.out.println("Took " + period);
	}

	public void printValue(String channelAddress) {
		SampledValue sampledValue = null;
		System.out.println("Try to read: " + attributeChannel.get(channelAddress).toString());
		try {
			sampledValue = channelAccess.getChannelValue(attributeChannel.get(channelAddress));
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Value value = sampledValue.getValue();
		printValue(value, channelAddress);
	}

	public Value readValue(String channelAddress) {
		// TODO alternative to printing the value
		ChannelConfiguration chConf = attributeChannel.get(channelAddress);
		SampledValueContainer container = new SampledValueContainer(chConf.getChannelLocator());

		List<SampledValueContainer> channelList = new ArrayList<SampledValueContainer>(1);

		channelList.add(container);

		try {
			channelAccess.readUnconfiguredChannels(channelList);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return channelList.get(0).getSampledValue().getValue();
	}

	public void deleteChannel(Generic_ZbConfig config) {
		ChannelConfiguration chConf = null;
		
		chConf = attributeChannel.remove(config.channelAddress);
		
		if (chConf == null)
			chConf = commandChannel.remove(config.channelAddress);
		
		if (chConf == null)
			return;
		
		removeFromUpdateListener(chConf);
		channelAccess.deleteChannel(chConf);
	}

	public void unifyResourceName(Generic_ZbConfig zbConfig) {
//		if (zbConfig.resourceName == null || zbConfig.resourceName.startsWith("Unknown_"))
//			zbConfig.resourceName = zbConfig.deviceAddress.replace(':', '_');
//		else {
//			zbConfig.resourceName += "_";
//			zbConfig.resourceName += zbConfig.deviceAddress.replace(':', '_');
//		}
	}
}
