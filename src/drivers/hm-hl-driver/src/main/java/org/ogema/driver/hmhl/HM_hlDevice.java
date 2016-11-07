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
import java.util.Map.Entry;
import java.util.Set;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.channelmanager.ChannelAccess;
import org.ogema.core.channelmanager.ChannelAccessException;
import org.ogema.core.channelmanager.ChannelConfiguration;
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
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
import org.ogema.core.resourcemanager.ResourceAccess;
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
	protected final ResourceAccess resourceAccess;
	protected final DeviceLocator deviceLocator;
	protected final ApplicationManager appManager;
	protected final DeviceDescriptor deviceDescriptor;

	protected final String type;

	protected final Map<String, ChannelConfiguration> attributeChannel; // <channelAddress, ChannelLocator>
	protected final List<ChannelConfiguration> channelList;
	protected final Map<String, ChannelConfiguration> commandChannel; // <channelAddress, ChannelLocator>

	protected ChannelEventListener channelEventListener;
	protected long timeout;
	protected String dataResourceId;

	protected int resourceNameCounter;

	protected HM_hlDriver driver;

	protected final OgemaLogger logger;

	public HM_hlDevice(HM_hlDriver driver, ApplicationManager appManager, HM_hlConfig config) {
		attributeChannel = new HashMap<String, ChannelConfiguration>();
		channelList = new ArrayList<ChannelConfiguration>(); // UpdateListenerList
		commandChannel = new HashMap<String, ChannelConfiguration>();
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		resourceAccess = appManager.getResourceAccess();
		this.appManager = appManager;
		this.logger = appManager.getLogger();
		hm_hlConfig = config;
		this.type = config.channelAddress.split(":")[0];
		this.driver = driver;
		deviceDescriptor = driver.getDeviceDescriptor();
		deviceLocator = new DeviceLocator(config.driverId, config.interfaceId, config.deviceAddress,
				config.deviceParameters);

		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				// System.out.print("ChannelEvent --> ");
				for (SampledValueContainer c : channels) {
					try {
						updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
					} catch (IllegalConversionException e) {
						logger.debug("Changed channel value could not be read");
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
		attributeChannel = new HashMap<String, ChannelConfiguration>();
		channelList = new ArrayList<ChannelConfiguration>();
		commandChannel = new HashMap<String, ChannelConfiguration>();
		resourceManager = appManager.getResourceManagement();
		resourceAccess = appManager.getResourceAccess();
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
				// System.out.print("ChannelEvent --> ");
				for (SampledValueContainer c : channels) {
					try {
						updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
					} catch (IllegalConversionException e) {
						logger.debug("Changed channel value could not be read");
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
		return new ChannelLocator(channelAddress, deviceLocator);
	}

	public void close() {
		terminate();
	}

	public ChannelLocator addChannel(HM_hlConfig config) {
		String[] splitAddress = config.channelAddress.split(":");
		ChannelLocator channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfig;
		switch (splitAddress[0]) {
		case "COMMAND":
			timeout = -1;
			try {
				channelConfig = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, timeout);
				commandChannel.put(config.channelAddress, channelConfig);
				driver.channelMap.put(config.resourceName, channelConfig);
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}
			break;
		case "ATTRIBUTE":
		case "MULTIPLE ATTRIBUTES":
			timeout = config.timeout;
			dataResourceId = config.resourceName;

			logger.debug("channel access addchannel");
			try {
				channelConfig = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, timeout);
				attributeChannel.put(config.channelAddress, channelConfig);
				driver.channelMap.put(config.resourceName, channelConfig);
				
				addToUpdateListener(channelConfig);
			} catch (ChannelAccessException e) {
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
		return channelLocator;
	}

	public void writeToChannel(ChannelConfiguration channelConfiguration, Value value) {
		try {
			channelAccess.setChannelValue(channelConfiguration, value);
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}

	protected void addToUpdateListener(ChannelConfiguration channelConfiguration) {
		channelList.add(channelConfiguration);
		try {
			channelAccess.registerUpdateListener(channelList, channelEventListener);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		channelList.clear();
	}

	public void printValue(Value value, String channelAddress) {
		logger.debug("New value: ");
		String[] splitChannelAddress = channelAddress.split(":");
		if (value instanceof StringValue) {
			logger.debug(value.getStringValue());
		}
		else if (value instanceof IntegerValue) {
			logger.debug(Integer.toString(value.getIntegerValue()));
		}
		else if (value instanceof FloatValue) {
			logger.debug(Float.toString(value.getFloatValue()));
		}
		else if (value instanceof DoubleValue) {
			logger.debug(Double.toString(value.getDoubleValue()));
		}
		else if (value instanceof ByteArrayValue) {
			logger.debug(Converter.toHexString(value.getByteArrayValue()));
		}
		else if (value instanceof BooleanValue) {
			logger.debug(Boolean.toString(value.getBooleanValue()));
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
			e.printStackTrace();
		}

		Value value = sampledValue.getValue();
		printValue(value, channel);
	}

	public void readValue(String channelAddress) {

	}

	public int getResourceNameCounter() {
		return resourceNameCounter++;
	}

	protected void removeChannels() {
		Set<Entry<String, ChannelConfiguration>> set = attributeChannel.entrySet();
		for (Entry<String, ChannelConfiguration> e : set) {
			channelAccess.deleteChannel(e.getValue());
		}

		set = commandChannel.entrySet();
		for (Entry<String, ChannelConfiguration> e : set) {
			channelAccess.deleteChannel(e.getValue());
		}
	}

	protected abstract void unifyResourceName(HM_hlConfig config);

	protected abstract void terminate();
}
