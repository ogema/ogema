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
import org.ogema.core.channelmanager.ChannelConfiguration.Direction;
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
	protected final Map<String, ChannelConfiguration> valueChannel; // <channelAddress, ChannelLocator>
	protected final List<ChannelConfiguration> ChannelLocatorList;

	protected ChannelEventListener channelEventListener;
	protected ZWaveHlDriver driver;
	protected final OgemaLogger logger;
	protected String deviceID;
	public final Map<String, ChannelConfiguration> channelMap; // Map a name to a channelLocator (resourceId)

	/**
	 * This Constructor is called by resourceAvailable method initiated by ShellCommand "createChannel".
	 * 
	 * @param driver
	 * @param appManager
	 * @param config
	 * @throws ChannelAccessException 
	 */
	public ZWaveHlDevice(ZWaveHlDriver driver, ApplicationManager appManager, ZWaveHlConfig config) {
		valueChannel = new HashMap<String, ChannelConfiguration>();
		ChannelLocatorList = new ArrayList<ChannelConfiguration>(); // UpdateListenerList
		channelAccess = appManager.getChannelAccess();
		resourceManager = appManager.getResourceManagement();
		this.appManager = appManager;
		this.logger = appManager.getLogger();
		zwaveHlConfig = config;
		this.driver = driver;
		deviceLocator = new DeviceLocator(config.driverId, config.interfaceId, config.deviceAddress,
				config.deviceParameters);
		this.channelMap = new HashMap<String, ChannelConfiguration>();

		init();
		try {
			channelAccess.discoverChannels(deviceLocator, this);
		} catch (ChannelAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				// System.out.print("ChannelEvent --> ");
				for (SampledValueContainer c : channels) {
					try {
						updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
					} catch (IllegalConversionException e) {
						logger.error("Changed channel value could not be read",e);
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
	 * @throws ChannelAccessException 
	 */
	public ZWaveHlDevice(ZWaveHlDriver driver, ApplicationManager appManager, DeviceLocator deviceLocator) {
		this.appManager = appManager;
		channelAccess = appManager.getChannelAccess();
		valueChannel = new HashMap<String, ChannelConfiguration>();
		ChannelLocatorList = new ArrayList<ChannelConfiguration>();
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
		this.channelMap = new HashMap<String, ChannelConfiguration>();

		init();

		channelEventListener = new ChannelEventListener() {

			@Override
			public void channelEvent(EventType type, List<SampledValueContainer> channels) {
				// System.out.print("ChannelEvent --> ");
				for (SampledValueContainer c : channels) {
					try {
						updateChannelValue(c.getChannelLocator().getChannelAddress(), c.getSampledValue().getValue());
					} catch (IllegalConversionException e) {
						logger.error("Changed channel value could not be read",e);
					}
				}
			}

			private void updateChannelValue(String channelAddress, Value value) {
				// TODO
				printValue(value, channelAddress);
				parseValue(value, channelAddress);
			}
		};
		logger.debug("Zwave device inited: " + deviceLocator.toString());
		try {
			channelAccess.discoverChannels(deviceLocator, this);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected abstract void parseValue(Value value, String channelAddress);

	protected ChannelLocator createChannelLocator(String channelAddress) {
		return new ChannelLocator(channelAddress, deviceLocator);
	}

	public void close() {
		// TODO
	}

	public ChannelConfiguration addChannel(ZWaveHlConfig config) {
		
		logger.debug("channel access addchannel");

		ChannelLocator channelLocator = createChannelLocator(config.channelAddress);
		ChannelConfiguration channelConfiguration = null;
		
		this.deviceID = config.resourceName;

		try {
			channelConfiguration = channelAccess.addChannel(channelLocator, Direction.DIRECTION_INOUT, ChannelConfiguration.LISTEN_FOR_UPDATE);
			addToUpdateListener(channelConfiguration);
			channelMap.put(config.resourceName, channelConfiguration);
			valueChannel.put(config.channelAddress, channelConfiguration);
		} catch (ChannelAccessException e) {
			logger.error("addChannel failed!");
			e.printStackTrace();
		}

		return channelConfiguration;
	}

	protected void removeChannels() {
		Set<Entry<String, ChannelConfiguration>> set = channelMap.entrySet();
		for (Entry<String, ChannelConfiguration> e : set) {
				ChannelConfiguration chConf = e.getValue();
				channelAccess.deleteChannel(chConf);
		}
	}

	public void writeToChannel(ChannelConfiguration channelConfiguration, Value value) {

		try {
			channelAccess.setChannelValue(channelConfiguration, value);
		} catch (ChannelAccessException e) {
			e.printStackTrace();
		}
	}

	public void deleteChannel(ZWaveHlConfig config) {
		ChannelConfiguration chConf = valueChannel.remove(config.channelAddress);
		
		if (chConf == null)
			return;
		
		removeFromUpdateListener(chConf);
		channelAccess.deleteChannel(chConf);
	}

	protected void addToUpdateListener(ChannelConfiguration channelConfiguration) throws ChannelAccessException {
		ChannelLocatorList.add(channelConfiguration);
		channelAccess.registerUpdateListener(ChannelLocatorList, channelEventListener);
	}

	private void removeFromUpdateListener(ChannelConfiguration channelConfiguration) {
		ChannelLocatorList.remove(channelConfiguration);
		channelAccess.unregisterUpdateListener(ChannelLocatorList, channelEventListener);
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

	public void readValue(String channelAddress) throws ChannelAccessException {
		ChannelConfiguration chConf = valueChannel.get(channelAddress);
		SampledValueContainer container = new SampledValueContainer(chConf.getChannelLocator());

		List<SampledValueContainer> channelList = new ArrayList<SampledValueContainer>(1);

		channelList.add(container);

		channelAccess.readUnconfiguredChannels(channelList);
		Value c = channelList.get(0).getSampledValue().getValue();
		printValue(c, channelAddress);
		parseValue(c, channelAddress);
	}

	public void readValue(ChannelLocator channel) {
		String channelAddress = channel.getChannelAddress();
		SampledValueContainer container = new SampledValueContainer(channel);

		List<SampledValueContainer> channelList = new ArrayList<SampledValueContainer>(1);

		channelList.add(container);

		try {
			channelAccess.readUnconfiguredChannels(channelList);
		} catch (ChannelAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Value c = channelList.get(0).getSampledValue().getValue();
		parseValue(c, channelAddress);
	}

	protected abstract void unifyResourceName(ZWaveHlConfig config);

	protected abstract void terminate();

	protected abstract void init();

	public ChannelConfiguration getChannel(String resourceId) {
		return channelMap.get(resourceId);
	}
}
