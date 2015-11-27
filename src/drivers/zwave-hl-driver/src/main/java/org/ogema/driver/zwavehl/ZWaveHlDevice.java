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
import org.ogema.core.channelmanager.ChannelConfigurationException;
import org.ogema.core.channelmanager.ChannelEventListener;
import org.ogema.core.channelmanager.EventType;
import org.ogema.core.channelmanager.driverspi.ChannelLocator;
import org.ogema.core.channelmanager.driverspi.ChannelScanListener;
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
 * This class represents an open connection to a ZWave node.
 * 
 * @author baerthbn
 * 
 */
public abstract class ZWaveHlDevice implements ChannelScanListener {
	protected final ChannelAccess channelAccess;
	protected final ZWaveHlConfig zwaveHlConfig;
	protected final ResourceManagement resourceManager;
	protected final DeviceLocator deviceLocator;
	protected final ApplicationManager appManager;
	protected final Map<String, ChannelLocator> valueChannel; // <channelAddress, ChannelLocator>
	protected final List<ChannelLocator> ChannelLocatorList;

	protected ChannelEventListener channelEventListener;
	protected ZWaveHlDriver driver;
	protected final OgemaLogger logger;
	protected String deviceID;
	public final Map<String, ChannelLocator> channelMap; // Map a name to a channelLocator (resourceId)

	/**
	 * This Constructor is called by resourceAvailable method initiated by ShellCommand "createChannel".
	 * 
	 * @param driver
	 * @param appManager
	 * @param config
	 */
	public ZWaveHlDevice(ZWaveHlDriver driver, ApplicationManager appManager, ZWaveHlConfig config) {
		valueChannel = new HashMap<String, ChannelLocator>();
		ChannelLocatorList = new ArrayList<ChannelLocator>(); // UpdateListenerList
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		this.appManager = appManager;
		this.logger = appManager.getLogger();
		zwaveHlConfig = config;
		this.driver = driver;
		deviceLocator = channelAccess.getDeviceLocator(config.driverId, config.interfaceId, config.deviceAddress,
				config.deviceParameters);
		this.channelMap = new HashMap<String, ChannelLocator>();

		init();
		channelAccess.discoverChannels(deviceLocator, this);

		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				// System.out.print("ChannelEvent --> ");
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

	/**
	 * This Constructor is called by deviceFound method initiated by ChannelManager.
	 * 
	 * @param driver
	 * @param appManager
	 * @param deviceLocator
	 */
	public ZWaveHlDevice(ZWaveHlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		this.appManager = appManager;
		channelAccess = appManager.getChannelAccess();
		valueChannel = new HashMap<String, ChannelLocator>();
		ChannelLocatorList = new ArrayList<ChannelLocator>();
		resourceManager = appManager.getResourceManagement();
		logger = appManager.getLogger();
		this.zwaveHlConfig = new ZWaveHlConfig();
		this.driver = driver;
		zwaveHlConfig.interfaceId = deviceLocator.getInterfaceName();
		zwaveHlConfig.deviceAddress = deviceLocator.getDeviceAddress();
		zwaveHlConfig.driverId = deviceLocator.getDriverName();
		zwaveHlConfig.deviceParameters = deviceLocator.getParameters();
		zwaveHlConfig.resourceName = deviceLocator.getParameters().split(":")[1].replaceAll(" ", "_") + "_";

		this.deviceLocator = deviceLocator;
		unifyResourceName(zwaveHlConfig);
		this.channelMap = new HashMap<String, ChannelLocator>();

		init();
		channelAccess.discoverChannels(deviceLocator, this);

		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				// System.out.print("ChannelEvent --> ");
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

	public ChannelLocator addChannel(ZWaveHlConfig config) {
		ChannelLocator channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfig = channelAccess.getChannelConfiguration(channelLocator);
		channelMap.put(config.resourceName, channelLocator);
		valueChannel.put(config.channelAddress, channelLocator);
		channelConfig.setSamplingPeriod(ChannelConfiguration.LISTEN_FOR_UPDATE);
		this.deviceID = config.resourceName;

		logger.debug("channel access addchannel");
		try {
			channelAccess.addChannel(channelConfig);
		} catch (ChannelConfigurationException e) {
			logger.error("addChannel failed!");
			e.printStackTrace();
			return null;
		}
		addToUpdateListener(channelLocator);
		return channelLocator;
	}

	protected void removeChannels() {
		Set<Entry<String, ChannelLocator>> set = channelMap.entrySet();
		for (Entry<String, ChannelLocator> e : set) {
			try {
				ChannelLocator chLoc = e.getValue();
				channelAccess.deleteChannel(chLoc);
			} catch (ChannelConfigurationException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void writeToChannel(ChannelLocator channelLocator, Value value) {

		try {
			channelAccess.setChannelValue(channelLocator, value);
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}

	public void deleteChannel(ZWaveHlConfig config) {
		ChannelLocator locator = null;
		if (valueChannel.containsKey(config.channelAddress)) {
			locator = valueChannel.get(config.channelAddress);
			valueChannel.remove(config.channelAddress);
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
			System.out.println(value.getByteArrayValue());
		}
		else if (value instanceof BooleanValue) {
			System.out.println(Boolean.toString(value.getBooleanValue()));
		}
		else if (value instanceof ObjectValue) {
			// Object objectValue = value.getObjectValue();
			// if (objectValue instanceof Map) {
			// @SuppressWarnings("unchecked")
			// HashMap<Short, SampledValue> sampledValueMap = (HashMap<Short, SampledValue>) objectValue;
			// for (Map.Entry<Short, SampledValue> mapEntry : sampledValueMap.entrySet()) {
			// printValue(mapEntry.getValue().getValue(), channelAddress);
			// }
			// }
		}
	}

	public void printValue(String channel) {
		SampledValue sampledValue = null;
		try {
			sampledValue = channelAccess.getChannelValue(valueChannel.get(channel));
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Value value = sampledValue.getValue();
		printValue(value, channel);
	}

	public void readValue(String channelAddress) {
		ChannelLocator chloc = valueChannel.get(channelAddress);
		SampledValueContainer container = new SampledValueContainer(chloc);

		List<SampledValueContainer> channelList = new ArrayList<SampledValueContainer>(1);

		channelList.add(container);

		channelAccess.readUnconfiguredChannels(channelList);
		Value c = channelList.get(0).getSampledValue().getValue();
		printValue(c, channelAddress);
		parseValue(c, channelAddress);
	}

	protected abstract void unifyResourceName(ZWaveHlConfig config);

	protected abstract void terminate();

	protected abstract void init();

	public ChannelLocator getChannel(String resourceId) {
		return channelMap.get(resourceId);
	}
}
