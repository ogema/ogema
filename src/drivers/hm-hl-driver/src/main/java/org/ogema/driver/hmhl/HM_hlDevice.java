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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.DeviceLocator;
import org.ogema.core.channelmanager.driverspi.SampledValueContainer;
import org.ogema.core.channelmanager.measurements.BooleanValue;
import org.ogema.core.channelmanager.measurements.ByteArrayValue;
import org.ogema.core.channelmanager.measurements.DoubleValue;
import org.ogema.core.channelmanager.measurements.FloatValue;
import org.ogema.core.channelmanager.measurements.IllegalConversionException;
import org.ogema.core.channelmanager.measurements.IntegerValue;
import org.ogema.core.channelmanager.measurements.ObjectValue;
import org.ogema.core.channelmanager.measurements.SampledValue;
import org.ogema.core.channelmanager.measurements.StringValue;
import org.ogema.core.channelmanager.measurements.Value;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.resourcemanager.ResourceManagement;

/**
 * This class represents an open connection to a Homematic device.
 * 
 * // * @author puschas/baerthbn
 * 
 */
public abstract class HM_hlDevice {
	protected final ChannelAccess channelAccess;
	protected final HM_hlConfig hm_hlConfig;
	protected final ResourceManagement resourceManager;
	protected final DeviceLocator deviceLocator;
	protected final ApplicationManager appManager;
	protected final DeviceDescriptor deviceDescriptor;

	protected final String type;

	protected final Map<String, ChannelLocator> attributeChannel; // <channelAddress, ChannelLocator>
	protected final List<ChannelLocator> ChannelLocatorList;
	protected final Map<String, ChannelLocator> commandChannel; // <channelAddress, ChannelLocator>

	protected ChannelEventListener channelEventListener;
	protected long timeout;
	protected String dataResourceId;

	protected int resourceNameCounter;

	protected HM_hlDriver driver;

	protected final OgemaLogger logger;

	public HM_hlDevice(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		attributeChannel = new HashMap<String, ChannelLocator>();
		ChannelLocatorList = new ArrayList<ChannelLocator>(); // UpdateListenerList
		commandChannel = new HashMap<String, ChannelLocator>();
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		this.appManager = appManager;
		this.logger = appManager.getLogger();
		hm_hlConfig = config;
		this.type = config.channelAddress.split(":")[0];
		this.driver = driver;
		deviceDescriptor = driver.getDeviceDescriptor();
		deviceLocator = channelAccess.getDeviceLocator(config.driverId, config.interfaceId, config.deviceAddress,
				config.deviceParameters);

		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				//				System.out.print("ChannelEvent --> ");
				for (SampledValueContainer c : channels) {
					try {
						updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
					} catch (IllegalConversionException e) {
						System.out.println("Changed channel value could not be read");
					}
				}
			}

			private void updateChannelValue(String channelAddress, Value value) {
				// TODO
				printValue(value, channelAddress);
				parseValue(value, channelAddress);
			}
		};
	}

	public HM_hlDevice(HM_hlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		this.appManager = appManager;
		channelAccess = appManager.getChannelAccess();
		attributeChannel = new HashMap<String, ChannelLocator>();
		ChannelLocatorList = new ArrayList<ChannelLocator>();
		commandChannel = new HashMap<String, ChannelLocator>();
		resourceManager = appManager.getResourceManagement();
		logger = appManager.getLogger();
		this.hm_hlConfig = new HM_hlConfig();
		this.driver = driver;
		deviceDescriptor = driver.getDeviceDescriptor();
		this.type = deviceLocator.getParameters();

		hm_hlConfig.interfaceId = deviceLocator.getInterfaceName();
		hm_hlConfig.deviceAddress = deviceLocator.getDeviceAddress();
		hm_hlConfig.driverId = deviceLocator.getDriverName();
		hm_hlConfig.deviceParameters = deviceLocator.getParameters();
		hm_hlConfig.resourceName = deviceDescriptor.getName(type).replace('-', '_');

		this.deviceLocator = deviceLocator;
		unifyResourceName(hm_hlConfig);
		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				//				System.out.print("ChannelEvent --> ");
				for (SampledValueContainer c : channels) {
					try {
						updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
					} catch (IllegalConversionException e) {
						System.out.println("Changed channel value could not be read");
					}
				}
			}

			private void updateChannelValue(String channelAddress, Value value) {
				// TODO
				printValue(value, channelAddress);
				parseValue(value, channelAddress);
			}
		};
	}

	protected abstract void parseValue(Value value, String channelAddress);

	protected ChannelLocator createChannelLocator(String channelAddress) {
		return channelAccess.getChannelLocator(channelAddress, deviceLocator);
	}

	public void close() {
		// TODO
	}

	public ChannelLocator addChannel(HM_hlConfig config) {
		String[] splitAddress = config.channelAddress.split(":");
		ChannelLocator channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfig = channelAccess.getChannelConfiguration(channelLocator);
		driver.channelMap.put(config.resourceName, channelLocator);
		switch (splitAddress[0]) {
		case "COMMAND":
			commandChannel.put(config.channelAddress, channelLocator);
			channelConfig.setSamplingPeriod(0);
			try {
				channelAccess.addChannel(channelConfig);
			} catch (ChannelConfigurationException e) {
				e.printStackTrace();
			}
			break;
		case "ATTRIBUTE":
		case "MULTIPLE ATTRIBUTES":
			attributeChannel.put(config.channelAddress, channelLocator);
			timeout = config.timeout;
			channelConfig.setSamplingPeriod(timeout);
			dataResourceId = config.resourceName;

			System.out.println("channel access addchannel");
			try {
				channelAccess.addChannel(channelConfig);
			} catch (ChannelConfigurationException e) {
				e.printStackTrace();
			} catch (NullPointerException ex) {
				ex.printStackTrace();
			}
			addToUpdateListener(channelLocator);
			break;
		default:
			break;
		}
		return channelLocator;
	}

	public void writeToChannel(ChannelLocator channelLocator, String writeValue) {
		Value value = null;
		// while (writeValue.length() % 2 != 0) {
		// // value is uneven but it needs to be even to be parsed to a byte
		// // array. Possible cases are uneven length and/or length of zero
		// writeValue = "0" + writeValue;
		// }
		value = new ByteArrayValue(DatatypeConverter.parseHexBinary(writeValue));

		try {
			channelAccess.setChannelValue(channelLocator, value);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteChannel(HM_hlConfig config) {
		ChannelLocator locator = null;
		if (attributeChannel.containsKey(config.channelAddress)) {
			locator = attributeChannel.get(config.channelAddress);
			attributeChannel.remove(config.channelAddress);
		}
		else if (commandChannel.containsKey(config.channelAddress)) {
			locator = commandChannel.get(config.channelAddress);
			commandChannel.remove(config.channelAddress);
		}
		else {
			return;
		}

		removeFromUpdateListener(locator);

		try {
			channelAccess.deleteChannel(locator);
		} catch (ChannelConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void addToUpdateListener(ChannelLocator channelLocator) {
		ChannelLocatorList.add(channelLocator);
		channelAccess.registerUpdateListener(ChannelLocatorList, channelEventListener);
	}

	private void removeFromUpdateListener(ChannelLocator channelLocator) {
		ChannelLocatorList.remove(channelLocator);
		channelAccess.registerUpdateListener(ChannelLocatorList, channelEventListener);
	}

	public void printValue(Value value, String channelAddress) {
		System.out.print("New value: ");
		String[] splitChannelAddress = channelAddress.split(":");
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
			System.out.println(Converter.toHexString(value.getByteArrayValue()));
		}
		else if (value instanceof BooleanValue) {
			System.out.println(Boolean.toString(value.getBooleanValue()));
		}
		else if (value instanceof ObjectValue) {
			Object objectValue = value.getObjectValue();
			if (splitChannelAddress[1].equals("MULTIPLE ATTRIBUTES") && objectValue instanceof Map) {
				@SuppressWarnings("unchecked")
				HashMap<Short, SampledValue> sampledValueMap = (HashMap<Short, SampledValue>) objectValue;
				for (Map.Entry<Short, SampledValue> mapEntry : sampledValueMap.entrySet()) {
					printValue(mapEntry.getValue().getValue(), channelAddress);
				}
			}
		}
	}

	public void printValue(String channel) {
		SampledValue sampledValue = null;
		try {
			sampledValue = channelAccess.getChannelValue(attributeChannel.get(channel));
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Value value = sampledValue.getValue();
		printValue(value, channel);
	}

	public void readValue(String channelAddress) {
		// TODO Auto-generated method stub

	}

	public int getResourceNameCounter() {
		return resourceNameCounter++;
	}

	protected abstract void unifyResourceName(HM_hlConfig config);
}
